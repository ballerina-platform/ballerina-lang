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
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.exception.UnknownWebSocketFrameTypeException;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.internal.websocket.WebSocketSessionImpl;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * This class handles all kinds of WebSocketFrames
 * after connection is upgraded from HTTP to WebSocket.
 */
public class WebSocketSourceHandler extends SourceHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSourceHandler.class);
    private final String uri;
    private CarbonMessage cMsg;
    private final String channelId;
    private final boolean isSecured;
    private final WebSocketSessionImpl session;

    /**
     * @param channelId This works as the session id of the WebSocket connection.
     * @param connectionManager connection manager for WebSocket connection.
     * @param listenerConfiguration Listener configuration for WebSocket connection.
     * @param uri Requested URI of WebSocket connection.
     * @param isSecured indication of whether the connection is secured or not.
     * @param ctx {@link ChannelHandlerContext} of WebSocket connection.
     */
    public WebSocketSourceHandler(String channelId,
                                  ConnectionManager connectionManager,
                                  ListenerConfiguration listenerConfiguration,
                                  String uri,
                                  boolean isSecured,
                                  ChannelHandlerContext ctx) throws Exception {
        super(connectionManager, listenerConfiguration);
        this.uri = uri;
        this.channelId = channelId;
        this.isSecured = isSecured;
        this.session = new WebSocketSessionImpl(ctx, isSecured, uri, channelId);
        sendOnOpenMessage(ctx, isSecured, uri);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnknownWebSocketFrameTypeException {
        cMsg = null;
        if (!(msg instanceof WebSocketFrame)) {
            logger.error("Expecting WebSocketFrame. Unknown type.");
            throw new UnknownWebSocketFrameTypeException("Expecting WebSocketFrame. Unknown type.");
        }
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) msg;
            String text = textWebSocketFrame.text();
            cMsg = new TextCarbonMessage(text);

        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) msg;
            boolean finalFragment = binaryWebSocketFrame.isFinalFragment();
            ByteBuf byteBuf = binaryWebSocketFrame.content();
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
            cMsg = new BinaryCarbonMessage(byteBuffer, finalFragment);

        } else if (msg instanceof CloseWebSocketFrame) {
            CloseWebSocketFrame closeWebSocketFrame = (CloseWebSocketFrame) msg;
            String reasonText = closeWebSocketFrame.reasonText();
            int statusCode = closeWebSocketFrame.statusCode();
            ctx.channel().close();
            session.setIsOpen(false);
            cMsg = new StatusCarbonMessage(org.wso2.carbon.messaging.Constants.STATUS_CLOSE, statusCode, reasonText);

        } else if (msg instanceof PingWebSocketFrame) {
            PingWebSocketFrame pingWebSocketFrame = (PingWebSocketFrame) msg;
            ctx.channel().writeAndFlush(new PongWebSocketFrame(pingWebSocketFrame.content()));

        } else if (msg instanceof PongWebSocketFrame) {
            //Control message for WebSocket is Pong Message
            PongWebSocketFrame pongWebSocketFrame = (PongWebSocketFrame) msg;
            boolean finalFragment = pongWebSocketFrame.isFinalFragment();
            ByteBuf byteBuf = pongWebSocketFrame.content();
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
            cMsg = new ControlCarbonMessage(org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_HEARTBEAT,
                                            byteBuffer, finalFragment);
            setupCarbonMessage(ctx);
        }
        setupCarbonMessage(ctx);
        publishToMessageProcessor(cMsg);
    }


    /*
    Carbon Message is published to registered message processor
    Message Processor should return transport thread immediately
     */
    @Override
    protected void publishToMessageProcessor(CarbonMessage cMsg) {
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }

        CarbonMessageProcessor carbonMessageProcessor = HTTPTransportContextHolder.getInstance()
                .getMessageProcessor();
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


    private void sendOnOpenMessage(ChannelHandlerContext ctx, boolean isSecured, String uri) throws URISyntaxException {
        cMsg = new StatusCarbonMessage(org.wso2.carbon.messaging.Constants.STATUS_OPEN, 0, null);
        setupCarbonMessage(ctx);
        cMsg.setProperty(Constants.CONNECTION, Constants.UPGRADE);
        cMsg.setProperty(Constants.UPGRADE, Constants.WEBSOCKET_UPGRADE);
        publishToMessageProcessor(cMsg);
    }

    /*
     Extract all the necessary properties from ChannelHandlerContext
     Add them into a CarbonMessage
     Note : This method only add details of ChannelHandlerContext to the CarbonMessage
     Adding other custom details should be done separately
     @return basic CarbonMessage with necessary details
     */
    private void setupCarbonMessage(ChannelHandlerContext ctx) {
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }
        cMsg.setProperty(Constants.PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.TO, this.uri);
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT,
                         ((InetSocketAddress) ctx.channel().localAddress()).getPort());
        cMsg.setProperty(Constants.IS_SECURED_CONNECTION, isSecured);
        cMsg.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        cMsg.setProperty(Constants.LOCAL_NAME, ((InetSocketAddress) ctx.channel().localAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_ADDRESS, ctx.channel().remoteAddress());
        cMsg.setProperty(Constants.REMOTE_HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.CHANNEL_ID, channelId);
        cMsg.setProperty(Constants.PROTOCOL, Constants.WEBSOCKET_PROTOCOL_NAME);
        cMsg.setProperty(Constants.WEBSOCKET_SESSION, session);
    }
}
