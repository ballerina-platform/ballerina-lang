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

package org.wso2.transport.http.netty.connectionpool;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.SendChannelIDServerInitializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.wso2.transport.http.netty.util.TestUtil.sendRequestAsync;

/**
 * Tests for connection pool implementation.
 */
public class ConnectionPoolMainTestCase {

    private HttpServer httpServer;
    private HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() {
        httpServer = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new SendChannelIDServerInitializer(5000));

        connectorFactory = new DefaultHttpWsConnectorFactory();
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.getPoolConfiguration().setMaxIdlePerPool(1);
        senderConfiguration.getPoolConfiguration().setMaxActivePerPool(2);
        httpClientConnector = connectorFactory.createHttpClientConnector(new HashMap<>(), senderConfiguration);
    }

    @Test
    public void testConnectionReuseForMain() {
        try {
            CountDownLatch requestOneLatch = new CountDownLatch(1);
            CountDownLatch requestTwoLatch = new CountDownLatch(1);
            CountDownLatch requestThreeLatch = new CountDownLatch(1);

            DefaultHttpConnectorListener responseListener;

            responseListener = sendRequestAsync(requestOneLatch, httpClientConnector);

            // While the first request is being processed by the back-end,
            // we send the second request which forces the client connector to
            // create a new connection.
            Thread.sleep(2500);
            sendRequestAsync(requestTwoLatch, httpClientConnector);

            String responseOne = TestUtil.waitAndGetStringEntity(requestOneLatch, responseListener);

            responseListener = sendRequestAsync(requestThreeLatch, httpClientConnector);
            String responseThree = TestUtil.waitAndGetStringEntity(requestThreeLatch, responseListener);

            assertEquals(responseOne, responseThree);

            // Wait for response two to be completed before finishing the test
            requestTwoLatch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            TestUtil.handleException("IOException occurred while running testConnectionReuseForMain", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        TestUtil.cleanUp(new ArrayList<>(), httpServer, connectorFactory);
    }
}
