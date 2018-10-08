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
package org.wso2.transport.http.netty.contractimpl.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionPrefaceAndSettingsFrameWrittenEvent;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contractimpl.common.states.MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.TimeoutHandler;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.contractimpl.common.Util.createInboundRespCarbonMsg;
import static org.wso2.transport.http.netty.contractimpl.common.Util.safelyRemoveHandlers;

/**
 * A class responsible for handling responses coming from BE.
 */
public class TargetHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(TargetHandler.class);

    private HttpResponseFuture httpResponseFuture;
    private HttpCarbonMessage inboundResponseMsg;
    private ConnectionManager connectionManager;
    private TargetChannel targetChannel;
    private Http2TargetHandler http2TargetHandler;
    private HttpCarbonMessage outboundRequestMsg;
    private HandlerExecutor handlerExecutor;
    private KeepAliveConfig keepAliveConfig;
    private boolean idleTimeoutTriggered;
    private ChannelHandlerContext context;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        context = ctx;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageStateContext messageStateContext = outboundRequestMsg.getMessageStateContext();

        if (handlerExecutor != null) {
            handlerExecutor.executeAtTargetResponseReceiving(inboundResponseMsg);
        }
        if (targetChannel.isRequestHeaderWritten()) {
            if (msg instanceof HttpResponse) {
                inboundResponseMsg = createInboundRespCarbonMsg(ctx, (HttpResponse) msg, outboundRequestMsg);
                messageStateContext.getSenderState().readInboundResponseHeaders(this, (HttpResponse) msg);
            } else {
                if (inboundResponseMsg != null) {
                    messageStateContext.getSenderState().readInboundResponseEntityBody(ctx, (HttpContent) msg,
                                                                                       getInboundResponseMsg());
                }
            }
        } else {
            if (msg instanceof HttpResponse) {
                LOG.warn("Received a response for an obsolete request {}", msg);
            }
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        if (handlerExecutor != null) {
            handlerExecutor.executeAtTargetConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!idleTimeoutTriggered) {
            outboundRequestMsg.getMessageStateContext().getSenderState().handleAbruptChannelClosure(httpResponseFuture);
        }
        connectionManager.invalidateTargetChannel(targetChannel);

        if (handlerExecutor != null) {
            handlerExecutor.executeAtTargetConnectionTermination(Integer.toString(ctx.hashCode()));
            handlerExecutor = null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        closeChannel(ctx);
        LOG.warn("Exception occurred in TargetHandler : {}", cause.getMessage());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            targetChannel.getChannel().pipeline().remove(Constants.IDLE_STATE_HANDLER);
            this.idleTimeoutTriggered = true;
            ctx.close();
            outboundRequestMsg.getMessageStateContext().getSenderState().handleIdleTimeoutConnectionClosure(
                    httpResponseFuture, ctx.channel().id().asLongText());
        } else if (evt instanceof HttpClientUpgradeHandler.UpgradeEvent) {
            HttpClientUpgradeHandler.UpgradeEvent upgradeEvent = (HttpClientUpgradeHandler.UpgradeEvent) evt;
            if (HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_SUCCESSFUL.name().equals(upgradeEvent.name())) {
                executePostUpgradeActions(ctx);
            }
            ctx.fireUserEventTriggered(evt);
        } else if (evt instanceof Http2ConnectionPrefaceAndSettingsFrameWrittenEvent) {
            LOG.debug("Connection Preface and Settings frame written");
        } else if (evt instanceof SslCloseCompletionEvent) {
            LOG.debug("SSL close completion event received");
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

    private void executePostUpgradeActions(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
        ctx.pipeline().addLast(Constants.HTTP2_TARGET_HANDLER, http2TargetHandler);
        Http2ClientChannel http2ClientChannel = http2TargetHandler.getHttp2ClientChannel();

        // Remove Http specific handlers
        safelyRemoveHandlers(targetChannel.getChannel().pipeline(), Constants.IDLE_STATE_HANDLER,
                             Constants.HTTP_TRACE_LOG_HANDLER);
        http2ClientChannel.addDataEventListener(
                Constants.IDLE_STATE_HANDLER,
                new TimeoutHandler(http2ClientChannel.getSocketIdleTimeout(), http2ClientChannel));

        http2ClientChannel.getInFlightMessage(Http2CodecUtil.HTTP_UPGRADE_STREAM_ID).setRequestWritten(true);
        http2ClientChannel.getDataEventListeners().
                forEach(dataEventListener ->
                                dataEventListener.onStreamInit(ctx, Http2CodecUtil.HTTP_UPGRADE_STREAM_ID));
        handoverChannelToHttp2ConnectionManager();
    }

    private void handoverChannelToHttp2ConnectionManager() {
        connectionManager.getHttp2ConnectionManager().
                addHttp2ClientChannel(targetChannel.getHttpRoute(), targetChannel.getHttp2ClientChannel());
    }

    public void closeChannel(ChannelHandlerContext ctx) {
        // The if condition here checks if the connection has already been closed by either the client or the backend.
        // If it was the backend which closed the connection, the channel inactive event will be triggered and
        // subsequently, this method will be called.
        if (ctx != null && ctx.channel().isActive()) {
            ctx.close();
        }
    }

    public void setHttpResponseFuture(HttpResponseFuture httpResponseFuture) {
        this.httpResponseFuture = httpResponseFuture;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setOutboundRequestMsg(HttpCarbonMessage outboundRequestMsg) {
        this.outboundRequestMsg = outboundRequestMsg;
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    public void setKeepAliveConfig(KeepAliveConfig keepAliveConfig) {
        this.keepAliveConfig = keepAliveConfig;
    }

    public HttpResponseFuture getHttpResponseFuture() {
        return httpResponseFuture;
    }

    void setHttp2TargetHandler(Http2TargetHandler http2TargetHandler) {
        this.http2TargetHandler = http2TargetHandler;
    }

    public HttpCarbonMessage getInboundResponseMsg() {
        return inboundResponseMsg;
    }

    public Http2TargetHandler getHttp2TargetHandler() {
        return http2TargetHandler;
    }

    public void resetInboundMsg() {
        this.inboundResponseMsg = null;
    }

    public TargetChannel getTargetChannel() {
        return targetChannel;
    }

    public KeepAliveConfig getKeepAliveConfig() {
        return keepAliveConfig;
    }

    public HttpCarbonMessage getOutboundRequestMsg() {
        return outboundRequestMsg;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Gets the {@link ChannelHandlerContext} of the {@link TargetHandler}.
     *
     * @return the {@link ChannelHandlerContext} of this handler.
     */
    public ChannelHandlerContext getContext() {
        return context;
    }
}
