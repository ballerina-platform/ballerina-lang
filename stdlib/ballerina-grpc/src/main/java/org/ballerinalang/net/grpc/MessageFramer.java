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
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.min;

/**
 * Encodes gRPC messages to be delivered via the transport layer which implements.
 */
public class MessageFramer implements Framer {

    private static final int NO_MAX_OUTBOUND_MESSAGE_SIZE = -1;

    private static final int HEADER_LENGTH = 5;
    private static final byte UNCOMPRESSED = 0;
    private static final byte COMPRESSED = 1;

    // effectively final.  Can only be set once.
    private int maxOutboundMessageSize = NO_MAX_OUTBOUND_MESSAGE_SIZE;
    private ByteBuffer buffer;
    private Compressor compressor = Codec.Identity.NONE;
    private boolean messageCompression = true;
    private final OutputStreamAdapter outputStreamAdapter = new OutputStreamAdapter();
    private final byte[] headerScratch = new byte[HEADER_LENGTH];
    private final HTTPCarbonMessage carbonMessage;

    // transportTracer is nullable until it is integrated with client transports
    private boolean closed;

    // Tracing and stats-related states
    private int messagesBuffered;
    private int currentMessageSeqNo = -1;
    private long currentMessageWireSize;

    /**
     * Creates a {@code MessageFramer}.
     *
     * @param carbonMessage            the sink used to deliver frames to the transport
     */
    public MessageFramer(HTTPCarbonMessage carbonMessage) {

        this.carbonMessage = carbonMessage;
    }

    @Override
    public MessageFramer setCompressor(Compressor compressor) {

        this.compressor = compressor;
        return this;
    }

    @Override
    public MessageFramer setMessageCompression(boolean enable) {

        messageCompression = enable;
        return this;
    }

    @Override
    public void setMaxOutboundMessageSize(int maxSize) {

        if (maxSize > 0) {
            maxOutboundMessageSize = maxSize;
        }
    }

    /**
     * Writes out a payload message.
     *
     * @param message contains the message to be written out. It will be completely consumed.
     */
    @Override
    public void writePayload(InputStream message) {

        verifyNotClosed();
        messagesBuffered++;
        currentMessageSeqNo++;
        currentMessageWireSize = 0;
        boolean compressed = messageCompression && compressor != Codec.Identity.NONE;
        int written = -1;
        int messageLength = -2;
        try {
            messageLength = getKnownLength(message);
            if (messageLength != 0 && compressed) {
                written = writeCompressed(message);
            } else {
                written = writeUncompressed(message, messageLength);
            }
        } catch (IOException | RuntimeException | ServerConnectorException e) {
            // This should not be possible, since sink#deliverFrame doesn't throw.
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

    private int writeUncompressed(InputStream message, int messageLength) throws IOException, ServerConnectorException {

        if (messageLength != -1) {
            currentMessageWireSize = messageLength;
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

    private int writeCompressed(InputStream message) throws IOException, ServerConnectorException {

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
     * Write an unserialized message with a known length, uncompressed.
     */
    private int writeKnownLengthUncompressed(InputStream message, int messageLength)
            throws IOException, ServerConnectorException {

        if (maxOutboundMessageSize >= 0 && messageLength > maxOutboundMessageSize) {
            throw Status.Code.RESOURCE_EXHAUSTED.toStatus()
                    .withDescription(
                            String.format("message too large %d > %d", messageLength, maxOutboundMessageSize))
                    .asRuntimeException();
        }
        ByteBuffer header = ByteBuffer.wrap(headerScratch);
        header.put(UNCOMPRESSED);
        header.putInt(messageLength);
        // Allocate the initial buffer chunk based on frame header + payload length.
        // Note that the allocator may allocate a buffer larger or smaller than this length
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
        ByteBuffer writeableHeader = ByteBuffer.allocate(HEADER_LENGTH);
        writeableHeader.put(headerScratch, 0, header.position());
        if (messageLength == 0) {
            // the payload had 0 length so make the header the current buffer.
            buffer = writeableHeader;
            return;
        }
        // Note that we are always delivering a small message to the transport here which
        // may incur transport framing overhead as it may be sent separately to the contents
        // of the GRPC frame.
        // The final message may not be completely written because we do not flush the last buffer.
        // Do not report the last message as sent.
        carbonMessage.addHttpContent(new DefaultHttpContent(Unpooled.wrappedBuffer(writeableHeader)));
        messagesBuffered = 1;
        // Commit all except the last buffer to the sink
        List<ByteBuffer> bufferList = bufferChain.bufferList;
        for (int i = 0; i < bufferList.size(); i++) {
            carbonMessage.addHttpContent(new DefaultHttpContent((Unpooled.wrappedBuffer(bufferList.get(i)))));
        }
        // Assign the current buffer to the last in the chain so it can be used
        // for future writes or written with end-of-stream=true on close.
        buffer = bufferList.get(bufferList.size() - 1);
        currentMessageWireSize = messageLength;
    }

    private static int writeToOutputStream(InputStream message, OutputStream outputStream)
            throws IOException {

        if (message instanceof Drainable) {
            return ((Drainable) message).drainTo(outputStream);
        } else {
            // This makes an unnecessary copy of the bytes when bytebuf supports array(). However, we
            // expect performance-critical code to support flushTo().
            long written = MessageUtils.copy(message, outputStream);
            checkArgument(written <= Integer.MAX_VALUE, "Message size overflow: %s", written);
            return (int) written;
        }
    }

    private void writeRaw(byte[] b, int off, int len) throws ServerConnectorException {

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
     * Flushes any buffered data in the framer to the sink.
     */
    @Override
    public void flush() {

        if (buffer != null && buffer.limit() > 0) {
            commitToSink(false);
        }
    }

    /**
     * Indicates whether or not this framer has been closed via a call to either
     * {@link #close()} or {@link #dispose()}.
     */
    @Override
    public boolean isClosed() {

        return closed;
    }

    /**
     * Flushes and closes the framer and releases any buffers. After the framer is closed or
     * disposed, additional calls to this method will have no affect.
     */
    @Override
    public void close() {

        if (!isClosed()) {
            closed = true;
            // With the current code we don't expect readableBytes > 0 to be possible here, added
            // defensively to prevent buffer leak issues if the framer code changes later.
            if (buffer != null && buffer.limit() == 0) {
                releaseBuffer();
            }
            commitToSink(true);
        }
    }

    /**
     * Closes the framer and releases any buffers, but does not flush. After the framer is
     * closed or disposed, additional calls to this method will have no affect.
     */
    @Override
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
        messagesBuffered = 0;
    }

    private void verifyNotClosed() {

        if (isClosed()) {
            throw new IllegalStateException("Framer already closed");
        }
    }

    /**
     * OutputStream whose write()s are passed to the framer.
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

            try {
                writeRaw(b, off, len);
            } catch (ServerConnectorException e) {
                throw Status.Code.INTERNAL.toStatus()
                        .withDescription("Failed to frame message")
                        .withCause(e)
                        .asRuntimeException();
            }
        }
    }

    /**
     * Produce a collection of ByteBuffer instances from the data written to an
     * {@link OutputStream}.
     */
    private final class BufferChainOutputStream extends OutputStream {

        private final List<ByteBuffer> bufferList = new ArrayList<ByteBuffer>();
        private ByteBuffer current;

        /**
         * This is slow, don't call it.  If you care about write overhead, use a BufferedOutputStream.
         * Better yet, you can use your own single byte buffer and call
         * {@link #write(byte[], int, int)}.
         */
        @Override
        public void write(int b) throws IOException {

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
                current = ByteBuffer.allocate(len);
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
