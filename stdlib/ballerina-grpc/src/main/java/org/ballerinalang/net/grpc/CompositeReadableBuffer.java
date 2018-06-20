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

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A {@link CompositeReadableBuffer} that is composed of 0 or more {@link CompositeReadableBuffer}s. This provides a
 * facade that allows multiple buffers to be treated as one.
 * <p>
 * <p>When a buffer is added to a composite, its life cycle is controlled by the composite. Once
 * the composite has read past the end of a given buffer, that buffer is automatically closed and
 * removed from the composite.
 */
public class CompositeReadableBuffer {

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

    protected final void checkReadable(int length) {
        if (readableBytes() < length) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Adds a new {@link CompositeReadableBuffer} at the end of the buffer list. After a buffer is added, it is
     * expected that this {@code CompositeBuffer} has complete ownership. Any attempt to modify the
     * buffer (i.e. modifying the readable bytes) may result in corruption of the internal state of
     * this {@code CompositeBuffer}.
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

    public void skipBytes(int length) {

        execute(new ReadOperation() {
            @Override
            public int readInternal(ByteBuf buffer, int length) {

                buffer.skipBytes(length);
                return 0;
            }
        }, length);
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

    public void readBytes(final ByteBuffer dest) {

        execute(new ReadOperation() {
            @Override
            public int readInternal(ByteBuf buffer, int length) {
                // Change the limit so that only lengthToCopy bytes are available.
                int prevLimit = dest.limit();
                dest.limit(dest.position() + length);

                // Write the bytes and restore the original limit.
                buffer.readBytes(dest);
                dest.limit(prevLimit);
                return 0;
            }
        }, dest.remaining());
    }

    public void readBytes(final OutputStream dest, int length) throws IOException {

        ReadOperation op = new ReadOperation() {
            @Override
            public int readInternal(ByteBuf buffer, int length) throws IOException {

                buffer.readBytes(dest, length);
                return 0;
            }
        };
        execute(op, length);

        // If an exception occurred, throw it.
        if (op.isError()) {
            throw op.ex;
        }
    }

    public ByteBuf readBuffer(int length) {
        ByteBuf buffer = buffers.peek();
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
     * Executes the given {@link ReadOperation} against the {@link CompositeReadableBuffer}s required to
     * satisfy the requested {@code length}.
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
        if (buffer.readableBytes() == 0 && buffer.refCnt() != 0) {
            buffers.remove().release();
        }
    }

    /**
     * A simple read operation to perform on a single {@link CompositeReadableBuffer}. All state management for
     * the buffers is done by {@link CompositeReadableBuffer#execute(ReadOperation, int)}.
     */
    private abstract static class ReadOperation {

        /**
         * Only used by {@link CompositeReadableBuffer#readUnsignedByte()}.
         */
        int value;

        /**
         * Only used by {@link CompositeReadableBuffer#readBytes(OutputStream, int)}.
         */
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
