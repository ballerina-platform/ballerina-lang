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

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.NettyCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Util;

import java.nio.ByteBuffer;

/**
 * A Class responsible for handling the response.
 */
public class ResponseCallback implements CarbonCallback {

    private ChannelHandlerContext ctx;

    private static final Logger LOG = LoggerFactory.getLogger(ResponseCallback.class);

    public ResponseCallback(ChannelHandlerContext channelHandlerContext) {
        this.ctx = channelHandlerContext;
    }

    public void done(CarbonMessage cMsg) {
        final HttpResponse response = Util.createHttpResponse(cMsg);
        ctx.write(response);
        while (true) {
            if (cMsg instanceof NettyCarbonMessage) {
                HttpContent httpContent = ((NettyCarbonMessage) cMsg).getHttpContent();
                if (httpContent instanceof LastHttpContent) {
                    ctx.writeAndFlush(httpContent);
                    break;
                }
                ctx.write(httpContent);

            } else {
                ByteBuffer byteBuffer = cMsg.getMessageBody();
                ByteBuf bbuf = Unpooled.copiedBuffer(byteBuffer);
                DefaultHttpContent httpContent = new DefaultHttpContent(bbuf);
                ctx.write(httpContent);
                if (cMsg.isEomAdded() && cMsg.isEmpty()) {
                    ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    break;

                }

            }
        }
    }
}
