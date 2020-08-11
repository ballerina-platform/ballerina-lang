/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.transport.http.netty.connectionpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.HttpsServer;
import org.wso2.transport.http.netty.util.server.initializers.SendChannelIDServerInitializer;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.testng.Assert.assertNotEquals;
import static org.wso2.transport.http.netty.util.TestUtil.sendRequestAsync;
import static org.wso2.transport.http.netty.util.TestUtil.sendRequestAsyncWithGivenPort;

/**
 * Test that when two different clients have the same route with different schemes that they do not share the connection
 * pool, even though they have the same connection manager.
 *
 * @since 6.0.260
 */
public class SameRouteWithDifferentSchemeTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(SameRouteWithDifferentSchemeTestCase.class);

    private HttpServer httpServer;
    private HttpsServer httpsServer;
    private HttpClientConnector httpClientConnector;
    private HttpClientConnector httpsClientConnector;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() {
        httpServer = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new SendChannelIDServerInitializer(5000));
        httpsServer = TestUtil.startHttpsServer(TestUtil.HTTPS_SERVER_PORT, new SendChannelIDServerInitializer(5000));

        connectorFactory = new DefaultHttpWsConnectorFactory();
        SenderConfiguration senderConfigurationForHttp = new SenderConfiguration();
        SenderConfiguration senderConfigurationForHttps = new SenderConfiguration();
        senderConfigurationForHttps.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.TRUST_STORE_FILE_PATH));
        senderConfigurationForHttps.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        senderConfigurationForHttps.setScheme(Constants.HTTPS_SCHEME);
        ConnectionManager sharedConnectionManager = new ConnectionManager(new PoolConfiguration());
        httpClientConnector = connectorFactory
            .createHttpClientConnector(new HashMap<>(), senderConfigurationForHttp, sharedConnectionManager);
        httpsClientConnector = connectorFactory
            .createHttpClientConnector(new HashMap<>(), senderConfigurationForHttps, sharedConnectionManager);
    }

    @Test
    public void testPoolWithDifferentSchemes() {
        try {
            CountDownLatch requestOneLatch = new CountDownLatch(1);
            CountDownLatch requestTwoLatch = new CountDownLatch(1);

            DefaultHttpConnectorListener responseListener1 = sendRequestAsync(requestOneLatch, httpClientConnector);
            String responseOne = TestUtil.waitAndGetStringEntity(requestOneLatch, responseListener1);

            DefaultHttpConnectorListener responseListener2 = sendRequestAsyncWithGivenPort(requestTwoLatch,
                                                                                           httpsClientConnector,
                                                                                           TestUtil.HTTPS_SERVER_PORT);
            String responseTwo = TestUtil.waitAndGetStringEntity(requestTwoLatch, responseListener2);

            assertNotEquals(responseOne, responseTwo);
        } catch (Exception e) {
            TestUtil.handleException("IOException occurred while running testPoolWithDifferentSchemes", e);
        }
    }

    @AfterClass
    public void cleanUp() {
        try {
            httpServer.shutdown();
            httpsServer.shutdown();
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.error("Failed to shutdown the test server");
        }
    }
}
