/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.util.server.listeners;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Continue100AfterRespReceivedListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(Continue100AfterRespReceivedListener.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onMessage(HttpCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                String expectHeader = httpRequest.getHeader(HttpHeaderNames.EXPECT.toString());

                if (expectHeader != null && expectHeader.equalsIgnoreCase("100-continue")) {
                    HttpCarbonMessage httpResponse;
                    String statusHeader = httpRequest.getHeader("X-Status");
                    if (statusHeader != null && statusHeader.equalsIgnoreCase("Positive")) {
                        httpResponse = createPositiveContinueResponse();
                    } else {
                        httpResponse = createNegativeContinueResponse();
                    }

                    Thread.sleep(5000);
                    httpRequest.respond(httpResponse);
                }

                HttpCarbonMessage httpResponse =
                        new HttpCarbonResponse(
                                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                httpResponse.setHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
                httpResponse.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
                httpResponse.setHttpStatusCode(HttpResponseStatus.OK.code());

                do {
                    HttpContent httpContent = httpRequest.getHttpContent();
                    httpResponse.addHttpContent(httpContent);
                    if (httpContent instanceof LastHttpContent) {
                        break;
                    }
                } while (true);
                httpResponse.addHttpContent(new DefaultLastHttpContent());
                httpRequest.respond(httpResponse);
            } catch (Exception e) {
                LOG.error("Error occurred during message notification: " + e.getMessage());
            }
        });
    }

    private HttpCarbonMessage createPositiveContinueResponse() {
        HttpCarbonMessage httpResponse =
                new HttpCarbonResponse(
                        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
        httpResponse.setHeader(HttpHeaderNames.CONNECTION.toString(),
                               HttpHeaderValues.KEEP_ALIVE.toString());
        httpResponse.setHttpStatusCode(HttpResponseStatus.CONTINUE.code());
        httpResponse.addHttpContent(new DefaultLastHttpContent());
        return httpResponse;
    }

    private HttpCarbonMessage createNegativeContinueResponse() {
        String payload = "Do not send me any payload";
        HttpCarbonMessage httpResponse =
                new HttpCarbonResponse(
                        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EXPECTATION_FAILED));
        httpResponse.setHeader(HttpHeaderNames.CONNECTION.toString(),
                               HttpHeaderValues.KEEP_ALIVE.toString());
        httpResponse.setHttpStatusCode(HttpResponseStatus.EXPECTATION_FAILED.code());
        httpResponse.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(payload.getBytes())));
        return httpResponse;
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
