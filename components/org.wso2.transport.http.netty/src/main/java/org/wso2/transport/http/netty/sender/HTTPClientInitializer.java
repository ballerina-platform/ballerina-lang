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
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.ProxyServerConfiguration;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.listener.CustomHttpContentCompressor;
import org.wso2.transport.http.netty.listener.HTTPTraceLoggingHandler;
import org.wso2.transport.http.netty.sender.channel.pool.ConnectionManager;

import javax.net.ssl.SSLEngine;

/**
 * A class that responsible for initialize target server pipeline.
 */
public class HTTPClientInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(HTTPClientInitializer.class);

    private SSLEngine sslEngine;
    private TargetHandler targetHandler;
    private boolean httpTraceLogEnabled;
    private boolean followRedirect;
    private boolean certificateRevocationVerifier;
    private int maxRedirectCount;
    private int cacheSize;
    private int cacheDelay;
    private boolean isKeepAlive;
    private ProxyServerConfiguration proxyServerConfiguration;
    private ConnectionManager connectionManager;

    public HTTPClientInitializer(SenderConfiguration senderConfiguration, SSLEngine sslEngine,
            ConnectionManager connectionManager) {
        this.sslEngine = sslEngine;
        this.httpTraceLogEnabled = senderConfiguration.isHttpTraceLogEnabled();
        this.followRedirect = senderConfiguration.isFollowRedirect();
        this.maxRedirectCount = senderConfiguration.getMaxRedirectCount(Constants.MAX_REDIRECT_COUNT);
        this.isKeepAlive = senderConfiguration.isKeepAlive();
        this.proxyServerConfiguration = senderConfiguration.getProxyServerConfiguration();
        this.connectionManager = connectionManager;
        this.certificateRevocationVerifier = senderConfiguration.getCertificateRevocationVerifier();
        this.cacheDelay = senderConfiguration.getCacheDelay();
        this.cacheSize = senderConfiguration.getCacheSize();
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
        if (certificateRevocationVerifier && sslEngine != null) {
            ch.pipeline().addLast("certificateValidation",
                    new CertificateValidationHandler(this.sslEngine, this.cacheDelay, this.cacheSize));
        }
        ch.pipeline().addLast("compressor", new CustomHttpContentCompressor());
        ch.pipeline().addLast("decoder", new HttpResponseDecoder());
        ch.pipeline().addLast("encoder", new HttpRequestEncoder());
        ch.pipeline().addLast("chunkWriter", new ChunkedWriteHandler());
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
        targetHandler.setKeepAlive(isKeepAlive);
        ch.pipeline().addLast(Constants.TARGET_HANDLER, targetHandler);
    }

    public TargetHandler getTargetHandler() {
        return targetHandler;
    }

    public boolean isKeepAlive() {
        return isKeepAlive;
    }
}
