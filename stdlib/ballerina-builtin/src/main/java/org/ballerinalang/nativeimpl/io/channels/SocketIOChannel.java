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

package org.ballerinalang.nativeimpl.io.channels;

import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.channels.base.readers.BlockingReader;
import org.ballerinalang.nativeimpl.io.channels.base.writers.BlockingWriter;
import org.ballerinalang.nativeimpl.socket.SocketByteChannel;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Represents the channel to perform I/O operations on {@link java.net.Socket}.
 *
 * @since 0.963.0
 */
public class SocketIOChannel extends Channel {

    private ByteChannel channel;

    public SocketIOChannel(ByteChannel channel, int size) throws IOException {
        super(channel, new BlockingReader(), new BlockingWriter(), size);
        this.channel = channel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(int position, int count, WritableByteChannel dstChannel) throws IOException {
        throw new BallerinaIOException("Unsupported method");
    }

    /**
     * Shutdown the connection for reading.
     *
     * @throws IOException If some other I/O error occurs.
     */
    public void shutdownInput() throws IOException {
        if (channel instanceof SocketChannel) {
            SocketChannel socketChannel = (SocketChannel) channel;
            socketChannel.shutdownInput();
        } else if (channel instanceof SocketByteChannel) {
            SocketByteChannel socketByteChannel = (SocketByteChannel) channel;
            socketByteChannel.shutdownInput();
        }
    }

    /**
     * Shutdown the connection for writing.
     *
     * @throws IOException If some other I/O error occurs.
     */
    public void shutdownOutput() throws IOException {
        if (channel instanceof SocketChannel) {
            SocketChannel socketChannel = (SocketChannel) channel;
            socketChannel.shutdownOutput();
        } else if (channel instanceof SocketByteChannel) {
            SocketByteChannel socketByteChannel = (SocketByteChannel) channel;
            socketByteChannel.shutdownOutput();
        }
    }
}
