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

package org.ballerinalang.test.nativeimpl.functions.io;

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.readers.AsyncReader;
import org.ballerinalang.stdlib.io.channels.base.readers.BlockingReader;
import org.ballerinalang.stdlib.io.channels.base.writers.AsyncWriter;
import org.ballerinalang.stdlib.io.channels.base.writers.BlockingWriter;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;

import java.nio.channels.ByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Mock implementation of byte channel for testing purposes.
 */
public class MockByteChannel extends Channel {

    public MockByteChannel(ByteChannel channel, int fixedBufferSize) {
        super(channel, new BlockingReader(), new BlockingWriter(), fixedBufferSize);
    }

    public MockByteChannel(ByteChannel channel) {
        super(channel, new AsyncReader(), new AsyncWriter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(int position, int count, WritableByteChannel dstChannel) throws BallerinaIOException {
        throw new UnsupportedOperationException();
    }
}
