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
import io.netty.handler.codec.http.HttpContent;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Deframer for GRPC frames.
 *
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * @since 0.980.0
 */
public class MessageDeframer implements Closeable {
    private static final int HEADER_LENGTH = 5;
    private static final int COMPRESSED_FLAG_MASK = 1;
    private static final int RESERVED_MASK = 0xFE;

    /**
     * A listener of deframing events.
     */
    public interface Listener {

        /**
         * Invoked to deliver the next complete message as input stream.
         *
         * @param inputStream single message as input stream.
         */
        void messagesAvailable(InputStream inputStream);

        /**
         * Invoked when the deframer closes.
         *
         * @param hasPartialMessage indicates whether the deframer has incomplete message.
         */
        void deframerClosed(boolean hasPartialMessage);

        /**
         * Invoked when a {@link #deframe(HttpContent)} operation failed.
         *
         * @param cause of the failure.
         */
        void deframeFailed(Throwable cause);
    }

    private enum State {
        HEADER, BODY
    }

    private Listener listener;
    private int maxInboundMessageSize;
    private Decompressor decompressor;
    private State state = State.HEADER;
    private int requiredLength = HEADER_LENGTH;
    private boolean compressedFlag;
    private CompositeContent nextFrame;
    private CompositeContent unprocessed = new CompositeContent();
    private volatile boolean inDelivery = false;

    private boolean closeWhenComplete = false;

    /**
     * Create a deframer.
     *
     * @param listener listener for deframer events.
     * @param decompressor the compression used if a compressed frame is encountered.
     * @param maxMessageSize the maximum allowed size for received messages.
     */
    MessageDeframer(
            Listener listener,
            Decompressor decompressor,
            int maxMessageSize) {
        this.listener = listener;
        this.decompressor = decompressor;
        this.maxInboundMessageSize = maxMessageSize;
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setDecompressor(Decompressor decompressor) {
        this.decompressor = decompressor;
    }

    public void deframe(HttpContent data) {
        if (data == null) {
            throw new RuntimeException("Data buffer is null");
        }
        boolean needToCloseData = true;
        try {
            if (!isClosedOrScheduledToClose()) {
                unprocessed.addBuffer(data.content());
                needToCloseData = false;
                deliver();
            }
        } finally {
            if (needToCloseData && data.refCnt() != 0) {
                data.release();
            }
        }
    }

    public void closeWhenComplete() {
        if (isStalled()) {
            close();
        } else if (!isClosed()) {
            closeWhenComplete = true;
        }
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        boolean hasPartialMessage = nextFrame != null && nextFrame.readableBytes() > 0;
        try {
            if (unprocessed != null) {
                unprocessed.close();
            }
            if (nextFrame != null) {
                nextFrame.close();
            }
        } finally {
            unprocessed = null;
            nextFrame = null;
        }
        listener.deframerClosed(hasPartialMessage);
    }

    /**
     * Indicates whether deframer has been closed or not.
     *
     * @return true, if deframer is closed, false otherwise.
     */
    public boolean isClosed() {
        return unprocessed == null;
    }

    /**
     * Indicates whether deframer is closed or about to close.
     *
     * @return true, if deframer is closed or about to close. false otherwise
     */
    private boolean isClosedOrScheduledToClose() {
        return isClosed() || closeWhenComplete;
    }

    private boolean isStalled() {
        return unprocessed == null || unprocessed.readableBytes() == 0;
    }

    /**
     * Reads and delivers message frames.
     */
    private void deliver() {
        if (inDelivery) {
            return;
        }
        inDelivery = true;
        try {
            // Process the uncompressed bytes.
            while (readRequiredBytes()) {
                switch (state) {
                    case HEADER:
                        processHeader();
                        break;
                    case BODY:
                        // Read the body and deliver the message.
                        processBody();
                        break;
                    default:
                        throw new IllegalStateException("Invalid state: " + state);
                }
            }
            if (closeWhenComplete && isStalled()) {
                close();
            }
        } finally {
            inDelivery = false;
        }
    }

    /**
     * Prepare next message frame to be processed.
     *
     * @return true if there are pending messages to read.
     */
    private boolean readRequiredBytes() {
        if (nextFrame == null) {
            nextFrame = new CompositeContent();
        }
        // Read until the buffer contains all the required bytes.
        int missingBytes;
        while ((missingBytes = requiredLength - nextFrame.readableBytes()) > 0) {
            if (unprocessed.readableBytes() == 0) {
                // No more data is available.
                return false;
            }
            int toRead = Math.min(missingBytes, unprocessed.readableBytes());
            while (toRead > 0) {
                ByteBuf buffer = unprocessed.readBuffer(toRead);
                nextFrame.addBuffer(buffer);
                toRead -= buffer.readableBytes();
            }

        }
        return true;
    }

    /**
     * Processes headers bytes of message frames.
     */
    private void processHeader() {
        int type = nextFrame.readUnsignedByte();
        if ((type & RESERVED_MASK) != 0) {
            throw Status.Code.INTERNAL.toStatus().withDescription("Frame header malformed: reserved bits not zero")
                    .asRuntimeException();
        }
        compressedFlag = (type & COMPRESSED_FLAG_MASK) != 0;
        // Update the required length to include the length of the frame.
        requiredLength = nextFrame.readInt();
        if (requiredLength < 0 || requiredLength > maxInboundMessageSize) {
            throw Status.Code.RESOURCE_EXHAUSTED.toStatus().withDescription(
                    String.format("Frame size %d exceeds maximum: %d. ",
                            requiredLength, maxInboundMessageSize))
                    .asRuntimeException();
        }
        // Continue reading the frame body.
        state = State.BODY;
    }

    /**
     * Processes message body.
     */
    private void processBody() {
        InputStream stream = compressedFlag ? getCompressedBody() : getUncompressedBody();
        listener.messagesAvailable(stream);
        // Done with this frame, begin processing the next header.
        state = State.HEADER;
        requiredLength = HEADER_LENGTH;
        nextFrame.close();
        nextFrame = null;
    }

    private InputStream getUncompressedBody() {
        return new BufferInputStream(nextFrame);
    }

    private InputStream getCompressedBody() {
        if (decompressor == Codec.Identity.NONE) {
            throw Status.Code.INTERNAL.toStatus().withDescription(
                    "Can't decode compressed frame as compression not configured.")
                    .asRuntimeException();
        }
        try {
            return decompressor.decompress(new BufferInputStream(nextFrame));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Buffer Input Stream.
     *
     * <p>
     * Referenced from grpc-java implementation.
     * <p>
     */
    private static final class BufferInputStream extends InputStream implements KnownLength {
        final CompositeContent buffer;

        BufferInputStream(CompositeContent buffer) {
            this.buffer = buffer;
        }

        @Override
        public int available() {
            return buffer.readableBytes();
        }

        @Override
        public int read() {
            if (buffer.readableBytes() == 0) {
                return -1;
            }
            return buffer.readUnsignedByte();
        }

        @Override
        public int read(byte[] dest, int destOffset, int length) throws IOException {
            if (buffer.readableBytes() == 0) {
                // EOF.
                return -1;
            }
            length = Math.min(buffer.readableBytes(), length);
            buffer.readBytes(dest, destOffset, length);
            return length;
        }
    }
}
