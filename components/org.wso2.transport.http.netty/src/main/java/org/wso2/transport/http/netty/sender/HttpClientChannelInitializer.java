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
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.HttpRoute;
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
import javax.net.ssl.SSLException;

import static io.netty.handler.logging.LogLevel.TRACE;

/**
 * A class that responsible for initialize target server pipeline.
 */
public class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(HttpClientChannelInitializer.class);

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
    private boolean http2 = false;
    private Http2ConnectionHandler http2ConnectionHandler;
    private ClientInboundHandler clientInboundHandler;
    private ClientOutboundHandler clientOutboundHandler;
    private Http2Connection connection;
    private SSLConfig sslConfig;
    private HttpRoute httpRoute;
    private SenderConfiguration senderConfiguration;
    private ConnectionAvailabilityFuture connectionAvailabilityFuture;

    public HttpClientChannelInitializer(SenderConfiguration senderConfiguration, HttpRoute httpRoute,
            ConnectionManager connectionManager, ConnectionAvailabilityFuture connectionAvailabilityFuture) {
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
        this.httpRoute = httpRoute;
        this.sslConfig = senderConfiguration.getSSLConfig();
        this.connectionAvailabilityFuture = connectionAvailabilityFuture;

        String httpVersion = senderConfiguration.getHttpVersion();
        if (Float.valueOf(httpVersion) == Constants.HTTP_2_0) {
            http2 = true;
        }
        connection = new DefaultHttp2Connection(false);
        clientInboundHandler = new ClientInboundHandler();
        Http2FrameListener frameListener = new DelegatingDecompressorFrameListener(connection, clientInboundHandler);

        Http2ConnectionHandlerBuilder connectionHandlerBuilder = new Http2ConnectionHandlerBuilder();
        if (httpTraceLogEnabled) {
            connectionHandlerBuilder.frameLogger(new Http2FrameLogger(TRACE, "tracelog.http.upstream"));
        }
        http2ConnectionHandler = connectionHandlerBuilder.connection(connection).frameListener(frameListener).build();
        clientOutboundHandler = new ClientOutboundHandler(connection, http2ConnectionHandler.encoder());
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // Add the generic handlers to the pipeline
        // e.g. SSL handler
        ChannelPipeline clientPipeline = socketChannel.pipeline();
        configureProxyServer(clientPipeline);
        HttpClientCodec sourceCodec = new HttpClientCodec();
        targetHandler = new TargetHandler();
        targetHandler.setHttp2ClientOutboundHandler(clientOutboundHandler);
        targetHandler.setKeepAlive(isKeepAlive);
        if (http2) {
            SSLConfig sslConfig = senderConfiguration.getSSLConfig();
            if (sslConfig != null) {
                configureSslForHttp2(socketChannel, clientPipeline, sslConfig);
            } else if (senderConfiguration.isForceHttp2()) {
                configureHttp2Pipeline(clientPipeline);
            } else {
                configureHttp2UpgradePipeline(clientPipeline, sourceCodec, targetHandler);
            }
        } else {
            if (sslConfig != null) {
                configureSslForHttp(clientPipeline, targetHandler, socketChannel);
            } else {
                configureHttpPipeline(clientPipeline, targetHandler);
            }
        }
    }

    private void configureProxyServer(ChannelPipeline clientPipeline) {
        if (proxyServerConfiguration != null) {
            if (proxyServerConfiguration.getProxyUsername() != null
                    && proxyServerConfiguration.getProxyPassword() != null) {
                clientPipeline.addLast(Constants.PROXY_HANDLER,
                        new HttpProxyHandler(proxyServerConfiguration.getInetSocketAddress(),
                                proxyServerConfiguration.getProxyUsername(),
                                proxyServerConfiguration.getProxyPassword()));
            } else {
                clientPipeline.addLast(Constants.PROXY_HANDLER,
                        new HttpProxyHandler(proxyServerConfiguration.getInetSocketAddress()));
            }
        }
    }

    private void configureSslForHttp(ChannelPipeline clientPipeline, TargetHandler targetHandler,
            SocketChannel socketChannel)
            throws SSLException {
        log.debug("adding ssl handler");
        connectionAvailabilityFuture.setSSLEnabled(true);
        if (senderConfiguration.isOcspStaplingEnabled()) {
            SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(sslConfig);
            ReferenceCountedOpenSslContext referenceCountedOpenSslContext = sslHandlerFactory
                    .buildClientReferenceCountedOpenSslContext();

            if (referenceCountedOpenSslContext != null) {
                SslHandler sslHandler = referenceCountedOpenSslContext.newHandler(socketChannel.alloc());
                ReferenceCountedOpenSslEngine engine = (ReferenceCountedOpenSslEngine) sslHandler.engine();
                socketChannel.pipeline().addLast(sslHandler);
                socketChannel.pipeline().addLast(new OCSPStaplingHandler(engine));
            }
        } else {
            SSLEngine sslEngine = instantiateAndConfigSSL(sslConfig);
            clientPipeline.addLast(Constants.SSL_HANDLER, new SslHandler(sslEngine));
            if (validateCertEnabled) {
                clientPipeline.addLast(Constants.HTTP_CERT_VALIDATION_HANDLER,
                        new CertificateValidationHandler(sslEngine, this.cacheDelay, this.cacheSize));
            }
        }
        clientPipeline.addLast(Constants.SSL_COMPLETION_HANDLER,
                new SslHandshakeCompletionHandlerForClient(connectionAvailabilityFuture, this, targetHandler));
    }

    private void configureSslForHttp2(SocketChannel ch, ChannelPipeline clientPipeline, SSLConfig sslConfig)
            throws SSLException {
        connectionAvailabilityFuture.setSSLEnabled(true);
        if (senderConfiguration.isOcspStaplingEnabled()) {
            ReferenceCountedOpenSslContext referenceCountedOpenSslContext =
                    (ReferenceCountedOpenSslContext) new SSLHandlerFactory(sslConfig).
                            createHttp2TLSContextForClient(senderConfiguration.isOcspStaplingEnabled());
            if (referenceCountedOpenSslContext != null) {
                SslHandler sslHandler = referenceCountedOpenSslContext.newHandler(ch.alloc());
                ReferenceCountedOpenSslEngine engine = (ReferenceCountedOpenSslEngine) sslHandler.engine();
                ch.pipeline().addLast(sslHandler);
                ch.pipeline().addLast(new OCSPStaplingHandler(engine));
            }
        } else {
            SslContext sslCtx = new SSLHandlerFactory(sslConfig).createHttp2TLSContextForClient(false);
            SslHandler sslHandler = sslCtx.newHandler(ch.alloc());
            clientPipeline.addLast(sslHandler);
            if (validateCertEnabled) {
                clientPipeline.addLast(Constants.HTTP_CERT_VALIDATION_HANDLER,
                        new CertificateValidationHandler(sslHandler.engine(), this.cacheDelay, this.cacheSize));
            }
        }
        clientPipeline.addLast(new Http2PipelineConfiguratorForClient(targetHandler, connectionAvailabilityFuture));
    }

    public TargetHandler getTargetHandler() {
        return targetHandler;
    }

    public Http2ConnectionManager getHttp2ConnectionManager() {
        return http2ConnectionManager;
    }

    /**
     * Creates the pipeline for handing http2 upgrade.
     *
     * @param pipeline      the client channel pipeline
     * @param sourceCodec   the source codec handler
     * @param targetHandler the target handler
     */
    private void configureHttp2UpgradePipeline(ChannelPipeline pipeline, HttpClientCodec sourceCodec,
                                               TargetHandler targetHandler) {
        pipeline.addLast(sourceCodec);
        addCommonHandlers(pipeline);
        Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(http2ConnectionHandler);
        HttpClientUpgradeHandler upgradeHandler = new HttpClientUpgradeHandler(sourceCodec, upgradeCodec,
                Integer.MAX_VALUE);
        pipeline.addLast(Constants.HTTP2_UPGRADE_HANDLER, upgradeHandler);
        pipeline.addLast(Constants.TARGET_HANDLER, targetHandler);
    }

    /**
     * Creates the pipeline for http2 requests which does not involve a connection upgrade.
     *
     * @param pipeline the client channel pipeline
     */
    private void configureHttp2Pipeline(ChannelPipeline pipeline) {
        pipeline.addLast(Constants.CONNECTION_HANDLER, http2ConnectionHandler);
        pipeline.addLast(Constants.OUTBOUND_HANDLER, clientOutboundHandler);
        addCommonHandlers(pipeline);
    }

    /**
     * Creates pipeline for http requests.
     *
     * @param pipeline      the client channel pipeline
     * @param targetHandler the target handler
     */
    public void configureHttpPipeline(ChannelPipeline pipeline, TargetHandler targetHandler) {
        pipeline.addLast(Constants.HTTP_CLIENT_CODEC, new HttpClientCodec());
        addCommonHandlers(pipeline);
        pipeline.addLast(Constants.TARGET_HANDLER, targetHandler);
    }

    /**
     * Add common handlers used in both http2 and http.
     *
     * @param pipeline the client channel pipeline
     */
    private void addCommonHandlers(ChannelPipeline pipeline) {
        pipeline.addLast(Constants.DECOMPRESSOR_HANDLER, new HttpContentDecompressor());
        if (httpTraceLogEnabled) {
            pipeline.addLast(Constants.HTTP_TRACE_LOG_HANDLER,
                    new HTTPTraceLoggingHandler("tracelog.http.upstream"));
        }
        if (followRedirect) {
            if (log.isDebugEnabled()) {
                log.debug("Follow Redirect is enabled, so adding the redirect handler to the pipeline.");
            }
            RedirectHandler redirectHandler = new RedirectHandler(instantiateAndConfigSSL(sslConfig),
                    httpTraceLogEnabled, maxRedirectCount, connectionManager);
            pipeline.addLast(Constants.REDIRECT_HANDLER, redirectHandler);
        }
    }

    /**
     * Set configurations to create ssl engine.
     *
     * @param sslConfig ssl related configurations
     * @return ssl engine
     */
    private SSLEngine instantiateAndConfigSSL(SSLConfig sslConfig) {
        // set the pipeline factory, which creates the pipeline for each newly created channels
        SSLEngine sslEngine = null;
        if (sslConfig != null) {
            SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(sslConfig);
            sslEngine = sslHandlerFactory.buildClientSSLEngine(httpRoute.getHost(), httpRoute.getPort());
            sslEngine.setUseClientMode(true);
            sslHandlerFactory.setSNIServerNames(sslEngine, httpRoute.getHost());
            if (senderConfiguration.hostNameVerificationEnabled()) {
                sslHandlerFactory.setHostNameVerfication(sslEngine);
            }
        }
        return sslEngine;
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
                configureHttp2Pipeline(ctx.pipeline());
                connectionAvailabilityFuture.notifySuccess(ApplicationProtocolNames.HTTP_2);
            } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                // handles pipeline for HTTP/1.x requests after SSL handshake
                configureHttpPipeline(ctx.pipeline(), targetHandler);
                connectionAvailabilityFuture.notifySuccess(Constants.HTTP_SCHEME);
            } else {
                throw new IllegalStateException("Unknown protocol: " + protocol);
            }
        }

        @Override
        protected void handshakeFailure(ChannelHandlerContext ctx, Throwable cause) {
            connectionAvailabilityFuture.notifyFailure(cause);
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (ctx != null && ctx.channel().isActive()) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
