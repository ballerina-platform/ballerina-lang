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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import javax.net.ssl.SSLSocket;

/**
 * This will wrap {@link InputStream} and {@link OutputStream} into a {@link ByteChannel} implementation.
 */
public class SocketByteChannel implements ByteChannel {

    private SSLSocket sslSocket;
    private InputStream in;
    private OutputStream out;
    private ReadableByteChannel inChannel;
    private WritableByteChannel writeChannel;

    public SocketByteChannel(SSLSocket sslSocket) throws IOException {
        this.sslSocket = sslSocket;
        this.in = sslSocket.getInputStream();
        this.inChannel = Channels.newChannel(in);
        this.out = sslSocket.getOutputStream();
        this.writeChannel = Channels.newChannel(out);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return inChannel.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return writeChannel.write(src);
    }

    @Override
    public boolean isOpen() {
        return inChannel.isOpen() || writeChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        inChannel.close();
        writeChannel.close();
        in.close();
        out.close();
    }

    /**
     * Shutdown the connection for reading.
     *
     * @throws IOException If some other I/O error occurs.
     */
    public void shutdownInput() throws IOException {
        sslSocket.shutdownInput();
    }

    /**
     * Shutdown the connection for writing.
     *
     * @throws IOException If some other I/O error occurs.
     */
    public void shutdownOutput() throws IOException {
        sslSocket.shutdownOutput();
    }
}
