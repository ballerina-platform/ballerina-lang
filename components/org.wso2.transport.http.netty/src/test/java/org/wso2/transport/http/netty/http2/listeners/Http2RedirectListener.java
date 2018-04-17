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
 *
 */

package org.wso2.transport.http.netty.http2.listeners;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code Http2RedirectListener} is a HttpConnectorListener which receives messages and respond back with
 * redirect response messages.
 */
public class Http2RedirectListener implements HttpConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(Http2RedirectListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private int numberOfRedirects = 0;
    private String expectedResponse;

    private int redirectCount = 0;
    private final String baseLocation = "/resource";

    public Http2RedirectListener(int numberOfRedirects, String expectedResponse) {
        this.numberOfRedirects = numberOfRedirects;
        this.expectedResponse = expectedResponse;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                if (redirectCount < numberOfRedirects) {
                    if (redirectCount != 0) {
                        String url = (String) httpRequest.getProperty(Constants.TO);
                        if (!url.contains(baseLocation.concat(String.valueOf(redirectCount)))) {
                            HttpResponseFuture responseFuture =
                                    httpRequest.respond(MessageGenerator.generateResponse("Error:Incorrect location"));
                            responseFuture.sync();
                            return;
                        }
                    }
                    redirectCount++;
                    HTTPCarbonMessage response =
                            MessageGenerator.generateResponse(null, HttpResponseStatus.MOVED_PERMANENTLY);
                    response.setHeader(HttpHeaderNames.LOCATION.toString(),
                                       baseLocation.concat(String.valueOf(redirectCount)));
                    HttpResponseFuture responseFuture = httpRequest.respond(response);
                    responseFuture.sync();
                } else {
                    // Send the intended response message
                    HttpResponseFuture responseFuture =
                            httpRequest.respond(MessageGenerator.generateResponse(expectedResponse));
                    responseFuture.sync();
                    Throwable error = responseFuture.getStatus().getCause();
                    if (error != null) {
                        responseFuture.resetStatus();
                        logger.error("Error occurred while sending the response " + error.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("Error occurred while processing message: " + e.getMessage());
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {
    }
}
