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

package org.ballerinalang.nativeimpl.io.channels.base.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.NonReadableChannelException;

/**
 * <p>
 * Asynchronously reads bytes from the channel.
 * </p>
 * <p>
 * This will be a non-blocking operation.
 * </p>
 */
public class AsyncReader implements Reader {

    private static final Logger log = LoggerFactory.getLogger(BlockingReader.class);

    /**
     * Reads bytes asynchronously.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int read(ByteBuffer content, ByteChannel channel) throws IOException {
        try {
            return channel.read(content);
        } catch (IOException | NonReadableChannelException e) {
            String message = "could not read from the channel";
            log.error(message, e);
            throw new IOException(message, e);
        }
    }
}
