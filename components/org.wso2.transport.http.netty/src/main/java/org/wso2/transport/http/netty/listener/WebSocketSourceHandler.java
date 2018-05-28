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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
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
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultWebSocketMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketInboundFrameHandler;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketCloseMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketControlMessage;
import org.wso2.transport.http.netty.exception.UnknownWebSocketFrameTypeException;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.websocket.WebSocketUtil;

import java.net.InetSocketAddress;

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
    private final WebSocketFramesBlockingHandler blockingHandler;
    private DefaultWebSocketConnection webSocketConnection;
    private final String interfaceId;
    private HandlerExecutor handlerExecutor;
    private ChannelPromise closePromise;
    private WebSocketFrameType continuationFrameType;
    private boolean closeFrameReceived;

    /**
     * @param connectorFuture {@link ServerConnectorFuture} to notify messages to application.
     * @param blockingHandler {@link WebSocketFramesBlockingHandler} to be used when creating the WebSocket connection.
     * @param isSecured       indication of whether the connection is secured or not.
     * @param httpRequest     {@link FullHttpRequest} which contains the details of WebSocket Upgrade.
     * @param ctx             {@link ChannelHandlerContext} of WebSocket connection.
     * @param interfaceId     given ID for the socket interface.
     */
    public WebSocketSourceHandler(ServerConnectorFuture connectorFuture, WebSocketFramesBlockingHandler blockingHandler,
                                  boolean isSecured, FullHttpRequest httpRequest,
                                  ChannelHandlerContext ctx, String interfaceId) {
        this.connectorFuture = connectorFuture;
        this.blockingHandler = blockingHandler;
        this.isSecured = isSecured;
        this.ctx = ctx;
        this.interfaceId = interfaceId;
        this.target = httpRequest.uri();
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
    public void setClosePromise(ChannelPromise closePromise) {
        this.closePromise = closePromise;
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
        webSocketConnection = WebSocketUtil.getWebSocketConnection(ctx, this, blockingHandler, isSecured, target);
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

        if (webSocketConnection != null && !this.isCloseFrameReceived() && closePromise == null) {
            // Notify abnormal closure.
            DefaultWebSocketMessage webSocketCloseMessage =
                    new DefaultWebSocketCloseMessage(Constants.WEBSOCKET_STATUS_CODE_ABNORMAL_CLOSURE);
            setupCommonProperties(webSocketCloseMessage);
            connectorFuture.notifyWSListener((WebSocketCloseMessage) webSocketCloseMessage);
            return;
        }

        if (closePromise != null && !closePromise.isDone()) {
            String errMsg = "Connection is closed by remote endpoint without echoing a close frame";
            ctx.close().addListener(closeFuture -> closePromise.setFailure(new IllegalStateException(errMsg)));
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
        DefaultWebSocketMessage webSocketTextMessage = WebSocketUtil.getWebSocketMessage(frame, text, finalFragment);
        setupCommonProperties(webSocketTextMessage);
        connectorFuture.notifyWSListener((WebSocketTextMessage) webSocketTextMessage);
    }

    private void notifyBinaryMessage(WebSocketFrame frame, ByteBuf content, boolean finalFragment)
            throws ServerConnectorException {
        DefaultWebSocketMessage webSocketBinaryMessage = WebSocketUtil.getWebSocketMessage(frame, content,
                                                                                           finalFragment);
        setupCommonProperties(webSocketBinaryMessage);
        connectorFuture.notifyWSListener((WebSocketBinaryMessage) webSocketBinaryMessage);
    }

    private void notifyCloseMessage(CloseWebSocketFrame closeWebSocketFrame) throws ServerConnectorException {
        String reasonText = closeWebSocketFrame.reasonText();
        int statusCode = closeWebSocketFrame.statusCode();
        // closePromise == null means that WebSocketConnection has not yet initiated a connection closure.
        if (closePromise == null) {
            DefaultWebSocketMessage webSocketCloseMessage = new DefaultWebSocketCloseMessage(statusCode, reasonText);
            setupCommonProperties(webSocketCloseMessage);
            connectorFuture.notifyWSListener((WebSocketCloseMessage) webSocketCloseMessage);
            closeFrameReceived = true;
        } else {
            if (webSocketConnection.getCloseInitiatedStatusCode() != closeWebSocketFrame.statusCode()) {
                String errMsg = String.format(
                        "Expected status code %d but found %d in echoed close frame from remote endpoint",
                        webSocketConnection.getCloseInitiatedStatusCode(), closeWebSocketFrame.statusCode());
                closePromise.setFailure(new IllegalStateException(errMsg));
                return;
            }
            closePromise.setSuccess();
        }
        closeWebSocketFrame.release();
    }

    private void notifyPingMessage(PingWebSocketFrame pingWebSocketFrame) throws ServerConnectorException {
        WebSocketControlMessage webSocketControlMessage = WebSocketUtil.
                getWebSocketControlMessage(pingWebSocketFrame, WebSocketControlSignal.PING);
        setupCommonProperties((DefaultWebSocketMessage) webSocketControlMessage);
        connectorFuture.notifyWSListener(webSocketControlMessage);
    }

    private void notifyPongMessage(PongWebSocketFrame pongWebSocketFrame) throws ServerConnectorException {
        WebSocketControlMessage webSocketControlMessage = WebSocketUtil.
                getWebSocketControlMessage(pongWebSocketFrame, WebSocketControlSignal.PONG);
        setupCommonProperties((DefaultWebSocketMessage) webSocketControlMessage);
        connectorFuture.notifyWSListener(webSocketControlMessage);
    }

    private void notifyIdleTimeout() throws ServerConnectorException {
        DefaultWebSocketMessage webSocketControlMessage = new DefaultWebSocketControlMessage(
                WebSocketControlSignal.IDLE_TIMEOUT, null);
        setupCommonProperties(webSocketControlMessage);
        connectorFuture.notifyWSIdleTimeout((WebSocketControlMessage) webSocketControlMessage);
    }

    private void setupCommonProperties(DefaultWebSocketMessage webSocketMessage) {
        webSocketMessage.setTarget(target);
        webSocketMessage.setListenerInterface(interfaceId);
        webSocketMessage.setIsConnectionSecured(isSecured);
        webSocketMessage.setIsServerMessage(true);
        webSocketMessage.setWebSocketConnection(webSocketConnection);
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
        ChannelFuture closeFrameFuture = ctx.channel().writeAndFlush(new CloseWebSocketFrame(
                Constants.WEBSOCKET_STATUS_CODE_UNEXPECTED_CONDITION, "Encountered an unexpected condition"));
        closeFrameFuture.addListener(future -> ctx.close().addListener(
                closeFuture -> connectorFuture.notifyWSListener(cause)));
    }
}
