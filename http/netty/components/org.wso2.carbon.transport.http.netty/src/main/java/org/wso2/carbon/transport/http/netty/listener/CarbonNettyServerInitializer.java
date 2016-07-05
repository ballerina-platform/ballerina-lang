/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.BufferFactory;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorConfig;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorFactory;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLHandlerFactory;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.Map;

/**
 * A class that responsible for create server side channels.
 */
public class CarbonNettyServerInitializer extends ChannelInitializer<SocketChannel>
        implements CarbonTransportInitializer {

    private static final Logger log = LoggerFactory.getLogger(CarbonNettyServerInitializer.class);
    private ConnectionManager connectionManager;

    private ListenerConfiguration listenerConfiguration;

    private SSLConfig sslConfig;

    private Map<String, org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig> sslConfigMap;

    public CarbonNettyServerInitializer(ListenerConfiguration listenerConfiguration) {
        this.listenerConfiguration = listenerConfiguration;
    }

    @Override
    public void setup(Map<String, String> parameters) {

        if (parameters != null && parameters.get(Constants.OUTPUT_CONTENT_BUFFER_SIZE) != null) {
            BufferFactory.createInstance(Integer.parseInt(parameters.get(Constants.OUTPUT_CONTENT_BUFFER_SIZE)));
        }
        try {
            connectionManager = ConnectionManager.getInstance(parameters);

            if (listenerConfiguration.getEnableDisruptor()) {
                if (parameters != null && !parameters.isEmpty()) {
                    log.debug("Disruptor is enabled");
                    log.debug("Disruptor configuration creating");
                    DisruptorConfig disruptorConfig = new DisruptorConfig(parameters
                            .getOrDefault(Constants.DISRUPTOR_BUFFER_SIZE, Constants.DEFAULT_DISRUPTOR_BUFFER_SIZE),
                            parameters.getOrDefault(Constants.DISRUPTOR_COUNT, Constants.DEFAULT_DISRUPTOR_COUNT),
                            parameters.getOrDefault(Constants.DISRUPTOR_EVENT_HANDLER_COUNT,
                                    Constants.DEFAULT_DISRUPTOR_EVENT_HANDLER_COUNT),
                            parameters.getOrDefault(Constants.WAIT_STRATEGY, Constants.DEFAULT_WAIT_STRATEGY),
                            Boolean.parseBoolean(parameters.getOrDefault(Constants.SHARE_DISRUPTOR_WITH_OUTBOUND,
                                    Constants.DEFAULT_SHARE_DISRUPTOR_WITH_OUTBOUND)), parameters
                            .getOrDefault(Constants.DISRUPTOR_CONSUMER_EXTERNAL_WORKER_POOL,
                                    Constants.DEFAULT_DISRUPTOR_CONSUMER_EXTERNAL_WORKER_POOL));
                    // TODO: Need to have a proper service
                    DisruptorFactory.createDisruptors(DisruptorFactory.DisruptorType.INBOUND, disruptorConfig);
                } else {
                    log.warn("Disruptor specific parameters are not specified in "
                            + "configuration hence using default configs");
                    DisruptorConfig disruptorConfig = new DisruptorConfig();
                    DisruptorFactory.createDisruptors(DisruptorFactory.DisruptorType.INBOUND, disruptorConfig);
                }
            } else {
                if (parameters != null && !parameters.isEmpty()) {
                    int executorWorkerPoolSize = Integer.parseInt(parameters
                            .getOrDefault(Constants.EXECUTOR_WORKER_POOL_SIZE,
                                    String.valueOf(Constants.DEFAULT_EXECUTOR_WORKER_POOL_SIZE)));
                    log.debug("Disruptor is disabled and using executor thread pool with size of "
                            + executorWorkerPoolSize);
                    if (executorWorkerPoolSize > 0) {
                        listenerConfiguration.setExecHandlerThreadPoolSize(executorWorkerPoolSize);
                    } else {
                        log.warn("Please enable disruptor or specify executorHandlerThreadPool size greater than 0,"
                                + " starting with default value");
                        listenerConfiguration.setExecHandlerThreadPoolSize(Constants.DEFAULT_EXECUTOR_WORKER_POOL_SIZE);
                    }
                } else {
                    log.warn("ExecutorHandlerThreadPool size is not specified using the default value");
                    listenerConfiguration.setExecHandlerThreadPoolSize(Constants.DEFAULT_EXECUTOR_WORKER_POOL_SIZE);
                }

            }
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
        String host = ch.remoteAddress().getHostName();
        int port = ch.remoteAddress().getPort();

        String id = host + ":" + port;
        if (sslConfigMap.get(id) != null) {
            SslHandler sslHandler = new SSLHandlerFactory(sslConfigMap.get(id)).create();
            ch.pipeline().addLast("ssl", sslHandler);
        } else if (sslConfig != null) {
            SslHandler sslHandler = new SSLHandlerFactory(sslConfig).create();
            ch.pipeline().addLast("ssl", sslHandler);
        }
        ChannelPipeline p = ((SocketChannel) ch).pipeline();
        p.addLast("encoder", new HttpResponseEncoder());
        if (RequestSizeValidationConfiguration.getInstance().isHeaderSizeValidation()) {
            p.addLast("decoder", new CustomHttpRequestDecoder());
        } else {
            p.addLast("decoder", new HttpRequestDecoder());
        }
        if (RequestSizeValidationConfiguration.getInstance().isRequestSizeValidation()) {
            p.addLast("custom-aggregator", new CustomHttpObjectAggregator());
        }
        p.addLast("compressor", new HttpContentCompressor());
        p.addLast("chunkWriter", new ChunkedWriteHandler());
        try {
            if (listenerConfiguration.getEnableDisruptor()) {
                p.addLast("handler", new SourceHandler(connectionManager, listenerConfiguration));
            } else {
                p.addLast("handler", new WorkerPoolDispatchingSourceHandler(connectionManager, listenerConfiguration));
            }
        } catch (Exception e) {
            log.error("Cannot Create SourceHandler ", e);
        }
    }

    @Override
    public boolean isServerInitializer() {
        return true;
    }

    public void setSslConfig(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    public void setSslConfigMap(Map<String, SSLConfig> sslConfigMap) {
        this.sslConfigMap = sslConfigMap;
    }

}
