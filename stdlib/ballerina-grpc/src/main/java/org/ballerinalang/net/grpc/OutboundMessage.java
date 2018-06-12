/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.grpc;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Class that represents an HTTP response in MSF4J level.
 */
public class OutboundMessage {

    private static final String COMMA_SEPARATOR = ", ";
    private static final int NULL_STATUS_CODE = -1;
    public static final int NO_CHUNK = 0;
    public static final int DEFAULT_CHUNK_SIZE = -1;

    private final HTTPCarbonMessage responseMessage;
    private int statusCode = NULL_STATUS_CODE;
    private String mediaType = null;
    private int chunkSize = NO_CHUNK;
    private MessageFramer framer;
    private boolean outboundClosed;

    public OutboundMessage(HTTPCarbonMessage responseMessage) {
        this.responseMessage = responseMessage;
        this.framer = new MessageFramer(responseMessage);
    }

    public OutboundMessage(InboundMessage inboundMessage) {
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
     * Remove a property from the underlining CarbonMessage object.
     *
     * @param key property key
     */
    public void removeProperty(String key) {
        responseMessage.removeProperty(key);
    }

    /**
     * @return the underlining CarbonMessage object.
     */
    HTTPCarbonMessage getResponseMessage() {
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
     * Set HTTP media type of the response.
     *
     * @param mediaType HTTP media type string
     * @return OutboundMessage object
     */
    public OutboundMessage setMediaType(String mediaType) {

        this.mediaType = mediaType;
        return this;
    }

    /**
     * Set http body for the HTTP response.
     *
     * @param entity object that should be set as the response body
     */
    public void sendMessage(InputStream entity) {

//        if (responseMessage.isEmpty()) {
//            throw new IllegalStateException("CarbonMessage should not contain a message body");
//        }
        if (entity != null) {
            framer().writePayload(entity);
            framer().flush();
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(0);
            responseMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
        }
    }

    /**
     * Specify the chunk size to send the response.
     *
     * @param chunkSize if 0 response will be sent without chunking
     *                  if -1 a default chunk size will be applied
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void complete(Status status, io.netty.handler.codec.http.HttpHeaders trailers) {

        framer().flush();
        addStatusToTrailers(status, trailers);
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        lastHttpContent.trailingHeaders().set(trailers);
        responseMessage.addHttpContent(lastHttpContent);
    }

    private void addStatusToTrailers(Status status, io.netty.handler.codec.http.HttpHeaders trailers) {
        trailers.remove("grpc-status");
        trailers.remove("grpc-message");
        trailers.add("grpc-status", status);
        if (status.getDescription() != null) {
            trailers.add("grpc-message", status.getDescription());
        }
    }

    public final void halfClose() {
        if (!outboundClosed) {
            outboundClosed = true;
            framer().close();
        }
    }

    protected final MessageFramer framer() {
        return framer;
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
}
