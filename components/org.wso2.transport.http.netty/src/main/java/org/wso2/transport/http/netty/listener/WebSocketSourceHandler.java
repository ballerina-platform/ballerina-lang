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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
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
import org.wso2.transport.http.netty.contract.websocket.WebSocketFrameType;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultWebSocketConnection;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketInboundFrameHandler;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketMessageImpl;
import org.wso2.transport.http.netty.contractimpl.websocket.message.WebSocketCloseMessageImpl;
import org.wso2.transport.http.netty.contractimpl.websocket.message.WebSocketControlMessageImpl;
import org.wso2.transport.http.netty.exception.UnknownWebSocketFrameTypeException;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.websocket.WebSocketUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This class handles all kinds of WebSocketFrames
 * after connection is upgraded from HTTP to WebSocket.
 */
public class WebSocketSourceHandler extends WebSocketInboundFrameHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSourceHandler.class);
    private final String target;
    private final ChannelHandlerContext ctx;
    private final boolean isSecured;
    private final ServerConnectorFuture connectorFuture;
    private final Map<String, String> headers;
    private final String interfaceId;
    private String subProtocol = null;
    private HandlerExecutor handlerExecutor;
    private WebSocketFrameType continuationFrameType;
    private CountDownLatch closeCountDownLatch = null;
    private DefaultWebSocketConnection webSocketConnection;
    private boolean closeFrameReceived;

    /**
     * @param connectorFuture {@link ServerConnectorFuture} to notify messages to application.
     * @param isSecured       indication of whether the connection is secured or not.
     * @param httpRequest     {@link FullHttpRequest} which contains the details of WebSocket Upgrade.
     * @param headers         Headers obtained from HTTP WebSocket upgrade request.
     * @param ctx             {@link ChannelHandlerContext} of WebSocket connection.
     * @param interfaceId     given ID for the socket interface.
     */
    public WebSocketSourceHandler(ServerConnectorFuture connectorFuture, boolean isSecured, FullHttpRequest httpRequest,
                                  Map<String, String> headers, ChannelHandlerContext ctx, String interfaceId) {
        this.connectorFuture = connectorFuture;
        this.isSecured = isSecured;
        this.ctx = ctx;
        this.interfaceId = interfaceId;
        this.target = httpRequest.uri();
        this.headers = headers;
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
    public DefaultWebSocketConnection getWebSocketConnection() {
        return webSocketConnection;
    }

    @Override
    public boolean isCloseFrameReceived() {
        return closeFrameReceived;
    }

    @Override
    public void setCloseCountDownLatch(CountDownLatch closeCountDownLatch) {
        this.closeCountDownLatch = closeCountDownLatch;
    }

    @Override
    public ChannelHandlerContext getChannelHandlerContext() {
        return this.ctx;
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
        webSocketConnection = WebSocketUtil.getWebSocketConnection(this, isSecured, target);
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
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
            handlerExecutor = null;
        }

        if (!(this.closeFrameReceived || webSocketConnection.closeFrameSent())) {
            // Notify abnormal closure.
            WebSocketMessageImpl webSocketCloseMessage =
                    new WebSocketCloseMessageImpl(Constants.WEBSOCKET_STATUS_CODE_ABNORMAL_CLOSURE);
            setupCommonProperties(webSocketCloseMessage);
            connectorFuture.notifyWSListener((WebSocketCloseMessage) webSocketCloseMessage);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws UnknownWebSocketFrameTypeException, ServerConnectorException {
        if (!(msg instanceof WebSocketFrame)) {
            logger.error("Expecting WebSocketFrame. Unknown type.");
            throw new UnknownWebSocketFrameTypeException("Expecting WebSocketFrame. Unknown type.");
        }

        // If the continuation of frames are not following the protocol, netty handles them internally.
        // Hence those situations are not handled here.
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
            if (!textFrame.isFinalFragment()) {
                continuationFrameType = WebSocketFrameType.TEXT;
            }
            notifyTextMessage(textFrame, textFrame.text(), textFrame.isFinalFragment());
        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) msg;
            if (!binaryFrame.isFinalFragment()) {
                continuationFrameType = WebSocketFrameType.BINARY;
            }
            notifyBinaryMessage(binaryFrame, binaryFrame.content(), binaryFrame.isFinalFragment());
        } else if (msg instanceof CloseWebSocketFrame) {
            webSocketConnection.setCloseFrameReceived(true);
            notifyCloseMessage((CloseWebSocketFrame) msg);
        } else if (msg instanceof PingWebSocketFrame) {
            notifyPingMessage((PingWebSocketFrame) msg);
        } else if (msg instanceof PongWebSocketFrame) {
            notifyPongMessage((PongWebSocketFrame) msg);
        } else if (msg instanceof ContinuationWebSocketFrame) {
            ContinuationWebSocketFrame frame = (ContinuationWebSocketFrame) msg;
            switch (continuationFrameType) {
                case TEXT:
                    notifyTextMessage(frame, frame.text(), frame.isFinalFragment());
                    break;
                case BINARY:
                    notifyBinaryMessage(frame, frame.content(), frame.isFinalFragment());
                    break;
            }
        }
    }

    private void notifyTextMessage(WebSocketFrame frame, String text, boolean finalFragment)
            throws ServerConnectorException {
        WebSocketMessageImpl webSocketTextMessage = WebSocketUtil.getWebSocketMessage(frame, text, finalFragment);
        setupCommonProperties(webSocketTextMessage);
        connectorFuture.notifyWSListener((WebSocketTextMessage) webSocketTextMessage);
    }

    private void notifyBinaryMessage(WebSocketFrame frame, ByteBuf content, boolean finalFragment)
            throws ServerConnectorException {
        WebSocketMessageImpl webSocketBinaryMessage = WebSocketUtil.getWebSocketMessage(frame, content, finalFragment);
        setupCommonProperties(webSocketBinaryMessage);
        connectorFuture.notifyWSListener((WebSocketBinaryMessage) webSocketBinaryMessage);
    }

    private void notifyCloseMessage(CloseWebSocketFrame closeWebSocketFrame) throws ServerConnectorException {
        String reasonText = closeWebSocketFrame.reasonText();
        int statusCode = closeWebSocketFrame.statusCode();

        // closeCountDownLatch == null means that WebSocketConnection has not yet initiated a connection closure.
        if (closeCountDownLatch == null) {
            WebSocketMessageImpl webSocketCloseMessage = new WebSocketCloseMessageImpl(statusCode, reasonText);
            setupCommonProperties(webSocketCloseMessage);
            connectorFuture.notifyWSListener((WebSocketCloseMessage) webSocketCloseMessage);
            closeFrameReceived = true;
        } else {
            ctx.writeAndFlush(new CloseWebSocketFrame(statusCode, reasonText)).addListener(
                    future -> closeCountDownLatch.countDown());
        }
        closeWebSocketFrame.release();
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
        webSocketMessage.setWebSocketConnection(webSocketConnection);
        webSocketMessage.setHeaders(headers);
        webSocketMessage.setSessionlID(webSocketConnection.getId());

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
