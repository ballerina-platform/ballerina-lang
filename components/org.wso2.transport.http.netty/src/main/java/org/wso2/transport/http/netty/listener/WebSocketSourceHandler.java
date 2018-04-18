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
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketMessageImpl;
import org.wso2.transport.http.netty.contractimpl.websocket.message.WebSocketCloseMessageImpl;
import org.wso2.transport.http.netty.contractimpl.websocket.message.WebSocketControlMessageImpl;
import org.wso2.transport.http.netty.exception.UnknownWebSocketFrameTypeException;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.websocket.WebSocketSessionImpl;
import org.wso2.transport.http.netty.internal.websocket.WebSocketUtil;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * This class handles all kinds of WebSocketFrames
 * after connection is upgraded from HTTP to WebSocket.
 */
public class WebSocketSourceHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSourceHandler.class);
    private final String target;
    private final ChannelHandlerContext ctx;
    private final boolean isSecured;
    private final ServerConnectorFuture connectorFuture;
    private final WebSocketSessionImpl channelSession;
    private final Map<String, String> headers;
    private final String interfaceId;
    private String subProtocol = null;
    private HandlerExecutor handlerExecutor;

    /**
     * @param connectorFuture {@link ServerConnectorFuture} to notify messages to application.
     * @param isSecured       indication of whether the connection is secured or not.
     * @param channelSession  session relates to the channel.
     * @param httpRequest     {@link HttpRequest} which contains the details of WebSocket Upgrade.
     * @param headers         Headers obtained from HTTP WebSocket upgrade request.
     * @param ctx             {@link ChannelHandlerContext} of WebSocket connection.
     * @param interfaceId     given ID for the socket interface.
     */
    public WebSocketSourceHandler(ServerConnectorFuture connectorFuture, boolean isSecured,
                                  WebSocketSessionImpl channelSession, HttpRequest httpRequest,
                                  Map<String, String> headers, ChannelHandlerContext ctx, String interfaceId) {
        this.connectorFuture = connectorFuture;
        this.isSecured = isSecured;
        this.channelSession = channelSession;
        this.ctx = ctx;
        this.interfaceId = interfaceId;
        this.target = httpRequest.uri();
        this.headers = headers;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        // Start the server connection Timer
        this.handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
        if (this.handlerExecutor != null) {
            this.handlerExecutor.executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
        ctx.channel().read();
    }

    /**
     * Retrieve server session of this source handler.
     *
     * @return the server session of this source handler.
     */
    public WebSocketSessionImpl getChannelSession() {
        return channelSession;
    }

    /**
     * Set if there is any negotiated sub protocol.
     *
     * @param negotiatedSubProtocol negotiated sub protocol for a given connection.
     */
    public void setNegotiatedSubProtocol(String negotiatedSubProtocol) {
        this.subProtocol = negotiatedSubProtocol;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleStateEvent.ALL_IDLE_STATE_EVENT.state()) {
                notifyIdleTimeout();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Stop the connector timer
        ctx.close();
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
            handlerExecutor = null;
        }

        if (channelSession.isOpen()) {
            channelSession.setIsOpen(false);
            int statusCode = 1001; // Client is going away.
            String reasonText = "Client is going away";
            notifyCloseMessage(statusCode, reasonText);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws UnknownWebSocketFrameTypeException, ServerConnectorException {
        if (!(msg instanceof WebSocketFrame)) {
            logger.error("Expecting WebSocketFrame. Unknown type.");
            throw new UnknownWebSocketFrameTypeException("Expecting WebSocketFrame. Unknown type.");
        }
        if (msg instanceof TextWebSocketFrame) {
            notifyTextMessage((TextWebSocketFrame) msg);
        } else if (msg instanceof BinaryWebSocketFrame) {
            notifyBinaryMessage((BinaryWebSocketFrame) msg);
        } else if (msg instanceof CloseWebSocketFrame) {
            notifyCloseMessage((CloseWebSocketFrame) msg);
        } else if (msg instanceof PingWebSocketFrame) {
            notifyPingMessage((PingWebSocketFrame) msg);
        } else if (msg instanceof PongWebSocketFrame) {
            notifyPongMessage((PongWebSocketFrame) msg);
        }
        ctx.channel().read();
    }

    private void notifyTextMessage(TextWebSocketFrame textWebSocketFrame) throws ServerConnectorException {
        WebSocketMessageImpl webSocketTextMessage = WebSocketUtil.getWebSocketMessage(textWebSocketFrame);
        setupCommonProperties(webSocketTextMessage);
        connectorFuture.notifyWSListener((WebSocketTextMessage) webSocketTextMessage);
    }

    private void notifyBinaryMessage(BinaryWebSocketFrame binaryWebSocketFrame) throws ServerConnectorException {
        WebSocketMessageImpl webSocketBinaryMessage = WebSocketUtil.getWebSocketMessage(binaryWebSocketFrame);
        setupCommonProperties(webSocketBinaryMessage);
        connectorFuture.notifyWSListener((WebSocketBinaryMessage) webSocketBinaryMessage);
    }

    private void notifyCloseMessage(CloseWebSocketFrame closeWebSocketFrame) throws ServerConnectorException {
        String reasonText = closeWebSocketFrame.reasonText();
        int statusCode = closeWebSocketFrame.statusCode();
        ctx.channel().close();
        channelSession.setIsOpen(false);
        WebSocketMessageImpl webSocketCloseMessage = new WebSocketCloseMessageImpl(statusCode, reasonText);
        closeWebSocketFrame.release();
        setupCommonProperties(webSocketCloseMessage);
        connectorFuture.notifyWSListener((WebSocketCloseMessage) webSocketCloseMessage);
    }

    private void notifyCloseMessage(int statusCode, String reasonText) throws ServerConnectorException {
        ctx.channel().close();
        channelSession.setIsOpen(false);
        WebSocketMessageImpl webSocketCloseMessage =
                new WebSocketCloseMessageImpl(statusCode, reasonText);
        setupCommonProperties(webSocketCloseMessage);
        connectorFuture.notifyWSListener((WebSocketCloseMessage) webSocketCloseMessage);
    }

    private void notifyPingMessage(PingWebSocketFrame pingWebSocketFrame) throws ServerConnectorException {
        WebSocketControlMessage webSocketControlMessage = WebSocketUtil.
                getWebsocketControlMessage(pingWebSocketFrame, WebSocketControlSignal.PING);
        setupCommonProperties((WebSocketMessageImpl) webSocketControlMessage);
        connectorFuture.notifyWSListener(webSocketControlMessage);
    }

    private void notifyPongMessage(PongWebSocketFrame pongWebSocketFrame) throws ServerConnectorException {
        WebSocketControlMessage webSocketControlMessage = WebSocketUtil.
                getWebsocketControlMessage(pongWebSocketFrame, WebSocketControlSignal.PONG);
        setupCommonProperties((WebSocketMessageImpl) webSocketControlMessage);
        connectorFuture.notifyWSListener(webSocketControlMessage);
    }

    private void notifyIdleTimeout() throws ServerConnectorException {
        WebSocketMessageImpl websocketControlMessage = new WebSocketControlMessageImpl(
                WebSocketControlSignal.IDLE_TIMEOUT, null);
        setupCommonProperties(websocketControlMessage);
        connectorFuture.notifyWSIdleTimeout((WebSocketControlMessage) websocketControlMessage);
    }

    private void setupCommonProperties(WebSocketMessageImpl webSocketMessage) {
        webSocketMessage.setSubProtocol(subProtocol);
        webSocketMessage.setTarget(target);
        webSocketMessage.setListenerInterface(interfaceId);
        webSocketMessage.setIsConnectionSecured(isSecured);
        webSocketMessage.setIsServerMessage(true);
        webSocketMessage.setChannelSession(channelSession);
        webSocketMessage.setHeaders(headers);
        webSocketMessage.setSessionlID(channelSession.getId());

        webSocketMessage.setProperty(Constants.SRC_HANDLER, this);
        webSocketMessage.setProperty(Constants.LISTENER_PORT,
                                     ((InetSocketAddress) ctx.channel().localAddress()).getPort());
        webSocketMessage.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        webSocketMessage.setProperty(
                Constants.LOCAL_NAME, ((InetSocketAddress) ctx.channel().localAddress()).getHostName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().writeAndFlush(new CloseWebSocketFrame(1011,
                "Encountered an unexpected condition"));
        ctx.close();
        connectorFuture.notifyWSListener(cause);
    }
}
