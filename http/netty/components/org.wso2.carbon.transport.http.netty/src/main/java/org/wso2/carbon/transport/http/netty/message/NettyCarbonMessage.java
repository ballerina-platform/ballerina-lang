/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Netty based implementation for Carbon Message.
 */
public class NettyCarbonMessage extends CarbonMessage {

    private static final Logger LOG = LoggerFactory.getLogger(NettyCarbonMessage.class);

    private BlockingQueue<HttpContent> httpContentQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<HttpContent> outContentQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<HttpContent> garbageCollected = new LinkedBlockingQueue<>();

    public void addHttpContent(HttpContent httpContent) {
        try {
            httpContentQueue.put(httpContent);
        } catch (InterruptedException e) {
            LOG.error("Cannot put content to queue", e);
        }
    }

    public HttpContent getHttpContent() {
        try {
            return httpContentQueue.take();
        } catch (InterruptedException e) {
            LOG.error("Error while retrieving http content from queue.", e);
            return null;
        }
    }

    @Override
    public ByteBuffer getMessageBody() {
        try {
            HttpContent httpContent = httpContentQueue.take();
            ByteBuf buf = httpContent.content();
            garbageCollected.add(httpContent);
            return buf.nioBuffer();
        } catch (InterruptedException e) {
            LOG.error("Error while retrieving message body from queue.", e);
            return null;
        }
    }

    @Override
    public List<ByteBuffer> getFullMessageBody() {
        List<ByteBuffer> byteBufferList = new ArrayList<>();

        boolean isEndOfMessageProcessed = false;
        while (!isEndOfMessageProcessed) {
            try {
                HttpContent httpContent = httpContentQueue.take();
                if (httpContent instanceof LastHttpContent) {
                    isEndOfMessageProcessed = true;
                }
                ByteBuf buf = httpContent.content();
                garbageCollected.add(httpContent);
                byteBufferList.add(buf.nioBuffer());
            } catch (InterruptedException e) {
                LOG.error("Error while getting full message body", e);
            }
        }

        return byteBufferList;
    }

    @Override
    public boolean isEmpty() {
        return this.httpContentQueue.isEmpty();
    }

    @Override
    public int getFullMessageLength() {
        List<HttpContent> contentList = new ArrayList<>();
        boolean isEndOfMessageProcessed = false;
        while (!isEndOfMessageProcessed) {
            try {
                HttpContent httpContent = httpContentQueue.take();
                if (httpContent instanceof LastHttpContent) {
                    isEndOfMessageProcessed = true;
                }
                contentList.add(httpContent);

            } catch (InterruptedException e) {
                LOG.error("Error while getting full message length", e);
            }
        }
        int size = 0;
        for (HttpContent httpContent : contentList) {
            size += httpContent.content().readableBytes();
            httpContentQueue.add(httpContent);
        }

        return size;
    }

    @Override
    public void addMessageBody(ByteBuffer msgBody) {
        if (isAlreadyRead()) {
            outContentQueue.add(new DefaultHttpContent(Unpooled.copiedBuffer(msgBody)));
        } else if (httpContentQueue.isEmpty()) {
            httpContentQueue.add(new DefaultHttpContent(Unpooled.copiedBuffer(msgBody)));
        } else {
            LOG.error("Please don't add message body before reading existing values");
        }

    }

    @Override
    public void setEndOfMsgAdded(boolean endOfMsgAdded) {
        super.setEndOfMsgAdded(endOfMsgAdded);
        if (isAlreadyRead()) {
            outContentQueue.forEach(buffer -> {
                httpContentQueue.add(buffer);
            });
            outContentQueue.clear();
        }

    }

    @Override
    public void release() {
        httpContentQueue.forEach(content -> content.release());
        garbageCollected.forEach(content -> content.release());
    }
}
