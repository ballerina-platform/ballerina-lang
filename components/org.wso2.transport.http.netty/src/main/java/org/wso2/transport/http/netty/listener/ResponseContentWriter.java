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

package org.wso2.transport.http.netty.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Writer;

import java.nio.ByteBuffer;

/**
 * A class which wraps Inbound Channel Handler ctx and write content directly to netty IO works.
 */
public class ResponseContentWriter implements Writer {
    // TODO: This class not needed anymore. This is here only for references purposes and will remove in next release.

    private ChannelHandlerContext channelHandlerContext;

    private static final String HTTP_CONNECTION_CLOSE = "close";

    public ResponseContentWriter(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    @Override
    public void write(ByteBuffer byteBuffer) {
        ByteBuf bbuf = Unpooled.copiedBuffer(byteBuffer);
        DefaultHttpContent httpContent = new DefaultHttpContent(bbuf);
        this.channelHandlerContext.write(httpContent);
    }

    @Override
    public void writeLastContent(CarbonMessage carbonMessage) {
        ChannelFuture future = channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        String connection = carbonMessage.getHeader(HttpHeaderNames.CONNECTION.toString());
        if (connection != null && HTTP_CONNECTION_CLOSE.equalsIgnoreCase(connection)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
