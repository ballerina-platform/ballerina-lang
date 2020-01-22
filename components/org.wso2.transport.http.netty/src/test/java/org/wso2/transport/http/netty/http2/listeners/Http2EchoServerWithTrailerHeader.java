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

import io.netty.handler.codec.http.DefaultHttpResponse;
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
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code Http2ServerConnectorListener} is a HttpConnectorListener which receives messages and respond back with
 * different types of HTTP/2 messages depending on the request.
 */
public class Http2EchoServerWithTrailerHeader extends EchoMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(Http2EchoServerWithTrailerHeader.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private HttpHeaders expectedTrailer;
    private boolean request;

    public void setMessageType(boolean request) {
        this.request = request;
    }

    public Http2EchoServerWithTrailerHeader setTrailer(HttpHeaders trailers) {
        this.expectedTrailer = trailers;
        return this;
    }

    @Override
    public void onMessage(HttpCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                if (request) {
                    String trailerHeaderValue = httpRequest.getHeader(HttpHeaderNames.TRAILER.toString());
                    HttpHeaders trailingHeaders;
                    do {
                        HttpContent httpContent = httpRequest.getHttpContent();
                        if (httpContent instanceof LastHttpContent) {
                            trailingHeaders = ((LastHttpContent) httpContent).trailingHeaders();
                            break;
                        }
                    }
                    while (true);

                    HttpCarbonMessage httpResponse = getHttpCarbonMessage();
                    httpResponse.setHeader("Request-trailer", trailerHeaderValue);
                    httpResponse.getHeaders().add(trailingHeaders);
                    httpRequest.respond(httpResponse);
                } else {
                    HttpCarbonMessage httpResponse = getHttpCarbonMessage();
                    String trailerHeaderValue = String.join(",", expectedTrailer.names());
                    httpResponse.setHeader(HttpHeaderNames.TRAILER.toString(), trailerHeaderValue);
                    do {
                        HttpContent httpContent = httpRequest.getHttpContent();
                        httpResponse.addHttpContent(httpContent);
                        if (httpContent instanceof LastHttpContent) {
                            ((LastHttpContent) httpContent).trailingHeaders().add(expectedTrailer);
                            break;
                        }
                    }
                    while (true);
                    httpRequest.respond(httpResponse);
                }
            } catch (ServerConnectorException e) {
                LOG.error("Error occurred during message notification: " + e.getMessage());
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
}
