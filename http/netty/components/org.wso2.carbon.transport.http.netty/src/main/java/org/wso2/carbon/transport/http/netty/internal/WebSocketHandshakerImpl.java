/*
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.transport.http.netty.internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.websocket.WebSocketHandshaker;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.listener.WebSocketSourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

/**
 * This implementation handles the handshake of requested WebSocket Connection.
 * This implementation should be added to {@link org.wso2.carbon.messaging.CarbonMessage} as a property and sent.
 * @since 1.0.0
 */
public class WebSocketHandshakerImpl implements WebSocketHandshaker {

    private Logger log = LoggerFactory.getLogger(WebSocketHandshakerImpl.class);
    private final WebSocketServerHandshaker handshaker;
    private final ChannelHandlerContext ctx;
    private final HttpRequest request;
    private final ConnectionManager connectionManager;
    private final ListenerConfiguration listenerConfiguration;

    /**
     * @param handshaker {@link WebSocketServerHandshaker} netty dependency for handshake
     * @param ctx {@link ChannelHandlerContext} of the request
     * @param request Incoming {@link HttpRequest}
     * @param connectionManager {@link ConnectionManager} of the connection
     * @param listenerConfiguration {@link ListenerConfiguration} for the connection
     */
    public WebSocketHandshakerImpl(WebSocketServerHandshaker handshaker, ChannelHandlerContext ctx,
                                   HttpRequest request,
                                   ConnectionManager connectionManager,
                                   ListenerConfiguration listenerConfiguration) {
        this.handshaker = handshaker;
        this.ctx = ctx;
        this.request = request;
        this.connectionManager = connectionManager;
        this.listenerConfiguration = listenerConfiguration;
    }

    @Override
    public boolean handshake() {
        log.info("Upgrading the connection from Http to WebSocket for " +
                         "channel : " + ctx.channel());

        handleWebSocketHandshake(ctx, request);
        //Replace HTTP handlers  with  new Handlers for WebSocket in the pipeline
        ChannelPipeline pipeline = ctx.pipeline();
        try {
            pipeline.replace(SourceHandler.class,
                             "websocket_handler",
                             new WebSocketSourceHandler(this.connectionManager,
                                                        this.listenerConfiguration,
                                                        request.getUri()));
        } catch (Exception e) {
            log.error("Handshake error : " + e.toString());
        }
        log.info("WebSocket upgrade is successful");
        return true;
    }

    @Override
    public void cancel() {
        handshaker.close(ctx.channel(), new CloseWebSocketFrame(1001, "Cannot find requested URI"));
        log.info("Handshake is cancelled.");
    }

    /* Do the handshaking for WebSocket request */
    private void handleWebSocketHandshake(ChannelHandlerContext ctx, HttpRequest req) {
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            /*
            Since httpAggregator and handshaker is already added when the handshaker is given from
            WebSocketServerHandshakerFactory those should be removed when it is done separately.
             */
            ctx.pipeline().remove("httpAggregator");
            ctx.pipeline().remove("handshaker");
            handshaker.handshake(ctx.channel(), req);
        }
    }

}
