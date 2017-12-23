/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.message;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.BufferFactory;
import org.wso2.transport.http.netty.common.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Provides input and output stream by taking the HttpCarbonMessage
 */
public class HttpMessageDataStreamer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpMessageDataStreamer.class);

    private HTTPCarbonMessage httpCarbonMessage;

    private HttpMessageDataStreamer.ByteBufferInputStream byteBufferInputStream;
    private HttpMessageDataStreamer.ByteBufferOutputStream byteBufferOutputStream;

    public HttpMessageDataStreamer(HTTPCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
    }

    /**
     * A class which represents the InputStream of the ByteBuffers
     * No need to worry about thread safety of this class this is called only once by
     * for a message instance from one thread.
     */
    protected class ByteBufferInputStream extends InputStream {

        private int count;
        private boolean chunkFinished = true;
        private int limit;
        private ByteBuffer byteBuffer;
        private HttpContent httpContent;

        @Override
        public int read() throws IOException {
            if ((httpContent instanceof LastHttpContent) && chunkFinished) {
                return -1;
            } else if (chunkFinished) {
                httpContent = httpCarbonMessage.getHttpContent();
                if (httpContent == null) {
                    throw new IOException("No entity was added to the queue before the timeout");
                }
                byteBuffer = httpContent.content().nioBuffer();
                count = 0;
                limit = byteBuffer.limit();
                if (limit == 0) {
                    return -1;
                }
                chunkFinished = false;
            }
            count++;
            if (count == limit) {
                int value = byteBuffer.get() & 0xff;
                chunkFinished = true;
                byteBuffer = null;
                httpContent.release();

                return value;
            }
            return byteBuffer.get() & 0xff;
        }
    }

    /**
     * A class which write byteStream into ByteBuffers and add those
     * ByteBuffers to Content Queue.
     * No need to worry about thread safety of this class this is called only once by
     * one thread at particular time.
     */
    protected class ByteBufferOutputStream extends OutputStream {

        private ByteBuffer buffer;

        @Override
        public void write(int b) throws IOException {
            if (buffer == null) {
                buffer = BufferFactory.getInstance().getBuffer();
            }
            if (buffer.hasRemaining()) {
                buffer.put((byte) b);
            } else {
                buffer.flip();
                httpCarbonMessage.addHttpContent(new DefaultHttpContent(Unpooled.wrappedBuffer(buffer)));
                buffer = BufferFactory.getInstance().getBuffer();
                buffer.put((byte) b);
            }
        }

        @Override
        public void flush() throws IOException {
            if (buffer != null && buffer.position() > 0) {
                buffer.flip();
                httpCarbonMessage.addHttpContent(new DefaultHttpContent(Unpooled.wrappedBuffer(buffer)));
                buffer = BufferFactory.getInstance().getBuffer();
            }
        }

        @Override
        public void close() {
            try {
                flush();
                httpCarbonMessage.addHttpContent(new DefaultLastHttpContent());
                super.close();
            } catch (IOException e) {
                LOG.error("Error while closing output stream but underlying resources are reset", e);
            } finally {
                byteBufferOutputStream = null;
                buffer = null;
            }
        }
    }

    public OutputStream getOutputStream() {
        if (byteBufferOutputStream == null) {
            byteBufferOutputStream = new HttpMessageDataStreamer.ByteBufferOutputStream();
        }
        return byteBufferOutputStream;
    }

    private InputStream createInputStreamIfNull() {
        if (byteBufferInputStream == null) {
            byteBufferInputStream = new HttpMessageDataStreamer.ByteBufferInputStream();
        }
        return byteBufferInputStream;
    }

    public InputStream getInputStream() {
        String contentEncodingHeader = httpCarbonMessage.getHeader(Constants.CONTENT_ENCODING);
        if (contentEncodingHeader != null) {
            // removing the header because, we are handling the decoded content and we need to send out
            // as encoded one. so once this header is removed, transport will encode again by looking the
            // accept-encoding request header
            httpCarbonMessage.removeHeader(Constants.CONTENT_ENCODING);
            try {
                if (contentEncodingHeader.equalsIgnoreCase(Constants.ENCODING_GZIP)) {
                    return new GZIPInputStream(createInputStreamIfNull());
                } else if (contentEncodingHeader.equalsIgnoreCase(Constants.ENCODING_DEFLATE)) {
                    return new InflaterInputStream(createInputStreamIfNull());
                } else {
                    LOG.warn("Unknown Content-Encoding: " + contentEncodingHeader);
                }
            } catch (IOException e) {
                LOG.error("Error while creating inputStream for content-encoding: " + contentEncodingHeader, e);
            }
        }
        return createInputStreamIfNull();
    }
}
