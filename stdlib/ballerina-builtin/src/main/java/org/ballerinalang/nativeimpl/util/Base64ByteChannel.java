/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.nativeimpl.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Represent the byte channel that needs to be encode or decode using Base64 encoding scheme.
 *
 * @since 0.970.0
 */
public class Base64ByteChannel implements ByteChannel {
    private InputStream inputStream;
    private ReadableByteChannel byteChannel;

    public Base64ByteChannel(InputStream inputStream) {
        this.inputStream = inputStream;
        this.byteChannel = Channels.newChannel(inputStream);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return byteChannel.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return 0;
    }

    @Override
    public boolean isOpen() {
        return byteChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        byteChannel.close();
        inputStream.close();
    }
}
