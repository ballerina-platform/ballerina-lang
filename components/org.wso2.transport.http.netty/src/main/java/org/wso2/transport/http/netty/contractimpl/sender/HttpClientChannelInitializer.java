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

package org.wso2.transport.http.netty.contractimpl.sender;

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
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.config.ProxyServerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.common.FrameLogger;
import org.wso2.transport.http.netty.contractimpl.common.HttpRoute;
import org.wso2.transport.http.netty.contractimpl.common.OutboundThrottlingHandler;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLHandlerFactory;
import org.wso2.transport.http.netty.contractimpl.listener.HttpTraceLoggingHandler;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.http2.ClientFrameListener;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import static io.netty.handler.logging.LogLevel.TRACE;
import static org.wso2.transport.http.netty.contractimpl.common.Util.setHostNameVerfication;

/**
 * A class that responsible for initialize target server pipeline.
 */
public class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private TargetHandler targetHandler;
    private boolean httpTraceLogEnabled;
    private KeepAliveConfig keepAliveConfig;
    private ProxyServerConfiguration proxyServerConfiguration;
    private Http2ConnectionManager http2ConnectionManager;
    private boolean http2 = false;
    private Http2ConnectionHandler http2ConnectionHandler;
    private ClientFrameListener clientFrameListener;
    private Http2TargetHandler http2TargetHandler;
    private Http2Connection connection;
    private SSLConfig sslConfig;
    private HttpRoute httpRoute;
    private SenderConfiguration senderConfiguration;
    private ConnectionAvailabilityFuture connectionAvailabilityFuture;
    private SSLHandlerFactory sslHandlerFactory;

    public HttpClientChannelInitializer(SenderConfiguration senderConfiguration, HttpRoute httpRoute,
            ConnectionManager connectionManager, ConnectionAvailabilityFuture connectionAvailabilityFuture) {
        this.httpTraceLogEnabled = senderConfiguration.isHttpTraceLogEnabled();
        this.keepAliveConfig = senderConfiguration.getKeepAliveConfig();
        this.proxyServerConfiguration = senderConfiguration.getProxyServerConfiguration();
        this.http2ConnectionManager = connectionManager.getHttp2ConnectionManager();
        this.senderConfiguration = senderConfiguration;
        this.httpRoute = httpRoute;
        this.sslConfig = senderConfiguration.getClientSSLConfig();
        this.connectionAvailabilityFuture = connectionAvailabilityFuture;

        String httpVersion = senderConfiguration.getHttpVersion();
        if (Float.valueOf(httpVersion) == Constants.HTTP_2_0) {
            http2 = true;
        }
        connection = new DefaultHttp2Connection(false);
        clientFrameListener = new ClientFrameListener();
        Http2FrameListener frameListener = new DelegatingDecompressorFrameListener(connection, clientFrameListener);

        Http2ConnectionHandlerBuilder connectionHandlerBuilder = new Http2ConnectionHandlerBuilder();
        if (httpTraceLogEnabled) {
            connectionHandlerBuilder.frameLogger(new FrameLogger(TRACE, Constants.TRACE_LOG_UPSTREAM));
        }
        http2ConnectionHandler = connectionHandlerBuilder.connection(connection).frameListener(frameListener).build();
        http2TargetHandler = new Http2TargetHandler(connection, http2ConnectionHandler.encoder());
        if (sslConfig != null) {
            sslHandlerFactory = new SSLHandlerFactory(sslConfig);
        }
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // Add the generic handlers to the pipeline
        // e.g. SSL handler
        ChannelPipeline clientPipeline = socketChannel.pipeline();
        configureProxyServer(clientPipeline);
        HttpClientCodec sourceCodec = new HttpClientCodec();
        targetHandler = new TargetHandler();
        targetHandler.setHttp2TargetHandler(http2TargetHandler);
        targetHandler.setKeepAliveConfig(getKeepAliveConfig());
        if (http2) {
            if (sslConfig != null) {
                configureSslForHttp2(socketChannel, clientPipeline, sslConfig);
            } else if (senderConfiguration.isForceHttp2()) {
                configureHttp2Pipeline(clientPipeline);
            } else {
                configureHttp2UpgradePipeline(clientPipeline, sourceCodec, targetHandler);
            }
        } else {
            if (sslConfig != null) {
                connectionAvailabilityFuture.setSSLEnabled(true);
                Util.configureHttpPipelineForSSL(socketChannel, httpRoute.getHost(), httpRoute.getPort(), sslConfig);
                clientPipeline.addLast(Constants.SSL_COMPLETION_HANDLER,
                        new SslHandshakeCompletionHandlerForClient(connectionAvailabilityFuture, this, targetHandler));
            } else {
                configureHttpPipeline(clientPipeline, targetHandler);
            }
        }
    }

    // Use netty proxy handler only if scheme is https
    private void configureProxyServer(ChannelPipeline clientPipeline) {
        if (proxyServerConfiguration != null && sslConfig != null) {
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

    private void configureSslForHttp2(SocketChannel ch, ChannelPipeline clientPipeline, SSLConfig sslConfig)
            throws SSLException {
        connectionAvailabilityFuture.setSSLEnabled(true);
        if (sslConfig.isOcspStaplingEnabled()) {
            ReferenceCountedOpenSslContext referenceCountedOpenSslContext =
                    (ReferenceCountedOpenSslContext) sslHandlerFactory.
                            createHttp2TLSContextForClient(sslConfig.isOcspStaplingEnabled());
            if (referenceCountedOpenSslContext != null) {
                SslHandler sslHandler = referenceCountedOpenSslContext.newHandler(ch.alloc());
                ReferenceCountedOpenSslEngine engine = (ReferenceCountedOpenSslEngine) sslHandler.engine();
                ch.pipeline().addLast(sslHandler);
                ch.pipeline().addLast(new OCSPStaplingHandler(engine));
            }
        } else {
            sslHandlerFactory.createSSLContextFromKeystores();
            SslContext sslCtx = sslHandlerFactory.createHttp2TLSContextForClient(false);
            SslHandler sslHandler = sslCtx.newHandler(ch.alloc(), httpRoute.getHost(), httpRoute.getPort());
            SSLEngine sslEngine = sslHandler.engine();
            sslHandlerFactory.setSNIServerNames(sslEngine, httpRoute.getHost());
            if (sslConfig.isHostNameVerificationEnabled()) {
                setHostNameVerfication(sslEngine);
            }
            clientPipeline.addLast(new SslHandler(sslEngine));
            if (sslConfig.isValidateCertEnabled()) {
                clientPipeline.addLast(Constants.HTTP_CERT_VALIDATION_HANDLER,
                        new CertificateValidationHandler(sslHandler.engine(), sslConfig.getCacheValidityPeriod(),
                                sslConfig.getCacheSize()));
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
        pipeline.addLast(Constants.HTTP2_TARGET_HANDLER, http2TargetHandler);
        pipeline.addLast(Constants.DECOMPRESSOR_HANDLER, new HttpContentDecompressor());
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
        pipeline.addLast(Constants.OUTBOUND_THROTTLING_HANDLER, new OutboundThrottlingHandler());
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
                    new HttpTraceLoggingHandler(Constants.TRACE_LOG_UPSTREAM));
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

    public KeepAliveConfig getKeepAliveConfig() {
        return keepAliveConfig;
    }

    public void setHttp2ClientChannel(Http2ClientChannel http2ClientChannel) {
        http2TargetHandler.setHttp2ClientChannel(http2ClientChannel);
        clientFrameListener.setHttp2ClientChannel(http2ClientChannel);
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
