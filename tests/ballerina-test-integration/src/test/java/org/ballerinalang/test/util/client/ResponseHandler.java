/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.util.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 * A Netty handler for reading the responses sent by the server.
 */
public class ResponseHandler extends ChannelInboundHandlerAdapter {

    private CountDownLatch latch;
    private CountDownLatch waitForConnectionClosureLatch;
    private LinkedList<FullHttpResponse> fullHttpResponses = new LinkedList<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpResponse) {
            this.fullHttpResponses.add((FullHttpResponse) msg);
            latch.countDown();
        }
    }

    FullHttpResponse getHttpFullResponse() {
        return this.fullHttpResponses.getFirst();
    }

    LinkedList<FullHttpResponse> getHttpFullResponses() {
        return this.fullHttpResponses;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.waitForConnectionClosureLatch.countDown();
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    void setWaitForConnectionClosureLatch(CountDownLatch waitForConnectionClosureLatch) {
        this.waitForConnectionClosureLatch = waitForConnectionClosureLatch;
    }
}
