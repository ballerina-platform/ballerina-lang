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
 *
 */

package org.wso2.transport.http.netty.util.client.http;

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Handle http response.
 */
public class ResponseHandler extends ChannelInboundHandlerAdapter {

    private CountDownLatch latch;
    private CountDownLatch waitForConnectionClosureLatch;
    private LinkedList<FullHttpResponse> fullHttpResponses = new LinkedList<>();
    private FullHttpResponseMessage fullHttpResponseMessage;
    private HttpResponse response;
    private List<HttpContent> contentList = new ArrayList<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            response = (HttpResponse) msg;
        } else {
            if (msg instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) msg;
                contentList.add(httpContent);
                if (httpContent instanceof LastHttpContent) {
                    fullHttpResponseMessage = new FullHttpResponseMessage(contentList, response);
                    fullHttpResponses.add(getHttpFullResponse());
                    latch.countDown();
                }
            }
        }
    }

    FullHttpResponse getHttpFullResponse() {
        HttpHeaders trailers = EmptyHttpHeaders.INSTANCE;
        CompositeByteBuf allContent = Unpooled.compositeBuffer();
        HttpResponse httpResponse = this.fullHttpResponseMessage.getHttpResponse();
        for (HttpContent httpContent : this.fullHttpResponseMessage.getContentList()) {
            allContent.addComponent(true, httpContent.content());
            if (httpContent instanceof LastHttpContent) {
                trailers = ((LastHttpContent) httpContent).trailingHeaders();
            }
        }
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(httpResponse.protocolVersion(),
                httpResponse.status(), allContent, httpResponse.headers(), trailers);
        return defaultFullHttpResponse;
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
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.waitForConnectionClosureLatch.countDown();
        if (this.latch.getCount() >= 1) {
            this.latch.countDown();
        }
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    void setWaitForConnectionClosureLatch(CountDownLatch waitForConnectionClosureLatch) {
        this.waitForConnectionClosureLatch = waitForConnectionClosureLatch;
    }
}
