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
 * Attempts to fill the entire buffer with content.
 */
public class BlockingReader implements Reader {

    private static final Logger log = LoggerFactory.getLogger(BlockingReader.class);

    /**
     * Will be marked if the given channel has reached it's end.
     */
    private boolean hasReachedEnd = false;

    /**
     * Reads bytes in blocking mode. Could be used if it's required to readFull().
     * {@inheritDoc}
     */
    @Override
    public int read(ByteBuffer content, ByteChannel channel) throws IOException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Reading from channel " + channel.hashCode());
            }
            //We will need to allocate a buffer per each call, reusing this will not be an option
            // a - the size of the buffer would very based on the requested number of bytes
            // b - the bytes getByteArray should be re-used
            int numberOfReadBytes;
            int totalNumberOfReadBytes = 0;
            int channelEndOfStreamFlag = 0;
            //Fills the inputBuffer with the
            while (content.hasRemaining() && !hasReachedEnd) {
                numberOfReadBytes = channel.read(content);
                if (numberOfReadBytes <= channelEndOfStreamFlag) {
                    hasReachedEnd = true;
                } else {
                    totalNumberOfReadBytes = totalNumberOfReadBytes + numberOfReadBytes;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Read from channel " + numberOfReadBytes + " from " + channel.hashCode());
                }
            }
            return totalNumberOfReadBytes;
        } catch (IOException | NonReadableChannelException e) {
            String message = "Could not read from the channel";
            log.error(message, e);
            throw new IOException(message, e);
        }
    }
}
