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

import io.netty.channel.ChannelInitializer;
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
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.ProxyServerConfiguration;
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

    private static final Http2FrameLogger logger =
            new Http2FrameLogger(DEBUG,     // Change mode to INFO for logging frames
                                 HttpClientChannelInitializer.class);

    public HttpClientChannelInitializer(SenderConfiguration senderConfiguration, SSLEngine sslEngine,
            ConnectionManager connectionManager) {
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

        String httpVersion = senderConfiguration.getHttpVersion();
        if (Float.valueOf(httpVersion) == Constants.HTTP_2_0) {
            this.isHttp2 = true;
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
        if (proxyServerConfiguration != null) {
            if (proxyServerConfiguration.getProxyUsername() != null
                    && proxyServerConfiguration.getProxyPassword() != null) {
                ch.pipeline().addLast("proxyServer",
                        new HttpProxyHandler(proxyServerConfiguration.getInetSocketAddress(),
                                proxyServerConfiguration.getProxyUsername(),
                                proxyServerConfiguration.getProxyPassword()));
            } else {
                ch.pipeline()
                        .addLast("proxyServer", new HttpProxyHandler(proxyServerConfiguration.getInetSocketAddress()));
            }
        }
        if (sslEngine != null) {
            log.debug("adding ssl handler");
            ch.pipeline().addLast("ssl", new SslHandler(this.sslEngine));
        }
        if (validateCertEnabled && sslEngine != null) {
            ch.pipeline().addLast("certificateValidation",
                    new CertificateValidationHandler(this.sslEngine, this.cacheDelay, this.cacheSize));
        }
        HttpClientCodec sourceCodec = new HttpClientCodec();
        if (isHttp2) {
            ch.pipeline().addLast(sourceCodec);
        } else {
            ch.pipeline().addLast("decoder", new HttpResponseDecoder());
            ch.pipeline().addLast("encoder", new HttpRequestEncoder());
        }
        ch.pipeline().addLast("deCompressor", new HttpContentDecompressor());
        if (httpTraceLogEnabled) {
            ch.pipeline().addLast(Constants.HTTP_TRACE_LOG_HANDLER,
                                  new HTTPTraceLoggingHandler("tracelog.http.upstream"));
        }
        if (followRedirect) {
            if (log.isDebugEnabled()) {
                log.debug("Follow Redirect is enabled, so adding the redirect handler to the pipeline.");
            }
            RedirectHandler redirectHandler = new RedirectHandler(sslEngine, httpTraceLogEnabled, maxRedirectCount
                    , connectionManager);
            ch.pipeline().addLast(Constants.REDIRECT_HANDLER, redirectHandler);
        }
        targetHandler = new TargetHandler();
        targetHandler.setHttp2ClientOutboundHandler(clientOutboundHandler);
        targetHandler.setKeepAlive(isKeepAlive);
        if (isHttp2 && sslEngine == null) {
            Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(http2ConnectionHandler);
            HttpClientUpgradeHandler upgradeHandler =
                    new HttpClientUpgradeHandler(sourceCodec, upgradeCodec, Integer.MAX_VALUE);
            ch.pipeline().addLast(Constants.HTTP2_UPGRADE_HANDLER, upgradeHandler);
        }
        ch.pipeline().addLast(Constants.TARGET_HANDLER, targetHandler);
    }

    public TargetHandler getTargetHandler() {
        return targetHandler;
    }

    public Http2ConnectionManager getHttp2ConnectionManager() {
        return http2ConnectionManager;
    }

    /**
     * Get the associated {@code Http2Connection}
     *
     * @return the associated Http2Connection
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
}
