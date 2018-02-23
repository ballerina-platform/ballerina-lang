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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AsciiString;
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
public class HTTPServerChannelInitializer extends ChannelInitializer<SocketChannel>
        implements CarbonTransportInitializer {

    private static final Logger log = LoggerFactory.getLogger(HTTPServerChannelInitializer.class);

    private int socketIdleTimeout;
    private boolean httpTraceLogEnabled;
    private ChunkConfig chunkConfig;
    private String interfaceId;
    private String serverName;
    private SSLConfig sslConfig;
    private ServerConnectorFuture serverConnectorFuture;
    private RequestSizeValidationConfig reqSizeValidationConfig;
    private boolean isHttp2Enabled = false;
    private boolean validateCertEnabled;
    private int cacheDelay;
    private int cacheSize;
    private SSLEngine sslEngine;

    @Override
    public void setup(Map<String, String> parameters) {
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Initializing source channel pipeline");
        }

        ChannelPipeline pipeline = ch.pipeline();

        if (isHttp2Enabled) {
            if (sslConfig != null) {
                sslEngine = new SSLHandlerFactory(sslConfig).buildServerSSLEngine();
                pipeline.addLast(Constants.SSL_HANDLER, new SslHandler(sslEngine));
                pipeline.addLast(Constants.HTTP2_ALPN_HANDLER, new H2PipelineConfigurator());
            } else {
                configureH2cPipeline(pipeline);
            }
        } else {
            if (sslConfig != null) {
                sslEngine = new SSLHandlerFactory(sslConfig).buildServerSSLEngine();
                pipeline.addLast(Constants.SSL_HANDLER, new SslHandler(sslEngine));
            }

            if (validateCertEnabled) {
                ch.pipeline().addLast("certificateValidation",
                                      new CertificateValidationHandler(sslEngine, cacheDelay, cacheSize));
            }
            pipeline.addLast("encoder", new HttpResponseEncoder());
            configureHTTPPipeline(pipeline);

            if (socketIdleTimeout > 0) {
                pipeline.addBefore(
                        Constants.HTTP_SOURCE_HANDLER, Constants.IDLE_STATE_HANDLER,
                        new IdleStateHandler(socketIdleTimeout, socketIdleTimeout, socketIdleTimeout,
                                             TimeUnit.MILLISECONDS));
            }
        }
    }

    /**
     * Configure the pipeline if user sent HTTP requests
     *
     * @param pipeline Channel
     */
    public void configureHTTPPipeline(ChannelPipeline pipeline) {

        pipeline.addLast("decoder",
                new HttpRequestDecoder(reqSizeValidationConfig.getMaxUriLength(),
                        reqSizeValidationConfig.getMaxHeaderSize(), reqSizeValidationConfig.getMaxChunkSize()));
        pipeline.addLast("compressor", new CustomHttpContentCompressor());
        pipeline.addLast("chunkWriter", new ChunkedWriteHandler());

        if (httpTraceLogEnabled) {
            pipeline.addLast(Constants.HTTP_TRACE_LOG_HANDLER,
                             new HTTPTraceLoggingHandler("tracelog.http.downstream"));
        }

        pipeline.addLast("uriLengthValidator", new UriAndHeaderLengthValidator(this.serverName));
        if (reqSizeValidationConfig.getMaxEntityBodySize() > -1) {
            pipeline.addLast("maxEntityBodyValidator", new MaxEntityBodyValidator(this.serverName,
                    reqSizeValidationConfig.getMaxEntityBodySize()));
        }

        pipeline.addLast(Constants.WEBSOCKET_SERVER_HANDSHAKE_HANDLER,
                         new WebSocketServerHandshakeHandler(this.serverConnectorFuture, this.interfaceId));
        pipeline.addLast(Constants.HTTP_SOURCE_HANDLER, new SourceHandler(this.serverConnectorFuture,
                this.interfaceId, this.chunkConfig, this.serverName));
    }

    public void configureH2cPipeline(ChannelPipeline pipeline) {
        // Add http2 upgrade decoder and upgrade handler for check http version
        final HttpServerCodec sourceCodec = new HttpServerCodec();

        final HttpServerUpgradeHandler.UpgradeCodecFactory upgradeCodecFactory = protocol -> {
            if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
                return new Http2ServerUpgradeCodec(Constants.HTTP2_SOURCE_HANDLER,
                                                   new HTTP2SourceHandlerBuilder(
                                                           this.interfaceId, this.serverConnectorFuture).build());
            } else {
                return null;
            }
        };
        pipeline.addLast("encoder", sourceCodec);
        pipeline.addLast("http2-upgrade",
                         new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory, Integer.MAX_VALUE));
        // Max size of the upgrade request is limited to 2GB. Need to see whether there is better approach to handle
        // large upgrade requests
        //Requests will be propagated to next handlers if no upgrade has been attempted
        configureHTTPPipeline(pipeline);
    }

    @Override
    public boolean isServerInitializer() {
        return true;
    }

    public void setServerConnectorFuture(ServerConnectorFuture serverConnectorFuture) {
        this.serverConnectorFuture = serverConnectorFuture;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.socketIdleTimeout = idleTimeout;
    }

    public void setHttpTraceLogEnabled(boolean httpTraceLogEnabled) {
        this.httpTraceLogEnabled = httpTraceLogEnabled;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public void setSslConfig(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    public void setReqSizeValidationConfig(RequestSizeValidationConfig reqSizeValidationConfig) {
        this.reqSizeValidationConfig = reqSizeValidationConfig;
    }

    public void setChunkingConfig(ChunkConfig chunkConfig) {
        this.chunkConfig = chunkConfig;
    }

    public void setValidateCertEnabled(boolean validateCertEnabled) {
        this.validateCertEnabled = validateCertEnabled;
    }

    public void setCacheDelay(int cacheDelay) {
        this.cacheDelay = cacheDelay;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setHttp2Enabled(boolean http2Enabled) {
        isHttp2Enabled = http2Enabled;
    }

    class H2PipelineConfigurator extends ApplicationProtocolNegotiationHandler {

        public H2PipelineConfigurator() {
            super(ApplicationProtocolNames.HTTP_1_1);
        }

        @Override
        /**
         *  Configure pipeline after SSL handshake
         */
        protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
            if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                // handles pipeline for HTTP/2 requests after SSL handshake
                ctx.pipeline().addLast(Constants.HTTP2_SOURCE_HANDLER,
                                       new HTTP2SourceHandlerBuilder(interfaceId, serverConnectorFuture).build());
            } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                // handles pipeline for HTTP/1 requests after SSL handshake
                configureHTTPPipeline(ctx.pipeline());
            } else {
                throw new IllegalStateException("unknown protocol: " + protocol);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (ctx != null && ctx.channel().isActive()) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

}
