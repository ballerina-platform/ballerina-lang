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

package org.ballerinalang.nativeimpl.io.channels;

import org.ballerinalang.nativeimpl.io.channels.base.BByteChannel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Represents TCP socket to handle relevant I/O operations
 */
public class BSocketChannel extends BByteChannel {
    public BSocketChannel(ByteChannel channel) throws IOException {
        super(channel);
    }

    public BSocketChannel(ByteChannel channel, int fixedBufferSize) {
        super(channel, fixedBufferSize);
    }

    @Override
    public void transfer(int position, int count, WritableByteChannel dstChannel) throws IOException {
        throw new NotImplementedException();
    }
}
