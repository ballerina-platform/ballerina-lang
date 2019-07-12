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

package org.wso2.transport.http.netty.connectionpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.testng.Assert.assertTrue;

/**
 * Test case for testing the max active connections per pool configuration.
 */
public class ConnectionPoolMaxConnTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionPoolMaxConnTestCase.class);

    private static final int MAX_ACTIVE_CONNECTIONS = 7;

    private HttpServer httpServer;
    private HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() {
        httpServer = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new SendChannelIDServerInitializer(1000));

        connectorFactory = new DefaultHttpWsConnectorFactory();
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.getPoolConfiguration().setMaxActivePerPool(MAX_ACTIVE_CONNECTIONS);
        httpClientConnector = connectorFactory.createHttpClientConnector(new HashMap<>(), senderConfiguration);
    }

    @Test
    public void testMaxActiveConnectionsPerPool() {
        try {
            int noOfRequests = 15;
            Set<String> channelIDs = new HashSet<>();
            CountDownLatch[] countDownLatches = getLatchesArray(noOfRequests);
            DefaultHttpConnectorListener[] responseListeners = new DefaultHttpConnectorListener[noOfRequests];

            // Send multiple requests asynchronously to force the creation of multiple connections
            for (int i = 0; i < countDownLatches.length; i++) {
                responseListeners[i] = TestUtil.sendRequestAsync(countDownLatches[i], httpClientConnector);
            }

            // Gather the unique responses (channel IDs) for all the requests sent
            for (int i = 0; i < responseListeners.length; i++) {
                String response = TestUtil.waitAndGetStringEntity(countDownLatches[i], responseListeners[i]);
                channelIDs.add(response);
                LOG.info("Response #" + (i + 1) + " received: " + response);
            }

            // The number of unique responses (channel IDs) should be equal or less than the max no. of active
            // connections configured
            assertTrue(channelIDs.size() <= MAX_ACTIVE_CONNECTIONS);
        } catch (Exception e) {
            TestUtil.handleException("IOException occurred while running testMaxActiveConnectionsPerPool", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        TestUtil.cleanUp(new ArrayList<>(), httpServer, connectorFactory);
    }

    private CountDownLatch[] getLatchesArray(int n) {
        CountDownLatch[] countDownLatches = new CountDownLatch[n];

        for (int i = 0; i < countDownLatches.length; i++) {
            countDownLatches[i] = new CountDownLatch(1);
        }

        return countDownLatches;
    }
}
