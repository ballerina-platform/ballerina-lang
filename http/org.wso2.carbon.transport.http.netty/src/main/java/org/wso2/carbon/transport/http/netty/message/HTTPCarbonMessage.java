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
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.messaging.MessageUtil;
import org.wso2.carbon.messaging.exceptions.MessagingException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.BootstrapConfiguration;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * HTTP based representation for HTTPCarbonMessage.
 */
public class HTTPCarbonMessage {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPCarbonMessage.class);

    protected Headers headers = new Headers();
    protected Map<String, Object> properties = new HashMap<>();

    private BlockingQueue<HttpContent> httpContentQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<HttpContent> outContentQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<HttpContent> garbageCollected = new LinkedBlockingQueue<>();
    private AtomicBoolean alreadyRead = new AtomicBoolean(false);
    private AtomicBoolean endOfMsgAdded = new AtomicBoolean(false);

    private MessagingException messagingException = null;

    private MessageDataSource messageDataSource;

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

    public boolean isEndOfMsgAdded() {
        return endOfMsgAdded.get();
    }

    public void addMessageBody(ByteBuffer msgBody) {
        if (isAlreadyRead()) {
            outContentQueue.add(new DefaultHttpContent(Unpooled.copiedBuffer(msgBody)));
        } else {
            httpContentQueue.add(new DefaultHttpContent(Unpooled.copiedBuffer(msgBody)));
        }
    }

    private void markMessageEnd() {
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

    public Headers getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void setHeader(String key, String value) {
        headers.set(key, value);
    }

    public void setHeaders(Map<String, String> headerMap) {
        headers.set(headerMap);
    }

    public void setHeaders(List<Header> headerList) {
        headers.set(headerList);
    }

    public Object getProperty(String key) {
        if (properties != null) {
            return properties.get(key);
        } else {
            return null;
        }
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }

    public void removeProperty(String key) {
        properties.remove(key);
    }

    /**
     * Copy Message properties and transport headers
     *
     * @return HTTPCarbonMessage
     */
    public HTTPCarbonMessage cloneCarbonMessageWithOutData() {
        HTTPCarbonMessage newCarbonMessage = new HTTPCarbonMessage();
//        newCarbonMessage.setBufferContent(this.isBufferContent());
//        newCarbonMessage.setWriter(this.getWriter());

        List<Header> transportHeaders = this.getHeaders().getClone();
        newCarbonMessage.setHeaders(transportHeaders);

        Map<String, Object> propertiesMap = this.getProperties();
        propertiesMap.forEach(newCarbonMessage::setProperty);

//        newCarbonMessage.setFaultHandlerStack(this.getFaultHandlerStack());
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

//        httpCarbonMessage.setFaultHandlerStack(this.getFaultHandlerStack());

        this.getCopyOfFullMessageBody().forEach(httpCarbonMessage::addMessageBody);
        httpCarbonMessage.setEndOfMsgAdded(true);
        return httpCarbonMessage;
    }

    public List<ByteBuffer> getCopyOfFullMessageBody() {
        List<ByteBuffer> fullMessageBody = getFullMessageBody();
        List<ByteBuffer> newCopy = fullMessageBody.stream().map(MessageUtil::deepCopy)
                .collect(Collectors.toList());
        fullMessageBody.forEach(this::addMessageBody);
        markMessageEnd();
        return newCopy;
    }

    public boolean isAlreadyRead() {
        return alreadyRead.get();
    }

    public void setAlreadyRead(boolean alreadyRead) {
        this.alreadyRead.set(alreadyRead);
    }

    public MessageDataSource getMessageDataSource() {
        return messageDataSource;
    }

    public void setMessageDataSource(MessageDataSource messageDataSource) {
        this.messageDataSource = messageDataSource;
    }

    /**
     * Get CarbonMessageException
     *
     * @return CarbonMessageException instance related to faulty HTTPCarbonMessage.
     */
    public MessagingException getMessagingException() {
        return messagingException;
    }

    /**
     * Set CarbonMessageException.
     *
     * @param messagingException exception related to faulty HTTPCarbonMessage.
     */
    public void setMessagingException(MessagingException messagingException) {
        this.messagingException = messagingException;
    }
}
