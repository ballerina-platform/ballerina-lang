/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.tcp;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.AbstractNativeChannel;
import org.ballerinalang.nativeimpl.io.channels.BSocketChannel;
import org.ballerinalang.nativeimpl.io.channels.base.BByteChannel;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Native function ballerina.tcp#OpenChannel
 *
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "ballerina.tcp",
        functionName = "openChannel",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Socket", structPackage = "ballerina.tcp"),
        args = {@Argument(name = "permission", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io")},
        isPublic = true
)
public class OpenChannel extends AbstractNativeChannel {

    /**
     * Specifies the address in ballerina.tcp#openChannel
     */
    private static final int ADDRESS_INDEX = 0;

    /**
     * Specifies the destination in ballerina.tcp#openChannel
     */
    private static final int DESTINATION_INDEX = 0;

    /**
     * Specified the port in ballerina.tcp#openChannel
     */
    private static final int PORT_INDEX = 0;

    /**
     * Specifies the permission in ballerina.tcp#openChannel
     */
    private static final int PERMISSION_INDEX = 0;

    /**
     * Creates a tcp socket for the relevant destination
     *
     * @param address destination ip address
     * @param port    destination TCP port
     * @return I/O channel representation
     */
    private SocketChannel createTcpSocketForWriting(String address, int port) throws IOException {
        InetSocketAddress destinationAddress = new InetSocketAddress(address, port);
        return SocketChannel.open(destinationAddress);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
       // Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * <p>
     * Will listen to a connection in the given port
     * </p>
     * <p>
     * <b>Note : </b> This is a blocking call
     * </p>
     *
     * @param address destination ip address
     * @param port    Port which will accept messages
     * @return I/O channel representation
     */
    private ByteChannel createTcpSocketForReading(String address, int port) throws IOException {
        Selector tcpSocketSelector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        InetSocketAddress destinationAddress = new InetSocketAddress(address, port);

        server.bind(destinationAddress);

        server.configureBlocking(false);

        server.register(tcpSocketSelector, SelectionKey.OP_ACCEPT);

        while (true) {

            tcpSocketSelector.select();

            // Iterate over the set of keys for which events are available
            Iterator selectedKeys = tcpSocketSelector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = (SelectionKey) selectedKeys.next();
                selectedKeys.remove();

                if (!key.isValid()) {
                    continue;
                }

                // Check what event is available and deal with it
                if (key.isAcceptable()) {
                    accept(tcpSocketSelector, key);
                } else if (key.isReadable()) {
                    return (ByteChannel) key.channel();
                }
            }
        }
    }

    @Override
    public BByteChannel flow(Context context) throws BallerinaException {
        BByteChannel byteChannel;

        try {
            BStruct address = (BStruct) getRefArgument(context, ADDRESS_INDEX);
            String destination = address.getStringField(DESTINATION_INDEX);
            int port = (int) address.getIntField(PORT_INDEX);
            String permission = getStringArgument(context, PERMISSION_INDEX);
            ByteChannel tcpSocket;

            if ("r".contains(permission)) {
                tcpSocket = createTcpSocketForReading(destination, port);
            } else {
                //We create a socket
                tcpSocket = createTcpSocketForWriting(destination, port);
            }

            byteChannel = new BSocketChannel(tcpSocket);

        } catch (Throwable e) {
            String message = "Error occurred while opening TCP channel ";
            throw new BallerinaException(message + e.getMessage(), context);
        }

        return byteChannel;
    }
}
