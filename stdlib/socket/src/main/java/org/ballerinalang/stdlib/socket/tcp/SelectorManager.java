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

import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.threadpool.BLangThreadFactory;
import org.ballerinalang.stdlib.io.events.EventExecutor;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.tcp.client.SocketConnectCallbackRegistry;
import org.ballerinalang.stdlib.socket.tcp.server.SocketAcceptCallback;
import org.ballerinalang.stdlib.socket.tcp.server.SocketAcceptCallbackQueue;
import org.ballerinalang.stdlib.socket.tcp.server.SocketIOExecutorQueue;
import org.ballerinalang.stdlib.socket.tcp.server.SocketQueue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectableChannel;
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

    private Selector selector;
    private boolean running = false;
    private ThreadFactory threadFactory = new BLangThreadFactory("socket-selector");
    private ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
    private boolean execution = true;

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
                throw new BallerinaException(e);
            }
        }
    }

    /**
     * This method will return SelectorManager singleton instance.
     *
     * @return {@link SelectorManager} instance
     * @throws BallerinaException when unable to open a selector
     */
    public static SelectorManager getInstance() throws BallerinaException {
        return SelectorManagerHolder.manager;
    }

    /**
     * Register the given SelectableChannel instance like ServerSocketChannel or SocketChannel in the selector instance.
     *
     * @param socketService A {@link SocketService} instance which contains the resources,
     *                      packageInfo and A {@link SelectableChannel}.
     * @throws ClosedChannelException       {@inheritDoc}
     * @throws IllegalBlockingModeException {@inheritDoc}
     * @throws CancelledKeyException        {@inheritDoc}
     * @throws IllegalArgumentException     {@inheritDoc}
     */
    public void registerChannel(SocketService socketService) throws ClosedChannelException {
        SelectableChannel channel = socketService.getSocketChannel();
        int ops = SelectionKey.OP_ACCEPT;
        if (channel instanceof SocketChannel) {
            ops = SelectionKey.OP_CONNECT;
        }
        channel.register(selector, ops, socketService);
    }

    /**
     * Start the selector loop.
     */
    public void start() {
        if (!running) {
            SocketAcceptCallbackQueue acceptCallbackQueue = SocketAcceptCallbackQueue.getInstance();
            SocketIOExecutorQueue ioQueue = SocketIOExecutorQueue.getInstance();
            SocketQueue socketQueue = SocketQueue.getInstance();
            SocketConnectCallbackRegistry connectCallbackRegistry = SocketConnectCallbackRegistry.getInstance();
            executor.execute(() -> {
                while (execution) {
                    try {
                        selector.select(2000);
                        if (!selector.isOpen()) {
                            break;
                        }
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
                                SocketService socketService = (SocketService) key.attachment();
                                ServerSocketChannel channel = (ServerSocketChannel) socketService.getSocketChannel();
                                final SocketChannel client = channel.accept();
                                SocketService clientService = new SocketService(client, socketService.getResources());
                                client.configureBlocking(false);
                                client.register(selector, SelectionKey.OP_READ, clientService);
                                final Resource acceptResource = socketService.getResources()
                                        .get(SocketConstants.LISTENER_RESOURCE_ON_ACCEPT);
                                ProgramFile programFile = acceptResource.getResourceInfo().getServiceInfo()
                                        .getPackageInfo().getProgramFile();
                                final Socket socket = client.socket();
                                final int remotePort = socket.getPort();
                                final int localPort = socket.getLocalPort();
                                final String remoteHost = socket.getInetAddress().getHostAddress();
                                final String localHost = socket.getLocalAddress().getHostAddress();
                                final BMap<String, BValue> tcpSocketMeta = BLangConnectorSPIUtil
                                        .createBStruct(programFile, SocketConstants.SOCKET_PACKAGE, "TCPSocketMeta",
                                                remotePort, localPort, remoteHost, localHost);
                                Executor.submit(acceptResource, new TCPSocketCallableUnitCallback(), null, null,
                                        tcpSocketMeta);
                            } else if (key.isReadable()) {
//                                if (log.isDebugEnabled()) {
//                                    log.debug("Selector triggered for client read ready.");
//                                }
//                                final boolean readDispatchSuccess = readData(key, ioQueue);
//                                if (readDispatchSuccess) {
//                                    if (log.isDebugEnabled()) {
//                                        log.debug("Read ready selection key removed.");
//                                    }
//                                    keyIterator.remove();
//                                }
                            } else if (key.isConnectable()) {
//                                if (log.isDebugEnabled()) {
//                                    log.debug("Selector triggered for socket connectable.");
//                                }
//                                if (isConnectPending(connectCallbackRegistry, key)) {
//                                    continue;
//                                }
                            }
                            keyIterator.remove();
                        }
                    } catch (Throwable e) {
                        log.error("An error occurred in selector loop: " + e.getMessage(), e);
                    }
                }
            });
            running = true;
        }
    }

    private boolean isConnectPending(SocketConnectCallbackRegistry connectCallbackRegistry, SelectionKey key)
            throws IOException {
        SocketChannel channel = (SocketChannel) key.attachment();
        if (!channel.finishConnect()) {
            return true;
        }
        log.debug("Successfully connected to the remote server.");
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        connectCallbackRegistry.getCallback(channel.hashCode()).notifyConnect();
        return false;
    }

    private void handleAccept(SelectionKey key, SocketAcceptCallbackQueue acceptCallbackQueue,
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

    private boolean readData(SelectionKey key, SocketIOExecutorQueue ioQueue) {
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

    public void stop() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Stopping the selector loop.");
            }
            execution = false;
            running = false;
            selector.wakeup();
            selector.close();
            Thread.sleep(1500);
            executor.shutdownNow();
        } catch (Throwable e) {
            log.error("Error occurred while stopping the selector loop: " + e.getMessage(), e);
        }
    }
}
