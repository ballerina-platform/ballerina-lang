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

package org.wso2.transport.http.netty.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorException;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketInboundFrameHandler;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.websocket.WebSocketUtil;

import java.net.URISyntaxException;

/**
 * This class handles all kinds of WebSocketFrames
 * after connection is upgraded from HTTP to WebSocket.
 */
public class WebSocketSourceHandler extends WebSocketInboundFrameHandler {


    private final WebSocketFramesBlockingHandler blockingHandler;
    private HandlerExecutor handlerExecutor;

    /**
     * @param connectorFuture {@link WebSocketConnectorFuture} to notify messages to application.
     * @param blockingHandler {@link WebSocketFramesBlockingHandler} to be used when creating the WebSocket connection.
     * @param isSecured       indication of whether the connection is secured or not.
     * @param httpRequest     {@link FullHttpRequest} which contains the details of WebSocket Upgrade.
     * @param interfaceId     given ID for the socket interface.
     */
    public WebSocketSourceHandler(WebSocketConnectorFuture connectorFuture,
                                  WebSocketFramesBlockingHandler blockingHandler, boolean isSecured,
                                  FullHttpRequest httpRequest, String interfaceId) {
        super(connectorFuture, true, isSecured, httpRequest.uri(), interfaceId);
        this.blockingHandler = blockingHandler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws URISyntaxException {
        // Start the server connection Timer
        this.handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
        if (this.handlerExecutor != null) {
            this.handlerExecutor.executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
        webSocketConnection =
                WebSocketUtil.getWebSocketConnection(ctx, this, blockingHandler, securedConnection, target);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws WebSocketConnectorException {
        // Stop the connector timer
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
            handlerExecutor = null;
        }
        super.channelInactive(ctx);
    }
}
