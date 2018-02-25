/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.http2;

import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.Http2ClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.HTTPConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.RequestGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/* This contains basic test cases for HTTP2 Client connector */
public class Http2ServerConnectorBasicTestCase {
    private static Http2ClientConnector http2ClientConnector;
    private static ServerConnector serverConnector;

    @BeforeClass
    public void setup() throws InterruptedException {

        TransportsConfiguration transportsConfiguration = TestUtil
                .getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");

        HttpWsConnectorFactory factory = new HttpWsConnectorFactoryImpl();
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.HTTP_SERVER_PORT);
        listenerConfiguration.setScheme(Constants.HTTP_SCHEME);
        listenerConfiguration.setHttp2(true);
        serverConnector = factory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        http2ClientConnector = factory
                .createHttp2ClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                                            HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration,
                                                                                     Constants.HTTP_SCHEME));
    }

    @Test
    public void testHttp2Post() {
        String testValue = "Test Http2 Message";
        HTTPCarbonMessage httpCarbonMessage = RequestGenerator.generateRequest(HttpMethod.POST, testValue);
        HTTPCarbonMessage response = sendMessage(httpCarbonMessage);
        assertNotNull(response);
        String result = new BufferedReader(
                new InputStreamReader(new HttpMessageDataStreamer(
                        response).getInputStream())).lines().collect(Collectors.joining("\n"));
        assertEquals(testValue, result);
    }

    private HTTPCarbonMessage sendMessage(HTTPCarbonMessage httpCarbonMessage) {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(countDownLatch);
            HttpResponseFuture responseFuture = http2ClientConnector.send(httpCarbonMessage);
            responseFuture.setHttpConnectorListener(listener);
            countDownLatch.await(10, TimeUnit.SECONDS);
            return listener.getHttpResponseMessage();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while sending a message", e);
        }
        return null;
    }

    @AfterClass
    public void cleanUp() {
        http2ClientConnector.close();
        serverConnector.stop();
    }
}

