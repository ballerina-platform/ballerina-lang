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

package org.wso2.carbon.transport.http.netty.contractImpl.websocket.message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.contractImpl.websocket.BasicWebSocketChannelContextImpl;
import org.wso2.carbon.transport.http.netty.internal.websocket.WebSocketUtil;
import org.wso2.carbon.transport.http.netty.internal.websocket.WebSocketSessionImpl;
import org.wso2.carbon.transport.http.netty.listener.WebSocketSourceHandler;

import java.net.ProtocolException;
import javax.websocket.Session;

/**
 * Implementation of {@link WebSocketInitMessage}.
 */
public class WebSocketInitMessageImpl extends BasicWebSocketChannelContextImpl implements WebSocketInitMessage {

    private final ChannelHandlerContext ctx;
    private final HttpRequest httpRequest;
    private final WebSocketServerHandshaker handshaker;
    private final BasicWebSocketChannelContextImpl basicWebSocketChannelContext;
    private final ServerConnectorFuture connectorFuture;

    public WebSocketInitMessageImpl(HttpRequest httpRequest, ServerConnectorFuture connectorFuture,
                                    BasicWebSocketChannelContextImpl webSocketChannelContext,
                                    ChannelHandlerContext ctx) {
        super(webSocketChannelContext.getSubProtocol(), webSocketChannelContext.getTarget(),
              webSocketChannelContext.getListenerPort(), webSocketChannelContext.isConnectionSecured(),
              webSocketChannelContext.isServerMessage(), webSocketChannelContext.getConnectionManager(),
              webSocketChannelContext.getListenerConfiguration());
        this.httpRequest = httpRequest;
        this.basicWebSocketChannelContext = webSocketChannelContext;
        this.ctx = ctx;
        this.connectorFuture = connectorFuture;

        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(httpRequest), subProtocol, true);
        this.handshaker = wsFactory.newHandshaker(httpRequest);
    }

    @Override
    public Session handshake() throws ProtocolException {

        try {
            WebSocketSessionImpl serverSession = WebSocketUtil.getSession(ctx, isConnectionSecured, target);
            WebSocketSourceHandler webSocketSourceHandler =
                    new WebSocketSourceHandler(WebSocketUtil.getSessionID(ctx), this.connectionManager,
                                               this.listenerConfiguration, httpRequest, isConnectionSecured, ctx,
                                               basicWebSocketChannelContext, connectorFuture, serverSession);
            handshaker.handshake(ctx.channel(), httpRequest);

            //Replace HTTP handlers  with  new Handlers for WebSocket in the pipeline
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addLast("ws_handler", webSocketSourceHandler);

            // TODO: handle Idle state in WebSocket with configurations.
            pipeline.remove(Constants.IDLE_STATE_HANDLER);
            pipeline.remove("SourceHandler");

            setProperty(Constants.SRC_HANDLER, webSocketSourceHandler);
            return serverSession;
        } catch (Exception e) {
            /*
            Code 1002 : indicates that an endpoint is terminating the connection
            due to a protocol error.
             */
            handshaker.close(ctx.channel(),
                             new CloseWebSocketFrame(1002,
                                                     "Terminating the connection due to a protocol error."));
            throw new ProtocolException("Error occurred in HTTP to WebSocket Upgrade : " + e.getMessage());
        }
    }

    @Override
    public void cancelHandShake(int closeCode, String closeReason) {
        handshaker.close(ctx.channel(), new CloseWebSocketFrame(closeCode, closeReason));
    }

    /* Get the URL of the given connection */
    private String getWebSocketURL(HttpRequest req) {
        String protocol = "ws";
        if (isConnectionSecured) {
            protocol = "wss";
        }
        String url =   protocol + "://" + req.headers().get("Host") + req.getUri();
        return url;
    }
}
