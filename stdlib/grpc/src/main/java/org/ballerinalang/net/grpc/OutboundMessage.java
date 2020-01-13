/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.GRPC_MESSAGE_KEY;
import static org.ballerinalang.net.grpc.GrpcConstants.GRPC_STATUS_KEY;

/**
 * Class that represents an GRPC outbound message.
 *
 * @since 0.980.0
 */
public class OutboundMessage {

    private static final int NULL_STATUS_CODE = -1;
    private final HttpCarbonMessage responseMessage;
    private int statusCode = NULL_STATUS_CODE;
    private boolean outboundClosed;
    private final ThreadLocal<MessageFramer> framer;

    public OutboundMessage(HttpCarbonMessage responseMessage) {
        this.responseMessage = responseMessage;
        this.framer = ThreadLocal.withInitial(() -> new MessageFramer(responseMessage));
    }

    OutboundMessage(InboundMessage inboundMessage) {
        this(inboundMessage.getHttpCarbonMessage().cloneCarbonMessageWithOutData());
    }

    /**
     * @return returns true if the message body is empty.
     */
    public boolean isEmpty() {
        return responseMessage.isEmpty();
    }

    /**
     * @return map of headers in the response object.
     */
    public HttpHeaders getHeaders() {
        return responseMessage.getHeaders();
    }

    /**
     * Get a header of the response.
     *
     * @param key header neame
     * @return value of the header
     */
    public String getHeader(String key) {
        return responseMessage.getHeader(key);
    }

    /**
     * Set a header in the response.
     *
     * @param key   header name
     * @param value value of the header
     * @return OutboundMessage object
     */
    public OutboundMessage setHeader(String key, String value) {
        responseMessage.setHeader(key, value);
        return this;
    }

    /**
     * Add a set of headers to the response as a map.
     *
     * @param headerMap headers to be added to the response
     */
    public void setHeaders(Map<String, String> headerMap) {
        headerMap.forEach(responseMessage::setHeader);
    }

    /**
     * Get a property of the CarbonMessage.
     *
     * @param key Property key
     * @return property value
     */
    public Object getProperty(String key) {
        return responseMessage.getProperty(key);
    }

    /**
     * @return map of properties in the CarbonMessage.
     */
    public Map<String, Object> getProperties() {
        return responseMessage.getProperties();
    }

    /**
     * Set a property in the underlining CarbonMessage object.
     *
     * @param key   property key
     * @param value property value
     */
    public void setProperty(String key, Object value) {
        responseMessage.setProperty(key, value);
    }

    /**
     * @param key remove the header with this name.
     */
    public void removeHeader(String key) {
        responseMessage.removeHeader(key);
    }

    /**
     * @return the underlining CarbonMessage object.
     */
    HttpCarbonMessage getResponseMessage() {
        return responseMessage;
    }

    /**
     * Set the status code of the HTTP response.
     *
     * @param statusCode HTTP status code
     * @return OutboundMessage object
     */
    public OutboundMessage setStatus(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Get the status code of the HTTP response.
     *
     * @return status code value
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Set http body for the HTTP response.
     *
     * @param entity object that should be set as the response body
     */
    public void sendMessage(InputStream entity) {
        if (entity != null) {
            framer().writePayload(entity);
            framer().flush();
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(0);
            responseMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
        }
    }

    /**
     * Close the stream by sending trailer header with gRPC status code.
     *
     * @param status gRPC status
     * @param trailers trailer headers
     */
    public void complete(Status status, HttpHeaders trailers) {
        framer().flush();
        framer().dispose();
        addStatusToTrailers(status, trailers);
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        lastHttpContent.trailingHeaders().set(trailers);
        responseMessage.addHttpContent(lastHttpContent);
    }

    private void addStatusToTrailers(Status status, HttpHeaders trailers) {
        trailers.remove(GRPC_STATUS_KEY);
        trailers.remove(GRPC_MESSAGE_KEY);
        byte[] bytes = Status.CODE_MARSHALLER.toAsciiString(status);
        trailers.add(GRPC_STATUS_KEY, new String(bytes, StandardCharsets.US_ASCII));
        if (status.getDescription() != null) {
            trailers.add(GRPC_MESSAGE_KEY, new String(
                    Status.MESSAGE_MARSHALLER.toAsciiString(status.getDescription()), StandardCharsets.US_ASCII));
        }
    }

    /**
     * Invoked when stream is closed by other parties.
     */
    final void halfClose() {
        if (!outboundClosed) {
            outboundClosed = true;
            framer().close();
        }
    }

    final MessageFramer framer() {
        return framer.get();
    }

    public boolean isReady() {
        return !framer().isClosed();
    }

    public final void setMessageCompression(boolean enable) {
        framer().setMessageCompression(enable);
    }

    public final void flush() {
        if (!framer().isClosed()) {
            framer().flush();
        }
    }

    public String getHttpVersion() {
        return responseMessage.getHttpVersion();
    }

    public void setHttpVersion(String httpVersion) {
        responseMessage.setHttpVersion(httpVersion);
    }

    void setHttpMethod() {
        responseMessage.setHttpMethod(GrpcConstants.HTTP_METHOD);
    }
}
