/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;

import java.util.concurrent.atomic.AtomicInteger;

import static org.wso2.transport.http.netty.contractimpl.common.Util.isLastHttpContent;

/**
 * Default implementation of the message Listener.
 */
public class DefaultListener implements Listener {

    private static final int MAXIMUM_BYTE_SIZE = 2097152; //Maximum threshold of reading bytes(2MB)
    private AtomicInteger cumulativeByteQuantity = new AtomicInteger(0);
    private ChannelHandlerContext ctx;
    private boolean readCompleted = false;
    private boolean first = true;

    public DefaultListener(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onAdd(HttpContent httpContent) {
        if (first) {
            this.ctx.channel().config().setAutoRead(false);
            first = false;
        }
        int count = this.cumulativeByteQuantity.addAndGet(httpContent.content().readableBytes());
        if (count < MAXIMUM_BYTE_SIZE && !readCompleted) {
            if (isLastHttpContent(httpContent)) {
                readCompleted = true;
                this.ctx.channel().config().setAutoRead(true);
                this.ctx = null;
            } else {
                this.ctx.channel().read();
            }
        }
    }

    @Override
    public void onRemove(HttpContent httpContent) {
        int count = this.cumulativeByteQuantity.addAndGet(-(httpContent.content().readableBytes()));
        if (count < MAXIMUM_BYTE_SIZE && !readCompleted) {
            this.ctx.channel().read();
        }
    }

    @Override
    public void resumeReadInterest() {
        ctx.channel().config().setAutoRead(true);
    }
}
