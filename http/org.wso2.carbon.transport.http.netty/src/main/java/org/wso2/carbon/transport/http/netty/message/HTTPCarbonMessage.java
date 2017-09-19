/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.BufferFactory;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.BootstrapConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * HTTP based representation for CarbonMessage.
 */
public class HTTPCarbonMessage extends CarbonMessage {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPCarbonMessage.class);

    private BlockingQueue<HttpContent> httpContentQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<HttpContent> outContentQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<HttpContent> garbageCollected = new LinkedBlockingQueue<>();

    private ByteBufferInputStream byteBufferInputStream;
    private ByteBufferOutputStream byteBufferOutputStream;

    private int soTimeOut = 60;
    private ServerConnectorFuture serverConnectorFuture = new HttpWsServerConnectorFuture();

    public HTTPCarbonMessage() {
        BootstrapConfiguration clientBootstrapConfig = BootstrapConfiguration.getInstance();
        if (clientBootstrapConfig != null) {
            soTimeOut = clientBootstrapConfig.getSocketTimeout();
            return;
        }
        ServerBootstrapConfiguration serverBootstrapConfiguration = ServerBootstrapConfiguration.getInstance();
        if (serverBootstrapConfiguration != null) {
            soTimeOut = serverBootstrapConfiguration.getSoTimeOut();
        }
    }

    public void addHttpContent(HttpContent httpContent) {
        try {
            httpContentQueue.put(httpContent);
        } catch (InterruptedException e) {
            LOG.error("Cannot put content to queue", e);
        }
    }

    public HttpContent getHttpContent() {
        try {
            return httpContentQueue.poll(soTimeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Error while retrieving http content from queue.", e);
            return null;
        }
    }

    @Override
    public ByteBuffer getMessageBody() {
        try {
            HttpContent httpContent = httpContentQueue.poll(soTimeOut, TimeUnit.SECONDS);
            if (httpContent instanceof LastHttpContent) {
                this.endOfMsgAdded.set(true);
            }
            ByteBuf buf = httpContent.content();
            garbageCollected.add(httpContent);
            return buf.nioBuffer();
        } catch (InterruptedException e) {
            LOG.error("Error while retrieving message body from queue.", e);
            return null;
        }
    }

    @Override
    public List<ByteBuffer> getFullMessageBody() {
        List<ByteBuffer> byteBufferList = new ArrayList<>();

        boolean isEndOfMessageProcessed = false;
        while (!isEndOfMessageProcessed) {
            try {
                HttpContent httpContent = httpContentQueue.poll(soTimeOut, TimeUnit.SECONDS);
                // This check is to make sure we add the last http content after getClone and avoid adding
                // empty content to bytebuf list again and again
                if (httpContent instanceof EmptyLastHttpContent) {
                    break;
                }

                if (httpContent instanceof LastHttpContent) {
                    isEndOfMessageProcessed = true;
                }
                ByteBuf buf = httpContent.content();
                garbageCollected.add(httpContent);
                byteBufferList.add(buf.nioBuffer());
            } catch (InterruptedException e) {
                LOG.error("Error while getting full message body", e);
            }
        }

        return byteBufferList;
    }

    public InputStream getInputStream() {
        String contentEncodingHeader = getHeader(Constants.CONTENT_ENCODING);
        if (contentEncodingHeader != null) {
            // removing the header because, we are handling the decoded content and we need to send out
            // as encoded one. so once this header is removed, transport will encode again by looking the
            // accept-encoding request header
            removeHeader(Constants.CONTENT_ENCODING);
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

    @Override
    public boolean isEmpty() {
        return this.httpContentQueue.isEmpty();
    }

    public int getFullMessageLength() {
        List<HttpContent> contentList = new ArrayList<>();
        boolean isEndOfMessageProcessed = false;
        while (!isEndOfMessageProcessed) {
            try {
                HttpContent httpContent = httpContentQueue.poll(soTimeOut, TimeUnit.SECONDS);
                if ((httpContent instanceof LastHttpContent) || (isEndOfMsgAdded() && httpContentQueue.isEmpty())) {
                    isEndOfMessageProcessed = true;
                }
                contentList.add(httpContent);

            } catch (InterruptedException e) {
                LOG.error("Error while getting full message length", e);
            }
        }
        int size = 0;
        for (HttpContent httpContent : contentList) {
            size += httpContent.content().readableBytes();
            httpContentQueue.add(httpContent);
        }

        return size;
    }

    @Override
    public boolean isEndOfMsgAdded() {
        return super.isEndOfMsgAdded();
    }

    @Override
    public void addMessageBody(ByteBuffer msgBody) {
        if (isAlreadyRead()) {
            outContentQueue.add(new DefaultHttpContent(Unpooled.copiedBuffer(msgBody)));
        } else {
            httpContentQueue.add(new DefaultHttpContent(Unpooled.copiedBuffer(msgBody)));
        }
    }

    public void markMessageEnd() {
        if (isAlreadyRead()) {
            outContentQueue.add(new EmptyLastHttpContent());
        } else {
            httpContentQueue.add(new EmptyLastHttpContent());
        }
    }

    public void setEndOfMsgAdded(boolean endOfMsgAdded) {
        this.endOfMsgAdded.compareAndSet(false, endOfMsgAdded);
        if (isAlreadyRead()) {
            httpContentQueue.addAll(outContentQueue);
            outContentQueue.clear();
        }
        if (byteBufferOutputStream != null) {
            try {
                this.byteBufferOutputStream.flush();
            } catch (IOException e) {
                LOG.error("Exception occurred while flushing the buffer", e);
                byteBufferOutputStream.close();
            }
        }
    }

    public void release() {
        httpContentQueue.forEach(ReferenceCounted::release);
        garbageCollected.forEach(ReferenceCounted::release);
    }

    public ServerConnectorFuture getHTTPConnectorFuture() {
        return this.serverConnectorFuture;
    }

    public void respond(HTTPCarbonMessage httpCarbonMessage) throws ServerConnectorException {
        serverConnectorFuture.notifyHttpListener(httpCarbonMessage);
    }

    /**
     * Copy Message properties and transport headers
     *
     * @return CarbonMessage
     */
    public HTTPCarbonMessage cloneCarbonMessageWithOutData() {
        HTTPCarbonMessage newCarbonMessage = new HTTPCarbonMessage();
//        newCarbonMessage.setBufferContent(this.isBufferContent());
//        newCarbonMessage.setWriter(this.getWriter());

        List<Header> transportHeaders = this.getHeaders().getClone();
        newCarbonMessage.setHeaders(transportHeaders);

        Map<String, Object> propertiesMap = this.getProperties();
        propertiesMap.forEach(newCarbonMessage::setProperty);

        newCarbonMessage.setFaultHandlerStack(this.getFaultHandlerStack());
        return newCarbonMessage;
    }

    /**
     * Copy the Full carbon message with data
     *
     * @return carbonMessage
     */
    public HTTPCarbonMessage cloneCarbonMessageWithData() {

        HTTPCarbonMessage httpCarbonMessage = new HTTPCarbonMessage();
//        httpCarbonMessage.setBufferContent(this.isBufferContent());
//        httpCarbonMessage.setWriter(this.getWriter());

        List<Header> transportHeaders = this.getHeaders().getClone();
        httpCarbonMessage.setHeaders(transportHeaders);

        Map<String, Object> propertiesMap = this.getProperties();
        propertiesMap.forEach(httpCarbonMessage::setProperty);

        httpCarbonMessage.setFaultHandlerStack(this.getFaultHandlerStack());

        this.getCopyOfFullMessageBody().forEach(httpCarbonMessage::addMessageBody);
        httpCarbonMessage.setEndOfMsgAdded(true);
        return httpCarbonMessage;
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

        @Override
        public int read() throws IOException {
            setAlreadyRead(true); // TODO: No need to set this again and again
            if (isEndOfMsgAdded() && isEmpty() && chunkFinished) {
                return -1;
            } else if (chunkFinished) {
                byteBuffer = getMessageBody();
                count = 0;
                limit = byteBuffer.limit();
                if (limit == 0) {
                    return -1;
                }
                chunkFinished = false;
            }
            count++;
            if (count == limit) {
                chunkFinished = true;
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
                addMessageBody(buffer);
                buffer = BufferFactory.getInstance().getBuffer();
                buffer.put((byte) b);
            }
        }

        @Override
        public void flush() throws IOException {
            if (buffer != null && buffer.position() > 0) {
                buffer.flip();
                addMessageBody(buffer);
                buffer = BufferFactory.getInstance().getBuffer();
            }
        }

        @Override
        public void close() {
            try {
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
            byteBufferOutputStream = new HTTPCarbonMessage.ByteBufferOutputStream();
        }
        return byteBufferOutputStream;
    }

    public InputStream createInputStreamIfNull() {
        if (byteBufferInputStream == null) {
            byteBufferInputStream = new ByteBufferInputStream();
        }
        return byteBufferInputStream;
    }
}
