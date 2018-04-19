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

package org.wso2.transport.http.netty.sender.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultWebSocketConnection;
import org.wso2.transport.http.netty.contractimpl.websocket.HandshakeFutureImpl;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;

/**
 * WebSocket client for sending and receiving messages in WebSocket as a client.
 */
public class WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

    private WebSocketTargetHandler webSocketTargetHandler;

    private final String url;
    private final String subProtocols;
    private final int idleTimeout;
    private final Map<String, String> headers;
    private final WebSocketConnectorListener connectorListener;
    private final EventLoopGroup wsClientEventLoopGroup;
    private final boolean autoRead;
    private Channel channel = null;

    /**
     * @param url url of the remote endpoint.
     * @param subProtocols the negotiable sub-protocol if server is asking for it.
     * @param idleTimeout Idle timeout of the connection.
     * @param headers any specific headers which need to send to the server.
     * @param connectorListener connector listener to notify incoming messages.
     */
    public WebSocketClient(String url, String subProtocols, int idleTimeout, EventLoopGroup wsClientEventLoopGroup,
                           Map<String, String> headers, WebSocketConnectorListener connectorListener,
                           boolean autoRead) {
        this.url = url;
        this.subProtocols = subProtocols;
        this.idleTimeout = idleTimeout;
        this.headers = headers;
        this.connectorListener = connectorListener;
        this.wsClientEventLoopGroup = wsClientEventLoopGroup;
        this.autoRead = autoRead;
    }

    /**
     * Handle the handshake with the server.
     *
     * @return handshake future for connection.
     */
    public HandshakeFuture handshake() {
        HandshakeFutureImpl handshakeFuture = new HandshakeFutureImpl();
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
            final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
            final int port = getPort(uri);

            if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                log.error("Only WS(S) is supported.");
                throw new SSLException("");
            }

            final boolean ssl = "wss".equalsIgnoreCase(scheme);
            final SslContext sslCtx = getSslContext(ssl);
            HttpHeaders httpHeaders = new DefaultHttpHeaders();

            // Adding custom headers to the handshake request.
            if (headers != null) {
                headers.forEach(httpHeaders::add);
            }

            WebSocketClientHandshaker websocketHandshaker = WebSocketClientHandshakerFactory.newHandshaker(
                    uri, WebSocketVersion.V13, subProtocols, true, httpHeaders);
            webSocketTargetHandler = new WebSocketTargetHandler(websocketHandshaker, ssl, url, connectorListener);

            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(wsClientEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            if (sslCtx != null) {
                                pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                            }
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
                            if (idleTimeout > 0) {
                                pipeline.addLast(new IdleStateHandler(idleTimeout, idleTimeout,
                                                               idleTimeout, TimeUnit.MILLISECONDS));
                            }
                            pipeline.addLast(webSocketTargetHandler);
                        }
                    });

            channel = clientBootstrap.connect(uri.getHost(), port).sync().channel();
            ChannelFuture future = webSocketTargetHandler
                    .handshakeFuture().addListener((ChannelFutureListener) clientHandshakeFuture -> {
                Throwable cause = clientHandshakeFuture.cause();
                if (clientHandshakeFuture.isSuccess() && cause == null) {
                    channel.config().setAutoRead(autoRead);
                    DefaultWebSocketConnection webSocketConnection = webSocketTargetHandler.getWebSocketConnection();
                    String actualSubProtocol = websocketHandshaker.actualSubprotocol();
                    webSocketTargetHandler.setActualSubProtocol(actualSubProtocol);
                    webSocketConnection.getDefaultWebSocketSession().setNegotiatedSubProtocol(actualSubProtocol);
                    webSocketConnection.getDefaultWebSocketSession().setIsOpen(true);
                    handshakeFuture.notifySuccess(webSocketConnection);
                } else {
                    handshakeFuture.notifyError(cause);
                }
            }).sync();
            handshakeFuture.setChannelFuture(future);
        } catch (Throwable t) {
            handshakeFuture.notifyError(t);
        }

        return handshakeFuture;
    }

    private int getPort(URI uri) {
        String scheme = uri.getScheme();
        if (uri.getPort() == -1) {
            switch (scheme) {
                case "ws":
                    return 80;
                case "wss":
                    return 443;
                default:
                    return -1;
            }
        } else {
            return uri.getPort();
        }
    }

    private SslContext getSslContext(boolean ssl) throws SSLException {
        if (ssl) {
            return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        }
        return null;
    }
}
