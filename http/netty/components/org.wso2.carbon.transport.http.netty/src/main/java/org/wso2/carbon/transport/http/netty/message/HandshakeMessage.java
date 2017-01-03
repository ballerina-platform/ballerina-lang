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

package org.wso2.carbon.transport.http.netty.message;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.websocket.WebSocketHandshakeMessage;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.WebSocketResponderImpl;
import org.wso2.carbon.transport.http.netty.listener.WebSocketSourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.URISyntaxException;


/**
 * This class is responsible for handling handshake for new WebSocket Connection.
 */
public class HandshakeMessage extends WebSocketHandshakeMessage {

    private Logger log = LoggerFactory.getLogger(HandshakeMessage.class);
    private final WebSocketServerHandshaker handshaker;
    private final ChannelHandlerContext ctx;
    private final HttpRequest request;
    private final ConnectionManager connectionManager;
    private final ListenerConfiguration listenerConfiguration;


    public HandshakeMessage(ChannelHandlerContext ctx, HttpRequest request,
                            ConnectionManager connectionManager,
                            ListenerConfiguration listenerConfiguration) {
        super(new WebSocketResponderImpl(ctx));
        this.ctx = ctx;
        this.request = request;
        this.connectionManager = connectionManager;
        this.listenerConfiguration = listenerConfiguration;

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketURL(request),
                                                                                          null, true);
        this.handshaker = wsFactory.newHandshaker(request);
    }

    @Override
    public boolean handshake() {
        log.info("Upgrading the connection from Http to WebSocket for " +
                         "channel : " + ctx.channel());

        boolean isDone = false;
        try {
            isDone = handleWebSocketHandshake(ctx, request);

            //Replace HTTP handlers  with  new Handlers for WebSocket in the pipeline
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.replace("handler",
                             "ws_handler",
                             new WebSocketSourceHandler(this.connectionManager,
                                                        this.listenerConfiguration,
                                                        request.getUri()));
            log.info("WebSocket upgrade is successful");
        } catch (URISyntaxException e) {
            log.error(e.toString());
        } catch (Exception e) {
            log.error(e.toString());
        }

        return isDone;
    }

    @Override
    public void cancel() {
        handshaker.close(ctx.channel(), new CloseWebSocketFrame());
        log.info("Handshake is cancelled.");
    }

    /* Do the handshaking for WebSocket request */
    private boolean handleWebSocketHandshake(ChannelHandlerContext ctx, HttpRequest req) throws URISyntaxException {
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return false;
        } else {
            ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), req);
            return channelFuture.isDone();
        }
    }

    private String getWebSocketURL(HttpRequest req) {
        String url =  "ws://" + req.headers().get("Host") + req.getUri();
        return url;
    }
}
