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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.BufferFactory;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLHandlerFactory;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.RequestSizeValidationConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class that responsible for create server side channels.
 */
public class HTTPServerChannelInitializer extends ChannelInitializer<SocketChannel>
        implements CarbonTransportInitializer {

    private static final Logger log = LoggerFactory.getLogger(HTTPServerChannelInitializer.class);
    private ConnectionManager connectionManager;

    private Map<Integer, ListenerConfiguration> listenerConfigurationMap = new HashMap<>();

    public HTTPServerChannelInitializer() {
    }

    public void registerListenerConfig(ListenerConfiguration listenerConfiguration) {
        listenerConfigurationMap.put(listenerConfiguration.getPort(), listenerConfiguration);
    }

    public void unRegisterListenerConfig(ListenerConfiguration listenerConfiguration) {
        listenerConfigurationMap.remove(listenerConfiguration.getPort());
    }

    @Override
    //TODO: Check the usage of this
    public void setup(Map<String, String> parameters) {
        if (parameters != null && parameters.get(Constants.OUTPUT_CONTENT_BUFFER_SIZE) != null) {
            BufferFactory.createInstance(Integer.parseInt(parameters.get(Constants.OUTPUT_CONTENT_BUFFER_SIZE)));
        }
    }

    public void setupConnectionManager(Set<TransportProperty> transportPropertySet) {
        try {
            connectionManager = ConnectionManager.getInstance(transportPropertySet);
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
        int port = ch.localAddress().getPort();

        ListenerConfiguration listenerConfiguration = listenerConfigurationMap.get(port);
        if (listenerConfiguration.getSslConfig() != null) {
            SslHandler sslHandler = new SSLHandlerFactory(listenerConfiguration.getSslConfig()).create();
            ch.pipeline().addLast("ssl", sslHandler);
        }
        ChannelPipeline p = ch.pipeline();
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
            p.addLast("handler", new SourceHandler(connectionManager, listenerConfiguration));
        } catch (Exception e) {
            log.error("Cannot Create SourceHandler ", e);
        }
    }

    @Override
    public boolean isServerInitializer() {
        return true;
    }

}
