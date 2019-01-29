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

package org.ballerinalang.stdlib.io.channels.base.writers;

import org.ballerinalang.stdlib.io.utils.BallerinaIOException;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Represents how the bytes should be written to a channel.
 */
public interface Writer {
    /**
     * Writes bytes to the given channel. The writing will begin from the buffers current position.
     *
     * @param content the content which should be written.
     * @param channel the channel to which the bytes will be written to
     * @return the number of bytes written.
     * @throws BallerinaIOException during i/o error while writing.
     */
    int write(ByteBuffer content, ByteChannel channel) throws BallerinaIOException;;
}
