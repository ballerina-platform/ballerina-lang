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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.CONNECTED;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.ENTITY_BODY_RECEIVED;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.ENTITY_BODY_SENT;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.EXPECT_100_CONTINUE_HEADER_RECEIVED;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.RECEIVING_ENTITY_BODY;
import static org.wso2.transport.http.netty.common.Util.createInboundReqCarbonMsg;
import static org.wso2.transport.http.netty.common.Util.is100ContinueRequest;

/**
 * A Class responsible for handle  incoming message through netty inbound pipeline.
 */
public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(SourceHandler.class);

    private HttpCarbonMessage inboundRequestMsg;
    private HandlerExecutor handlerExecutor;
    private Map<String, GenericObjectPool> targetChannelPool;
    private ChunkConfig chunkConfig;
    private KeepAliveConfig keepAliveConfig;

    private final ServerConnectorFuture serverConnectorFuture;

    private String interfaceId;
    private String serverName;
    private boolean idleTimeout;
    private ChannelGroup allChannels;
    protected ChannelHandlerContext ctx;
    private SocketAddress remoteAddress;
    private SourceErrorHandler sourceErrorHandler;
    private boolean continueRequest = false;

    public SourceHandler(ServerConnectorFuture serverConnectorFuture, String interfaceId, ChunkConfig chunkConfig,
                         KeepAliveConfig keepAliveConfig, String serverName, ChannelGroup allChannels) {
        this.serverConnectorFuture = serverConnectorFuture;
        this.interfaceId = interfaceId;
        this.chunkConfig = chunkConfig;
        this.keepAliveConfig = keepAliveConfig;
        this.targetChannelPool = new ConcurrentHashMap<>();
        this.idleTimeout = false;
        this.serverName = serverName;
        this.allChannels = allChannels;
        this.sourceErrorHandler = new SourceErrorHandler(this.serverConnectorFuture, serverName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            readInboundRequestHeaders(ctx, (HttpRequest) msg);
        } else {
            readInboundReqEntityBody(msg);
        }
    }

    private void readInboundRequestHeaders(ChannelHandlerContext ctx, HttpRequest inboundRequestHeaders) {
        sourceErrorHandler.setState(CONNECTED);
        inboundRequestMsg = createInboundReqCarbonMsg(inboundRequestHeaders, ctx, remoteAddress, interfaceId, this);
        continueRequest = is100ContinueRequest(inboundRequestMsg);
        if (continueRequest) {
            sourceErrorHandler.setState(EXPECT_100_CONTINUE_HEADER_RECEIVED);
        }
        notifyRequestListener(inboundRequestMsg, ctx);

        if (inboundRequestHeaders.decoderResult().isFailure()) {
            log.warn(inboundRequestHeaders.decoderResult().cause().getMessage());
        }
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceRequestReceiving(inboundRequestMsg);
        }
    }

    private void readInboundReqEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        if (inboundRequestMsg != null) {
            if (inboundRequestEntityBody instanceof HttpContent) {
                sourceErrorHandler.setState(RECEIVING_ENTITY_BODY);
                HttpContent httpContent = (HttpContent) inboundRequestEntityBody;
                try {
                    inboundRequestMsg.addHttpContent(httpContent);
                    if (Util.isLastHttpContent(httpContent)) {
                        if (handlerExecutor != null) {
                            handlerExecutor.executeAtSourceRequestSending(inboundRequestMsg);
                        }
                        if (isDiffered(inboundRequestMsg)) {
                            this.serverConnectorFuture.notifyHttpListener(inboundRequestMsg);
                        }
                        inboundRequestMsg = null;
                        sourceErrorHandler.setState(ENTITY_BODY_RECEIVED);
                    }
                } catch (RuntimeException ex) {
                    httpContent.release();
                    log.warn("Response already received before completing the inbound request. {}", ex.getMessage());
                }
            }
        } else {
            log.warn("Inconsistent state detected : inboundRequestMsg is null for channel read event");
        }
    }

    private void notifyRequestListener(HttpCarbonMessage httpRequestMsg, ChannelHandlerContext ctx) {

        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceRequestReceiving(httpRequestMsg);
        }

        if (serverConnectorFuture != null) {
            try {
                ServerConnectorFuture outboundRespFuture = httpRequestMsg.getHttpResponseFuture();
                outboundRespFuture.setHttpConnectorListener(
                        new HttpOutboundRespListener(ctx, httpRequestMsg, chunkConfig, keepAliveConfig, serverName,
                                                     sourceErrorHandler, continueRequest));
                this.serverConnectorFuture.notifyHttpListener(httpRequestMsg);
            } catch (Exception e) {
                log.error("Error while notifying listeners", e);
            }
        } else {
            log.error("Cannot find registered listener to forward the message");
        }
    }

    private boolean isDiffered(HttpCarbonMessage sourceReqCmsg) {
        //Http resource stored in the HttpCarbonMessage means execution waits till payload.
        return sourceReqCmsg.getProperty(Constants.HTTP_RESOURCE) != null;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        allChannels.add(ctx.channel());
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        if (this.handlerExecutor != null) {
            this.handlerExecutor.executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
        this.ctx = ctx;
        this.remoteAddress = ctx.channel().remoteAddress();
        sourceErrorHandler.setState(CONNECTED);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.close();
        if (!idleTimeout) {
            sourceErrorHandler.handleErrorCloseScenario(inboundRequestMsg);
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
        sourceErrorHandler.exceptionCaught(cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            this.idleTimeout = true;
            boolean inCompleteRequest = sourceErrorHandler.getState() == RECEIVING_ENTITY_BODY;
            ChannelFuture outboundRespFuture = sourceErrorHandler.handleIdleErrorScenario(inboundRequestMsg, ctx);
            if (outboundRespFuture == null) {
                this.channelInactive(ctx);
            } else {
                outboundRespFuture.addListener((ChannelFutureListener) channelFuture -> {
                    sourceErrorHandler.setState(ENTITY_BODY_SENT);
                    Throwable cause = channelFuture.cause();
                    if (cause != null) {
                        log.warn("Failed to send: {}", cause.getMessage());
                    }
                    this.channelInactive(ctx);
                    if (inCompleteRequest) {
                        sourceErrorHandler.handleIncompleteInboundRequest(
                                IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST);
                    }
                });
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

    public EventLoop getEventLoop() {
        return this.ctx.channel().eventLoop();
    }

    public Map<String, GenericObjectPool> getTargetChannelPool() {
        return targetChannelPool;
    }

    public ChannelHandlerContext getInboundChannelContext() {
        return ctx;
    }
}
