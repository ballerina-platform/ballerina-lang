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

package org.wso2.transport.http.netty.message;

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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Blocking entity collector
 */
public class BlockingEntityCollector implements EntityCollector {

    private static final Logger LOG = LoggerFactory.getLogger(BlockingEntityCollector.class);

    private int soTimeOut;
    private AtomicBoolean endOfMsgAdded;
    private AtomicBoolean isExpectingEntity;
    private AtomicBoolean isConsumed;
    private BlockingQueue<HttpContent> httpContentQueue;
    private Lock readWriteLock;
    private Condition readCondition;

    public BlockingEntityCollector(int soTimeOut) {
        this.soTimeOut = soTimeOut;
        this.endOfMsgAdded = new AtomicBoolean(false);
        this.isExpectingEntity = new AtomicBoolean(false);
        this.isConsumed = new AtomicBoolean(false);
        this.readWriteLock = new ReentrantLock();
        this.httpContentQueue = new LinkedBlockingQueue<>();
        this.readCondition = readWriteLock.newCondition();
    }

    public void addHttpContent(HttpContent httpContent) {
        try {
            readWriteLock.lock();

            isConsumed.set(false);
            isExpectingEntity.set(false);
            httpContentQueue.add(httpContent);

            readCondition.signalAll();
        } catch (Exception e) {
            LOG.error("Cannot put content to queue", e);
        } finally {
            readWriteLock.unlock();
        }
    }

    public void addMessageBody(ByteBuffer msgBody) {
        addHttpContent(new DefaultHttpContent(Unpooled.copiedBuffer(msgBody)));
    }

    public ByteBuf getMessageBody() {
        HttpContent httpContent = getHttpContent();
        if (httpContent != null) {
            return httpContent.content();
        }
        return null;
    }

    public HttpContent getHttpContent() {
        try {
            readWriteLock.lock();
            // Consumed but expecting entity.
            if (!isConsumed.get() || isExpectingEntity.get()) {
                waitForEntity();
                HttpContent httpContent = httpContentQueue.poll();

                if (httpContent instanceof LastHttpContent) {
                    isConsumed.set(true);
                    httpContentQueue.clear();
                }

                return httpContent;
            }
        } catch (InterruptedException e) {
            LOG.error("Error while retrieving http content from queue", e);
        } finally {
            readWriteLock.unlock();
        }
        return null;
    }

    public int getFullMessageLength() {
        int size = 0;
        try {
            readWriteLock.lock();
            List<HttpContent> contentList = new ArrayList<>();
            boolean isEndOfMessageProcessed = false;
            while (isExpectingEntity.get() || (!isConsumed.get() && !isEndOfMessageProcessed)) {
                try {
                    waitForEntity();
                    HttpContent httpContent = httpContentQueue.poll();
                    if ((httpContent instanceof LastHttpContent)) {
                        isEndOfMessageProcessed = true;
                    }
                    contentList.add(httpContent);

                } catch (InterruptedException e) {
                    LOG.warn("Error while getting full message length", e);
                }
            }
            size = 0;
            for (HttpContent httpContent : contentList) {
                size += httpContent.content().readableBytes();
                httpContentQueue.add(httpContent);
            }
        } catch (Exception e) {
            LOG.error("Error while retrieving http content length", e);
        } finally {
            readWriteLock.unlock();
        }

        return size;
    }

    private void waitForEntity() throws InterruptedException {
        while (httpContentQueue.isEmpty()) {
            if (!readCondition.await(soTimeOut, TimeUnit.SECONDS)) {
                break;
            }
        }
    }

    public void waitAndReleaseAllEntities() {
        try {
            readWriteLock.lock();
            if (!isConsumed.get()) {
                boolean isEndOfMessageProcessed = false;
                while (!isEndOfMessageProcessed) {
                    try {
                        waitForEntity();
                        HttpContent httpContent = httpContentQueue.poll();
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
                        LOG.error("Error while getting content from queue", e);
                    }
                }
            }
            isExpectingEntity.set(true);
        } catch (Exception e) {
            LOG.error("Error while waiting and releasing the content", e);
        } finally {
            readWriteLock.unlock();
        }
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
        this.addHttpContent(new DefaultLastHttpContent());
    }

    public HttpContent peek() {
        return this.httpContentQueue.peek();
    }

    @Deprecated
    public synchronized void release() {
    }
}
