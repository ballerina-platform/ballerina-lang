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

package org.wso2.carbon.transport.http.netty.passthrough;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HttpClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * A Message Processor class to be used for test pass through scenarios
 */
public class PassthroughMessageProcessorListener implements HttpConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(PassthroughMessageProcessorListener.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private TransportsConfiguration configuration;
    private HttpClientConnector clientConnector;

    public PassthroughMessageProcessorListener(TransportsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequestMessage) {
        executor.execute(() -> {
            httpRequestMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
            httpRequestMessage.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);
            try {
                Map<String, Object> transportProperties = new HashMap<>();
                Set<TransportProperty> transportPropertiesSet = configuration.getTransportProperties();
                if (transportPropertiesSet != null && !transportPropertiesSet.isEmpty()) {
                    transportProperties = transportPropertiesSet.stream().collect(
                            Collectors.toMap(TransportProperty::getName, TransportProperty::getValue));

                }

                String protocol = (String) httpRequestMessage.getProperty(Constants.PROTOCOL);
                boolean isHttps = Constants.PROTOCOL_HTTPS.equals(protocol) ? true : false;
                SenderConfiguration senderConfiguration = HTTPMessageUtil.getSenderConfiguration(configuration,
                                                                                                 isHttps);

                HttpWsConnectorFactory httpWsConnectorFactory = new HttpWsConnectorFactoryImpl();
                clientConnector =
                        httpWsConnectorFactory.createHttpClientConnector(transportProperties, senderConfiguration);
                HttpResponseFuture future = clientConnector.send(httpRequestMessage);
                future.setHttpConnectorListener(new HttpConnectorListener() {
                    @Override
                    public void onMessage(HTTPCarbonMessage httpResponse) {
                        executor.execute(() -> {
                            try {
                                httpRequestMessage.respond(httpResponse);
                            } catch (ServerConnectorException e) {
                                logger.error("Error occurred during message notification: " + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
            } catch (Exception e) {
                logger.error("Error occurred during message processing: ", e);
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
