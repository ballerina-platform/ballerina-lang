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

package org.ballerinalang.nativeimpl.socket;

import org.ballerinalang.nativeimpl.socket.server.SocketAcceptCallback;
import org.ballerinalang.nativeimpl.socket.server.SocketAcceptCallbackQueue;
import org.ballerinalang.nativeimpl.socket.server.SocketQueue;
import org.ballerinalang.runtime.threadpool.BLangThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @since 0.971.1
 */
public class SelectorManager {

    private static final Logger log = LoggerFactory.getLogger(SelectorManager.class);

    private static Selector selector;
    private static boolean running = false;
    private static ThreadFactory factory = new BLangThreadFactory("socket-select");
    private static ExecutorService executor = Executors.newSingleThreadExecutor(factory);

    public static Selector getInstance() throws IOException {
        if (selector == null) {
            selector = Selector.open();
        }
        return selector;
    }

    public static void start() {
        if (!running) {
            executor.execute(() -> {
                while (true) {
                    try {
                        selector.select();
                    } catch (IOException e) {
                        log.error("An error occurred in selector loop: " + e.getMessage(), e);
                        continue;
                    }
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectedKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        }
                        if (key.isReadable()) {
                        }
                        iter.remove();
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
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            int serverSocketHash = serverSocketChannel.hashCode();
            SocketQueue.addSocket(serverSocketHash, client);
            final Queue<SocketAcceptCallback> callbackQueue = SocketAcceptCallbackQueue
                    .getCallbackQueue(serverSocketHash);
            if (callbackQueue != null) {
                final SocketAcceptCallback callback = callbackQueue.poll();
                callback.notifyAccept();
            }
        } catch (IOException e) {
            log.error("Unable to accept new client socket connection: " + e.getMessage(), e);
        }
    }
}
