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

package org.ballerinalang.stdlib.io.channels;

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.readers.AsyncReader;
import org.ballerinalang.stdlib.io.channels.base.readers.BlockingReader;
import org.ballerinalang.stdlib.io.channels.base.writers.AsyncWriter;
import org.ballerinalang.stdlib.io.channels.base.writers.BlockingWriter;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * <p>
 * Represents the channel to perform I/O operations on file.
 * </p>
 */
public class FileIOChannel extends Channel {

    /**
     * Maintains the file channel implementation.
     */
    private FileChannel channel;

    FileIOChannel(FileChannel channel, int size) throws BallerinaIOException {
        super(channel, new BlockingReader(), new BlockingWriter(), size);
        this.channel = channel;
    }

    public FileIOChannel(FileChannel channel) throws BallerinaIOException {
        super(channel, new AsyncReader(), new AsyncWriter());
    }

    /**
     * Transfer file content to the specified destination.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void transfer(int position, int count, WritableByteChannel dstChannel) throws IOException {
        try {
            channel.transferTo(position, count, dstChannel);
        } catch (IOException e) {
            throw new BallerinaIOException("Error occurred while transferring file", e);
        }
    }
}
