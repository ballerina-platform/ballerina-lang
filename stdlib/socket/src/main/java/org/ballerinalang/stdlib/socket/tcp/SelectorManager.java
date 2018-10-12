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
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.threadpool.BLangThreadFactory;
import org.ballerinalang.stdlib.socket.exceptions.SelectorInitializeException;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.ballerinalang.stdlib.socket.SocketConstants.CALLER_ACTION;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_ACCEPT;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_CLOSE;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_READ_READY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static java.nio.channels.SelectionKey.OP_READ;

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
     * Register the given SelectableChannel instance like ServerSocketChannel or SocketChannel in the selector instance.
     *
     * @param socketService A {@link SocketService} instance which contains the resources,
     *                      packageInfo and A {@link SelectableChannel}.
     * @param interest The interest set for the resulting key
     * @throws ClosedChannelException       {@inheritDoc}
     * @throws CancelledKeyException        {@inheritDoc}
     */
    public void registerChannel(SocketService socketService, int interest) throws ClosedChannelException {
        SelectableChannel channel = socketService.getSocketChannel();
        channel.register(selector, interest, socketService);
    }

    /**
     * Register the given SelectableChannel instance like ServerSocketChannel or SocketChannel in the selector instance.
     *
     * @param channel  A {@link SocketChannel} instance which interest to register against the given ops.
     * @param interest The interest set for the resulting key
     * @throws ClosedChannelException       {@inheritDoc}
     * @throws CancelledKeyException        {@inheritDoc}
     */
    public void registerChannel(SocketChannel channel, int interest) throws ClosedChannelException {
        channel.register(selector, interest);
    }

    /**
     * Unregister the given client channel from the selector instance.
     *
     * @param channel {@link SocketChannel} that about to unregister.
     */
    public void unRegisterChannel(SocketChannel channel) {
        final SelectionKey selectionKey = channel.keyFor(selector);
        if (selectionKey != null) {
            selectionKey.cancel();
        }
    }

    /**
     * Start the selector loop.
     */
    public void start() {
        if (running) {
            return;
        }
        executor.execute(() -> {
            while (execution) {
                try {
                    selector.select();
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        performAction(key);
                        keyIterator.remove();
                    }
                } catch (Throwable e) {
                    log.error("An error occurred in selector loop: " + e.getMessage(), e);
                }
            }
        });
        running = true;
    }

    private void performAction(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            key.cancel();
        } else if (key.isAcceptable()) {
            onAccept(key);
        } else if (key.isReadable()) {
            onReadReady(key);
        }
    }

    private void onAccept(SelectionKey key) throws IOException {
        SocketService socketService = (SocketService) key.attachment();
        ServerSocketChannel server = (ServerSocketChannel) socketService.getSocketChannel();
        final SocketChannel client = server.accept();
        client.configureBlocking(false);
        SocketService clientService = new SocketService(client, socketService.getResources());
        client.register(selector, OP_READ, clientService);
        final Resource acceptResource = socketService.getResources().get(LISTENER_RESOURCE_ON_ACCEPT);
        ProgramFile programFile = acceptResource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
        BValue[] signatureParams = getAcceptMethodSignature(client, programFile);
        Executor.submit(acceptResource, new TCPSocketCallableUnitCallback(), null, null, signatureParams);
    }

    private void onReadReady(SelectionKey key) throws IOException {
        SocketService socketService = (SocketService) key.attachment();
        final Resource readReadyResource = socketService.getResources().get(LISTENER_RESOURCE_ON_READ_READY);
        ProgramFile programFile = readReadyResource.getResourceInfo().getServiceInfo().getPackageInfo()
                .getProgramFile();
        SocketChannel socketChannel = (SocketChannel) socketService.getSocketChannel();
        BMap<String, BValue> tcpSocketMeta = getTcpSocketMeta(programFile, socketChannel);
        ByteBuffer buffer = ByteBuffer.allocate(socketChannel.socket().getReceiveBufferSize() * 2);
        int read = socketChannel.read(buffer);
        if (read == -1) {
            socketChannel.close();
            unRegisterChannel(socketChannel);
            BValue[] signatureParams = { tcpSocketMeta };
            final Resource onCloseResource = socketService.getResources().get(LISTENER_RESOURCE_ON_CLOSE);
            Executor.submit(onCloseResource, new TCPSocketCallableUnitCallback(), null, null, signatureParams);
        } else {
            BMap<String, BValue> endpoint = getCallerAction(programFile, socketService.getSocketChannel());
            BValue[] signatureParams = { endpoint, tcpSocketMeta, new BByteArray(getByteArrayFromByteBuffer(buffer)) };
            Executor.submit(readReadyResource, new TCPSocketCallableUnitCallback(), null, null, signatureParams);
        }
    }

    private BValue[] getAcceptMethodSignature(SocketChannel client, ProgramFile programFile) {
        BMap<String, BValue> tcpSocketMeta = getTcpSocketMeta(programFile, client);
        BMap<String, BValue> endpoint = getCallerAction(programFile, client);
        return new BValue[] { endpoint, tcpSocketMeta };
    }

    private BMap<String, BValue> getCallerAction(ProgramFile programFile, SelectableChannel client) {
        BMap<String, BValue> callerEndpoint = BLangConnectorSPIUtil
                .createBStruct(programFile, SOCKET_PACKAGE, CALLER_ACTION);
        callerEndpoint.addNativeData(SOCKET_KEY, client);
        BMap<String, BValue> endpoint = BLangConnectorSPIUtil.createBStruct(programFile, SOCKET_PACKAGE, "Listener");
        endpoint.put("callerAction", callerEndpoint);
        return endpoint;
    }

    private BMap<String, BValue> getTcpSocketMeta(ProgramFile programFile, SocketChannel socketChannel) {
        Socket socket = socketChannel.socket();
        int remotePort = socket.getPort();
        int localPort = socket.getLocalPort();
        String remoteHost = socket.getInetAddress().getHostAddress();
        String localHost = socket.getLocalAddress().getHostAddress();
        Object[] fields = { remotePort, localPort, remoteHost, localHost };
        return BLangConnectorSPIUtil.createBStruct(programFile, SOCKET_PACKAGE, "TCPSocketMeta", fields);
    }

    private byte[] getByteArrayFromByteBuffer(ByteBuffer content) {
        int contentLength = content.position();
        byte[] bytesArray = new byte[contentLength];
        content.flip();
        content.get(bytesArray, 0, contentLength);
        return bytesArray;
    }

    /**
     * Stop the selector loop.
     */
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
