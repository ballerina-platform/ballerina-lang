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

package org.wso2.carbon.transport.http.netty.sender.channel.pool;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.ChannelUtils;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;

/**
 * A class which creates a TargetChannel pool for each route.
 */
public class PoolableTargetChannelFactory implements PoolableObjectFactory {

    private static final Logger log = LoggerFactory.getLogger(PoolableTargetChannelFactory.class);

    private EventLoopGroup eventLoopGroup;
    private Class eventLoopClass;
    private HttpRoute httpRoute;
    private SenderConfiguration senderConfiguration;

    public PoolableTargetChannelFactory(HttpRoute httpRoute, EventLoopGroup eventLoopGroup,
                                        Class eventLoopClass, SenderConfiguration senderConfiguration) {
        this.eventLoopGroup = eventLoopGroup;
        this.eventLoopClass = eventLoopClass;
        this.httpRoute = httpRoute;
        this.senderConfiguration = senderConfiguration;
    }


    @Override
    public Object makeObject() throws Exception {
        TargetChannel targetChannel = new TargetChannel();
        ChannelFuture channelFuture = ChannelUtils.getNewChannelFuture(targetChannel,
                eventLoopGroup, eventLoopClass, httpRoute, senderConfiguration);
        Channel channel = ChannelUtils.openChannel(channelFuture, httpRoute);
        log.debug("Created channel: {}", channel);
        targetChannel.setChannel(channel);
        return targetChannel;
    }

    @Override
    public void destroyObject(Object o) throws Exception {
        log.debug("Destroying channel: {}", o);
        if (((TargetChannel) o).getChannel().isOpen()) {
            ((TargetChannel) o).getChannel().close();

        }
    }

    @Override
    public boolean validateObject(Object o) {
        boolean answer = ((TargetChannel) o).getChannel().isActive();
        log.debug("Validating channel: {} -> {}", o, answer);
        return answer;
    }

    @Override
    public void activateObject(Object o) throws Exception {

    }

    @Override
    public void passivateObject(Object o) throws Exception {

    }


}
