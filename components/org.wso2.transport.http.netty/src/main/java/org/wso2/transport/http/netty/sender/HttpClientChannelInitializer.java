/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.transport.http.netty.sender;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.ProxyServerConfiguration;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.common.ssl.SSLHandlerFactory;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.listener.HTTPTraceLoggingHandler;
import org.wso2.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.sender.http2.ClientInboundHandler;
import org.wso2.transport.http.netty.sender.http2.ClientOutboundHandler;
import org.wso2.transport.http.netty.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.sender.http2.Http2ConnectionManager;

import javax.net.ssl.SSLEngine;

import static io.netty.handler.logging.LogLevel.DEBUG;

/**
 * A class that responsible for initialize target server pipeline.
 */
public class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(HttpClientChannelInitializer.class);

    private SSLEngine sslEngine;
    private TargetHandler targetHandler;
    private boolean httpTraceLogEnabled;
    private boolean followRedirect;
    private boolean validateCertEnabled;
    private int maxRedirectCount;
    private int cacheSize;
    private int cacheDelay;
    private boolean isKeepAlive;
    private ProxyServerConfiguration proxyServerConfiguration;
    private ConnectionManager connectionManager;
    private Http2ConnectionManager http2ConnectionManager;
    private boolean isHttp2 = false;
    private Http2ConnectionHandler http2ConnectionHandler;
    private ClientInboundHandler clientInboundHandler;
    private ClientOutboundHandler clientOutboundHandler;
    private Http2Connection connection;
    private SenderConfiguration senderConfiguration;
    private SslContext sslCtx;
    private SSLConfig sslConfig;
    private ConnectionAvailabilityFuture connectionAvailabilityFuture;

    private static final Http2FrameLogger logger =
            new Http2FrameLogger(DEBUG,     // Change mode to INFO for logging frames
                                 HttpClientChannelInitializer.class);

    public HttpClientChannelInitializer(SenderConfiguration senderConfiguration, SSLEngine sslEngine,
            ConnectionManager connectionManager, ConnectionAvailabilityFuture connectionAvailabilityFuture) {
        this.sslEngine = sslEngine;
        this.httpTraceLogEnabled = senderConfiguration.isHttpTraceLogEnabled();
        this.followRedirect = senderConfiguration.isFollowRedirect();
        this.maxRedirectCount = senderConfiguration.getMaxRedirectCount(Constants.MAX_REDIRECT_COUNT);
        this.isKeepAlive = senderConfiguration.isKeepAlive();
        this.proxyServerConfiguration = senderConfiguration.getProxyServerConfiguration();
        this.connectionManager = connectionManager;
        this.http2ConnectionManager = connectionManager.getHttp2ConnectionManager();
        this.validateCertEnabled = senderConfiguration.validateCertEnabled();
        this.cacheDelay = senderConfiguration.getCacheValidityPeriod();
        this.cacheSize = senderConfiguration.getCacheSize();
        this.senderConfiguration = senderConfiguration;
        this.sslConfig = senderConfiguration.getSSLConfig();
        this.connectionAvailabilityFuture = connectionAvailabilityFuture;

        String httpVersion = senderConfiguration.getHttpVersion();
        if (Float.valueOf(httpVersion) == Constants.HTTP_2_0) {
            isHttp2 = true;
        }
        connection = new DefaultHttp2Connection(false);
        clientInboundHandler = new ClientInboundHandler();
        Http2FrameListener frameListener = new DelegatingDecompressorFrameListener(connection, clientInboundHandler);
        http2ConnectionHandler  =
                new Http2ConnectionHandlerBuilder().
                        connection(connection).frameLogger(logger).frameListener(frameListener).build();
        clientOutboundHandler = new ClientOutboundHandler(connection, http2ConnectionHandler.encoder());
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // Add the generic handlers to the pipeline
        // e.g. SSL handler
        ChannelPipeline clientPipeline = ch.pipeline();
        if (proxyServerConfiguration != null) {
            if (proxyServerConfiguration.getProxyUsername() != null
                    && proxyServerConfiguration.getProxyPassword() != null) {
                clientPipeline.addLast("proxyServer",
                        new HttpProxyHandler(proxyServerConfiguration.getInetSocketAddress(),
                                proxyServerConfiguration.getProxyUsername(),
                                proxyServerConfiguration.getProxyPassword()));
            } else {
                clientPipeline
                        .addLast("proxyServer", new HttpProxyHandler(proxyServerConfiguration.getInetSocketAddress()));
            }
        }
        HttpClientCodec sourceCodec = new HttpClientCodec();
        targetHandler = new TargetHandler();
        targetHandler.setHttp2ClientOutboundHandler(clientOutboundHandler);
        targetHandler.setKeepAlive(isKeepAlive);
        if (isHttp2) {
            SSLConfig sslConfig = senderConfiguration.getSSLConfig();
            if (sslConfig != null) {
                connectionAvailabilityFuture.setSSLEnabled(true);
                sslCtx = new SSLHandlerFactory(sslConfig).createHttp2TLSContextForClient();
                clientPipeline.addLast(sslCtx.newHandler(ch.alloc()));
                if (validateCertEnabled && sslEngine != null) {
                    clientPipeline.addLast("certificateValidation",
                            new CertificateValidationHandler(this.sslEngine, this.cacheDelay, this.cacheSize));
                }
                clientPipeline.addLast(new Http2PipelineConfiguratorForClient(targetHandler,
                        connectionAvailabilityFuture));
            } else {
                configureH2cPipeline(clientPipeline, sourceCodec, targetHandler);
            }
        } else {
            if (sslEngine != null) {
                log.debug("adding ssl handler");
                connectionAvailabilityFuture.setSSLEnabled(true);
                clientPipeline.addLast("ssl", new SslHandler(this.sslEngine));
                clientPipeline.addLast("tlsHandshakeCompletionHandler",
                        new TLSHandshakeCompletionHandler(connectionAvailabilityFuture));
            }
            configureHttpPipeline(clientPipeline, targetHandler);
        }
    }

    public TargetHandler getTargetHandler() {
        return targetHandler;
    }

    public Http2ConnectionManager getHttp2ConnectionManager() {
        return http2ConnectionManager;
    }

    private void configureH2cPipeline(ChannelPipeline pipeline, HttpClientCodec sourceCodec,
            TargetHandler targetHandler) {
        pipeline.addLast(sourceCodec);
        addCommonHandlers(pipeline);
        Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(http2ConnectionHandler);
        HttpClientUpgradeHandler upgradeHandler = new HttpClientUpgradeHandler(sourceCodec, upgradeCodec,
                Integer.MAX_VALUE);
        pipeline.addLast(Constants.HTTP2_UPGRADE_HANDLER, upgradeHandler);
        pipeline.addLast(Constants.TARGET_HANDLER, targetHandler);
    }

    private void configureH2Pipeline(ChannelPipeline pipeline) {
        pipeline.addLast("connectioHandler", http2ConnectionHandler);
        pipeline.addLast("outboundHandler", clientOutboundHandler);
        addCommonHandlers(pipeline);
    }

    private void configureHttpPipeline(ChannelPipeline pipeline, TargetHandler targetHandler) {
        if (validateCertEnabled && sslEngine != null) {
            pipeline.addLast("certificateValidation",
                    new CertificateValidationHandler(this.sslEngine, this.cacheDelay, this.cacheSize));
        }
        pipeline.addLast("decoder", new HttpResponseDecoder());
        pipeline.addLast("encoder", new HttpRequestEncoder());
        addCommonHandlers(pipeline);
        pipeline.addLast(Constants.TARGET_HANDLER, targetHandler);
    }

    private void addCommonHandlers(ChannelPipeline pipeline) {
        pipeline.addLast("deCompressor", new HttpContentDecompressor());
        if (httpTraceLogEnabled) {
            pipeline.addLast(Constants.HTTP_TRACE_LOG_HANDLER,
                    new HTTPTraceLoggingHandler("tracelog.http.upstream"));
        }
        if (followRedirect) {
            if (log.isDebugEnabled()) {
                log.debug("Follow Redirect is enabled, so adding the redirect handler to the pipeline.");
            }
            RedirectHandler redirectHandler = new RedirectHandler(sslEngine, httpTraceLogEnabled, maxRedirectCount
                    , connectionManager);
            pipeline.addLast(Constants.REDIRECT_HANDLER, redirectHandler);
        }
    }

    /**
     * Gets the associated {@link Http2Connection}.
     *
     * @return the associated {@code Http2Connection}
     */
    public Http2Connection getConnection() {
        return connection;
    }

    public boolean isKeepAlive() {
        return isKeepAlive;
    }

    public void setHttp2ClientChannel(Http2ClientChannel http2ClientChannel) {
        clientOutboundHandler.setHttp2ClientChannel(http2ClientChannel);
        clientInboundHandler.setHttp2ClientChannel(http2ClientChannel);
    }

    /**
     * A handler to create the pipeline based on the ALPN negotiated protocol.
     */
    class Http2PipelineConfiguratorForClient extends ApplicationProtocolNegotiationHandler {

        private TargetHandler targetHandler;
        private ConnectionAvailabilityFuture connectionAvailabilityFuture;

        public Http2PipelineConfiguratorForClient(TargetHandler targetHandler,
                ConnectionAvailabilityFuture connectionAvailabilityFuture) {
            super(ApplicationProtocolNames.HTTP_1_1);
            this.targetHandler = targetHandler;
            this.connectionAvailabilityFuture = connectionAvailabilityFuture;
        }

        /**
         *  Configure pipeline after TLS handshake.
         */
        @Override
        protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
            if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                configureH2Pipeline(ctx.pipeline());
                connectionAvailabilityFuture.notifyListener(ApplicationProtocolNames.HTTP_2);
            } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                // handles pipeline for HTTP/1 requests after SSL handshake
                configureHttpPipeline(ctx.pipeline(), targetHandler);
                connectionAvailabilityFuture.notifyListener(Constants.HTTP_SCHEME);
            } else {
                throw new IllegalStateException("Unknown protocol: " + protocol);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (ctx != null && ctx.channel().isActive()) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
