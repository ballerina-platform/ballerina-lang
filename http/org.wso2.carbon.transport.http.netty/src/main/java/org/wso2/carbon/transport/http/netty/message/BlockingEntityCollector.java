/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Blocking entity collector
 */
public class BlockingEntityCollector implements EntityCollector {

    private static final Logger LOG = LoggerFactory.getLogger(BlockingEntityCollector.class);

    private int soTimeOut;
    private AtomicBoolean alreadyRead;
    private AtomicBoolean endOfMsgAdded;
    private AtomicBoolean isConsumed;
    private BlockingQueue<HttpContent> httpContentQueue;

    public BlockingEntityCollector(int soTimeOut) {
        this.soTimeOut = soTimeOut;
        this.alreadyRead = new AtomicBoolean(false);
        this.endOfMsgAdded = new AtomicBoolean(false);
        this.isConsumed = new AtomicBoolean(false);
        this.httpContentQueue = new LinkedBlockingQueue<>();
    }

    public void addHttpContent(HttpContent httpContent) {
        try {
            isConsumed.set(false);
            httpContentQueue.add(httpContent);
        } catch (Exception e) {
            LOG.error("Cannot put content to queue", e);
        }
    }

    public HttpContent getHttpContent() {
        try {
            if (!isConsumed.get() || !alreadyRead.get()) {
                HttpContent httpContent = httpContentQueue.poll(soTimeOut, TimeUnit.SECONDS);

                if (httpContent instanceof LastHttpContent) {
                    isConsumed.set(true);
                    alreadyRead.set(false);
                    httpContentQueue.clear();
                }

                return httpContent;
            }
        } catch (InterruptedException e) {
            LOG.error("Error while retrieving http content from queue.", e);
        }
        return null;
    }

    public void addMessageBody(ByteBuffer msgBody) {
        isConsumed.set(false);
        httpContentQueue.add(new DefaultHttpContent(Unpooled.copiedBuffer(msgBody)));
    }

    public ByteBuf getMessageBody() {
        HttpContent httpContent = getHttpContent();
        if (httpContent != null) {
            return httpContent.content();
        }
        return null;
    }

    @Deprecated
    public List<ByteBuffer> getFullMessageBody() {
        List<ByteBuffer> byteBufferList = new ArrayList<>();

        if (!isConsumed.get()) {
            boolean isEndOfMessageProcessed = false;
            while (!isEndOfMessageProcessed) {
                try {
                    HttpContent httpContent = httpContentQueue.poll(soTimeOut, TimeUnit.SECONDS);
                    // This check is to make sure we add the last http content after getClone and avoid adding
                    // empty content to bytebuf list again and again
                    if (httpContent instanceof EmptyLastHttpContent) {
                        break;
                    }

                    if (httpContent instanceof LastHttpContent) {
                        isEndOfMessageProcessed = true;
                        isConsumed.set(true);
                        httpContentQueue.clear();
                    }
                    ByteBuf buf = httpContent.content();
                    byteBufferList.add(buf.nioBuffer());
                } catch (InterruptedException e) {
                    LOG.error("Error while getting full message body", e);
                }
            }
        }

        return byteBufferList;
    }

    public void waitAndReleaseAllEntities() {
        if (!isConsumed.get() && !alreadyRead.get()) {
            boolean isEndOfMessageProcessed = false;
            while (!isEndOfMessageProcessed) {
                try {
                    HttpContent httpContent = httpContentQueue.poll(soTimeOut, TimeUnit.SECONDS);
                    // This check is to make sure we add the last http content after getClone and avoid adding
                    // empty content to bytebuf list again and again
                    if (httpContent instanceof EmptyLastHttpContent) {
                        break;
                    }

                    if (httpContent instanceof LastHttpContent) {
                        isEndOfMessageProcessed = true;
                        isConsumed.set(true);
                    }
                    httpContent.release();
                } catch (InterruptedException e) {
                    LOG.error("Error while getting full message body", e);
                }
            }
        }
    }

    public int getFullMessageLength() {
        List<HttpContent> contentList = new ArrayList<>();
        boolean isEndOfMessageProcessed = false;
        while (!isEndOfMessageProcessed) {
            try {
                HttpContent httpContent = httpContentQueue.poll(soTimeOut, TimeUnit.SECONDS);
                if ((httpContent instanceof LastHttpContent)) {
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

    public boolean isEmpty() {
        return this.httpContentQueue.isEmpty();
    }

    public boolean isEndOfMsgAdded() {
        return endOfMsgAdded.get();
    }

    public void markMessageEnd() {
        httpContentQueue.add(new EmptyLastHttpContent());
    }

    public void setEndOfMsgAdded(boolean endOfMsgAdded) {
        this.endOfMsgAdded.compareAndSet(false, endOfMsgAdded);
        this.httpContentQueue.add(new DefaultLastHttpContent());
    }

    @Deprecated
    public synchronized void release() {
    }

    // TODO: Need to move below two to ballerina code
    public boolean isAlreadyRead() {
        return alreadyRead.get();
    }

    public void setAlreadyRead(boolean alreadyRead) {
        this.alreadyRead.set(alreadyRead);
    }
}
