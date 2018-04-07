/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.io.channels.base.writers;

import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.NonWritableChannelException;

/**
 * Writes bytes to a given channel asynchronously.
 */
public class AsyncWriter implements Writer {

    private static final Logger log = LoggerFactory.getLogger(AsyncWriter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public int write(ByteBuffer content, ByteChannel channel) throws BallerinaIOException {
        int numberOfBytesWritten;
        try {
            numberOfBytesWritten = channel.write(content);
            if (log.isTraceEnabled()) {
                log.trace("Number of bytes " + numberOfBytesWritten + " written to channel " + channel.hashCode());
            }
        } catch (IOException | NonWritableChannelException e) {
            String message = "Error occurred while writing to channel ";
            throw new BallerinaIOException(message, e);
        }
        return numberOfBytesWritten;
    }
}
