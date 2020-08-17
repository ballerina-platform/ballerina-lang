/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.netty;

import io.netty.handler.codec.DecoderException;
import org.ballerinalang.net.netty.contentaware.listeners.MockHalfResponseMessageListener;
import org.ballerinalang.net.netty.contract.Constants;
import org.ballerinalang.net.netty.contract.HttpClientConnector;
import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contract.HttpWsConnectorFactory;
import org.ballerinalang.net.netty.contract.ServerConnector;
import org.ballerinalang.net.netty.contract.ServerConnectorFuture;
import org.ballerinalang.net.netty.contract.config.ListenerConfiguration;
import org.ballerinalang.net.netty.contract.config.SenderConfiguration;
import org.ballerinalang.net.netty.contract.config.ServerBootstrapConfiguration;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.ballerinalang.net.netty.message.HttpMessageDataStreamer;
import org.ballerinalang.net.netty.util.DefaultHttpConnectorListener;
import org.ballerinalang.net.netty.util.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * This class tests server-connector timeout implementation. In this case it tests if the server-connector returns the
 * correct response if it time-out while sending entity body of outbound response.
 */
public class ServerConnectorTimeoutWhileSendingBodyTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(ServerConnectorTimeoutWhileSendingBodyTestCase.class);

    protected ServerConnector serverConnector;
    private HttpClientConnector httpClientConnector;
    protected ListenerConfiguration listenerConfiguration;
    protected HttpWsConnectorFactory httpWsConnectorFactory;

    ServerConnectorTimeoutWhileSendingBodyTestCase() {
        this.listenerConfiguration = new ListenerConfiguration();
    }

    @BeforeClass
    private void setUp() {
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        listenerConfiguration.setServerHeader(TestUtil.TEST_SERVER);
        listenerConfiguration.setSocketIdleTimeout(3000);

        ServerBootstrapConfiguration serverBootstrapConfig = new ServerBootstrapConfiguration(new HashMap<>());
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();

        serverConnector = httpWsConnectorFactory.createServerConnector(serverBootstrapConfig, listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new MockHalfResponseMessageListener());
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
            LOG.error("Thread Interrupted while sleeping ", e);
        }

        SenderConfiguration senderConfiguration = new SenderConfiguration();
        httpClientConnector = httpWsConnectorFactory.createHttpClientConnector(new HashMap<>(), senderConfiguration);
    }

    @Test
    public void testHttpPost() {
        try {
            HttpCarbonMessage msg = TestUtil
                    .createHttpsPostReq(TestUtil.SERVER_CONNECTOR_PORT, "Test request body", "");

            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(6, TimeUnit.SECONDS);

            HttpCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String decoderExceptionMsg = "";
            try {
                TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
            } catch (DecoderException de) {
                decoderExceptionMsg = de.getLocalizedMessage();
            }
            assertEquals(decoderExceptionMsg, Constants.REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_BODY);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running httpsGetTest", e);
        }
    }

    @AfterClass
    public void cleanup() throws InterruptedException {
        serverConnector.stop();
        httpWsConnectorFactory.shutdown();
    }
}
