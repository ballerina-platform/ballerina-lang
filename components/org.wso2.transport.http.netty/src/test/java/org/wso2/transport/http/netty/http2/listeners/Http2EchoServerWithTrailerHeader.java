/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.transport.http.netty.http2.listeners;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code Http2ServerConnectorListener} is a HttpConnectorListener which receives messages and respond back with
 * different types of HTTP/2 messages along with trailing headers.
 *
 * @since 6.3.0
 */
public class Http2EchoServerWithTrailerHeader extends EchoMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(Http2EchoServerWithTrailerHeader.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private HttpHeaders expectedTrailer;
    private MessageType messageType;

    public enum MessageType {
        REQUEST,
        RESPONSE,
        PUSH_RESPONSE
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setTrailer(HttpHeaders trailers) {
        this.expectedTrailer = trailers;
    }

    @Override
    public void onMessage(HttpCarbonMessage httpRequest) {
        executor.execute(() -> {
            switch (messageType) {
                case REQUEST:
                    String trailerHeaderValue = httpRequest.getHeader(HttpHeaderNames.TRAILER.toString());
                    HttpCarbonMessage httpResponse = getHttpCarbonMessage();
                    do {
                        HttpContent httpContent = httpRequest.getHttpContent();
                        httpResponse.addHttpContent(httpContent);
                        if (httpContent instanceof LastHttpContent) {
                            expectedTrailer = ((LastHttpContent) httpContent).trailingHeaders();
                            break;
                        }
                    }
                    while (true);
                    httpResponse.setHeader("Request-trailer", trailerHeaderValue);
                    httpResponse.getHeaders().add(expectedTrailer);
                    try {
                        httpRequest.respond(httpResponse);
                    } catch (ServerConnectorException e) {
                        LOG.error("Error occurred during message notification: " + e.getMessage());
                    }
                    break;
                case RESPONSE:
                    httpResponse = getHttpCarbonMessage();
                    populateTrailerHeader(httpResponse);
                    do {
                        HttpContent httpContent = httpRequest.getHttpContent();
                        httpResponse.addHttpContent(httpContent);
                        if (httpContent instanceof LastHttpContent) {
                            ((LastHttpContent) httpContent).trailingHeaders().add(expectedTrailer);
                            break;
                        }
                    }
                    while (true);
                    try {
                        httpRequest.respond(httpResponse);
                    } catch (ServerConnectorException e) {
                        LOG.error("Error occurred during message notification: " + e.getMessage());
                    }
                    break;
                case PUSH_RESPONSE:
                    Http2PushPromise promise = new Http2PushPromise(Constants.HTTP_GET_METHOD, "/resource1");
                    HttpResponseFuture responseFuture = null;
                    try {
                        responseFuture = httpRequest.pushPromise(promise);
                        responseFuture.sync();

                    } catch (ServerConnectorException | InterruptedException e) {
                        LOG.error("Error occurred while processing message: " + e.getMessage());
                    }
                    Throwable error = Objects.requireNonNull(responseFuture).getStatus().getCause();
                    if (error != null) {
                        responseFuture.resetStatus();
                        LOG.error("Error occurred while sending push promises " + error.getMessage());
                        break;
                    }

                    // Send Promised response message
                    try {
                        responseFuture = httpRequest.pushResponse(
                                generateResponse("Resource for " + promise.getPath(), expectedTrailer), promise);
                        responseFuture.sync();
                    } catch (ServerConnectorException | InterruptedException e) {
                        LOG.error("Error occurred while processing message: " + e.getMessage());
                    }
                    error = responseFuture.getStatus().getCause();
                    if (error != null) {
                        responseFuture.resetStatus();
                        LOG.error("Error occurred while sending promised response " + error.getMessage());
                    }
                    break;
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {
    }

    private HttpCarbonMessage getHttpCarbonMessage() {
        HttpCarbonMessage httpResponse = new HttpCarbonResponse(
                new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));

        httpResponse.setHeader(HttpHeaderNames.CONNECTION.toString(),
                               HttpHeaderValues.KEEP_ALIVE.toString());
        httpResponse.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
        httpResponse.setHttpStatusCode(HttpResponseStatus.OK.code());
        return httpResponse;
    }

    private void populateTrailerHeader(HttpCarbonMessage httpResponse) {
        String trailerHeaderValue = String.join(",", expectedTrailer.names());
        httpResponse.setHeader(HttpHeaderNames.TRAILER.toString(), trailerHeaderValue);
    }

    private HttpCarbonMessage generateResponse(String response, HttpHeaders expectedTrailer) {
        HttpResponseStatus status = HttpResponseStatus.OK;
        HttpCarbonMessage httpResponse = new HttpCarbonResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1, status));
        httpResponse.setHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
        httpResponse.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
        httpResponse.setHttpStatusCode(status.code());
        populateTrailerHeader(httpResponse);

        DefaultLastHttpContent lastHttpContent;
        if (response != null) {
            byte[] responseByteValues = response.getBytes(StandardCharsets.UTF_8);
            ByteBuffer responseValueByteBuffer = ByteBuffer.wrap(responseByteValues);
            lastHttpContent = new DefaultLastHttpContent(Unpooled.wrappedBuffer(responseValueByteBuffer));
        } else {
            lastHttpContent = new DefaultLastHttpContent();
        }
        lastHttpContent.trailingHeaders().add(expectedTrailer);
        httpResponse.addHttpContent(lastHttpContent);
        return httpResponse;
    }
}
