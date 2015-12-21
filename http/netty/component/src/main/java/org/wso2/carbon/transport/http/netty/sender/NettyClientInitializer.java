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

package org.wso2.carbon.transport.http.netty.sender;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLHandlerFactory;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;

/**
 * A class that responsible for initialize target server pipeline.
 */
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {


    private static final Logger log = LoggerFactory.getLogger(NettyClientInitializer.class);

    private String transportID;
    private SSLConfig sslConfig;
    private CarbonNettyClientInitializer initializer;

    public NettyClientInitializer(String transportID) {
        this.transportID = transportID;
    }

    public void setSslConfig(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // Add the generic handlers to the pipeline
        // e.g. SSL handler
        if (sslConfig != null) {
            SslHandler sslHandler = new SSLHandlerFactory(sslConfig).create();
            sslHandler.engine().setUseClientMode(true);
            ch.pipeline().addLast("ssl", sslHandler);
        }

        // Add the rest of the handlers to the pipeline
        initializer =
                   (CarbonNettyClientInitializer) NettyTransportContextHolder.getInstance().
                              getClientChannelInitializer(transportID);

        if (initializer != null) {
            if (log.isDebugEnabled()) {
                log.debug("Calling CarbonNettyServerInitializer OSGi service " + initializer);
            }
            initializer.initChannel(ch);
        }

    }

    public TargetHandler getTargetHandler() {
        return initializer.getTargetHandler();
    }
}
