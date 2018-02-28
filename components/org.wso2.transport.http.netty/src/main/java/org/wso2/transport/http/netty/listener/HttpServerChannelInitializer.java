/*
 *  Copyright (c) 2017 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.transport.http.netty.listener;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.common.ssl.SSLHandlerFactory;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.RequestSizeValidationConfig;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.sender.CertificateValidationHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

/**
 * A class that responsible for build server side channels.
 */
public class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel>
        implements CarbonTransportInitializer {

    private static final Logger log = LoggerFactory.getLogger(HttpServerChannelInitializer.class);

    private int socketIdleTimeout;
    private boolean httpTraceLogEnabled;
    private ChunkConfig chunkConfig;
    private String interfaceId;
    private String serverName;
    private SSLConfig sslConfig;
    private ServerConnectorFuture serverConnectorFuture;
    private RequestSizeValidationConfig reqSizeValidationConfig;
    private boolean validateCertEnabled;
    private int cacheDelay;
    private int cacheSize;
    private SSLEngine sslEngine;
    private boolean ocspStaplingEnabled;
    private ChannelGroup allChannels;

    @Override
    public void setup(Map<String, String> parameters) {
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Initializing source channel pipeline");
        }

        ChannelPipeline serverPipeline = ch.pipeline();

        if (ocspStaplingEnabled && sslConfig != null) {

            OCSPResp response = OCSPResponseBuilder.getOcspResponse(sslConfig, cacheSize, cacheDelay);
            if (!OpenSsl.isAvailable()) {
                throw new IllegalStateException("OpenSSL is not available!");
            }
            if (!OpenSsl.isOcspSupported()) {
                throw new IllegalStateException("OCSP is not supported!");
            }

            ReferenceCountedOpenSslContext context = new SSLHandlerFactory(sslConfig)
                    .getServerReferenceCountedOpenSslContext();
            SslHandler sslHandler = context.newHandler(ch.alloc());

            ReferenceCountedOpenSslEngine engine = (ReferenceCountedOpenSslEngine) sslHandler.engine();
            engine.setOcspResponse(response.getEncoded());
            ch.pipeline().addLast(sslHandler);

        } else if (sslConfig != null) {
            sslEngine = new SSLHandlerFactory(sslConfig).buildServerSSLEngine();
            serverPipeline.addLast(Constants.SSL_HANDLER, new SslHandler(sslEngine));
        }

        if (validateCertEnabled) {
            ch.pipeline().addLast("certificateValidation",
                    new CertificateValidationHandler(sslEngine, cacheDelay, cacheSize));
        }
        serverPipeline.addLast("encoder", new HttpResponseEncoder());
        configureHTTPPipeline(serverPipeline);

        if (socketIdleTimeout > 0) {
            serverPipeline.addBefore(
                    Constants.HTTP_SOURCE_HANDLER, Constants.IDLE_STATE_HANDLER,
                    new IdleStateHandler(socketIdleTimeout, socketIdleTimeout, socketIdleTimeout,
                                         TimeUnit.MILLISECONDS));
        }
    }

    /**
     * Configure the pipeline if user sent HTTP requests
     *
     * @param serverPipeline Channel
     */
    private void configureHTTPPipeline(ChannelPipeline serverPipeline) {

        serverPipeline.addLast("decoder",
                new HttpRequestDecoder(reqSizeValidationConfig.getMaxUriLength(),
                        reqSizeValidationConfig.getMaxHeaderSize(), reqSizeValidationConfig.getMaxChunkSize()));
        serverPipeline.addLast("compressor", new CustomHttpContentCompressor());
        serverPipeline.addLast("chunkWriter", new ChunkedWriteHandler());

        if (httpTraceLogEnabled) {
            serverPipeline.addLast(Constants.HTTP_TRACE_LOG_HANDLER,
                             new HTTPTraceLoggingHandler("tracelog.http.downstream"));
        }

        serverPipeline.addLast("uriLengthValidator", new UriAndHeaderLengthValidator(this.serverName));
        if (reqSizeValidationConfig.getMaxEntityBodySize() > -1) {
            serverPipeline.addLast("maxEntityBodyValidator", new MaxEntityBodyValidator(this.serverName,
                    reqSizeValidationConfig.getMaxEntityBodySize()));
        }

        serverPipeline.addLast(Constants.WEBSOCKET_SERVER_HANDSHAKE_HANDLER,
                         new WebSocketServerHandshakeHandler(this.serverConnectorFuture, this.interfaceId));
        serverPipeline.addLast(Constants.HTTP_SOURCE_HANDLER, new SourceHandler(this.serverConnectorFuture,
                this.interfaceId, this.chunkConfig, this.serverName, this.allChannels));
    }

    @Override
    public boolean isServerInitializer() {
        return true;
    }

    public void setServerConnectorFuture(ServerConnectorFuture serverConnectorFuture) {
        this.serverConnectorFuture = serverConnectorFuture;
    }

    void setIdleTimeout(int idleTimeout) {
        this.socketIdleTimeout = idleTimeout;
    }

    void setHttpTraceLogEnabled(boolean httpTraceLogEnabled) {
        this.httpTraceLogEnabled = httpTraceLogEnabled;
    }

    void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    void setSslConfig(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    void setReqSizeValidationConfig(RequestSizeValidationConfig reqSizeValidationConfig) {
        this.reqSizeValidationConfig = reqSizeValidationConfig;
    }

    public void setChunkingConfig(ChunkConfig chunkConfig) {
        this.chunkConfig = chunkConfig;
    }

    void setValidateCertEnabled(boolean validateCertEnabled) {
        this.validateCertEnabled = validateCertEnabled;
    }

    void setCacheDelay(int cacheDelay) {
        this.cacheDelay = cacheDelay;
    }

    void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setOcspStaplingEnabled(boolean ocspStaplingEnabled) {
        this.ocspStaplingEnabled = ocspStaplingEnabled;
    }

    void setAllChannels(ChannelGroup allChannels) {
        this.allChannels = allChannels;
    }
}
