/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.transport.http.netty.message;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * Default implementation of the {@link BackPressureListener}.
 */
public class DefaultBackPressureListener implements BackPressureListener {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBackPressureListener.class);

    private Semaphore semaphore;
    private Channel channel;

    /**
     * Creates the semaphore and sets the source or target channel.
     *
     * @param context The {@link org.wso2.transport.http.netty.contractimpl.listener.SourceHandler} context for
     *                outbound response and the
     *                {@link org.wso2.transport.http.netty.contractimpl.sender.TargetHandler} context for the
     *                outbound request.
     */
    public DefaultBackPressureListener(ChannelHandlerContext context) {
        this.channel = context.channel();
        this.semaphore = new Semaphore(0);
    }

    @Override
    public void onAcquire() {
        if (!channel.isWritable() && channel.isActive()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Semaphore acquired for channel {}.", channel.id());
            }
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onRelease() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Semaphore released for channel {}.", channel.id());
        }
        semaphore.release();
    }
}
