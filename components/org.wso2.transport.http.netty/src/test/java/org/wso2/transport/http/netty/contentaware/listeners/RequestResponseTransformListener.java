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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.config.TransportProperty;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Transform message in request and response path
 */
public class RequestResponseTransformListener implements HttpConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseTransformListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String responseValue;
    private String requestValue;
    private TransportsConfiguration configuration;

    public RequestResponseTransformListener(String responseValue, TransportsConfiguration configuration) {
        this.responseValue = responseValue;
        this.configuration = configuration;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                requestValue = TestUtil.getStringFromInputStream(
                        new HttpMessageDataStreamer(httpRequest).getInputStream());

                httpRequest.setProperty(Constants.HOST, TestUtil.TEST_HOST);
                httpRequest.setProperty(Constants.PORT, TestUtil.HTTP_SERVER_PORT);

                if (responseValue != null) {
                    byte[] array = responseValue.getBytes("UTF-8");
                    ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
                    byteBuffer.put(array);
                    httpRequest.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
                    byteBuffer.flip();
                    httpRequest.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
                }

                Map<String, Object> transportProperties = new HashMap<>();
                Set<TransportProperty> transportPropertiesSet = configuration.getTransportProperties();
                if (transportPropertiesSet != null && !transportPropertiesSet.isEmpty()) {
                    transportProperties = transportPropertiesSet.stream().collect(
                            Collectors.toMap(TransportProperty::getName, TransportProperty::getValue));

                }

                String scheme = (String) httpRequest.getProperty(Constants.PROTOCOL);
                SenderConfiguration senderConfiguration = HTTPConnectorUtil
                        .getSenderConfiguration(configuration, scheme);

                HttpWsConnectorFactory httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
                HttpClientConnector clientConnector =
                        httpWsConnectorFactory.createHttpClientConnector(transportProperties, senderConfiguration);
                HttpResponseFuture future = clientConnector.send(httpRequest);
                future.setHttpConnectorListener(new HttpConnectorListener() {
                    @Override
                    public void onMessage(HTTPCarbonMessage httpMessage) {
                        executor.execute(() -> {
                            String content = TestUtil.getStringFromInputStream(
                                    new HttpMessageDataStreamer(httpMessage).getInputStream());

                            String responseValue = content + ":" + requestValue;
                            if (requestValue != null) {
                                byte[] array = new byte[0];
                                try {
                                    array = responseValue.getBytes("UTF-8");
                                } catch (UnsupportedEncodingException e) {

                                }
                                ByteBuffer byteBuff = ByteBuffer.allocate(array.length);
                                byteBuff.put(array);
                                httpMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
                                byteBuff.flip();
                                httpMessage.addHttpContent(
                                        new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuff)));
                                try {
                                    httpRequest.respond(httpMessage);
                                } catch (ServerConnectorException e) {
                                    logger.error("Error occurred during message notification: " + e.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
            } catch (Exception e) {
                logger.error("Error while reading stream", e);
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
