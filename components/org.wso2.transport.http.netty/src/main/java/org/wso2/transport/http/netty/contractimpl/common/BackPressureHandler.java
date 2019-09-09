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

package org.wso2.transport.http.netty.contractimpl.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.BackPressureObservable;
import org.wso2.transport.http.netty.message.DefaultBackPressureObservable;

/**
 * Handles backpressure.
 * Overrides the channelWritabilityChanged method to check the writability of the channel which is needed for
 * handling backpressure.
 */
public class BackPressureHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(BackPressureHandler.class);
    private final BackPressureObservable backPressureObservable = new DefaultBackPressureObservable();

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        if (ctx.channel().isWritable()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("HTTP/1.1 channel writable in thread {} ", Thread.currentThread().getName());
            }
            backPressureObservable.notifyWritable();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HTTP/1.1 channel inactive and notifyWritable in thread {} ", Thread.currentThread().getName());
        }
        if (backPressureObservable.getListener() != null) {
            backPressureObservable.notifyWritable();
        }
        ctx.fireChannelInactive();
    }

    /**
     * @return the observable that could be used to set/notify the listeners.
     */
    public BackPressureObservable getBackPressureObservable() {
        return backPressureObservable;
    }
}
