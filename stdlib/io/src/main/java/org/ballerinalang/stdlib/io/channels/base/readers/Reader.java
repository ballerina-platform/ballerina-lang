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
package org.ballerinalang.stdlib.io.channels.base.readers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Represents how the bytes will be read through the channel.
 */
public interface Reader {
    /**
     * Reads bytes into the provided buffer.
     *
     * @param content the buffer which will hold the read content.
     * @param channel the source channel where the content will be read from.
     * @return the number of bytes read.
     * @throws IOException errors occur while reading.
     */
    int read(ByteBuffer content, ByteChannel channel) throws IOException;
}
