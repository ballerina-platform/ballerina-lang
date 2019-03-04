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

package org.wso2.transport.http.netty.contractimpl.sender.channel.pool;


import io.netty.channel.EventLoopGroup;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.sender.channel.TargetChannel;

/**
 * A class which creates a TargetChannel pool for each route.
 */
public class PoolableTargetChannelFactoryPerSrcHndlr implements PoolableObjectFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PoolableTargetChannelFactoryPerSrcHndlr.class);

    private final GenericObjectPool genericObjectPool;
    private final EventLoopGroup clientEventGroup;
    private final Class eventLoopClass;
    private final PoolableTargetChannelFactory channelFactory;

    PoolableTargetChannelFactoryPerSrcHndlr(GenericObjectPool genericObjectPool,
                                            PoolableTargetChannelFactory channelFactory,
                                            EventLoopGroup clientEventGroup,
                                            Class eventLoopClass) {
        this.genericObjectPool = genericObjectPool;
        this.channelFactory = channelFactory;
        this.clientEventGroup = clientEventGroup;
        this.eventLoopClass = eventLoopClass;
    }

    @Override
    public Object makeObject() throws Exception {
        //When borrowObject() creates a new channel, it should be created with the given eventloop class and group since
        //with http/2, eventloop of the channel cannot be changed later. This does not affect http/1.1.
        channelFactory.setEventLoopClass(eventLoopClass);
        channelFactory.setEventLoopGroup(clientEventGroup);
        TargetChannel targetChannel = (TargetChannel) this.genericObjectPool.borrowObject();
        LOG.debug("Created channel: {}", targetChannel);
        return targetChannel;
    }

    @Override
    public void destroyObject(Object o) throws Exception {
        if (((TargetChannel) o).getChannel().isActive()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Original Channel {} is returned to the pool. ", ((TargetChannel) o).getChannel().id());
            }
            this.genericObjectPool.returnObject(o);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Original Channel is destroyed. ");
            }
            this.genericObjectPool.invalidateObject(o);
        }
    }

    @Override
    public boolean validateObject(Object o) {
        if (((TargetChannel) o).getChannel() != null) {
            boolean answer = ((TargetChannel) o).getChannel().isActive();
            LOG.debug("Validating channel: {} -> {}", o, answer);
            return answer;
        }
        return true;
    }

    @Override
    public void activateObject(Object o) {}

    @Override
    public void passivateObject(Object o) {}
}
