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

import com.google.protobuf.CodedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Utility methods for using protobuf with grpc.
 *
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 *
 * @since 0.980.0
 */
public class ProtoUtils {

    private static final int BUF_SIZE = 8192;
    private static final int DEFAULT_MAX_MESSAGE_SIZE = 4 * 1024 * 1024;
    private static final ThreadLocal<Reference<byte[]>> bufs = ThreadLocal.withInitial(() -> new WeakReference<>(new
            byte[4096]));

    /**
     * Create a {@code Marshaller} for protos of the same type as {@code instance}.
     *
     * @param instance message instance
     * @return proto marshall
     */
    public static MethodDescriptor.Marshaller marshaller(MessageParser instance) {
        final MessageParser parser = instance;
        return new MethodDescriptor.Marshaller() {

            @Override
            public InputStream stream(Message value) {
                return new ProtoInputStream(value);
            }

            @Override
            public Message parse(InputStream stream) {
                CodedInputStream cis = null;
                try {
                    if (stream instanceof KnownLength) {
                        int size = stream.available();
                        if (size > 0 && size <= DEFAULT_MAX_MESSAGE_SIZE) {
                            // buf should not be used after this method has returned.
                            byte[] buf = bufs.get().get();
                            if (buf == null || buf.length < size) {
                                buf = new byte[size];
                                bufs.set(new WeakReference<>(buf));
                            }
                            int remaining = size;
                            while (remaining > 0) {
                                int position = size - remaining;
                                int count = stream.read(buf, position, remaining);
                                if (count == -1) {
                                    break;
                                }
                                remaining -= count;
                            }
                            if (remaining != 0) {
                                int position = size - remaining;
                                throw new RuntimeException("size inaccurate: " + size + " != " + position);
                            }
                            cis = CodedInputStream.newInstance(buf, 0, size);
                        } else if (size == 0) {
                            return instance.getDefaultInstance();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (cis == null) {
                    cis = CodedInputStream.newInstance(stream);
                }
                cis.setSizeLimit(Integer.MAX_VALUE);

                try {
                    return parseFrom(cis);
                } catch (IOException ipbe) {
                    throw Status.Code.INTERNAL.toStatus().withDescription("Invalid protobuf byte sequence")
                            .withCause(ipbe).asRuntimeException();
                }
            }

            private Message parseFrom(CodedInputStream stream) throws IOException {
                Message message = parser.parseFrom(stream);
                stream.checkLastTagWas(0);
                return message;
            }
        };
    }

    /**
     * Copies the data from input stream to output stream.
     */
    static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    private ProtoUtils() {
    }
}
