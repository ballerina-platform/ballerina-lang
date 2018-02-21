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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Message Processor which respond in streaming manner without buffering.
 */
public class ResponseStreamingWithoutBufferingListener implements HttpConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseTransformStreamingListener.class);
    private ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void onMessage(HTTPCarbonMessage httpRequestMessage) {
        executor.execute(() -> {
            HTTPCarbonMessage cMsg =
                    new HTTPCarbonMessage(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
            cMsg.setHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
            cMsg.setHeader(HttpHeaderNames.TRANSFER_ENCODING.toString(), HttpHeaderValues.CHUNKED.toString());
            cMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.TEXT_PLAIN.toString());
            cMsg.setProperty(Constants.HTTP_STATUS_CODE, HttpResponseStatus.OK.code());
            try {
                httpRequestMessage.respond(cMsg);
            } catch (ServerConnectorException e) {
                logger.error("Error occurred during message notification: " + e.getMessage());
            }
            while (true) {
                HttpContent httpContent = httpRequestMessage.getHttpContent();
                cMsg.addHttpContent(httpContent);
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
