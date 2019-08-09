/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.transport.http.netty.contractimpl.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorException;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketFrameType;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.contractimpl.listener.WebSocketMessageQueueHandler;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketCloseMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketControlMessage;

/**
 * Abstract WebSocket frame handler for WebSocket server and client.
 */
public class WebSocketInboundFrameHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketInboundFrameHandler.class);

    private final boolean isServer;
    private final boolean secureConnection;
    private final String target;
    private final String negotiatedSubProtocol;
    private final WebSocketConnectorFuture connectorFuture;
    private final WebSocketMessageQueueHandler webSocketMessageQueueHandler;
    private boolean caughtException;
    private boolean closeFrameReceived;
    private boolean closeInitialized;
    private DefaultWebSocketConnection webSocketConnection;
    private ChannelPromise closePromise;
    private WebSocketFrameType continuationFrameType;

    public WebSocketInboundFrameHandler(boolean isServer, boolean secureConnection, String target,
                                        String negotiatedSubProtocol, WebSocketConnectorFuture connectorFuture,
                                        WebSocketMessageQueueHandler webSocketMessageQueueHandler) {
        this.isServer = isServer;
        this.secureConnection = secureConnection;
        this.target = target;
        this.negotiatedSubProtocol = negotiatedSubProtocol;
        this.connectorFuture = connectorFuture;
        this.webSocketMessageQueueHandler = webSocketMessageQueueHandler;
        this.closeInitialized = false;
    }

    /**
     * Set channel promise for WebSocket connection close.
     *
     * @param closePromise {@link ChannelPromise} to indicate the receiving of close frame echo
     *                     back from the remote endpoint.
     */
    public void setClosePromise(ChannelPromise closePromise) {
        this.closePromise = closePromise;
    }

    /**
     * Retrieve the WebSocket connection associated with the frame handler.
     *
     * @return the WebSocket connection associated with the frame handler.
     */
    public DefaultWebSocketConnection getWebSocketConnection() {
        return this.webSocketConnection;
    }

    /**
     * Check whether a close frame is received without the relevant connection to this Frame handler sending a close
     * frame.
     *
     * @return true if a close frame is received without the relevant connection to this Frame handler sending a close
     * frame.
     */
    public boolean isCloseFrameReceived() {
        return closeFrameReceived;
    }

    public void setCloseInitialized(boolean closeInitialized) {
        this.closeInitialized = closeInitialized;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        webSocketConnection = new DefaultWebSocketConnection(ctx, this, webSocketMessageQueueHandler, secureConnection,
                negotiatedSubProtocol);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws WebSocketConnectorException {
        if (evt instanceof IdleStateEvent) {
            notifyIdleTimeout();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws WebSocketConnectorException {
        if (!caughtException && webSocketConnection != null && !closeFrameReceived && closePromise == null &&
                !closeInitialized) {
            // Notify abnormal closure.
            DefaultWebSocketMessage webSocketCloseMessage =
                    new DefaultWebSocketCloseMessage(Constants.WEBSOCKET_STATUS_CODE_ABNORMAL_CLOSURE);
            setupCommonProperties(webSocketCloseMessage);
            connectorFuture.notifyWebSocketListener((WebSocketCloseMessage) webSocketCloseMessage);
            return;
        }

        if (closePromise != null && !closeFrameReceived) {
            String errMsg = "Connection is closed by remote endpoint without echoing a close frame";
            ctx.close().addListener(closeFuture -> closePromise.setFailure(new IllegalStateException(errMsg)));
        }
        connectorFuture.notifyWebSocketListener(webSocketConnection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
            closeFrameReceived = true;
            notifyCloseMessage((CloseWebSocketFrame) msg);
        } else if (msg instanceof PingWebSocketFrame) {
            notifyPingMessage((PingWebSocketFrame) msg);
        } else if (msg instanceof PongWebSocketFrame) {
            notifyPongMessage((PongWebSocketFrame) msg);
        } else if (msg instanceof ContinuationWebSocketFrame) {
            ContinuationWebSocketFrame frame = (ContinuationWebSocketFrame) msg;
            if (continuationFrameType == WebSocketFrameType.TEXT) {
                notifyTextMessage(frame, frame.text(), frame.isFinalFragment());
            } else if (continuationFrameType == WebSocketFrameType.BINARY) {
                notifyBinaryMessage(frame, frame.content(), frame.isFinalFragment());
            } else {
                ReferenceCountUtil.release(frame);
            }
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws WebSocketConnectorException {
        caughtException = true;
        if (!(cause instanceof CorruptedFrameException)) {
            ChannelFuture closeFrameFuture = ctx.channel().writeAndFlush(new CloseWebSocketFrame(
                    Constants.WEBSOCKET_STATUS_CODE_UNEXPECTED_CONDITION, "Encountered an unexpected condition"));
            closeFrameFuture.addListener(future -> ctx.close().addListener(
                    closeFuture -> connectorFuture.notifyWebSocketListener(webSocketConnection, cause)));
            return;
        }
        connectorFuture.notifyWebSocketListener(webSocketConnection, cause);
    }

    private void notifyTextMessage(WebSocketFrame frame, String text, boolean finalFragment)
            throws WebSocketConnectorException {
        DefaultWebSocketMessage webSocketTextMessage = WebSocketUtil.getWebSocketMessage(frame, text, finalFragment);
        setupCommonProperties(webSocketTextMessage);
        connectorFuture.notifyWebSocketListener((WebSocketTextMessage) webSocketTextMessage);
    }

    private void notifyBinaryMessage(WebSocketFrame frame, ByteBuf content, boolean finalFragment)
            throws WebSocketConnectorException {
        DefaultWebSocketMessage webSocketBinaryMessage = WebSocketUtil.getWebSocketMessage(frame, content,
                finalFragment);
        setupCommonProperties(webSocketBinaryMessage);
        connectorFuture.notifyWebSocketListener((WebSocketBinaryMessage) webSocketBinaryMessage);
    }

    private void notifyCloseMessage(CloseWebSocketFrame closeWebSocketFrame) throws WebSocketConnectorException {
        String reasonText = closeWebSocketFrame.reasonText();
        int statusCode = closeWebSocketFrame.statusCode();
        if (statusCode == -1) {
            statusCode = 1005;
        }
        // closePromise == null means that WebSocketConnection has not yet initiated a connection closure.
        if (closePromise == null) {
            DefaultWebSocketMessage webSocketCloseMessage = new DefaultWebSocketCloseMessage(statusCode, reasonText);
            setupCommonProperties(webSocketCloseMessage);
            connectorFuture.notifyWebSocketListener((WebSocketCloseMessage) webSocketCloseMessage);
        } else {
            if (webSocketConnection.getCloseInitiatedStatusCode() != statusCode) {
                String errMsg = String.format(
                        "Expected status code %d but found %d in echoed close frame from remote endpoint",
                        webSocketConnection.getCloseInitiatedStatusCode(), statusCode);
                closePromise.setFailure(new IllegalStateException(errMsg));
                return;
            }
            closePromise.setSuccess();
        }
        closeWebSocketFrame.release();
    }

    private void notifyPingMessage(PingWebSocketFrame pingWebSocketFrame) throws WebSocketConnectorException {
        WebSocketControlMessage webSocketControlMessage = WebSocketUtil.
                getWebSocketControlMessage(pingWebSocketFrame, WebSocketControlSignal.PING);
        setupCommonProperties((DefaultWebSocketMessage) webSocketControlMessage);
        connectorFuture.notifyWebSocketListener(webSocketControlMessage);
    }

    private void notifyPongMessage(PongWebSocketFrame pongWebSocketFrame) throws WebSocketConnectorException {
        WebSocketControlMessage webSocketControlMessage = WebSocketUtil.
                getWebSocketControlMessage(pongWebSocketFrame, WebSocketControlSignal.PONG);
        setupCommonProperties((DefaultWebSocketMessage) webSocketControlMessage);
        connectorFuture.notifyWebSocketListener(webSocketControlMessage);
    }

    private void notifyIdleTimeout() throws WebSocketConnectorException {
        DefaultWebSocketMessage webSocketControlMessage = new DefaultWebSocketControlMessage(
                WebSocketControlSignal.IDLE_TIMEOUT, null);
        setupCommonProperties(webSocketControlMessage);
        connectorFuture.notifyWebSocketIdleTimeout((WebSocketControlMessage) webSocketControlMessage);
    }

    private void setupCommonProperties(DefaultWebSocketMessage webSocketMessage) {
        webSocketMessage.setTarget(target);
        webSocketMessage.setWebSocketConnection(webSocketConnection);
        webSocketMessage.setIsServerMessage(isServer);
    }
}
