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

package org.wso2.transport.http.netty.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpResponseFuture;
import org.wso2.transport.http.netty.contractimpl.HttpWsServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.listener.states.ListenerReqRespStateManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP based representation for HttpCarbonMessage.
 */
public class HttpCarbonMessage {

    protected HttpMessage httpMessage;
    private EntityCollector blockingEntityCollector;
    private Map<String, Object> properties = new HashMap<>(Constants.HTTP_CARBON_MESSAGE_PROPERTIES_MAP_DEFAULT_SIZE);

    private MessageFuture messageFuture;
    private final ServerConnectorFuture httpOutboundRespFuture = new HttpWsServerConnectorFuture();
    private final DefaultHttpResponseFuture httpOutboundRespStatusFuture = new DefaultHttpResponseFuture();
    private final Observable contentObservable = new DefaultObservable();
    private HttpHeaders httpTrailerHeaders = new DefaultLastHttpContent().trailingHeaders();
    private IOException ioException;
    public ListenerReqRespStateManager listenerReqRespStateManager;
    private Http2MessageStateContext http2MessageStateContext;
    private FullHttpMessageFuture fullHttpMessageFuture;

    private long sequenceId; //Keep track of request/response order
    private ChannelHandlerContext sourceContext;
    private ChannelHandlerContext targetContext;
    private HttpPipeliningFuture pipeliningFuture;
    private boolean keepAlive;
    private boolean pipeliningEnabled;
    private boolean passthrough = false;
    private boolean lastHttpContentArrived = false;
    private String httpVersion;
    private String httpMethod;
    private String requestUrl;
    private Integer httpStatusCode;

    public HttpCarbonMessage(HttpMessage httpMessage, Listener contentListener) {
        this.httpMessage = httpMessage;
        setBlockingEntityCollector(new BlockingEntityCollector(Constants.ENDPOINT_TIMEOUT));
        this.contentObservable.setListener(contentListener);
    }

    public HttpCarbonMessage(HttpMessage httpMessage, int maxWaitTime, Listener contentListener) {
        this.httpMessage = httpMessage;
        setBlockingEntityCollector(new BlockingEntityCollector(maxWaitTime));
        this.contentObservable.setListener(contentListener);
    }

    public HttpCarbonMessage(HttpMessage httpMessage) {
        this.httpMessage = httpMessage;
        setBlockingEntityCollector(new BlockingEntityCollector(Constants.ENDPOINT_TIMEOUT));
    }

    /**
     * Add http content to HttpCarbonMessage.
     *
     * @param httpContent chunks of the payload.
     */
    public synchronized void addHttpContent(HttpContent httpContent) {
        contentObservable.notifyAddListener(httpContent);
        if (messageFuture != null) {
            if (ioException != null) {
                blockingEntityCollector.addHttpContent(new DefaultLastHttpContent());
                messageFuture.notifyMessageListener(blockingEntityCollector.getHttpContent());
                removeMessageFuture();
                throw new RuntimeException(this.getIoException());
            }
            blockingEntityCollector.addHttpContent(httpContent);
            if (messageFuture.isMessageListenerSet()) {
                messageFuture.notifyMessageListener(blockingEntityCollector.getHttpContent());
                //This should only be called once the message listener is set and the HttpContent is retrieved from the
                //blocking entity collector. Calling this before that will raise a race condition in passthrough
                //scenario.
                contentObservable.notifyGetListener(httpContent);
            }
            // We remove the feature as the message has reached it life time. If there is a need
            // for using the same message again, we need to set the future again and restart
            // the life-cycle.
            if (httpContent instanceof LastHttpContent) {
                removeMessageFuture();
            }
        } else {
            if (ioException != null) {
                blockingEntityCollector.addHttpContent(new DefaultLastHttpContent());
                throw new RuntimeException(this.getIoException());
            } else {
                blockingEntityCollector.addHttpContent(httpContent);
            }
        }
    }

    /**
     * Get the available content of HttpCarbonMessage.
     *
     * @return HttpContent.
     */
    public HttpContent getHttpContent() {
        HttpContent httpContent = this.blockingEntityCollector.getHttpContent();
        this.contentObservable.notifyGetListener(httpContent);
        return httpContent;
    }

    public synchronized MessageFuture getHttpContentAsync() {
        this.messageFuture = new MessageFuture(this);
        return this.messageFuture;
    }

    /**
     * @deprecated
     * @return the message body.
     */
    @Deprecated
    public ByteBuf getMessageBody() {
        return blockingEntityCollector.getMessageBody();
    }

    /**
     * Check if the payload empty.
     *
     * @return true or false.
     */
    public boolean isEmpty() {
        return blockingEntityCollector.isEmpty();
    }

    /**
     * Count the message length till the given message length and returns.
     * If the message length is shorter than the given length it returns with the
     * available message size. This method is blocking function. Hence, use with care.
     *
     * @param maxLength is the maximum length to count
     * @return counted length
     * @throws IllegalStateException if illegal state occurs in the absence of content
     */
    public long countMessageLengthTill(long maxLength) throws IllegalStateException {
        return this.blockingEntityCollector.countMessageLengthTill(maxLength);
    }

    /**
     * Return the length of entire payload. This is a blocking method.
     * @return the length.
     */
    @Deprecated
    public long getFullMessageLength() {
        return blockingEntityCollector.getFullMessageLength();
    }

    /**
     * @deprecated
     * @param msgBody the message body.
     */
    @Deprecated
    public void addMessageBody(ByteBuffer msgBody) {
        blockingEntityCollector.addMessageBody(msgBody);
    }

    public void completeMessage() {
        blockingEntityCollector.completeMessage();
    }

    /**
     * Returns the header map of the request.
     *
     * @return all headers.
     */
    public HttpHeaders getHeaders() {
        return this.httpMessage.headers();
    }

    /**
     * Return the value of the given header name.
     *
     * @param key name of the header.
     * @return value of the header.
     */
    public String getHeader(String key) {
        return httpMessage.headers().get(key);
    }

    /**
     * Set the header value for the given name.
     *
     * @param key header name.
     * @param value header value.
     */
    public void setHeader(String key, String value) {
        this.httpMessage.headers().set(key, value);
    }

    /**
     * Set the header value for the given name.
     *
     * @param key header name.
     * @param value header value as object.
     */
    public void setHeader(String key, Object value) {
        this.httpMessage.headers().set(key, value);
    }

    /**
     * Let you set a set of headers.
     *
     * @param httpHeaders set of headers that needs to be set.
     */
    public void setHeaders(HttpHeaders httpHeaders) {
        this.httpMessage.headers().setAll(httpHeaders);
    }

    /**
     * Remove the header using header name.
     *
     * @param key header name.
     */
    public void removeHeader(String key) {
        httpMessage.headers().remove(key);
    }

    /**
     * Returns the trailer header map of the message. Dev should use this API to set/get trailer headers rather
     * dealing with LastHttpContent.
     *
     * @return all trailer headers.
     */
    public HttpHeaders getTrailerHeaders() {
        return httpTrailerHeaders;
    }

    public Object getProperty(String key) {
        if (properties != null) {
            return properties.get(key);
        } else {
            return null;
        }
    }

    public synchronized void removeMessageFuture() {
        this.messageFuture = null;
        // To ensure that the carbon message is reusable.
        passthrough = false;
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

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    private void setBlockingEntityCollector(BlockingEntityCollector blockingEntityCollector) {
        this.blockingEntityCollector = blockingEntityCollector;
    }

    /**
     * Returns the future responsible for sending back the response.
     *
     * @return httpOutboundRespFuture.
     */
    public ServerConnectorFuture getHttpResponseFuture() {
        return this.httpOutboundRespFuture;
    }

    /**
     * Returns the future responsible for notifying the response status.
     *
     * @return httpOutboundRespStatusFuture.
     */
    public HttpResponseFuture getHttpOutboundRespStatusFuture() {
        return httpOutboundRespStatusFuture;
    }

    public HttpResponseFuture respond(HttpCarbonMessage httpCarbonMessage) throws ServerConnectorException {
        httpOutboundRespFuture.notifyHttpListener(httpCarbonMessage);
        return httpOutboundRespStatusFuture;
    }

    /**
     * Sends a push response message back to the client.
     *
     * @param httpCarbonMessage the push response message
     * @param pushPromise       the push promise associated with the push response message
     * @return HttpResponseFuture which gives the status of the operation
     * @throws ServerConnectorException if there is an error occurs while doing the operation
     */
    public HttpResponseFuture pushResponse(HttpCarbonMessage httpCarbonMessage, Http2PushPromise pushPromise)
            throws ServerConnectorException {
        httpOutboundRespFuture.notifyHttpListener(httpCarbonMessage, pushPromise);
        return httpOutboundRespStatusFuture;
    }

    /**
     * Sends a push promise message back to the client.
     *
     * @param pushPromise the push promise message
     * @return HttpResponseFuture which gives the status of the operation
     * @throws ServerConnectorException if there is an error occurs while doing the operation
     */
    public HttpResponseFuture pushPromise(Http2PushPromise pushPromise)
            throws ServerConnectorException {
        httpOutboundRespFuture.notifyHttpListener(pushPromise);
        return httpOutboundRespStatusFuture;
    }

    /**
     * Copy Message properties and transport headers.
     *
     * @return HttpCarbonMessage.
     */
    public HttpCarbonMessage cloneCarbonMessageWithOutData() {
        HttpCarbonMessage newCarbonMessage = getNewHttpCarbonMessage();

        Map<String, Object> propertiesMap = this.getProperties();
        propertiesMap.forEach(newCarbonMessage::setProperty);
        newCarbonMessage.setHttpStatusCode(this.getHttpStatusCode());
        newCarbonMessage.setHttpMethod(this.getHttpMethod());
        newCarbonMessage.setRequestUrl(this.getRequestUrl());
        newCarbonMessage.setHttpVersion(this.getHttpVersion());

        return newCarbonMessage;
    }

    private HttpCarbonMessage getNewHttpCarbonMessage() {
        HttpMessage newHttpMessage;
        HttpHeaders httpHeaders;
        if (this.httpMessage instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) this.httpMessage;
            newHttpMessage = new DefaultHttpRequest(this.httpMessage.protocolVersion(),
                    ((HttpRequest) this.httpMessage).method(), httpRequest.uri());

            httpHeaders = new DefaultHttpHeaders();
            List<Map.Entry<String, String>> headerList = this.httpMessage.headers().entries();
            for (Map.Entry<String, String> entry : headerList) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        } else {
            HttpResponse httpResponse = (HttpResponse) this.httpMessage;
            newHttpMessage = new DefaultFullHttpResponse(this.httpMessage.protocolVersion(), httpResponse.status());

            httpHeaders = new DefaultHttpHeaders();
            List<Map.Entry<String, String>> headerList = this.httpMessage.headers().entries();
            for (Map.Entry<String, String> entry : headerList) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }
        HttpCarbonMessage httpCarbonMessage = new HttpCarbonMessage(newHttpMessage);
        httpCarbonMessage.getHeaders().set(httpHeaders);
        return httpCarbonMessage;
    }

    /**
     * Wait till the entire payload is received. This is important to avoid data corruption.
     * Before a set a new set of payload, we need remove the existing ones.
     */
    public void waitAndReleaseAllEntities() {
        blockingEntityCollector.waitAndReleaseAllEntities();
    }

    public EntityCollector getBlockingEntityCollector() {
        return blockingEntityCollector;
    }

    /**
     * Gives the underling netty request message.
     * @return netty request message
     */
    public HttpRequest getNettyHttpRequest() {
        return (HttpRequest) this.httpMessage;
    }

    /**
     * Gives the underling netty response message.
     * @return netty response message
     */
    public HttpResponse getNettyHttpResponse() {
        return (HttpResponse) this.httpMessage;
    }

    public synchronized IOException getIoException() {
        return ioException;
    }

    public synchronized void setIoException(IOException ioException) {
        this.ioException = ioException;
    }

    public Http2MessageStateContext getHttp2MessageStateContext() {
        return http2MessageStateContext;
    }

    public void setHttp2MessageStateContext(Http2MessageStateContext http2MessageStateContext) {
        this.http2MessageStateContext = http2MessageStateContext;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public ChannelHandlerContext getSourceContext() {
        return sourceContext;
    }

    /**
     * Set the source context. This is needed only for pipelining and HTTP/1.1 throttling.
     *
     * @param sourceContext represent the source context
     */
    public void setSourceContext(ChannelHandlerContext sourceContext) {
        this.sourceContext = sourceContext;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isPipeliningEnabled() {
        return pipeliningEnabled;
    }

    /**
     * Can be used to detect if a request is expecting 100 continue.
     *
     * @return true if the request is expecting 100 continue
     */
    public boolean is100ContinueExpected() {
        return HttpUtil.is100ContinueExpected(httpMessage);
    }

    public void setPipeliningEnabled(boolean pipeliningEnabled) {
        this.pipeliningEnabled = pipeliningEnabled;
    }

    public HttpPipeliningFuture getPipeliningFuture() {
        return pipeliningFuture;
    }

    /**
     * Sets the pipelining future to the outbound response. This method's only usage is in ballerina side, hence it
     * should not be removed.
     *
     * @param pipeliningFuture Represents pipelining future which is used for binding pipelining listener
     */
    public void setPipeliningFuture(HttpPipeliningFuture pipeliningFuture) {
        this.pipeliningFuture = pipeliningFuture;
    }

    /**
     * Removes the content listener that is set for handling inbound throttling in case of HTTP/1.1. For HTTP/2, flow
     * control cannot be disabled (https://tools.ietf.org/html/rfc7540#page-23). To prevent HTTP/2 streams from
     * stalling, receiver must aggressively update the window size kept by the sender, hence listener should not be
     * removed for HTTP/2.
     */
    public void removeInboundContentListener() {
        contentObservable.notifyReadInterest();
        String httpVersion = this.getHttpVersion();
        if (Constants.HTTP_1_1_VERSION.equalsIgnoreCase(httpVersion)) {
            contentObservable.removeListener();
        }
    }

    /**
     * The passthrough(when message body is not built) status of the message.
     *
     * @return true if it is a passthrough.
     */
    public boolean isPassthrough() {
        return passthrough;
    }

    /**
     * This value is to be set when sending the message to the consumer without building/processing it in the
     * application layer.
     *
     * @param passthrough if the message is a passthrough.
     */
    public void setPassthrough(boolean passthrough) {
        this.passthrough = passthrough;
    }

    /**
     * @param targetContext The target handler context.
     */
    public void setTargetContext(ChannelHandlerContext targetContext) {
        this.targetContext = targetContext;
    }

    /**
     * @return the target handler context for this message.
     */
    public ChannelHandlerContext getTargetContext() {
        return targetContext;
    }

    /**
     * Returns the {@link FullHttpMessageFuture} which notifies {@link FullHttpMessageListener} when the complete
     * content of the {@link HttpCarbonMessage} is accumulated. Should never remove content listener for HTTP/2.
     *
     * @return the default implementation of the {@link FullHttpMessageFuture}.
     */
    public synchronized FullHttpMessageFuture getFullHttpCarbonMessage() {
        removeInboundContentListener();
        fullHttpMessageFuture = new DefaultFullHttpMessageFuture(this);
        return fullHttpMessageFuture;
    }

    /**
     * Sets the lastHttpContentArrived flag true upon the last HTTP content arrival and notifies the
     * {@link FullHttpMessageFuture} if available.
     */
    public synchronized void setLastHttpContentArrived() {
        this.lastHttpContentArrived = true;
        if (fullHttpMessageFuture != null) {
            fullHttpMessageFuture.notifySuccess();
        }
    }

    synchronized boolean isLastHttpContentArrived() {
        return lastHttpContentArrived;
    }

    /**
     * Notifies {@link FullHttpMessageListener} if the content accumulation fails.
     *
     * @param exception of content accumulation
     */
    public synchronized void notifyContentFailure(Exception exception) {
        if (fullHttpMessageFuture != null) {
            fullHttpMessageFuture.notifyFailure(exception);
        }
    }

    public Listener getListener() {
        return this.contentObservable.getListener();
    }
}
