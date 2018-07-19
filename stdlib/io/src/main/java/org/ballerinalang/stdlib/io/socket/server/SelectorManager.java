/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.io.socket.server;

import org.ballerinalang.runtime.threadpool.BLangThreadFactory;
import org.ballerinalang.stdlib.io.events.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This will manage the Selector instance and handle the accept, read and write operations.
 *
 * @since 0.975.1
 */
public class SelectorManager {

    private static final Logger log = LoggerFactory.getLogger(SelectorManager.class);

    private static Selector selector;
    private static boolean running = false;
    private static ThreadFactory threadFactory = new BLangThreadFactory("socket-selector");
    private static ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
    private static boolean execution = true;

    /**
     * Get selector instance.
     *
     * @return {@link Selector} instance.
     * @throws IOException Throws if unable to create a new selector instance.
     */
    public static synchronized Selector getInstance() throws IOException {
        if (selector == null) {
            selector = Selector.open();
        }
        return selector;
    }

    /**
     * Start the selector loop.
     */
    public static void start() {
        SocketAcceptCallbackQueue acceptCallbackQueue = SocketAcceptCallbackQueue.getInstance();
        SocketIOExecutorQueue ioQueue = SocketIOExecutorQueue.getInstance();
        SocketQueue socketQueue = SocketQueue.getInstance();
        if (!running) {
            executor.execute(() -> {
                while (execution) {
                    try {
                        selector.select(2000);
                        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();
                            if (!key.isValid()) {
                                key.cancel();
                                keyIterator.remove();
                            } else if (key.isAcceptable()) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Selector triggered for client accept.");
                                }
                                handleAccept(key, acceptCallbackQueue, socketQueue);
                                keyIterator.remove();
                            } else if (key.isReadable()) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Selector triggered for client read read.");
                                }
                                final boolean readDispatchSuccess = readData(key, ioQueue);
                                if (readDispatchSuccess) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Read ready selection key removed.");
                                    }
                                    keyIterator.remove();
                                }
                            }
                        }
                    } catch (Throwable e) {
                        log.error("An error occurred selector loop: " + e.getMessage(), e);
                    }
                }
            });
            running = true;
        }
    }

    private static void handleAccept(SelectionKey key, SocketAcceptCallbackQueue acceptCallbackQueue,
            SocketQueue socketQueue) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.attachment();
        try {
            final SocketChannel client = serverSocketChannel.accept();
            if (client == null) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("[" + serverSocketChannel + "] <= A new client accepted [" + client + "].");
            }
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            int serverSocketHash = serverSocketChannel.hashCode();
            socketQueue.addSocket(serverSocketHash, client);
            final Queue<SocketAcceptCallback> callbackQueue = acceptCallbackQueue.getCallbackQueue(serverSocketHash);
            if (callbackQueue != null) {
                final SocketAcceptCallback callback = callbackQueue.poll();
                if (callback != null) {
                    callback.notifyAccept();
                    if (log.isDebugEnabled()) {
                        log.debug("[" + serverSocketChannel + "][" + client + "] Notify to the callback.");
                    }
                }
            }
        } catch (Throwable e) {
            log.error("Unable to accept a new client socket connection: " + e.getMessage(), e);
        }
    }

    private static boolean readData(SelectionKey key, SocketIOExecutorQueue ioQueue) {
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        final Queue<EventExecutor> readQueue = ioQueue.getReadQueue(clientSocketChannel.hashCode());
        if (readQueue != null) {
            final EventExecutor eventExecutor = readQueue.poll();
            if (eventExecutor != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Read request available from b7a code. Invoke EventExecutor.");
                }
                eventExecutor.execute();
                return true;
            }
        }
        return false;
    }

    public static void stop() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Stopping the selector loop.");
            }
            execution = false;
            running = false;
            selector.close();
            executor.shutdownNow();
        } catch (Throwable e) {
            log.error("Error occurred while stopping the selector loop: " + e.getMessage(), e);
        }
    }
}
