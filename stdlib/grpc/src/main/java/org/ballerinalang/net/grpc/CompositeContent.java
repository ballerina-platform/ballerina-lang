/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A {@link CompositeContent} that is composed of 0 or more {@link ByteBuf} contents.
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * @since 0.980.0
 */
public class CompositeContent {

    private int readableBytes;
    private final Queue<ByteBuf> buffers = new ArrayDeque<>();

    public final int readInt() {
        checkReadable(4);
        int b1 = readUnsignedByte();
        int b2 = readUnsignedByte();
        int b3 = readUnsignedByte();
        int b4 = readUnsignedByte();
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    private void checkReadable(int length) {
        if (readableBytes() < length) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Adds a new {@link ByteBuf} at the end of the buffer list.
     *
     * @param buffer Byte buffer
     */
    public void addBuffer(ByteBuf buffer) {
        buffers.add(buffer);
        readableBytes += buffer.readableBytes();
    }

    public int readableBytes() {
        return readableBytes;
    }

    public int readUnsignedByte() {
        ReadOperation op = new ReadOperation() {
            @Override
            int readInternal(ByteBuf buffer, int length) {

                return buffer.readUnsignedByte();
            }
        };
        execute(op, 1);
        return op.value;
    }

    public void readBytes(final byte[] dest, final int destOffset, int length) {
        execute(new ReadOperation() {
            int currentOffset = destOffset;

            @Override
            public int readInternal(ByteBuf buffer, int length) {
                buffer.readBytes(dest, currentOffset, length);
                currentOffset += length;
                return 0;
            }
        }, length);
    }

    public ByteBuf readBuffer(int length) {
        ByteBuf buffer = buffers.peek();
        if (buffer == null) {
            readableBytes = 0;
            throw new RuntimeException("Error while reading inbound data from buffer. The buffer queue is empty");
        }
        if (buffer.readableBytes() > length) {
            readableBytes -= length;
            return buffer.readBytes(length);
        } else {
            readableBytes -= buffer.readableBytes();
            return buffers.poll();
        }
    }

    public void close() {
        while (!buffers.isEmpty()) {
            ByteBuf byteBuf = buffers.remove();
            if (byteBuf.refCnt() != 0) {
                byteBuf.release();
            }
        }
    }

    /**
     * Executes the given {@link ReadOperation} against the {@link CompositeContent}.
     */
    private void execute(ReadOperation op, int length) {
        checkReadable(length);
        if (!buffers.isEmpty()) {
            advanceBufferIfNecessary();
        }
        for (; length > 0 && !buffers.isEmpty(); advanceBufferIfNecessary()) {
            ByteBuf buffer = buffers.peek();
            int lengthToCopy = Math.min(length, buffer.readableBytes());
            // Perform the read operation for this buffer.
            op.read(buffer, lengthToCopy);
            if (op.isError()) {
                return;
            }
            length -= lengthToCopy;
            readableBytes -= lengthToCopy;
        }
        if (length > 0) {
            // Should never get here.
            throw new AssertionError("Failed executing read operation");
        }
    }

    /**
     * If the current buffer is exhausted, removes and closes it.
     */
    private void advanceBufferIfNecessary() {
        ByteBuf buffer = buffers.peek();
        if (buffer != null && buffer.readableBytes() == 0 && buffer.refCnt() != 0) {
            buffers.remove().release();
        }
    }

    /**
     * A simple read operation to perform on a single {@link CompositeContent}.
     * <p>
     * Referenced from grpc-java implementation.
     * <p>
     */
    private abstract static class ReadOperation {

        /**
         * Only used by {@link CompositeContent#readUnsignedByte()}.
         */
        int value;
        IOException ex;

        final void read(ByteBuf buffer, int length) {
            try {
                value = readInternal(buffer, length);
            } catch (IOException e) {
                ex = e;
            }
        }

        final boolean isError() {
            return ex != null;
        }

        abstract int readInternal(ByteBuf buffer, int length) throws IOException;
    }
}
