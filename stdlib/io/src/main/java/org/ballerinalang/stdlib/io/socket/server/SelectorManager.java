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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
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
    private static ThreadFactory blangThreadFactory = new BLangThreadFactory("socket-select");
    private static ExecutorService executor = Executors.newSingleThreadExecutor(blangThreadFactory);

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
        if (!running) {
            executor.execute(() -> {
                while (true) {
                    try {
                        final int readyChannels = selector.select(2000);
                        if (readyChannels == 0) {
                            continue;
                        }
                        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                        while (iter.hasNext()) {
                            SelectionKey key = iter.next();
                            if (key.isAcceptable()) {
                                handleAccept(key);
                            } else if (key.isReadable()) {
                                readData(key);
                            } else if (key.isWritable()) {

                            }
                            iter.remove();
                        }
                    } catch (Throwable e) {
                        log.error("An error occurred: " + e.getMessage(), e);
                    }
                }
            });
            running = true;
        }
    }

    private static void handleAccept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.attachment();
        try {
            final SocketChannel client = serverSocketChannel.accept();
            if (client == null) {
                return;
            }
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            int serverSocketHash = serverSocketChannel.hashCode();
            SocketQueue.addSocket(serverSocketHash, client);
            final Queue<SocketAcceptCallback> callbackQueue = SocketAcceptCallbackQueue
                    .getCallbackQueue(serverSocketHash);
            if (callbackQueue != null) {
                final SocketAcceptCallback callback = callbackQueue.poll();
                if (callback != null) {
                    callback.notifyAccept();
                }
            }
        } catch (IOException e) {
            log.error("Unable to accept a new client socket connection: " + e.getMessage(), e);
        }
    }

    private static void readData(SelectionKey key) {
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        int numRead;
        try {
            numRead = clientSocketChannel.read(buffer);
            if (numRead == -1) {
                key.cancel();
            }
        } catch (IOException e) {
            return;
        }
        buffer.flip();
    }
}
