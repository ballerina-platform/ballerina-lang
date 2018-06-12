/*
 * Copyright 2014, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.grpc;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for using protobuf with grpc.
 */
public class ProtoUtils {

    private static volatile ExtensionRegistryLite globalRegistry =
            ExtensionRegistryLite.getEmptyRegistry();

    private static final int BUF_SIZE = 8192;

    private static final int DEFAULT_MAX_MESSAGE_SIZE = 4 * 1024 * 1024;

    private static final ThreadLocal<Reference<byte[]>> bufs = ThreadLocal.withInitial(() -> {

        return new WeakReference<>(new byte[4096]); // Picked at random.
    });

    /**
     * Create a {@code Marshaller} for protos of the same type as {@code defaultInstance}.
     */
    public static <T extends MessageLite> MethodDescriptor.Marshaller<T> marshaller(final T defaultInstance) {

        final Parser<T> parser = (Parser<T>) defaultInstance.getParserForType();
        return new MethodDescriptor.Marshaller<T>() {
            @SuppressWarnings("unchecked")

            @Override
            public InputStream stream(T value) {

                return new ProtoInputStream(value, parser);
            }

            @Override
            public T parse(InputStream stream) {

                CodedInputStream cis = null;
                try {
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
                        return defaultInstance;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (cis == null) {
                    cis = CodedInputStream.newInstance(stream);
                }
                // Pre-create the CodedInputStream so that we can remove the size limit restriction
                // when parsing.
                cis.setSizeLimit(Integer.MAX_VALUE);

                try {
                    return parseFrom(cis);
                } catch (InvalidProtocolBufferException ipbe) {
                    throw Status.Code.INTERNAL.toStatus().withDescription("Invalid protobuf byte sequence")
                            .withCause(ipbe).asRuntimeException();
                }
            }

            private T parseFrom(CodedInputStream stream) throws InvalidProtocolBufferException {

                T message = parser.parseFrom(stream, globalRegistry);
                try {
                    stream.checkLastTagWas(0);
                    return message;
                } catch (InvalidProtocolBufferException e) {
                    e.setUnfinishedMessage(message);
                    throw e;
                }
            }
        };
    }

    /**
     * Copies the data from input stream to output stream.
     */
    static long copy(InputStream from, OutputStream to) throws IOException {
        // Copied from guava com.google.common.io.ByteStreams because its API is unstable (beta)
        checkNotNull(from);
        checkNotNull(to);
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
