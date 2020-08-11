/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.httppipelining;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
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
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Mock pipelining listener.
 *
 * @since 6.0.228
 */
public class HttpPipeliningListener implements HttpConnectorListener {
    private static final Logger LOG = LoggerFactory.getLogger(HttpPipeliningListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onMessage(HttpCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                HttpCarbonMessage httpResponse =
                        new HttpCarbonResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                httpResponse.setHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
                httpResponse.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
                httpResponse.setHttpStatusCode(HttpResponseStatus.OK.code());
                httpResponse.setSequenceId(httpRequest.getSequenceId());
                httpResponse.setPipeliningEnabled(httpRequest.isPipeliningEnabled());
                ChannelHandlerContext sourceContext = httpRequest.getSourceContext();
                Long nextSequenceNumber = sourceContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get();
                httpResponse.setHeader("x-sequence-number", nextSequenceNumber.toString());
                do {
                    HttpContent httpContent = httpRequest.getHttpContent();
                    httpResponse.addHttpContent(httpContent);
                    if (httpContent instanceof LastHttpContent) {
                        break;
                    }
                } while (true);

                httpRequest.respond(httpResponse);
            } catch (ServerConnectorException e) {
                LOG.error("Error occurred during message notification: " + e.getMessage());
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("Error occurred in HttpPipeliningListener ", throwable);
    }
}
