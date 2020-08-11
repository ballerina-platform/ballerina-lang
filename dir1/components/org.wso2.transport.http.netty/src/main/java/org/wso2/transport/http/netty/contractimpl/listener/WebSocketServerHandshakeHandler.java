/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.Utf8FrameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorException;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketHandshaker;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;

import static org.wso2.transport.http.netty.contract.Constants.HTTP_OBJECT_AGGREGATOR;
import static org.wso2.transport.http.netty.contract.Constants.WEBSOCKET_COMPRESSION_HANDLER;

/**
 * WebSocket handshake handler for carbon transports.
 */
public class WebSocketServerHandshakeHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketServerHandshakeHandler.class);

    private final ServerConnectorFuture serverConnectorFuture;
    private boolean webSocketCompressionEnabled;
    private SourceHandler sourceHandler;

    public WebSocketServerHandshakeHandler(ServerConnectorFuture serverConnectorFuture,
                                           boolean webSocketCompressionEnabled) {
        this.serverConnectorFuture = serverConnectorFuture;
        this.webSocketCompressionEnabled = webSocketCompressionEnabled;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            HttpMethod requestMethod = httpRequest.method();

            if (containsUpgradeHeaders(httpRequest)) {
                if (HttpMethod.GET == requestMethod) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Upgrading the connection from Http to WebSocket for channel : {}", ctx.channel());
                    }
                    ChannelHandlerContext decoderCtx = modifyPipelineForUpgrade(ctx);
                    decoderCtx.fireChannelRead(msg);
                } else {
                    // According to spec since client must send a request with "GET" method, if method is not "GET"
                    // sends a bad request (status 400) in response.
                    DefaultHttpResponse badRequestResponse = new DefaultHttpResponse(httpRequest.protocolVersion(),
                                                                                     HttpResponseStatus.BAD_REQUEST);
                    ctx.writeAndFlush(badRequestResponse).addListener(future -> ctx.close());
                }
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }

    /**
     * Check the basic headers needed for WebSocket upgrade are contained in the request.
     *
     * @param httpRequest {@link HttpRequest} which is checked for WebSocket upgrade.
     * @return true if basic headers needed for WebSocket upgrade are contained in the request.
     */
    private boolean containsUpgradeHeaders(HttpRequest httpRequest) {
        HttpHeaders headers = httpRequest.headers();
        return headers.containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true) && headers
                .containsValue(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true);
    }

    /**
     * Modifies the pipeline and returns the context at the http decoder.
     *
     * @param ctx the current context
     * @return The context at the HttpRequestDecoder
     */
    private ChannelHandlerContext modifyPipelineForUpgrade(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        if (pipeline.get(Constants.BACK_PRESSURE_HANDLER) != null) {
            pipeline.remove(Constants.BACK_PRESSURE_HANDLER);
        }
        // Retain a reference for the sourceHandler to create a carbon message
        sourceHandler = (SourceHandler) pipeline.remove(Constants.HTTP_SOURCE_HANDLER);
        ChannelHandlerContext decoderCtx = pipeline.context(HttpRequestDecoder.class);
        pipeline.addAfter(decoderCtx.name(), HTTP_OBJECT_AGGREGATOR,
                          new HttpObjectAggregator(Constants.WEBSOCKET_REQUEST_SIZE));
        if (webSocketCompressionEnabled) {
            pipeline.addAfter(HTTP_OBJECT_AGGREGATOR, WEBSOCKET_COMPRESSION_HANDLER,
                              new WebSocketServerCompressionHandler());
            pipeline.addAfter(WEBSOCKET_COMPRESSION_HANDLER, Utf8FrameValidator.class.getName(),
                              new Utf8FrameValidator());
        } else {
            pipeline.addAfter(HTTP_OBJECT_AGGREGATOR, Utf8FrameValidator.class.getName(),
                              new Utf8FrameValidator());
        }
        pipeline.addAfter(Utf8FrameValidator.class.getName(), "handshake", new HandShakeHandler());
        return decoderCtx;
    }

    private class HandShakeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg)
                throws Exception {
            // Remove ourselves and do the actual handshake
            ctx.pipeline().remove(this);
            handleWebSocketHandshake(msg, ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Remove ourselves and fail the handshake
            ctx.pipeline().remove(this);
            ctx.fireExceptionCaught(cause);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            ctx.fireChannelInactive();
        }

        /**
         * Handle the WebSocket handshake.
         *
         * @param fullHttpRequest {@link HttpRequest} of the request.
         */
        private void handleWebSocketHandshake(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx)
                throws WebSocketConnectorException {
            DefaultWebSocketHandshaker webSocketHandshaker =
                    new DefaultWebSocketHandshaker(ctx, serverConnectorFuture, fullHttpRequest);

        // Setting common properties to handshaker
            webSocketHandshaker.setHttpCarbonRequest(
                    (HttpCarbonRequest) Util.createInboundReqCarbonMsg(fullHttpRequest, ctx, sourceHandler));

        ctx.channel().config().setAutoRead(false);
        serverConnectorFuture.notifyWebSocketListener(webSocketHandshaker);
    }
    }
}
