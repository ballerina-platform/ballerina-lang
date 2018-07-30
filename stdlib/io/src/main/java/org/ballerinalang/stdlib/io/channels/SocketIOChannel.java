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

package org.ballerinalang.stdlib.io.channels;

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.readers.ChannelReader;
import org.ballerinalang.stdlib.io.channels.base.writers.ChannelWriter;
import org.ballerinalang.stdlib.io.socket.SocketByteChannel;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;

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
    private boolean selectable;

    public SocketIOChannel(ByteChannel channel) {
        super(channel, new ChannelReader(), new ChannelWriter());
        this.channel = channel;
    }

    public SocketIOChannel(ByteChannel channel, boolean selectable) {
        super(channel, new ChannelReader(), new ChannelWriter());
        this.channel = channel;
        this.selectable = selectable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(int position, int count, WritableByteChannel dstChannel) {
        throw new BallerinaIOException("Unsupported method");
    }

    @Override
    public Channel getChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSelectable() {
        return selectable;
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
