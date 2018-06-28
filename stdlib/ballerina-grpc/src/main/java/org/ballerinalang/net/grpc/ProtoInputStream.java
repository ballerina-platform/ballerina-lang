/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc;

import com.google.protobuf.CodedOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.protobuf.CodedOutputStream.DEFAULT_BUFFER_SIZE;

/**
 * Protobuf input stream.
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * @since 0.980.0
 */
class ProtoInputStream extends InputStream implements Drainable, KnownLength {

    private Message message;
    private ByteArrayInputStream partial;

    ProtoInputStream(Message message) {
        this.message = message;
    }

    @Override
    public int read() {
        if (message != null) {
            partial = new ByteArrayInputStream(message.toByteArray());
            message = null;
        }
        if (partial != null) {
            return partial.read();
        }
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (message != null) {
            int size = message.getSerializedSize();
            if (size == 0) {
                message = null;
                partial = null;
                return -1;
            }
            if (len >= size) {
                CodedOutputStream stream = CodedOutputStream.newInstance(b, off, size);
                message.writeTo(stream);
                stream.flush();
                stream.checkNoSpaceLeft();

                message = null;
                partial = null;
                return size;
            }
            partial = new ByteArrayInputStream(message.toByteArray());
            message = null;
        }
        if (partial != null) {
            return partial.read(b, off, len);
        }
        return -1;
    }

    @Override
    public int available() {
        if (message != null) {
            return message.getSerializedSize();
        } else if (partial != null) {
            return partial.available();
        }
        return 0;
    }

    Message message() {
        if (message == null) {
            throw new IllegalStateException("message not available");
        }
        return message;
    }

    @Override
    public int drainTo(OutputStream target) throws IOException {
        int written;
        if (message != null) {
            written = message.getSerializedSize();

            if (written > DEFAULT_BUFFER_SIZE) {
                written = DEFAULT_BUFFER_SIZE;
            }
            final CodedOutputStream codedOutput =
                    CodedOutputStream.newInstance(target, written);
            message.writeTo(codedOutput);
            codedOutput.flush();
            message = null;
        } else if (partial != null) {
            written = (int) ProtoUtils.copy(partial, target);
            partial = null;
        } else {
            written = 0;
        }
        return written;
    }
}
