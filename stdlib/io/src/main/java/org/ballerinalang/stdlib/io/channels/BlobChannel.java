/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.io.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Creates channel which will reference byte content.
 */
public class BlobChannel implements ByteChannel {

    private ReadableByteChannel readableChannel;
    
    private WritableByteChannel writableChannel;

    public BlobChannel(ReadableByteChannel channel) {
        this.readableChannel = channel;
    }
    
    public BlobChannel(WritableByteChannel channel) {
        this.writableChannel = channel;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (readableChannel == null) {
            throw new UnsupportedOperationException();
        }
        return readableChannel.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (writableChannel == null) {
            throw new UnsupportedOperationException();
        }
        return this.writableChannel.write(src);
    }

    @Override
    public boolean isOpen() {
        if (readableChannel != null) {
            return readableChannel.isOpen();
        } else {
            return writableChannel.isOpen();
        }
    }

    @Override
    public void close() throws IOException {
        if (readableChannel != null) {
            readableChannel.close();
        } else {
            writableChannel.close();
        }
    }
    
    public boolean isReadable() {
        return readableChannel != null;
    }
    
}
