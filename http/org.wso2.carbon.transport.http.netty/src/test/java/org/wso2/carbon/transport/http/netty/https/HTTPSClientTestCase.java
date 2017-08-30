/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.https;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HttpClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.HttpsServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Tests for HTTPS client connector
 */
public class HTTPSClientTestCase {

    private final Logger log = LoggerFactory.getLogger(HTTPSClientTestCase.class);

    private HttpsServer httpsServer;
    private HttpClientConnector httpClientConnector;
    private int port = 9092;
    private String testValue = "Test Message";

    @BeforeClass
    public void setup() {
        TransportsConfiguration transportsConfiguration =
                TestUtil.getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");
        httpsServer = TestUtil.startHttpsServer(port, testValue, "text/plain");
        HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();
        httpClientConnector = connectorFactory.createHttpClientConnector(
                HTTPMessageUtil.getTransportProperties(transportsConfiguration),
                HTTPMessageUtil.getSenderConfiguration(transportsConfiguration, true));
    }

    @Test
    public void testHttpsGet() {
        try {
            HTTPCarbonMessage msg = new HTTPCarbonMessage();
            msg.setProperty("PORT", port);
            msg.setProperty("PROTOCOL", "https");
            msg.setProperty("HOST", "localhost");
            msg.setProperty("HTTP_METHOD", "GET");
            msg.setEndOfMsgAdded(true);

            CountDownLatch latch = new CountDownLatch(1);
            HTTPSConnectorListener listener = new HTTPSConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(5, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(new InputStreamReader(response.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            assertEquals(testValue, result);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running httpsGetTest", e);
        }
    }
}
