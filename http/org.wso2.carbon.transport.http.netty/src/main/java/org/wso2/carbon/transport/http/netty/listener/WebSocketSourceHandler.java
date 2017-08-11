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

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.carbon.transport.http.netty.contractimpl.HTTPServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.BasicWebSocketMessageContextImpl;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.WebSocketMessageContextImpl;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.message.WebSocketBinaryMessageImpl;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.message.WebSocketCloseMessageImpl;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.message.WebSocketControlMessageImpl;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.message.WebSocketTextMessageImpl;
import org.wso2.carbon.transport.http.netty.exception.UnknownWebSocketFrameTypeException;
import org.wso2.carbon.transport.http.netty.internal.websocket.WebSocketSessionImpl;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import javax.websocket.Session;

/**
 * This class handles all kinds of WebSocketFrames
 * after connection is upgraded from HTTP to WebSocket.
 */
public class WebSocketSourceHandler extends SourceHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSourceHandler.class);
    private final String uri;
    private final String channelId;
    private final ChannelHandlerContext ctx;
    private final boolean isSecured;
    private final WebSocketSessionImpl channelSession;
    private final WebSocketMessageContextImpl webSocketChannelContext;
    private final ServerConnectorFuture connectorFuture;

    /**
     * @param channelId This works as the channelSession id of the WebSocket connection.
     * @param connectionManager connection manager for WebSocket connection.
     * @param listenerConfiguration HTTPConnectorListener configuration for WebSocket connection.
     * @param httpRequest {@link HttpRequest} which contains the details of WebSocket Upgrade.
     * @param isSecured indication of whether the connection is secured or not.
     * @param ctx {@link ChannelHandlerContext} of WebSocket connection.
     * @param basicWebSocketMessageContext Basic message context to create messages.
     * @param connectorFuture {@link ServerConnectorFuture} to notify messages to application.
     * @param channelSession {@link Session} for the channel to write back messages.
     * @throws Exception throws if error occurred during the construction.
     */
    public WebSocketSourceHandler(String channelId, ConnectionManager connectionManager,
                                  ListenerConfiguration listenerConfiguration, HttpRequest httpRequest,
                                  boolean isSecured, ChannelHandlerContext ctx,
                                  BasicWebSocketMessageContextImpl basicWebSocketMessageContext,
                                  ServerConnectorFuture connectorFuture,
                                  WebSocketSessionImpl channelSession) throws Exception {
        super(connectionManager, listenerConfiguration, new HTTPServerConnectorFuture());
        this.uri = httpRequest.uri();
        this.channelId = channelId;
        this.ctx = ctx;
        this.isSecured = isSecured;
        this.connectorFuture = connectorFuture;
        this.channelSession = channelSession;
        this.webSocketChannelContext =
                setupCommonProperties(new WebSocketMessageContextImpl(channelSession, basicWebSocketMessageContext));
    }

    /**
     * Set the client session associated with the server session.
     *
     * @param clientSession {@link Session} of the client associated with this Server session.
     */
    public void addClientSession(Session clientSession) {
        webSocketChannelContext.addClientSession(clientSession);
    }

    /**
     * Retrieve client session associated with the this server session.
     *
     * @return the client session of the source handler.
     */
    public List<Session> getClientSessions() {
        return webSocketChannelContext.getClientSessions();
    }

    /**
     * Retrieve server session of this source handler.
     *
     * @return the server session of this source handler.
     */
    public Session getServerSession() {
        return channelSession;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (channelSession.isOpen()) {
            channelSession.setIsOpen(false);
            int statusCode = 1001; // Client is going away.
            String reasonText = "Client is going away";
            WebSocketCloseMessageImpl webSocketCloseMessage =
                    new WebSocketCloseMessageImpl(statusCode, reasonText, webSocketChannelContext);
            connectorFuture.notifyWSListener(webSocketCloseMessage);
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
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) msg;
            String text = textWebSocketFrame.text();
            boolean isFinalFragment = textWebSocketFrame.isFinalFragment();
            WebSocketTextMessageImpl webSocketTextMessage =
                    new WebSocketTextMessageImpl(text, isFinalFragment, webSocketChannelContext);
            connectorFuture.notifyWSListener(webSocketTextMessage);

        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) msg;
            ByteBuf byteBuf = binaryWebSocketFrame.content();
            boolean finalFragment = binaryWebSocketFrame.isFinalFragment();
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
            WebSocketBinaryMessageImpl webSocketBinaryMessage =
                    new WebSocketBinaryMessageImpl(byteBuffer, finalFragment, webSocketChannelContext);
            connectorFuture.notifyWSListener(webSocketBinaryMessage);

        } else if (msg instanceof CloseWebSocketFrame) {
            CloseWebSocketFrame closeWebSocketFrame = (CloseWebSocketFrame) msg;
            String reasonText = closeWebSocketFrame.reasonText();
            int statusCode = closeWebSocketFrame.statusCode();
            ctx.channel().close();
            channelSession.setIsOpen(false);
            WebSocketCloseMessageImpl webSocketCloseMessage =
                    new WebSocketCloseMessageImpl(statusCode, reasonText, webSocketChannelContext);
            connectorFuture.notifyWSListener(webSocketCloseMessage);

        } else if (msg instanceof PingWebSocketFrame) {
            PingWebSocketFrame pingWebSocketFrame = (PingWebSocketFrame) msg;
            ctx.channel().writeAndFlush(new PongWebSocketFrame(pingWebSocketFrame.content()));

        } else if (msg instanceof PongWebSocketFrame) {
            //Control message for WebSocket is Pong Message
            PongWebSocketFrame pongWebSocketFrame = (PongWebSocketFrame) msg;
            ByteBuf byteBuf = pongWebSocketFrame.content();
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
            WebSocketControlMessageImpl webSocketControlMessage =
                    new WebSocketControlMessageImpl(WebSocketControlSignal.PONG, byteBuffer, webSocketChannelContext);
            connectorFuture.notifyWSListener(webSocketControlMessage);
        }
    }

    private WebSocketMessageContextImpl setupCommonProperties(WebSocketMessageContextImpl webSocketChannelContext) {
        webSocketChannelContext.setProperty(Constants.SRC_HANDLER, this);
        webSocketChannelContext.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT,
                         ((InetSocketAddress) ctx.channel().localAddress()).getPort());
        webSocketChannelContext.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        webSocketChannelContext.setProperty(
                Constants.LOCAL_NAME, ((InetSocketAddress) ctx.channel().localAddress()).getHostName());
        webSocketChannelContext.setProperty(Constants.CHANNEL_ID, channelId);
        return webSocketChannelContext;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        connectorFuture.notifyWSListener(cause);
    }
}
