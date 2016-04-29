/*
<<<<<<< HEAD
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
=======
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
>>>>>>> upstream/master
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

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorConfig;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorFactory;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.PoolConfiguration;

import java.util.Map;

/**
 * A class that responsible for create server side channels.
 */
public class CarbonNettyServerInitializer implements CarbonTransportInitializer {

    private static final Logger log = LoggerFactory.getLogger(CarbonNettyServerInitializer.class);
    private ConnectionManager connectionManager;

    private ListenerConfiguration listenerConfiguration;


    public CarbonNettyServerInitializer(ListenerConfiguration listenerConfiguration) {
        this.listenerConfiguration = listenerConfiguration;
    }

    @Override
    public void setup(Map<String, String> parameters) {

        PoolConfiguration.createPoolConfiguration(parameters);
        try {
            connectionManager = ConnectionManager.getInstance();

            if (parameters != null && Boolean.parseBoolean(listenerConfiguration.getEnableDisruptor())) {
                log.debug("Disruptor is enabled");
                log.debug("Disruptor configuration creating");
                DisruptorConfig disruptorConfig = new DisruptorConfig
                           (parameters.get(Constants.DISRUPTOR_BUFFER_SIZE),
                            parameters.get(Constants.DISRUPTOR_COUNT),
                            parameters.get(Constants.DISRUPTOR_EVENT_HANDLER_COUNT),
                            parameters.get(Constants.WAIT_STRATEGY),
                            Boolean.parseBoolean(Constants.SHARE_DISRUPTOR_WITH_OUTBOUND),
                            parameters.get(Constants.DISRUPTOR_CONSUMER_EXTERNAL_WORKER_POOL));
                // TODO: Need to have a proper service
                DisruptorFactory.createDisruptors(DisruptorFactory.DisruptorType.INBOUND, disruptorConfig);
            } else if (!Boolean.parseBoolean(listenerConfiguration.getEnableDisruptor())) {
                int executorWorkerPoolSize = Integer.parseInt(parameters.get(Constants.EXECUTOR_WORKER_POOL_SIZE));
                log.debug("Disruptor is disabled and using executor thread pool with size of " +
                          executorWorkerPoolSize);
                if (executorWorkerPoolSize > 0) {
                    listenerConfiguration.setExecHandlerThreadPoolSize(executorWorkerPoolSize);
                } else {
                    log.error("Please enable disruptor or specify executorHandlerThreadPool size greater that 0");
                }
            } else {
                log.warn("Disruptor specific parameters are not specified in "
                         + "configuration hence using default configs");
                DisruptorConfig disruptorConfig = new DisruptorConfig();
                DisruptorFactory.createDisruptors(DisruptorFactory.DisruptorType.INBOUND, disruptorConfig);
            }
        } catch (Exception e) {
            log.error("Error initializing the transport ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initChannel(Object ch) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing source channel pipeline");
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
            if (Boolean.parseBoolean(listenerConfiguration.getEnableDisruptor())) {
                p.addLast("handler", new SourceHandler(connectionManager));
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

}
