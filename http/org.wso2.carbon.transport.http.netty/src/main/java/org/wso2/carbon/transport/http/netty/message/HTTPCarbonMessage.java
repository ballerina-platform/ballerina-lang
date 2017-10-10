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

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.messaging.MessageUtil;
import org.wso2.carbon.messaging.exceptions.MessagingException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.BootstrapConfiguration;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTTP based representation for HTTPCarbonMessage.
 */
public class HTTPCarbonMessage {

    protected HttpMessage httpMessage;
    private EntityCollector blockingEntityCollector;
    private Map<String, Object> properties = new HashMap<>();

    private MessagingException messagingException = null;
    private MessageDataSource messageDataSource;
    private ServerConnectorFuture serverConnectorFuture = new HttpWsServerConnectorFuture();

    public HTTPCarbonMessage(HttpMessage httpMessage) {
        int soTimeOut = 60;
        BootstrapConfiguration clientBootstrapConfig = BootstrapConfiguration.getInstance();
        if (clientBootstrapConfig != null) {
            soTimeOut = clientBootstrapConfig.getSocketTimeout();
        } else {
            ServerBootstrapConfiguration serverBootstrapConfiguration = ServerBootstrapConfiguration.getInstance();
            if (serverBootstrapConfiguration != null) {
                soTimeOut = serverBootstrapConfiguration.getSoTimeOut();
            }
        }
        this.httpMessage = httpMessage;
        setBlockingEntityCollector(new BlockingEntityCollector(soTimeOut));
    }

    public void addHttpContent(HttpContent httpContent) {
        blockingEntityCollector.addHttpContent(httpContent);
    }

    public HttpContent getHttpContent() {
        return blockingEntityCollector.getHttpContent();
    }

    public ByteBuffer getMessageBody() {
        return blockingEntityCollector.getMessageBody();
    }

    public List<ByteBuffer> getFullMessageBody() {
        return blockingEntityCollector.getFullMessageBody();
    }

    public boolean isEmpty() {
        return blockingEntityCollector.isEmpty();
    }

    public int getFullMessageLength() {
        return blockingEntityCollector.getFullMessageLength();
    }

    public boolean isEndOfMsgAdded() {
        return blockingEntityCollector.isEndOfMsgAdded();
    }

    public void addMessageBody(ByteBuffer msgBody) {
        blockingEntityCollector.addMessageBody(msgBody);
    }

    private void markMessageEnd() {
        blockingEntityCollector.markMessageEnd();
    }

    public void setEndOfMsgAdded(boolean endOfMsgAdded) {
        blockingEntityCollector.setEndOfMsgAdded(endOfMsgAdded);
    }

    public boolean isAlreadyRead() {
        return blockingEntityCollector.isAlreadyRead();
    }

    public void setAlreadyRead(boolean alreadyRead) {
        blockingEntityCollector.setAlreadyRead(alreadyRead);
    }

    public HttpHeaders getHeaders() {
        return this.httpMessage.headers();
    }

    public String getHeader(String key) {
        return httpMessage.headers().get(key);
    }

    public void setHeader(String key, String value) {
        this.httpMessage.headers().set(key, value);
    }

    public void setHeaders(HttpHeaders httpHeaders) {
        this.httpMessage.headers().setAll(httpHeaders);
    }

    public void removeHeader(String key) {
        httpMessage.headers().remove(key);
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

    public void removeProperty(String key) {
        properties.remove(key);
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

    private void setBlockingEntityCollector(BlockingEntityCollector blockingEntityCollector) {
        this.blockingEntityCollector = blockingEntityCollector;
    }

    public void release() {
        blockingEntityCollector.release();
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
     * @return HTTPCarbonMessage
     */
    public HTTPCarbonMessage cloneCarbonMessageWithOutData() {
        //        HTTPCarbonMessage newCarbonMessage = new HTTPCarbonMessage();
        //        newCarbonMessage.setBufferContent(this.isBufferContent());
        //        newCarbonMessage.setWriter(this.getWriter());
        //        List<Header> transportHeaders = this.getHeaders().getClone();
        //        newCarbonMessage.setHeaders(transportHeaders);

        HTTPCarbonMessage newCarbonMessage = getNewHttpCarbonMessage();

        Map<String, Object> propertiesMap = this.getProperties();
        propertiesMap.forEach(newCarbonMessage::setProperty);

        return newCarbonMessage;
    }

    /**
     * Copy the Full carbon message with data
     *
     * @return carbonMessage
     */
    public HTTPCarbonMessage cloneCarbonMessageWithData() {

        //        HTTPCarbonMessage httpCarbonMessage = new HTTPCarbonMessage();
        //        httpCarbonMessage.setBufferContent(this.isBufferContent());
        //        httpCarbonMessage.setWriter(this.getWriter());
        //        List<Header> transportHeaders = this.getHeaders().getClone();
        //        httpCarbonMessage.setHeaders(transportHeaders);

        HTTPCarbonMessage httpCarbonMessage = getNewHttpCarbonMessage();

        Map<String, Object> propertiesMap = this.getProperties();
        propertiesMap.forEach(httpCarbonMessage::setProperty);

        this.getCopyOfFullMessageBody().forEach(httpCarbonMessage::addMessageBody);
        httpCarbonMessage.setEndOfMsgAdded(true);
        return httpCarbonMessage;
    }

    private HTTPCarbonMessage getNewHttpCarbonMessage() {
        HttpMessage newHttpMessage;
        HttpHeaders httpHeaders;
        if (this.httpMessage instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) this.httpMessage;
            newHttpMessage = new DefaultHttpRequest(this.httpMessage.protocolVersion(),
                    ((HttpRequest) this.httpMessage).method(), httpRequest.uri());

            httpHeaders = new DefaultHttpHeaders();
            List<Map.Entry<String, String>> headerList = this.httpMessage.headers().entries();
            for (Map.Entry<String, String> entry : headerList) {
                httpHeaders.set(entry.getKey(), entry.getValue());
            }
        } else {
            HttpResponse httpResponse = (HttpResponse) this.httpMessage;
            newHttpMessage = new DefaultFullHttpResponse(this.httpMessage.protocolVersion(), httpResponse.status());

            httpHeaders = new DefaultHttpHeaders();
            List<Map.Entry<String, String>> headerList = this.httpMessage.headers().entries();
            for (Map.Entry<String, String> entry : headerList) {
                httpHeaders.set(entry.getKey(), entry.getValue());
            }
        }
        HTTPCarbonMessage httpCarbonMessage = new HTTPCarbonMessage(newHttpMessage);
        httpCarbonMessage.setHeaders(httpHeaders);
        return httpCarbonMessage;
    }

    private List<ByteBuffer> getCopyOfFullMessageBody() {
        List<ByteBuffer> fullMessageBody = getFullMessageBody();
        List<ByteBuffer> newCopy = fullMessageBody.stream().map(MessageUtil::deepCopy)
                .collect(Collectors.toList());
        fullMessageBody.forEach(this::addMessageBody);
        markMessageEnd();
        return newCopy;
    }
}
