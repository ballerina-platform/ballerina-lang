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

package org.wso2.transport.http.netty.method.head;

import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.TestUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listener to process the HEAD request related test cases.
 */
public class HeadRequestMessageProcessorListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(HeadRequestMessageProcessorListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override public void onMessage(HttpCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                HttpCarbonMessage httpResponse = new HttpCarbonResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                                                                                                HttpResponseStatus.OK));
                httpResponse.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
                HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpResponse);
                httpRequest.respond(httpResponse);
                if (Boolean.valueOf(httpRequest.getHeader("x-large-payload"))) {
                    LOG.info("Request for large payload");
                    httpMessageDataStreamer.getOutputStream().write(TestUtil.largeEntity.getBytes());
                } else {
                    httpMessageDataStreamer.getOutputStream().write(TestUtil.smallEntity.getBytes());
                }
                httpMessageDataStreamer.getOutputStream().close();
            } catch (Exception e) {
                LOG.error("Error occurred during message notification: " + e.getMessage());
            }
        });
    }

    @Override public void onError(Throwable throwable) {
        LOG.error("Error while receiving the request", throwable);
    }
}
