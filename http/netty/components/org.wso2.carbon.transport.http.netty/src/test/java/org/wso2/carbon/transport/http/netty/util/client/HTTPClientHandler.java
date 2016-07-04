/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.util.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class which handles responses for sent requests
 */
public class HTTPClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HTTPClientHandler.class);
    private ResponseCallback responseCallback;

    private Response response;

    public HTTPClientHandler(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpResponse) {
            response = new Response((HttpResponse) msg);
            response.addContent(((FullHttpResponse) msg).content().nioBuffer());

        } else if (msg instanceof HttpResponse) {
            response = new Response((HttpResponse) msg);
        } else if (msg instanceof HttpContent) {
            response.addContent(((HttpContent) msg).content().nioBuffer());
            if (msg instanceof LastHttpContent) {
                responseCallback.received(response);
            }
        }
    }
}
