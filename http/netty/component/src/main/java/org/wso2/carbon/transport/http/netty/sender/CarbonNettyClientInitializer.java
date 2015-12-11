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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.sender;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.PoolConfiguration;

import java.util.Map;

/**
 * Default ClientInitializer class used in netty gw
 */
public class CarbonNettyClientInitializer implements CarbonTransportInitializer {
    protected static final String HANDLER = "handler";
    private TargetHandler handler;

    @Override
    public void setup(Map<String, String> map) {

        PoolConfiguration.createPoolConfiguration(map);
    }

    @Override
    public void initChannel(Object o) {
        ChannelPipeline p = ((SocketChannel) o).pipeline();
        p.addLast("decoder", new HttpResponseDecoder());
        p.addLast("encoder", new HttpRequestEncoder());
        handler = new TargetHandler();
        p.addLast(HANDLER, handler);
    }

    @Override
    public boolean isServerInitializer() {
        return false;
    }

    public TargetHandler getTargetHandler() {
        return handler;
    }
}
