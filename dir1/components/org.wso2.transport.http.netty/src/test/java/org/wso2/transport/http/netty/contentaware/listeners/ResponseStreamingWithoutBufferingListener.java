/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderValues.CHUNKED;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * A Message Processor which respond in streaming manner without buffering.
 */
public class ResponseStreamingWithoutBufferingListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(RequestResponseTransformStreamingListener.class);
    private ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void onMessage(HttpCarbonMessage inboundRequest) {
        executor.execute(() -> {
            HttpCarbonMessage outboundResponse =
                    new HttpCarbonMessage(new DefaultHttpResponse(HTTP_1_1, OK));
            outboundResponse.setHeader(CONNECTION.toString(), KEEP_ALIVE.toString());
            outboundResponse.setHeader(TRANSFER_ENCODING.toString(), CHUNKED.toString());
            outboundResponse.setHeader(CONTENT_TYPE.toString(), TEXT_PLAIN.toString());
            outboundResponse.setHttpStatusCode(OK.code());
            try {
                inboundRequest.respond(outboundResponse);
            } catch (ServerConnectorException e) {
                LOG.error("Error occurred during message notification: " + e.getMessage());
            }
            while (true) {
                HttpContent httpContent = inboundRequest.getHttpContent();
                outboundResponse.addHttpContent(httpContent);
                if (httpContent instanceof LastHttpContent) {
                    break;
                }
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
