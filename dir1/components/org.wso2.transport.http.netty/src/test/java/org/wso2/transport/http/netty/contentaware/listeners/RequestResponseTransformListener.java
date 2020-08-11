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

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Transform message in request and response path
 */
public class RequestResponseTransformListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(RequestResponseTransformListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String responseValue;
    private String requestValue;

    public RequestResponseTransformListener(String responseValue) {
        this.responseValue = responseValue;
    }

    @Override
    public void onMessage(HttpCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                requestValue = TestUtil.getStringFromInputStream(
                        new HttpMessageDataStreamer(httpRequest).getInputStream());

                httpRequest.setProperty(Constants.HTTP_HOST, TestUtil.TEST_HOST);
                httpRequest.setProperty(Constants.HTTP_PORT, TestUtil.HTTP_SERVER_PORT);

                if (responseValue != null) {
                    byte[] array = responseValue.getBytes("UTF-8");
                    ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
                    byteBuffer.put(array);
                    httpRequest.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(array.length));
                    byteBuffer.flip();
                    httpRequest.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
                }

                HttpWsConnectorFactory httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
                HttpClientConnector clientConnector =
                        httpWsConnectorFactory.createHttpClientConnector(new HashMap<>(), new SenderConfiguration());
                HttpResponseFuture future = clientConnector.send(httpRequest);
                future.setHttpConnectorListener(new HttpConnectorListener() {
                    @Override
                    public void onMessage(HttpCarbonMessage httpMessage) {
                        executor.execute(() -> {
                            String content = TestUtil.getStringFromInputStream(
                                    new HttpMessageDataStreamer(httpMessage).getInputStream());

                            String responseValue = content + ":" + requestValue;
                            if (requestValue != null) {
                                byte[] array = new byte[0];
                                try {
                                    array = responseValue.getBytes("UTF-8");
                                } catch (UnsupportedEncodingException ignored) {

                                }
                                ByteBuffer byteBuff = ByteBuffer.allocate(array.length);
                                byteBuff.put(array);
                                httpMessage.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(),
                                                      String.valueOf(array.length));
                                byteBuff.flip();
                                httpMessage.addHttpContent(
                                        new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuff)));
                                try {
                                    httpRequest.respond(httpMessage);
                                } catch (ServerConnectorException e) {
                                    LOG.error("Error occurred during message notification: " + e.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
            } catch (Exception e) {
                LOG.error("Error while reading stream", e);
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
