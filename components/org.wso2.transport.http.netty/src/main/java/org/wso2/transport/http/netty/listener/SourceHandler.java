/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.listener;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.listener.states.StateContext;
import org.wso2.transport.http.netty.listener.states.listener.ReceivingHeaders;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.transport.http.netty.common.Constants.EXPECTED_SEQUENCE_NUMBER;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.NUMBER_OF_INITIAL_EVENTS_HELD;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Util.createInboundReqCarbonMsg;
import static org.wso2.transport.http.netty.common.Util.isKeepAliveConnection;

/**
 * A Class responsible for handle  incoming message through netty inbound pipeline.
 */
public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(SourceHandler.class);

    private HttpCarbonMessage inboundRequestMsg;
    private List<HttpCarbonMessage> requestList = new ArrayList<>();
    private HandlerExecutor handlerExecutor;
    private Map<String, GenericObjectPool> targetChannelPool;
    private ChunkConfig chunkConfig;

    private KeepAliveConfig keepAliveConfig;
    private ServerConnectorFuture serverConnectorFuture;
    private String interfaceId;
    private String serverName;
    private boolean idleTimeout;
    private ChannelGroup allChannels;
    protected ChannelHandlerContext ctx;
    private SocketAddress remoteAddress;

    private boolean pipeliningNeeded;
    private final int maximumEvents = 3; //TODO: We should let the user configure this
    private int sequenceId = 1; //Keep track of the request order for http 1.1 pipelining
    private final Queue holdingQueue = new PriorityQueue<>(NUMBER_OF_INITIAL_EVENTS_HELD);

    public SourceHandler(ServerConnectorFuture serverConnectorFuture, String interfaceId, ChunkConfig chunkConfig,
                         KeepAliveConfig keepAliveConfig, String serverName, ChannelGroup allChannels, boolean
                                 pipeliningNeeded) {
        this.serverConnectorFuture = serverConnectorFuture;
        this.interfaceId = interfaceId;
        this.chunkConfig = chunkConfig;
        this.keepAliveConfig = keepAliveConfig;
        this.targetChannelPool = new ConcurrentHashMap<>();
        this.idleTimeout = false;
        this.serverName = serverName;
        this.allChannels = allChannels;
        this.pipeliningNeeded = pipeliningNeeded;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            inboundRequestMsg = createInboundReqCarbonMsg((HttpRequest) msg, ctx, this);
            requestList.add(inboundRequestMsg);

            StateContext stateContext = new StateContext();
            inboundRequestMsg.setStateContext(stateContext);
            setRequestProperties();
            //Set the pipelining properties just before notifying the listener about the request for the first time
            //because in case the response got ready before receiving the last HTTP content there's a possibility
            //of seeing an incorrect sequence number
            setPipeliningProperties();
            stateContext.setListenerState(new ReceivingHeaders(this, stateContext));
            stateContext.getListenerState().readInboundRequestHeaders(inboundRequestMsg, (HttpRequest) msg);
        } else {
            if (inboundRequestMsg != null) {
                inboundRequestMsg.getStateContext().getListenerState().readInboundRequestEntityBody(msg);
            } else {
                log.warn("Inconsistent state detected : inboundRequestMsg is null for channel read event");
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.allChannels.add(ctx.channel());
        handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
        this.remoteAddress = ctx.channel().remoteAddress();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.close();
        if (!idleTimeout) {
            if (!requestList.isEmpty()) {
                requestList.forEach(inboundMsg -> inboundMsg.getStateContext().getListenerState()
                        .handleAbruptChannelClosure(serverConnectorFuture));
            } else {
                notifyErrorListenerAtConnectedState(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST);
            }
        }
        closeTargetChannels();
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
            handlerExecutor = null;
        }
    }

    private void closeTargetChannels() {
        targetChannelPool.forEach((hostPortKey, genericObjectPool) -> {
            try {
                targetChannelPool.remove(hostPortKey).close();
            } catch (Exception e) {
                log.error("Couldn't close target channel socket connections", e);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        log.warn("Exception occurred in SourceHandler : {}", cause.getMessage());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            this.idleTimeout = true;

            if (!requestList.isEmpty()) {
                requestList.forEach(inboundMsg -> {
                    ChannelFuture outboundRespFuture = inboundMsg.getStateContext().getListenerState()
                            .handleIdleTimeoutConnectionClosure(serverConnectorFuture, ctx);
                    if (outboundRespFuture == null) {
                        this.channelInactive(ctx);
                    }
                });
            } else {
                this.channelInactive(ctx);
                notifyErrorListenerAtConnectedState(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST);
            }
            String channelId = ctx.channel().id().asShortText();
            log.debug("Idle timeout has reached hence closing the connection {}", channelId);
        } else if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
            log.debug("Server upgrade event received");
        } else if (evt instanceof SslCloseCompletionEvent) {
            log.debug("SSL close completion event received");
        } else if (evt instanceof ChannelInputShutdownReadComplete) {
            // When you try to read from a channel which has already been closed by the peer,
            // 'java.io.IOException: Connection reset by peer' is thrown and it is a harmless exception.
            // We can ignore this most of the time. see 'https://github.com/netty/netty/issues/2332'.
            // As per the code, when an IOException is thrown when reading from a channel, it closes the channel.
            // When closing the channel, if it is already closed it will trigger this event. So we can ignore this.
            log.debug("Input side of the connection is already shutdown");
        } else {
            log.warn("Unexpected user event {} triggered", evt);
        }
    }

    private void notifyErrorListenerAtConnectedState(String errorMsg) {
        try {
            serverConnectorFuture.notifyErrorListener(new ServerConnectorException(errorMsg));
            // Error is notified to server connector. Debug log is to make transport layer aware
            log.debug(errorMsg);
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
    }

    /**
     * These properties are needed in ballerina side for pipelining checks.
     */
    private void setRequestProperties() {
        inboundRequestMsg.setPipeliningNeeded(pipeliningNeeded); //Value of listener config
        String connectionHeaderValue = inboundRequestMsg.getHeader(HttpHeaderNames.CONNECTION.toString());
        String httpVersion = (String) inboundRequestMsg.getProperty(Constants.HTTP_VERSION);
        inboundRequestMsg.setKeepAlive(isKeepAliveConnection(keepAliveConfig, connectionHeaderValue,
                httpVersion));
    }

    /**
     * Set pipeline related properties. These should be set only once per connection. But sequenceId should be
     * incremented per request.
     */
    private void setPipeliningProperties() {
        inboundRequestMsg.setSequenceId(sequenceId);
        sequenceId++;
        if (ctx.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get() == null) {
            ctx.channel().attr(Constants.MAX_RESPONSES_ALLOWED_TO_BE_QUEUED).set(maximumEvents);
        }
        if (ctx.channel().attr(Constants.RESPONSE_QUEUE).get() == null) {
            ctx.channel().attr(Constants.RESPONSE_QUEUE).set(holdingQueue);
        }
        if (ctx.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get() == null) {
            ctx.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).set(EXPECTED_SEQUENCE_NUMBER);
        }
    }

    public EventLoop getEventLoop() {
        return this.ctx.channel().eventLoop();
    }

    public Map<String, GenericObjectPool> getTargetChannelPool() {
        return targetChannelPool;
    }

    public ChannelHandlerContext getInboundChannelContext() {
        return ctx;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public ServerConnectorFuture getServerConnectorFuture() {
        return serverConnectorFuture;
    }

    public ChunkConfig getChunkConfig() {
        return chunkConfig;
    }

    public KeepAliveConfig getKeepAliveConfig() {
        return keepAliveConfig;
    }

    public String getServerName() {
        return serverName;
    }

    public void removeRequestEntry(HttpCarbonMessage inboundRequestMsg) {
        this.requestList.remove(inboundRequestMsg);
    }

    public void resetInboundRequestMsg() {
        inboundRequestMsg = null;
    }
}
