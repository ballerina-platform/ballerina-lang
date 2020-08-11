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

package org.wso2.transport.http.netty.contractimpl.listener;

import io.netty.channel.ChannelFuture;
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
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.exceptions.ClientClosedConnectionException;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.listener.states.ListenerReqRespStateManager;
import org.wso2.transport.http.netty.contractimpl.listener.states.ReceivingHeaders;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.net.SocketAddress;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.transport.http.netty.contract.Constants.EXPECTED_SEQUENCE_NUMBER;
import static org.wso2.transport.http.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.contract.Constants.NUMBER_OF_INITIAL_EVENTS_HELD;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.contractimpl.common.Util.createInboundReqCarbonMsg;
import static org.wso2.transport.http.netty.contractimpl.common.Util.isKeepAliveConnection;

/**
 * A Class responsible for handling incoming message through netty inbound pipeline.
 */
public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(SourceHandler.class);

    private HttpCarbonMessage inboundRequestMsg;
    private final Map<Integer, HttpCarbonMessage> requestSet = new ConcurrentHashMap<>();
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
    private boolean connectedState;

    private boolean pipeliningEnabled; //Based on the pipelining config
    private long pipeliningLimit; //Max number of responses allowed to be queued when pipelining is enabled
    private long sequenceId = 1L; //Keep track of the request order for http 1.1 pipelining
    private final Queue holdingQueue = new PriorityQueue<>(NUMBER_OF_INITIAL_EVENTS_HELD);
    private EventExecutorGroup pipeliningGroup;

    public SourceHandler(ServerConnectorFuture serverConnectorFuture, String interfaceId, ChunkConfig chunkConfig,
                         KeepAliveConfig keepAliveConfig, String serverName, ChannelGroup allChannels, boolean
                                 pipeliningEnabled, long pipeliningLimit, EventExecutorGroup pipeliningGroup) {
        this.serverConnectorFuture = serverConnectorFuture;
        this.interfaceId = interfaceId;
        this.chunkConfig = chunkConfig;
        this.keepAliveConfig = keepAliveConfig;
        this.targetChannelPool = new ConcurrentHashMap<>();
        this.idleTimeout = false;
        this.serverName = serverName;
        this.allChannels = allChannels;
        this.pipeliningEnabled = pipeliningEnabled;
        this.pipeliningLimit = pipeliningLimit;
        this.pipeliningGroup = pipeliningGroup;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            setConnectedState(false);
            inboundRequestMsg = createInboundReqCarbonMsg((HttpRequest) msg, ctx, this);
            if (requestSet.size() > this.pipeliningLimit) {
                LOG.warn("Pipelining request limit exceeded hence closing the channel {}", ctx.channel().id());
                closeChannel(ctx);
                return;
            }
            requestSet.put(inboundRequestMsg.hashCode(), inboundRequestMsg);

            ListenerReqRespStateManager listenerReqRespStateManager = new ListenerReqRespStateManager();
            inboundRequestMsg.listenerReqRespStateManager = listenerReqRespStateManager;

            setRequestProperties();
            //Set the sequence number just before notifying the listener about the request because in case the
            //response got ready before receiving the last HTTP content there's a possibility of seeing an
            //incorrect sequence number
            setSequenceNumber();

            listenerReqRespStateManager.state = new ReceivingHeaders(listenerReqRespStateManager, this);
            listenerReqRespStateManager.readInboundRequestHeaders(inboundRequestMsg, (HttpRequest) msg);
        } else {
            if (inboundRequestMsg != null) {
                inboundRequestMsg.listenerReqRespStateManager.readInboundRequestBody(msg);
            } else {
                LOG.warn("Inconsistent state detected : inboundRequestMsg is null for channel read event");
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        setConnectedState(true);
        this.ctx = ctx;
        this.allChannels.add(ctx.channel());
        setPipeliningProperties();
        handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
        this.remoteAddress = ctx.channel().remoteAddress();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (!idleTimeout) {
            if (!requestSet.isEmpty()) {
                requestSet.forEach((key, inboundMsg) -> inboundMsg.listenerReqRespStateManager
                        .handleAbruptChannelClosure(serverConnectorFuture));
            } else if (connectedState) {
                notifyErrorListenerAtConnectedState(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST);
            }
        }

        closeTargetChannels();

        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
            handlerExecutor = null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        closeChannel(ctx);
        LOG.warn("Exception occurred in SourceHandler : {}", cause.getMessage());
    }

    private void closeTargetChannels() {
        targetChannelPool.forEach((hostPortKey, genericObjectPool) -> {
            try {
                targetChannelPool.remove(hostPortKey).close();
            } catch (Exception e) {
                LOG.error("Couldn't close target channel socket connections", e);
            }
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            this.idleTimeout = true;

            if (!requestSet.isEmpty()) {
                requestSet.forEach((key, inboundMsg) -> {
                    ChannelFuture outboundRespFuture = inboundMsg.listenerReqRespStateManager
                            .handleIdleTimeoutConnectionClosure(serverConnectorFuture, ctx);
                    if (outboundRespFuture == null) {
                        closeChannel(ctx);
                    }
                });
            } else {
                closeChannel(ctx);
                if (connectedState) {
                    notifyErrorListenerAtConnectedState(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST);
                }
            }
            String channelId = ctx.channel().id().asShortText();
            LOG.debug("Idle timeout has reached hence closing the connection {}", channelId);
        } else {
            logTheErrorMsg(ctx, evt);
        }
    }

    private void logTheErrorMsg(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
            LOG.debug("Server upgrade event received");
        } else if (evt instanceof SslCloseCompletionEvent) {
            LOG.debug("SSL close completion event received");
            setConnectedState(false);
            closeChannel(ctx);
        } else if (evt instanceof ChannelInputShutdownReadComplete) {
            // When you try to read from a channel which has already been closed by the peer,
            // 'java.io.IOException: Connection reset by peer' is thrown and it is a harmless exception.
            // We can ignore this most of the time. see 'https://github.com/netty/netty/issues/2332'.
            // As per the code, when an IOException is thrown when reading from a channel, it closes the channel.
            // When closing the channel, if it is already closed it will trigger this event. So we can ignore this.
            LOG.debug("Input side of the connection is already shutdown");
        } else {
            LOG.warn("Unexpected user event {} triggered", evt);
        }
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        ctx.close();
    }

    private void notifyErrorListenerAtConnectedState(String errorMsg) {
        try {
            serverConnectorFuture.notifyErrorListener(new ClientClosedConnectionException(errorMsg));
            // Error is notified to server connector. Debug log is to make transport layer aware
            LOG.debug(errorMsg);
        } catch (ServerConnectorException e) {
            LOG.error("Error while notifying error state to server-connector listener");
        }
    }

    /**
     * These properties are needed in ballerina side for pipelining checks.
     */
    private void setRequestProperties() {
        inboundRequestMsg.setPipeliningEnabled(pipeliningEnabled); //Value of listener config
        String connectionHeaderValue = inboundRequestMsg.getHeader(HttpHeaderNames.CONNECTION.toString());
        String httpVersion = inboundRequestMsg.getHttpVersion();
        inboundRequestMsg.setKeepAlive(isKeepAliveConnection(keepAliveConfig, connectionHeaderValue,
                httpVersion));
    }

    /**
     * Set pipeline related properties. These should be set only once per connection.
     */
    private void setPipeliningProperties() {
        if (ctx.channel().attr(Constants.MAX_RESPONSES_ALLOWED_TO_BE_QUEUED).get() == null) {
            ctx.channel().attr(Constants.MAX_RESPONSES_ALLOWED_TO_BE_QUEUED).set(pipeliningLimit);
        }
        if (ctx.channel().attr(Constants.RESPONSE_QUEUE).get() == null) {
            ctx.channel().attr(Constants.RESPONSE_QUEUE).set(holdingQueue);
        }
        if (ctx.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get() == null) {
            ctx.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).set(EXPECTED_SEQUENCE_NUMBER);
        }

        if (ctx.channel().attr(Constants.PIPELINING_EXECUTOR).get() == null) {
            ctx.channel().attr(Constants.PIPELINING_EXECUTOR).set(pipeliningGroup);
        }
    }

    /**
     * Sequence number should be incremented per request.
     */
    private void setSequenceNumber() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sequence id of the request is set to : {}", sequenceId);
        }
        inboundRequestMsg.setSequenceId(sequenceId);
        sequenceId++;
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

    public void setConnectedState(boolean connectedState) {
        this.connectedState = connectedState;
    }

    public void removeRequestEntry(HttpCarbonMessage inboundRequestMsg) {
        this.requestSet.remove(inboundRequestMsg.hashCode());
    }

    public void resetInboundRequestMsg() {
        this.inboundRequestMsg = null;
    }
}
