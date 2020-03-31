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
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

/**
 * Framer for gRPC messages to be delivered via the transport layer.
 *
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * @since 0.980.0
 */
public class MessageFramer {

    private static final int NO_MAX_OUTBOUND_MESSAGE_SIZE = -1;

    private static final int HEADER_LENGTH = 5;
    private static final byte UNCOMPRESSED = 0;
    private static final byte COMPRESSED = 1;

    private int maxOutboundMessageSize = NO_MAX_OUTBOUND_MESSAGE_SIZE;
    private ByteBuffer buffer;
    private Compressor compressor = Codec.Identity.NONE;
    private boolean messageCompression = true;
    private final OutputStreamAdapter outputStreamAdapter = new OutputStreamAdapter();
    private final byte[] headerScratch = new byte[HEADER_LENGTH];
    private final HttpCarbonMessage carbonMessage;
    private boolean closed;

    // Use 4k as our minimum buffer size.
    private static final int MIN_BUFFER = 4096;

    // Set the maximum buffer size to 1MB
    private static final int MAX_BUFFER = 1024 * 1024;

    /**
     * Creates new {@link MessageFramer} instance.
     *
     * @param carbonMessage response carbon message to be delivered.
     */
    MessageFramer(HttpCarbonMessage carbonMessage) {
        this.carbonMessage = carbonMessage;
    }

    public void setCompressor(Compressor compressor) {
        this.compressor = compressor;
    }

    public void setMessageCompression(boolean enable) {
        messageCompression = enable;
    }

    /**
     * Writes out a payload message.
     *
     * @param message message to be written in form of input stream.
     */
    public void writePayload(InputStream message) {
        verifyNotClosed();
        boolean compressed = messageCompression && compressor != Codec.Identity.NONE;
        int written;
        int messageLength;
        try {
            messageLength = getKnownLength(message);
            if (messageLength != 0 && compressed) {
                written = writeCompressed(message);
            } else {
                written = writeUncompressed(message, messageLength);
            }
        } catch (StatusRuntimeException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw Status.Code.INTERNAL.toStatus()
                    .withDescription("Failed to frame message")
                    .withCause(e)
                    .asRuntimeException();
        }
        if (messageLength != -1 && written != messageLength) {
            String err = String.format("Message length inaccurate %s != %s", written, messageLength);
            throw Status.Code.INTERNAL.toStatus().withDescription(err).asRuntimeException();
        }
    }

    private int writeUncompressed(InputStream message, int messageLength) throws IOException {
        if (messageLength != -1) {
            return writeKnownLengthUncompressed(message, messageLength);
        }
        BufferChainOutputStream bufferChain = new BufferChainOutputStream();
        int written = writeToOutputStream(message, bufferChain);
        if (maxOutboundMessageSize >= 0 && written > maxOutboundMessageSize) {
            throw Status.Code.RESOURCE_EXHAUSTED.toStatus()
                    .withDescription(
                            String.format("message too large %d > %d", written, maxOutboundMessageSize))
                    .asRuntimeException();
        }
        writeBufferChain(bufferChain, false);
        return written;
    }

    private int writeCompressed(InputStream message) throws IOException {
        BufferChainOutputStream bufferChain = new BufferChainOutputStream();
        int written;
        try (OutputStream compressingStream = compressor.compress(bufferChain)) {
            written = writeToOutputStream(message, compressingStream);
        }
        if (maxOutboundMessageSize >= 0 && written > maxOutboundMessageSize) {
            throw Status.Code.RESOURCE_EXHAUSTED.toStatus()
                    .withDescription(
                            String.format("message too large %d > %d", written, maxOutboundMessageSize))
                    .asRuntimeException();
        }
        writeBufferChain(bufferChain, true);
        return written;
    }

    private int getKnownLength(InputStream inputStream) throws IOException {
        if (inputStream instanceof KnownLength || inputStream instanceof ByteArrayInputStream) {
            return inputStream.available();
        }
        return -1;
    }

    /**
     * Write an unserialized/uncompressed message with a known length.
     */
    private int writeKnownLengthUncompressed(InputStream message, int messageLength)
            throws IOException {
        if (maxOutboundMessageSize >= 0 && messageLength > maxOutboundMessageSize) {
            throw Status.Code.RESOURCE_EXHAUSTED.toStatus()
                    .withDescription(String.format("message too large %d > %d", messageLength, maxOutboundMessageSize))
                    .asRuntimeException();
        }
        ByteBuffer header = ByteBuffer.wrap(headerScratch);
        header.put(UNCOMPRESSED);
        header.putInt(messageLength);
        // Allocate the initial buffer chunk based on frame header + payload length.
        if (buffer == null) {
            buffer = ByteBuffer.allocate(header.position() + messageLength);
        }
        writeRaw(headerScratch, 0, header.position());
        return writeToOutputStream(message, outputStreamAdapter);
    }

    /**
     * Write a message that has been serialized to a sequence of buffers.
     */
    private void writeBufferChain(BufferChainOutputStream bufferChain, boolean compressed) {
        ByteBuffer header = ByteBuffer.wrap(headerScratch);
        header.put(compressed ? COMPRESSED : UNCOMPRESSED);
        int messageLength = bufferChain.readableBytes();
        header.putInt(messageLength);

        // Allocate the initial buffer chunk based on frame header + payload length.
        if (buffer == null) {
            buffer = ByteBuffer.allocate(header.position() + messageLength);
        }
        writeRaw(headerScratch, 0, header.position());
        if (messageLength == 0) {
            return;
        }

        List<ByteBuffer> bufferList = bufferChain.bufferList;
        // Assign the current buffer to the last in the chain so it can be used
        // for future writes or written with end-of-stream=true on close.
        for (ByteBuffer byteBuffer : bufferList) {
            buffer.put(byteBuffer.array(), 0, byteBuffer.limit() - 1);
        }
    }

    private static int writeToOutputStream(InputStream message, OutputStream outputStream)
            throws IOException {
        if (message instanceof Drainable) {
            return ((Drainable) message).drainTo(outputStream);
        } else {
            return (int) MessageUtils.copy(message, outputStream);
        }
    }

    private void writeRaw(byte[] b, int off, int len) {
        while (len > 0) {
            if (buffer != null && buffer.limit() == 0) {
                commitToSink(false);
            }
            if (buffer == null) {
                // InboundMessage a buffer allocation using the message length as a hint.
                buffer = ByteBuffer.allocate(len);
            }
            int toWrite = min(len, buffer.limit());
            buffer.put(b, off, toWrite);
            off += toWrite;
            len -= toWrite;
        }
    }

    /**
     * Writes any pending buffered data in the framer to carbon message.
     */
    public void flush() {
        if (buffer != null && buffer.limit() > 0) {
            commitToSink(false);
        }
    }

    /**
     * Returns whether this framer has been closed or not.
     *
     * @return is closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Flushes and closes the framer and releases any buffers.
     */
    public void close() {
        if (!isClosed()) {
            closed = true;
            //Release all the pending buffer before close.
            if (buffer != null && buffer.limit() == 0) {
                releaseBuffer();
            }
            commitToSink(true);
        }
    }

    /**
     * Closes the framer and releases any buffers, but does not flush.
     */
    public void dispose() {
        closed = true;
        releaseBuffer();
    }

    private void releaseBuffer() {
        if (buffer != null) {
            buffer.clear();
            buffer = null;
        }
    }

    private void commitToSink(boolean endOfStream) {
        ByteBuf content = Unpooled.buffer(0);
        if (buffer != null) {
            content = Unpooled.wrappedBuffer((ByteBuffer) buffer.rewind());
        }
        if (endOfStream) {
            carbonMessage.addHttpContent(new DefaultLastHttpContent(content));
        } else {
            carbonMessage.addHttpContent(new DefaultHttpContent(content));
        }
        buffer = null;
    }

    private void verifyNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException("Framer already closed");
        }
    }

    /**
     * OutputStream whose write()s are passed to the framer.
     *
     * <p>
     * Referenced from grpc-java implementation.
     * <p>
     */
    private class OutputStreamAdapter extends OutputStream {

        /**
         * This is slow, don't call it.  If you care about write overhead, use a BufferedOutputStream.
         * Better yet, you can use your own single byte buffer and call
         * {@link #write(byte[], int, int)}.
         */
        @Override
        public void write(int b) {
            byte[] singleByte = new byte[]{(byte) b};
            write(singleByte, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            writeRaw(b, off, len);
        }
    }

    /**
     * Produce a collection of ByteBuffer instances from the data written to an
     * {@link OutputStream}.
     *
     * <p>
     * Referenced from grpc-java implementation.
     * <p>
     */
    private static final class BufferChainOutputStream extends OutputStream {

        private final List<ByteBuffer> bufferList = new ArrayList<ByteBuffer>();
        private ByteBuffer current;

        /**
         * This is slow, don't call it.  If you care about write overhead, use a BufferedOutputStream.
         * Better yet, you can use your own single byte buffer and call
         * {@link #write(byte[], int, int)}.
         */
        @Override
        public void write(int b) {
            if (current != null && current.limit() > 0) {
                current.put((byte) b);
                return;
            }
            byte[] singleByte = new byte[]{(byte) b};
            write(singleByte, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            if (current == null) {
                // InboundMessage len bytes initially from the allocator, it may give us more.
                int capacityHint = Math.min(MAX_BUFFER, Math.max(MIN_BUFFER, len));
                current = ByteBuffer.allocate(capacityHint);
                bufferList.add(current);
            }
            while (len > 0) {
                int canWrite = Math.min(len, current.limit());
                if (canWrite == 0) {
                    // Assume message is twice as large as previous assumption if were still not done,
                    // the allocator may allocate more or less than this amount.
                    int needed = Math.max(len, current.limit() * 2);
                    current = ByteBuffer.allocate(needed);
                    bufferList.add(current);
                } else {
                    current.put(b, off, canWrite);
                    off += canWrite;
                    len -= canWrite;
                }
            }
        }

        private int readableBytes() {
            int readable = 0;
            for (ByteBuffer writableBuffer : bufferList) {
                readable += writableBuffer.limit();
            }
            return readable;
        }
    }
}
