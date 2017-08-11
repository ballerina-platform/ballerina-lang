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

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.RequestSizeValidationConfiguration;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLEngine;

/**
 * A class that responsible for build server side channels.
 */
public class HTTPServerChannelInitializer extends ChannelInitializer<SocketChannel>
        implements CarbonTransportInitializer {

    private static final Logger log = LoggerFactory.getLogger(HTTPServerChannelInitializer.class);

    private ConnectionManager connectionManager;
    private ListenerConfiguration listenerConfiguration;
    private ServerConnectorFuture serverConnectorFuture;
    private SSLEngine sslEngine;
    private int socketIdleTimeout;

    public HTTPServerChannelInitializer(ListenerConfiguration listenerConfiguration) {
        this.listenerConfiguration = listenerConfiguration;
    }

    @Override
    public void setup(Map<String, String> parameters) {
    }

    public void setupConnectionManager(Map<String, Object> transportProperties) {
        try {
            ConnectionManager.init(transportProperties);
            connectionManager = ConnectionManager.getInstance();
        } catch (Exception e) {
            log.error("Error initializing the transport ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Initializing source channel pipeline");
        }

        ListenerConfiguration listenerConfiguration = this.listenerConfiguration;
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("encoder", new HttpResponseEncoder());
        configureHTTPPipeline(pipeline, listenerConfiguration);

        if (socketIdleTimeout > 0) {
            pipeline.addBefore(
                    Constants.HTTP_SOURCE_HANDLER, Constants.IDLE_STATE_HANDLER,
                    new IdleStateHandler(socketIdleTimeout, socketIdleTimeout, socketIdleTimeout,
                                         TimeUnit.MILLISECONDS));
        }

        if (sslEngine != null) {
            pipeline.addFirst("ssl", new SslHandler(sslEngine));
        }
    }

    /**
     * Configure the pipeline if user sent HTTP requests
     *
     * @param pipeline                    Channel
     * @param listenerConfiguration HTTPConnectorListener Configuration
     */
    public void configureHTTPPipeline(ChannelPipeline pipeline, ListenerConfiguration listenerConfiguration) {
        // Removed the default encoder since http/2 version upgrade already added to pipeline
        if (RequestSizeValidationConfiguration.getInstance().isHeaderSizeValidation()) {
            pipeline.addLast("decoder", new CustomHttpRequestDecoder());
        } else {
            pipeline.addLast("decoder", new HttpRequestDecoder());
        }
        if (RequestSizeValidationConfiguration.getInstance().isRequestSizeValidation()) {
            pipeline.addLast("custom-aggregator", new CustomHttpObjectAggregator());
        }
        pipeline.addLast("compressor", new HttpContentCompressor());
        pipeline.addLast("chunkWriter", new ChunkedWriteHandler());
        try {
            pipeline.addLast(Constants.HTTP_SOURCE_HANDLER,
                             new SourceHandler(connectionManager, listenerConfiguration, this.serverConnectorFuture));
        } catch (Exception e) {
            log.error("Cannot Create SourceHandler ", e);
        }
    }

    @Override
    public boolean isServerInitializer() {
        return true;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setServerConnectorFuture(ServerConnectorFuture serverConnectorFuture) {
        this.serverConnectorFuture = serverConnectorFuture;
    }

    public void setSslEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.socketIdleTimeout = idleTimeout;
    }
}
