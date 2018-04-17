/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.wso2.transport.http.netty.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionPrefaceAndSettingsFrameWrittenEvent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.exception.EndpointTimeOutException;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;
import org.wso2.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.sender.http2.ClientOutboundHandler;
import org.wso2.transport.http.netty.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.sender.http2.OutboundMsgHolder;

/**
 * A class responsible for handling responses coming from BE.
 */
public class TargetHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(TargetHandler.class);

    private HttpResponseFuture httpResponseFuture;
    private HTTPCarbonMessage targetRespMsg;
    private ConnectionManager connectionManager;
    private TargetChannel targetChannel;
    private ClientOutboundHandler http2ClientOutboundHandler;
    private HTTPCarbonMessage incomingMsg;
    private HandlerExecutor handlerExecutor;
    private KeepAliveConfig keepAliveConfig;
    private boolean idleTimeoutTriggered;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
        if (handlerExecutor != null) {
            handlerExecutor.executeAtTargetConnectionInitiation(Integer.toString(ctx.hashCode()));
        }

        super.channelActive(ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (targetChannel.isRequestWritten()) {
            if (msg instanceof HttpResponse) {
                HttpResponse httpInboundResponse = (HttpResponse) msg;
                targetRespMsg = setUpCarbonMessage(ctx, msg);

                if (handlerExecutor != null) {
                    handlerExecutor.executeAtTargetResponseReceiving(targetRespMsg);
                }
                OutboundMsgHolder msgHolder = http2ClientOutboundHandler.
                        getHttp2ClientChannel().getInFlightMessage(Http2CodecUtil.HTTP_UPGRADE_STREAM_ID);
                if (msgHolder != null) {
                    // Response received over HTTP/1.x connection, so mark no push promises available in the channel
                    msgHolder.markNoPromisesReceived();
                }
                if (this.httpResponseFuture != null) {
                    httpResponseFuture.notifyHttpListener(targetRespMsg);
                } else {
                    log.error("Cannot notify the response to client as there is no associated responseFuture");
                }

                if (httpInboundResponse.decoderResult().isFailure()) {
                    log.warn(httpInboundResponse.decoderResult().cause().getMessage());
                }
            } else {
                if (targetRespMsg != null) {
                    HttpContent httpContent = (HttpContent) msg;
                    targetRespMsg.addHttpContent(httpContent);

                    if (Util.isLastHttpContent(httpContent)) {
                        if (handlerExecutor != null) {
                            handlerExecutor.executeAtTargetResponseSending(targetRespMsg);
                        }
                        this.targetRespMsg = null;
                        targetChannel.getChannel().pipeline().remove(Constants.IDLE_STATE_HANDLER);

                        if (!isKeepAlive(keepAliveConfig)) {
                            closeChannel(ctx);
                        }

                        connectionManager.returnChannel(targetChannel);
                    }
                }
            }
        } else {
            if (msg instanceof HttpResponse) {
                log.warn("Received a response for an obsolete request", msg.toString());
            }
            ReferenceCountUtil.release(msg);
        }
    }

    private HTTPCarbonMessage setUpCarbonMessage(ChannelHandlerContext ctx, Object msg) {
        targetRespMsg = new HttpCarbonResponse((HttpResponse) msg);
        targetRespMsg.setProperty(Constants.POOLED_BYTE_BUFFER_FACTORY, new PooledDataStreamerFactory(ctx.alloc()));

        targetRespMsg.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        HttpResponse httpResponse = (HttpResponse) msg;
        targetRespMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.status().code());

        //copy required properties for service chaining from incoming carbon message to the response carbon message
        //copy shared worker pool
        targetRespMsg.setProperty(Constants.EXECUTOR_WORKER_POOL, incomingMsg
                .getProperty(Constants.EXECUTOR_WORKER_POOL));

        if (handlerExecutor != null) {
            handlerExecutor.executeAtTargetResponseReceiving(targetRespMsg);
        }

        return targetRespMsg;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Channel " + ctx.channel().id() + " got inactive so closing it from TargetHandler");
        }

        closeChannel(ctx);
        handleErrorCloseScenarios(ctx.channel().id().asLongText());

        connectionManager.invalidateTargetChannel(targetChannel);

        if (handlerExecutor != null) {
            handlerExecutor.executeAtTargetConnectionTermination(Integer.toString(ctx.hashCode()));
            handlerExecutor = null;
        }
    }

    private void handleErrorCloseScenarios(String channelID) {
        if (!idleTimeoutTriggered) {
            if (targetChannel.isRequestWritten()) {
                httpResponseFuture.notifyHttpListener(new ClientConnectorException(channelID,
                        Constants.REMOTE_SERVER_CLOSE_RESPONSE_CONNECTION_AFTER_REQUEST_READ));
            } else if (targetRespMsg != null) {
                handleIncompleteInboundResponse(Constants.REMOTE_SERVER_ABRUPTLY_CLOSE_RESPONSE_CONNECTION);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception occurred in TargetHandler for channel " + ctx.channel().id().asLongText(), cause);

        httpResponseFuture.notifyHttpListener(cause);
        if (targetRespMsg != null) {
            handleIncompleteInboundResponse(Constants.EXCEPTION_CAUGHT_WHILE_READING_RESPONSE);
        }
        closeChannel(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE) {
                targetChannel.getChannel().pipeline().remove(Constants.IDLE_STATE_HANDLER);
                this.idleTimeoutTriggered = true;
                this.channelInactive(ctx);
                handleErrorIdleScenarios(ctx.channel().id().asLongText());

                log.warn("Idle timeout has reached hence closing the connection {}", ctx.channel().id());
            }
        } else if (evt instanceof HttpClientUpgradeHandler.UpgradeEvent) {
            HttpClientUpgradeHandler.UpgradeEvent upgradeEvent = (HttpClientUpgradeHandler.UpgradeEvent) evt;
            if (HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_SUCCESSFUL.name().equals(upgradeEvent.name())) {
                executePostUpgradeActions(ctx);
            }
            ctx.fireUserEventTriggered(evt);
        } else if (evt instanceof Http2ConnectionPrefaceAndSettingsFrameWrittenEvent) {
            log.debug("Connection Preface and Settings frame written");
        } else {
            log.warn("Unexpected user event {} triggered", evt.toString());
        }
    }

    private void executePostUpgradeActions(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
        ctx.pipeline().addLast(Constants.OUTBOUND_HANDLER, http2ClientOutboundHandler);

        Http2ClientChannel http2ClientChannel = http2ClientOutboundHandler.getHttp2ClientChannel();
        http2ClientChannel.setUpgradedToHttp2(true);

        // Remove Http specific handlers
        Util.safelyRemoveHandlers(targetChannel.getChannel().pipeline(),
                                  Constants.IDLE_STATE_HANDLER, Constants.HTTP_TRACE_LOG_HANDLER);

        http2ClientChannel.getInFlightMessage(Http2CodecUtil.HTTP_UPGRADE_STREAM_ID).setRequestWritten(true);
        http2ClientChannel.getDataEventListeners().
                forEach(dataEventListener ->
                                dataEventListener.onStreamInit(Http2CodecUtil.HTTP_UPGRADE_STREAM_ID, ctx));
        handoverChannelToHttp2ConnectionManager();
    }

    private void handoverChannelToHttp2ConnectionManager() {
        connectionManager.getHttp2ConnectionManager().
                addHttp2ClientChannel(targetChannel.getHttpRoute(), targetChannel.getHttp2ClientChannel());
    }

    private void handleErrorIdleScenarios(String channelID) {
        if (targetRespMsg == null) {
            httpResponseFuture.notifyHttpListener(new EndpointTimeOutException(channelID,
                    Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_READING_INBOUND_RESPONSE,
                    HttpResponseStatus.GATEWAY_TIMEOUT.code()));
        } else {
            handleIncompleteInboundResponse(Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE);
        }
    }

    private void closeChannel(ChannelHandlerContext ctx) throws Exception {
        // The if condition here checks if the connection has already been closed by either the client or the backend.
        // If it was the backend which closed the connection, the channel inactive event will be triggered and
        // subsequently, this method will be called.
        if (ctx != null && ctx.channel().isActive()) {
            ctx.close();
        }
    }

    private void handleIncompleteInboundResponse(String errorMessage) {
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(errorMessage)));
        targetRespMsg.addHttpContent(lastHttpContent);
        log.warn(errorMessage);
    }

    public void setHttpResponseFuture(HttpResponseFuture httpResponseFuture) {
        this.httpResponseFuture = httpResponseFuture;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public HTTPCarbonMessage getIncomingMsg() {
        return incomingMsg;
    }

    public void setIncomingMsg(HTTPCarbonMessage incomingMsg) {
        this.incomingMsg = incomingMsg;
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    public KeepAliveConfig getKeepAliveConfig() {
        return keepAliveConfig;
    }

    public void setKeepAliveConfig(KeepAliveConfig keepAliveConfig) {
        this.keepAliveConfig = keepAliveConfig;
    }

    public HttpResponseFuture getHttpResponseFuture() {
        return httpResponseFuture;
    }

    public void setHttp2ClientOutboundHandler(ClientOutboundHandler http2ClientOutboundHandler) {
        this.http2ClientOutboundHandler = http2ClientOutboundHandler;
    }

    private boolean isKeepAlive(KeepAliveConfig keepAliveConfig) {
        switch (keepAliveConfig) {
            case AUTO:
                if (Float.valueOf((String) getIncomingMsg().getProperty(Constants.HTTP_VERSION)) > Constants.HTTP_1_0) {
                    return true;
                } else {
                    return false;
                }
            case ALWAYS:
                return true;
            case NEVER:
                return false;
            default:
               // The execution will never reach here. To make the code compilable, default case has to be placed here.
               return true;
        }
    }
}
