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

package org.wso2.transport.http.netty.contentaware.listeners;

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
 * {@code TrailerHeaderListener} is a HttpConnectorListener which receives messages and respond back with
 * trailing headers.
 *
 * @since 6.3.0
 */
public class TrailerHeaderListener extends EchoMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(TrailerHeaderListener.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private HttpHeaders expectedTrailer;
    private MessageType messageType;

    public enum MessageType {
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
                case RESPONSE:
                    HttpCarbonMessage httpResponse = getHttpCarbonMessage();
                    do {
                        HttpContent httpContent = httpRequest.getHttpContent();
                        if (httpContent == LastHttpContent.EMPTY_LAST_CONTENT) {
                            httpResponse.addHttpContent(new DefaultLastHttpContent());
                            break;
                        }
                        httpResponse.addHttpContent(httpContent);
                        if (httpContent instanceof LastHttpContent) {
                            break;
                        }
                    }
                    while (true);
                    // test trailer headers lies in the carbon message
                    httpResponse.getTrailerHeaders().add(expectedTrailer);
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

    private HttpCarbonMessage generateResponse(String response, HttpHeaders expectedTrailer) {
        HttpResponseStatus status = HttpResponseStatus.OK;
        HttpCarbonMessage httpResponse = new HttpCarbonResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1, status));
        httpResponse.setHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
        httpResponse.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
        httpResponse.setHttpStatusCode(status.code());

        DefaultLastHttpContent lastHttpContent = new DefaultLastHttpContent();
        if (response != null) {
            byte[] responseByteValues = response.getBytes(StandardCharsets.UTF_8);
            ByteBuffer responseValueByteBuffer = ByteBuffer.wrap(responseByteValues);
            lastHttpContent = new DefaultLastHttpContent(Unpooled.wrappedBuffer(responseValueByteBuffer));
        }
        httpResponse.addHttpContent(lastHttpContent);
        httpResponse.getTrailerHeaders().add(expectedTrailer);
        return httpResponse;
    }
}
