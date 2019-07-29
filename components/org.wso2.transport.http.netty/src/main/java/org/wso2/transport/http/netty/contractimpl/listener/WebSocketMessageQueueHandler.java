/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.transport.http.netty.contractimpl.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This Handler is responsible for issuing frame by frame when the WebSocket connection is asked to read next frame
 * when autoRead is set to false.
 */
public class WebSocketMessageQueueHandler extends ChannelInboundHandlerAdapter {

    private final Queue<Object> messageQueue;
    private ChannelHandlerContext ctx;
    private boolean readNext;

    public WebSocketMessageQueueHandler() {
        this.messageQueue = new LinkedList<>();
    }

    @Override
    public synchronized void handlerAdded(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (readNext) {
            readNext = false;
            ctx.fireChannelRead(msg);
            return;
        }
        messageQueue.add(msg);
    }

    public synchronized void readNextFrame() {
        if (ctx == null) {
            throw new IllegalStateException("Cannot call readNextFrame() without an initialized ChannelHandlerContext");
        }
        if (messageQueue.isEmpty()) {
            readNext = true;
            ctx.channel().read();
            return;
        }
        ctx.fireChannelRead(messageQueue.poll());
    }
}
