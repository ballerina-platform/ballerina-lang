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
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.common.ssl.SSLHandlerFactory;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.RequestSizeValidationConfig;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Override
    public void setup(Map<String, String> parameters) {
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Initializing source channel pipeline");
        }

        ChannelPipeline pipeline = ch.pipeline();

        if (sslConfig != null) {
            pipeline.addLast(Constants.SSL_HANDLER, new SslHandler(new SSLHandlerFactory(sslConfig).build()));
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

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
