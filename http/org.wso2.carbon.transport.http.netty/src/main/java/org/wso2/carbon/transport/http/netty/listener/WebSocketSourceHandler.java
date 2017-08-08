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
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.contractImpl.HTTPServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.exception.UnknownWebSocketFrameTypeException;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.internal.websocket.BasicWebSocketChannelContextImpl;
import org.wso2.carbon.transport.http.netty.internal.websocket.WebSocketChannelContextImpl;
import org.wso2.carbon.transport.http.netty.internal.websocket.WebSocketSessionImpl;
import org.wso2.carbon.transport.http.netty.internal.websocket.message.WebSocketBinaryMessageImpl;
import org.wso2.carbon.transport.http.netty.internal.websocket.message.WebSocketCloseMessageImpl;
import org.wso2.carbon.transport.http.netty.internal.websocket.message.WebSocketControlMessageImpl;
import org.wso2.carbon.transport.http.netty.internal.websocket.message.WebSocketTextMessageImpl;
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
    private final WebSocketSessionImpl serverSession;
    private final WebSocketChannelContextImpl webSocketChannelContext;

    /**
     * @param channelId This works as the serverSession id of the WebSocket connection.
     * @param connectionManager connection manager for WebSocket connection.
     * @param listenerConfiguration HTTPConnectorListener configuration for WebSocket connection.
     * @param httpRequest {@link HttpRequest} which contains the details of WebSocket Upgrade.
     * @param isSecured indication of whether the connection is secured or not.
     * @param ctx {@link ChannelHandlerContext} of WebSocket connection.
     */
    public WebSocketSourceHandler(String channelId, ConnectionManager connectionManager,
                                  ListenerConfiguration listenerConfiguration, HttpRequest httpRequest,
                                  boolean isSecured, ChannelHandlerContext ctx,
                                  BasicWebSocketChannelContextImpl basicWebSocketChannelContext,
                                  WebSocketSessionImpl serverSession) throws Exception {
        super(connectionManager, listenerConfiguration, new HTTPServerConnectorFuture());
        this.uri = httpRequest.uri();
        this.channelId = channelId;
        this.ctx = ctx;
        this.isSecured = isSecured;
        this.serverSession = serverSession;
        this.webSocketChannelContext =
                setupCommonProperties(new WebSocketChannelContextImpl(serverSession, basicWebSocketChannelContext));
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
        return  serverSession;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (serverSession.isOpen()) {
            serverSession.setIsOpen(false);
            int statusCode = 1001; // Client is going away.
            String reasonText = "Client is going away";
            WebSocketCloseMessageImpl webSocketCloseMessage =
                    new WebSocketCloseMessageImpl(statusCode, reasonText, webSocketChannelContext);
            // TODO: Notify the observer.
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnknownWebSocketFrameTypeException {
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
            // TODO: Notify the observer.

        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) msg;
            ByteBuf byteBuf = binaryWebSocketFrame.content();
            boolean finalFragment = binaryWebSocketFrame.isFinalFragment();
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
            WebSocketBinaryMessageImpl webSocketBinaryMessage =
                    new WebSocketBinaryMessageImpl(byteBuffer, finalFragment, webSocketChannelContext);
            // TODO: Notify the observer.

        } else if (msg instanceof CloseWebSocketFrame) {
            CloseWebSocketFrame closeWebSocketFrame = (CloseWebSocketFrame) msg;
            String reasonText = closeWebSocketFrame.reasonText();
            int statusCode = closeWebSocketFrame.statusCode();
            ctx.channel().close();
            serverSession.setIsOpen(false);
            WebSocketCloseMessageImpl webSocketCloseMessage =
                    new WebSocketCloseMessageImpl(statusCode, reasonText, webSocketChannelContext);
            // TODO: Notify the observer.

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
        }
    }


    /*
    Carbon Message is published to registered message processor
    Message Processor should return transport thread immediately
     */
    protected void publishToMessageProcessor(CarbonMessage cMsg) {
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }

        CarbonMessageProcessor carbonMessageProcessor = HTTPTransportContextHolder.getInstance()
                .getMessageProcessor(listenerConfiguration.getMessageProcessorId());
        if (carbonMessageProcessor != null) {
            try {
                carbonMessageProcessor.receive(cMsg, null);
            } catch (Exception e) {
                logger.error("Error while submitting CarbonMessage to CarbonMessageProcessor.", e);
                ctx.channel().close();
            }
        } else {
            logger.error("Cannot find registered MessageProcessor to forward the message.");
            ctx.channel().close();
        }
    }

    private WebSocketChannelContextImpl setupCommonProperties(WebSocketChannelContextImpl webSocketChannelContext) {
        webSocketChannelContext.setProperty(Constants.SRC_HANDLER, this);
        webSocketChannelContext.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT,
                         ((InetSocketAddress) ctx.channel().localAddress()).getPort());
        webSocketChannelContext.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        webSocketChannelContext.setProperty(
                Constants.LOCAL_NAME, ((InetSocketAddress) ctx.channel().localAddress()).getHostName());
        webSocketChannelContext.setProperty(Constants.CHANNEL_ID, channelId);
        return webSocketChannelContext;
    }
}
