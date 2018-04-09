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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Blocking entity collector.
 */
public class BlockingEntityCollector implements EntityCollector {

    private static final Logger LOG = LoggerFactory.getLogger(BlockingEntityCollector.class);

    private int soTimeOut;
    private EntityBodyState state;

    private BlockingQueue<HttpContent> httpContentQueue;
    private Lock readWriteLock;
    private Condition readCondition;

    BlockingEntityCollector(int soTimeOut) {
        this.soTimeOut = soTimeOut;
        this.state = EntityBodyState.EXPECTING;
        this.readWriteLock = new ReentrantLock();
        this.httpContentQueue = new LinkedBlockingQueue<>();
        this.readCondition = readWriteLock.newCondition();
    }

    public void addHttpContent(HttpContent httpContent) {
        try {
            readWriteLock.lock();
            state = EntityBodyState.CONSUMABLE;
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

    public HttpContent getHttpContent() {
        try {
            readWriteLock.lock();
            if (state == EntityBodyState.CONSUMABLE || state == EntityBodyState.EXPECTING) {
                waitForEntity();
                HttpContent httpContent = httpContentQueue.poll();

                if (httpContent instanceof LastHttpContent) {
                    state = EntityBodyState.CONSUMED;
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

    public ByteBuf getMessageBody() {
        HttpContent httpContent = getHttpContent();
        if (httpContent != null) {
            return httpContent.content();
        }
        return null;
    }

    public int getFullMessageLength() {
        int size = 0;
        try {
            readWriteLock.lock();
            List<HttpContent> contentList = new ArrayList<>();
            while (state == EntityBodyState.CONSUMABLE || state == EntityBodyState.EXPECTING) {
                try {
                    waitForEntity();
                    HttpContent httpContent = httpContentQueue.poll();
                    size += httpContent.content().readableBytes();
                    contentList.add(httpContent);
                    if ((httpContent instanceof LastHttpContent)) {
                        state = EntityBodyState.CONSUMED;
                    }
                } catch (InterruptedException e) {
                    LOG.warn("Error while getting full message length", e);
                }
            }
            httpContentQueue.addAll(contentList);
            state = EntityBodyState.CONSUMABLE;
        } catch (Exception e) {
            LOG.error("Error while retrieving http content length", e);
        } finally {
            readWriteLock.unlock();
        }

        return size;
    }

    public int countMessageLengthTill(int maxSize) {
        int size = 0;
        try {
            readWriteLock.lock();
            List<HttpContent> contentList = new ArrayList<>();
            while (state == EntityBodyState.CONSUMABLE || state == EntityBodyState.EXPECTING) {
                try {
                    waitForEntity();
                    HttpContent httpContent = httpContentQueue.poll();
                    size += httpContent.content().readableBytes();
                    contentList.add(httpContent);
                    if (size >= maxSize) {
                        break;
                    } else if ((httpContent instanceof LastHttpContent)) {
                        state = EntityBodyState.CONSUMED;
                    }
                } catch (InterruptedException e) {
                    LOG.warn("Error while getting full message length", e);
                }
            }
            size = 0;
            httpContentQueue.addAll(contentList);
            state = EntityBodyState.CONSUMABLE;
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
            if (state == EntityBodyState.CONSUMABLE) {
                boolean isEndOfMessageProcessed = false;
                while (!isEndOfMessageProcessed) {
                    try {
                        waitForEntity();
                        HttpContent httpContent = httpContentQueue.poll();
                        if (httpContent instanceof LastHttpContent) {
                            isEndOfMessageProcessed = true;
                            state = EntityBodyState.CONSUMED;
                            httpContentQueue.clear();
                        }
                        httpContent.release();
                    } catch (InterruptedException e) {
                        LOG.error("Error while getting content from queue", e);
                    }
                }
            }
            state = EntityBodyState.EXPECTING;
        } catch (Exception e) {
            LOG.error("Error while waiting and releasing the content", e);
        } finally {
            readWriteLock.unlock();
        }
    }

    public boolean isEmpty() {
        try {
            readWriteLock.lock();
            return this.httpContentQueue.isEmpty();
        } finally {
            readWriteLock.unlock();
        }
    }

    public void completeMessage() {
        try {
            readWriteLock.lock();
            if (state == EntityBodyState.EXPECTING) {
                this.addHttpContent(new DefaultLastHttpContent());
            }
        } finally {
            readWriteLock.unlock();
        }
    }
}
