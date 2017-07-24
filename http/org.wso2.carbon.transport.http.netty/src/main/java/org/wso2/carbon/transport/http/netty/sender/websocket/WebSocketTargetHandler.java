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

package org.wso2.carbon.transport.http.netty.sender.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.exception.UnknownWebSocketFrameTypeException;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.internal.websocket.Util;
import org.wso2.carbon.transport.http.netty.internal.websocket.WebSocketSessionImpl;
import org.wso2.carbon.transport.http.netty.listener.WebSocketSourceHandler;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import javax.websocket.Session;

/**
 * WebSocket Client Handler. This class responsible for handling the inbound messages for the WebSocket Client.
 * <i>Note: If the user uses both WebSocket Client and the server it is recommended to check the
 * <b>{@link Constants}.IS_WEBSOCKET_SERVER</b> property to identify whether the message is coming from the client
 * or the server in the application level.</i>
 */
public class WebSocketTargetHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

    private final WebSocketClientHandshaker handshaker;
    private final boolean isSecure;
    private final String requestedUri;
    private final WebSocketSourceHandler sourceHandler;
    private final CarbonMessageProcessor carbonMessageProcessor;
    private final String clientServiceName;
    private WebSocketSessionImpl clientSession;
    private ChannelPromise handshakeFuture;
    private CarbonMessage cMsg;

    public WebSocketTargetHandler(WebSocketClientHandshaker handshaker, WebSocketSourceHandler sourceHandler,
                                  boolean isSecure, String requestedUri, String clientServiceName,
                                  CarbonMessageProcessor messageProcessor) {
        this.handshaker = handshaker;
        this.sourceHandler = sourceHandler;
        this.isSecure = isSecure;
        this.requestedUri = requestedUri;
        this.clientServiceName = clientServiceName;
        this.carbonMessageProcessor = messageProcessor;
        cMsg = null;
        handshakeFuture = null;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    public Session getClientSession() {
        return clientSession;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws URISyntaxException {
        handshaker.handshake(ctx.channel());
        clientSession = Util.getSession(ctx, isSecure, requestedUri);
        if (sourceHandler != null) {
            sourceHandler.addClientSession(clientSession);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("WebSocket Client disconnected!");
        cMsg = new StatusCarbonMessage(org.wso2.carbon.messaging.Constants.STATUS_CLOSE, 1001,
                                       "Server is going away");
        setupCarbonMessage(ctx);
        publishToMessageProcessor(cMsg, ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws UnknownWebSocketFrameTypeException, URISyntaxException {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            log.debug("WebSocket Client connected!");
            handshakeFuture.setSuccess();
            clientSession = Util.getSession(ctx, isSecure, requestedUri);
            if (sourceHandler != null) {
                sourceHandler.addClientSession(clientSession);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            cMsg = new TextCarbonMessage(textFrame.text());
        } else if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
            cMsg = new BinaryCarbonMessage(binaryFrame.content().nioBuffer(), binaryFrame.isFinalFragment());
        } else if (frame instanceof PongWebSocketFrame) {
            PongWebSocketFrame pongFrame = (PongWebSocketFrame) frame;
            cMsg = new ControlCarbonMessage(org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_HEARTBEAT,
                                            pongFrame.content().nioBuffer(), true);
        } else if (frame instanceof PingWebSocketFrame) {
            PingWebSocketFrame pingFrame = (PingWebSocketFrame) frame;
            ctx.channel().writeAndFlush(new PongWebSocketFrame(pingFrame.content()));
        } else if (frame instanceof CloseWebSocketFrame) {
            CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) frame;
            cMsg = new StatusCarbonMessage(org.wso2.carbon.messaging.Constants.STATUS_CLOSE,
                                           closeFrame.statusCode(), closeFrame.reasonText());
            ch.close();
        } else {
            throw new UnknownWebSocketFrameTypeException("Cannot identify the WebSocket frame type");
        }
        setupCarbonMessage(ctx);
        publishToMessageProcessor(cMsg, ctx);
    }


    protected void publishToMessageProcessor(CarbonMessage cMsg, ChannelHandlerContext ctx) {
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }

        if (carbonMessageProcessor != null) {
            try {
                carbonMessageProcessor.receive(cMsg, null);
            } catch (Exception e) {
                log.error("Error while submitting CarbonMessage to CarbonMessageProcessor.", e);
                ctx.channel().close();
            }
        } else {
            log.error("Cannot find registered MessageProcessor to forward the message.");
            ctx.channel().close();
        }
    }

    private void setupCarbonMessage(ChannelHandlerContext ctx) {
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }
        cMsg.setProperty(Constants.PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT,
                         ((InetSocketAddress) ctx.channel().localAddress()).getPort());
        cMsg.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        cMsg.setProperty(Constants.LOCAL_NAME, ((InetSocketAddress) ctx.channel().localAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_ADDRESS, requestedUri);
        cMsg.setProperty(Constants.REMOTE_HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.TO, clientServiceName);
        cMsg.setProperty(Constants.PROTOCOL, Constants.WEBSOCKET_PROTOCOL);
        cMsg.setProperty(Constants.IS_WEBSOCKET_SERVER, false);
        if (sourceHandler != null) {
            cMsg.setProperty(Constants.WEBSOCKET_SERVER_SESSION, sourceHandler.getServerSession());
        }
        cMsg.setProperty(Constants.WEBSOCKET_CLIENT_SESSION, clientSession);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!handshakeFuture.isDone()) {
            log.error("Handshake failed : " + cause.getMessage(), cause);
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
