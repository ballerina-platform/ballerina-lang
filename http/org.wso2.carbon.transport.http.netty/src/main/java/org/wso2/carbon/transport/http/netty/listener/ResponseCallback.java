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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.nio.ByteBuffer;

/**
 * A Class responsible for handling the response.
 */
public class ResponseCallback implements CarbonCallback {

    private ChannelHandlerContext ctx;

    private static final Logger logger = LoggerFactory.getLogger(ResponseCallback.class);
    private static final String HTTP_CONNECTION_CLOSE = "close";

    public ResponseCallback(ChannelHandlerContext channelHandlerContext) {
        this.ctx = channelHandlerContext;
    }

    public void done(CarbonMessage cMsg) {
        handleResponsesWithoutContentLength(cMsg);
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceResponseReceiving(cMsg);
        }
        final HttpResponse response = Util.createHttpResponse(cMsg);
        ctx.write(response);

        if (!cMsg.isBufferContent()) {
            cMsg.setWriter(new ResponseContentWriter(ctx));
        } else {

            if (cMsg instanceof HTTPCarbonMessage) {
                HTTPCarbonMessage nettyCMsg = (HTTPCarbonMessage) cMsg;
                while (true) {
                    if (nettyCMsg.isEndOfMsgAdded() && nettyCMsg.isEmpty()) {
                        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                        break;
                    }
                    HttpContent httpContent = nettyCMsg.getHttpContent();
                    if (httpContent instanceof LastHttpContent) {
                        ctx.writeAndFlush(httpContent);
                        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                    executeAtSourceResponseSending(cMsg);
                        }
                        break;
                    }
                    ctx.write(httpContent);
                }
            } else if (cMsg instanceof DefaultCarbonMessage) {
                DefaultCarbonMessage defaultCMsg = (DefaultCarbonMessage) cMsg;
                if (defaultCMsg.isEndOfMsgAdded() && defaultCMsg.isEmpty()) {
                    ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    return;
                }
                while (true) {
                    ByteBuffer byteBuffer = defaultCMsg.getMessageBody();
                    ByteBuf bbuf = Unpooled.wrappedBuffer(byteBuffer);
                    DefaultHttpContent httpContent = new DefaultHttpContent(bbuf);
                    ctx.write(httpContent);
                    if (defaultCMsg.isEndOfMsgAdded() && defaultCMsg.isEmpty()) {
                        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                    executeAtSourceResponseSending(cMsg);
                        }
                        String connection = cMsg.getHeader(Constants.HTTP_CONNECTION);
                        if (connection != null && HTTP_CONNECTION_CLOSE.equalsIgnoreCase(connection)) {
                            future.addListener(ChannelFutureListener.CLOSE);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void handleResponsesWithoutContentLength(CarbonMessage cMsg) {
        if (cMsg.isAlreadyRead()) {
            MessageDataSource messageDataSource = cMsg.getMessageDataSource();
            if (messageDataSource != null) {
                messageDataSource.serializeData();
                cMsg.setEndOfMsgAdded(true);
                cMsg.getHeaders().remove(Constants.HTTP_CONTENT_LENGTH);
            } else {
                logger.error("Message is already built but cannot find the MessageDataSource");
            }
        }
        if (cMsg.getHeader(Constants.HTTP_TRANSFER_ENCODING) == null
                && cMsg.getHeader(Constants.HTTP_CONTENT_LENGTH) == null) {
            cMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(cMsg.getFullMessageLength()));

        }
    }
}
