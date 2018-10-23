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

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.threadpool.BLangThreadFactory;
import org.ballerinalang.stdlib.socket.exceptions.SelectorInitializeException;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedByInterruptException;
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
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_ACCEPT;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_CLOSE;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_READ_READY;
import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;
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
        executor.execute(this::execute);
        running = true;
    }

    private void execute() {
        while (execution) {
            try {
                final int select = selector.select();
                if (select == 0) {
                    continue;
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    performAction(key);
                    keyIterator.remove();
                }
            } catch (Throwable e) {
                log.error("An error occurred in selector loop: " + e.getMessage(), e);
                e.printStackTrace();
            }
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
            client.register(selector, OP_READ, new SocketService(client, socketService.getResources()));
            invokeOnAccept(socketService, client);
        } catch (ClosedByInterruptException e) {
            invokeOnError(socketService, "Client accept interrupt by another process");
        } catch (AsynchronousCloseException e) {
            invokeOnError(socketService, "Client closed by another process");
        } catch (ClosedChannelException e) {
            invokeOnError(socketService, "Client is already closed");
        } catch (IOException e) {
            log.error("An error occurred while accepting new client", e);
            invokeOnError(socketService, "Unable to accept a new client");
        }
    }

    private void invokeOnAccept(SocketService socketService, SocketChannel client) {
        Resource accept = socketService.getResources().get(LISTENER_RESOURCE_ON_ACCEPT);
        ProgramFile programFile = accept.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
        BValue[] params = getAcceptResourceSignature(client, programFile);
        Executor.submit(accept, new TCPSocketCallableUnitCallback(), null, null, params);
    }

    private void onReadReady(SelectionKey key) {
        SocketService socketService = (SocketService) key.attachment();
        try {
            SocketChannel socketChannel = (SocketChannel) socketService.getSocketChannel();
            ByteBuffer buffer = ByteBuffer.allocate(socketChannel.socket().getReceiveBufferSize());
            int read = socketChannel.read(buffer);
            if (read == -1) {
                invokeOnClose(socketService);
            } else {
                invokeReadReady(socketService, buffer);
            }
        } catch (SocketException e) {
            invokeOnError(socketService, "Socket connection is closed");
        } catch (IOException e) {
            log.error("Unable to read from socket", e);
            invokeOnError(socketService, "Unable to read from socket");
        }
    }

    private void invokeOnError(SocketService socketService, String s) {
        Resource error = socketService.getResources().get(LISTENER_RESOURCE_ON_ERROR);
        ProgramFile programFile = error.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
        SocketChannel client = null;
        if (socketService.getSocketChannel() != null) {
            client = (SocketChannel) socketService.getSocketChannel();
        }
        BValue[] params = getOnErrorResourceSignature(client, programFile, s);
        Executor.submit(error, new TCPSocketCallableUnitCallback(), null, null, params);
    }

    private void invokeReadReady(SocketService socketService, ByteBuffer buffer) {
        final Resource readReady = socketService.getResources().get(LISTENER_RESOURCE_ON_READ_READY);
        ProgramFile programFile = readReady.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> endpoint = SocketUtils
                .createCallerAction(programFile, (SocketChannel) socketService.getSocketChannel());
        BValue[] params = { endpoint, new BByteArray(getByteArrayFromByteBuffer(buffer)) };
        Executor.submit(readReady, new TCPSocketCallableUnitCallback(), null, null, params);
    }

    private void invokeOnClose(SocketService socketService) {
        try {
            socketService.getSocketChannel().close();
            unRegisterChannel((SocketChannel) socketService.getSocketChannel());
            final Resource close = socketService.getResources().get(LISTENER_RESOURCE_ON_CLOSE);
            ProgramFile programFile = close.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
            BMap<String, BValue> endpoint = SocketUtils.createCallerAction(programFile,
                    (SocketChannel) socketService.getSocketChannel());
            BValue[] params = { endpoint };
            Executor.submit(close, new TCPSocketCallableUnitCallback(), null, null, params);
        } catch (IOException e) {
            invokeOnError(socketService, "Unable to close the client connection properly");
        }
    }

    private BValue[] getAcceptResourceSignature(SocketChannel client, ProgramFile programFile) {
        BMap<String, BValue> endpoint = SocketUtils.createCallerAction(programFile, client);
        return new BValue[] { endpoint };
    }

    private BValue[] getOnErrorResourceSignature(SocketChannel client, ProgramFile programFile, String msg) {
        BMap<String, BValue> endpoint = SocketUtils.createCallerAction(programFile, client);
        BMap<String, BValue> error = createError(programFile, msg);
        return new BValue[] { endpoint, error };
    }

    private BMap<String, BValue> createError(ProgramFile programFile, String msg) {
        PackageInfo builtInPkg = programFile.getPackageInfo(BALLERINA_BUILTIN_PKG);
        StructureTypeInfo error = builtInPkg.getStructInfo(BLangVMErrors.STRUCT_GENERIC_ERROR);
        return BLangVMStructs.createBStruct(error, msg);
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
