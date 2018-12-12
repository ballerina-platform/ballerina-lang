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

package org.ballerinalang.stdlib.socket.tcp;

import org.ballerinalang.runtime.threadpool.BLangThreadFactory;
import org.ballerinalang.stdlib.socket.exceptions.SelectorInitializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * This will manage the Selector instance and handle the accept, read and write operations.
 *
 * @since 0.985.0
 */
public class SelectorManager {

    private static final Logger log = LoggerFactory.getLogger(SelectorManager.class);

    private Selector selector;
    private boolean running = false;
    private ThreadFactory threadFactory = new BLangThreadFactory("socket-selector");
    private ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
    private boolean executing = true;
    private ConcurrentLinkedQueue<ChannelRegisterCallback> registerPendingSockets = new ConcurrentLinkedQueue<>();
    private final Object startStopLock = new Object();

    private SelectorManager() throws IOException {
        selector = Selector.open();
    }

    /**
     * This will use to hold the SelectorManager singleton object.
     */
    private static class SelectorManagerHolder {
        private static SelectorManager manager;
        static {
            try {
                manager = new SelectorManager();
            } catch (IOException e) {
                throw new SelectorInitializeException("Unable to initialize the selector", e);
            }
        }
    }

    /**
     * This method will return SelectorManager singleton instance.
     *
     * @return {@link SelectorManager} instance
     * @throws SelectorInitializeException when unable to open a selector
     */
    public static SelectorManager getInstance() throws SelectorInitializeException {
        return SelectorManagerHolder.manager;
    }

    /**
     * Add channel to register pending socket queue. Socket registration has to be happen in the same thread
     * that selector loop execute.
     *
     * @param callback A {@link ChannelRegisterCallback} instance which contains the resources,
     *                      packageInfo and A {@link SelectableChannel}.
     */
    public void registerChannel(ChannelRegisterCallback callback) {
        registerPendingSockets.add(callback);
        selector.wakeup();
    }

    /**
     * Unregister the given client channel from the selector instance.
     *
     * @param channel {@link AbstractSelectableChannel} that about to unregister.
     */
    public void unRegisterChannel(AbstractSelectableChannel channel) {
        final SelectionKey selectionKey = channel.keyFor(selector);
        if (selectionKey != null) {
            selectionKey.cancel();
        }
    }

    /**
     * Start the selector loop.
     */
    public void start() {
        synchronized (startStopLock) {
            if (running) {
                return;
            }
            executor.execute(this::execute);
            running = true;
        }
    }

    private void execute() {
        while (executing) {
            try {
                registerChannels();
                final int select = selector.select();
                if (select == 0) {
                    continue;
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    performAction(key);
                }
            } catch (Throwable e) {
                log.error("An error occurred in selector loop: " + e.getMessage(), e);
            }
        }
    }

    /*
    Channel registration has to be done in the same thread that selector loops runs.
     */
    private void registerChannels() {
        ChannelRegisterCallback channelRegisterCallback;
        while ((channelRegisterCallback = registerPendingSockets.poll()) != null) {
            try {
                SocketService socketService = channelRegisterCallback.getSocketService();
                socketService.getSocketChannel()
                        .register(selector, channelRegisterCallback.getInitialInterest(), socketService);
            } catch (ClosedChannelException e) {
                channelRegisterCallback.notifyFailure("Socket already closed");
            }
            channelRegisterCallback
                    .notifyRegister(channelRegisterCallback.getInitialInterest() == SelectionKey.OP_READ);
        }
    }

    private void performAction(SelectionKey key) {
        if (!key.isValid()) {
            key.cancel();
        } else if (key.isAcceptable()) {
            onAccept(key);
        } else if (key.isReadable()) {
            onReadReady(key);
        }
    }

    private void onAccept(SelectionKey key) {
        SocketService socketService = (SocketService) key.attachment();
        try {
            ServerSocketChannel server = (ServerSocketChannel) socketService.getSocketChannel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            // Registering the channel against the selector directly without going through the queue,
            // since we are in same thread.
            client.register(selector, OP_READ, new SocketService(client, socketService.getResources()));
            SelectorDispatcher.invokeOnAccept(socketService, client);
        } catch (ClosedByInterruptException e) {
            SelectorDispatcher.invokeOnError(socketService, "Client accept interrupt by another process");
        } catch (AsynchronousCloseException e) {
            SelectorDispatcher.invokeOnError(socketService, "Client closed by another process");
        } catch (ClosedChannelException e) {
            SelectorDispatcher.invokeOnError(socketService, "Client is already closed");
        } catch (IOException e) {
            log.error("An error occurred while accepting new client", e);
            SelectorDispatcher.invokeOnError(socketService, "Unable to accept a new client");
        }
    }

    private void onReadReady(SelectionKey key) {
        SocketService socketService = (SocketService) key.attachment();
        try {
            SocketChannel socketChannel = (SocketChannel) socketService.getSocketChannel();
            ByteBuffer buffer = ByteBuffer.allocate(socketChannel.socket().getReceiveBufferSize());
            int read = socketChannel.read(buffer);
            if (read == -1) {
                unRegisterChannel((SocketChannel) socketService.getSocketChannel());
                SelectorDispatcher.invokeOnClose(socketService);
            } else {
                SelectorDispatcher.invokeReadReady(socketService, buffer);
            }
        } catch (SocketException e) {
            SelectorDispatcher.invokeOnError(socketService, "Socket connection is closed");
        } catch (IOException e) {
            log.error("Unable to read from socket", e);
            SelectorDispatcher.invokeOnError(socketService, "Unable to read from socket");
        }
    }

    /**
     * Stop the selector loop.
     */
    public void stop() {
        synchronized (startStopLock) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Stopping the selector loop.");
                }
                selector.wakeup();
                executing = false;
                running = false;
                selector.close();
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
            } catch (Throwable e) {
                log.error("Error occurred while stopping the selector loop: " + e.getMessage(), e);
            }
        }
    }
}
