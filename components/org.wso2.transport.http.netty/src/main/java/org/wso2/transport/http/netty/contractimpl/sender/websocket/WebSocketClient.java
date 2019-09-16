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

package org.wso2.transport.http.netty.contractimpl.sender.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.Utf8FrameValidator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.contractimpl.listener.WebSocketMessageQueueHandler;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultClientHandshakeFuture;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * WebSocket client for sending and receiving messages in WebSocket as a client.
 */
public class WebSocketClient {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClient.class);

    private WebSocketClientHandshakeHandler clientHandshakeHandler;


    private final WebSocketClientConnectorConfig connectorConfig;
    private final EventLoopGroup wsClientEventLoopGroup;

    /**
     * @param wsClientEventLoopGroup of the client connector
     * @param connectorConfig        Connector configuration for WebSocket client.
     */
    public WebSocketClient(EventLoopGroup wsClientEventLoopGroup, WebSocketClientConnectorConfig connectorConfig) {
        this.wsClientEventLoopGroup = wsClientEventLoopGroup;
        this.connectorConfig = connectorConfig;
    }

    /**
     * Handle the handshake with the server.
     *
     * @return handshake future for connection.
     */
    public ClientHandshakeFuture handshake() {
        final DefaultClientHandshakeFuture handshakeFuture = new DefaultClientHandshakeFuture();
        try {
            URI uri = new URI(connectorConfig.getRemoteAddress());

            String scheme = uri.getScheme();
            if (!Constants.WS_SCHEME.equalsIgnoreCase(scheme) && !Constants.WSS_SCHEME.equalsIgnoreCase(scheme)) {
                LOG.error("Only WS(S) is supported.");
                throw new URISyntaxException(connectorConfig.getRemoteAddress(),
                                             "WebSocket client supports only WS(S) scheme");
            }

            final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
            final int port = getPort(uri);
            final boolean ssl = Constants.WSS_SCHEME.equalsIgnoreCase(scheme);
            WebSocketClientHandshaker webSocketHandshaker = WebSocketClientHandshakerFactory.newHandshaker(
                    uri, WebSocketVersion.V13, connectorConfig.getSubProtocolsStr(), true, connectorConfig.getHeaders(),
                    connectorConfig.getMaxFrameSize());
            WebSocketMessageQueueHandler webSocketMessageQueueHandler = new WebSocketMessageQueueHandler();
            clientHandshakeHandler = new WebSocketClientHandshakeHandler(
                    webSocketHandshaker, handshakeFuture, webSocketMessageQueueHandler, ssl,
                    connectorConfig.isAutoRead(), connectorConfig.getRemoteAddress(), handshakeFuture);
            Bootstrap clientBootstrap = initClientBootstrap(host, port, handshakeFuture);
            clientBootstrap.connect(uri.getHost(), port).addListener(future -> {
                Throwable cause = future.cause();
                if (!future.isSuccess() && cause != null) {
                    handshakeFuture.notifyError(cause, null);
                }
            });
        } catch (Exception throwable) {
            handleHandshakeError(handshakeFuture, throwable);
        }
        return handshakeFuture;
    }

    private void handleHandshakeError(DefaultClientHandshakeFuture handshakeFuture, Throwable throwable) {
        if (clientHandshakeHandler != null) {
            handshakeFuture.notifyError(throwable, clientHandshakeHandler.getHttpCarbonResponse());
        } else {
            handshakeFuture.notifyError(throwable, null);
        }
    }

    private Bootstrap initClientBootstrap(String host, int port, DefaultClientHandshakeFuture handshakeFuture) {
        Bootstrap clientBootstrap = new Bootstrap();
        SSLConfig sslConfig = connectorConfig.getClientSSLConfig();
        clientBootstrap.group(wsClientEventLoopGroup).channel(NioSocketChannel.class).handler(
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws SSLException {
                        if (sslConfig != null) {
                            SSLEngine sslEngine = Util
                                    .configureHttpPipelineForSSL(socketChannel, host, port, sslConfig);
                            socketChannel.pipeline().addLast(Constants.SSL_COMPLETION_HANDLER,
                                    new WebSocketClientSSLHandshakeCompletionHandler(handshakeFuture, sslEngine));
                        } else {
                            configureHandshakePipeline(socketChannel.pipeline());
                        }
                    }
                });
        return clientBootstrap;
    }

    private void configureHandshakePipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpClientCodec());
        // Assuming that WebSocket Handshake messages will not be large than 8KB
        pipeline.addLast(new HttpObjectAggregator(8192));
        if (connectorConfig.isWebSocketCompressionEnabled()) {
            pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
        }
        pipeline.addLast(Utf8FrameValidator.class.getName(), new Utf8FrameValidator());
        if (connectorConfig.getIdleTimeoutInMillis() > 0) {
            pipeline.addLast(
                    new IdleStateHandler(0, 0, connectorConfig.getIdleTimeoutInMillis(), TimeUnit.MILLISECONDS));
        }
        pipeline.addLast(Constants.WEBSOCKET_CLIENT_HANDSHAKE_HANDLER, clientHandshakeHandler);
    }

    private int getPort(URI uri) {
        String scheme = uri.getScheme();
        int port = uri.getPort();
        if (port == -1) {
            switch (scheme) {
            case Constants.WS_SCHEME:
                return 80;
            case Constants.WSS_SCHEME:
                return 443;
            default:
                return -1;
            }
        } else {
            return port;
        }
    }

    /**
     * handler to identify the SSL handshake completion.
     */
    private class WebSocketClientSSLHandshakeCompletionHandler extends ChannelInboundHandlerAdapter {
        private final DefaultClientHandshakeFuture clientHandshakeFuture;
        private SSLEngine sslEngine;

        private WebSocketClientSSLHandshakeCompletionHandler(DefaultClientHandshakeFuture clientHandshakeFuture,
                SSLEngine sslEngine) {
            this.clientHandshakeFuture = clientHandshakeFuture;
            this.sslEngine = sslEngine;
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
                throws CertificateNotYetValidException, CertificateExpiredException, SSLPeerUnverifiedException {
            if (evt instanceof SslHandshakeCompletionEvent) {
                SslHandshakeCompletionEvent event = (SslHandshakeCompletionEvent) evt;
                if (event.isSuccess() && event.cause() == null) {
                    try {
                        X509Certificate endUserCert = (X509Certificate) sslEngine.getSession().getPeerCertificates()[0];
                        endUserCert.checkValidity(new Date());
                    } catch (CertificateExpiredException e) {
                        clientHandshakeFuture
                                .notifyError(new SSLException("Certificate expired : " + e.getMessage()), null);
                    }
                    configureHandshakePipeline(ctx.channel().pipeline());
                    ctx.pipeline().remove(this);
                    ctx.fireChannelActive();
                } else {
                    clientHandshakeFuture.notifyError(event.cause(), null);
                }
            }
        }
    }
}
