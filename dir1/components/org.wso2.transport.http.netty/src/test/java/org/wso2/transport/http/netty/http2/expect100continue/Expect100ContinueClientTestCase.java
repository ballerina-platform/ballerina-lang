/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.http2.expect100continue;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.listeners.Continue100Listener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * This is to test the basic behavior of 100-continue. A client sends a 100-continue request, server responds
 * 100-continue and the client sends the body afterwards.
 */
public class Expect100ContinueClientTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(Expect100ContinueClientTestCase.class);
    private ServerConnector serverConnector;
    private HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory connectorFactory = new DefaultHttpWsConnectorFactory();
    private DefaultHttpConnectorListener listener;
    private String testValue = "Test Message";

    @BeforeClass
    public void setup() throws InterruptedException {
        given100ContinueSupportingServer();
        givenNormalClient();
    }

    @Test
    public void test100continueclient() {
        whenReqSentWithExpectContinue();
        thenRespShouldBeNormalResponse();
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        httpClientConnector.close();
        serverConnector.stop();
        try {
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.error("Interrupted while waiting for HttpWsFactory to shutdown", e);
        }
    }

    private void whenReqSentWithExpectContinue() {
        try {
            HttpCarbonMessage msg = TestUtil.createHttpsPostReq(TestUtil.SERVER_CONNECTOR_PORT, testValue, "");
            msg.setHeader(HttpHeaderNames.EXPECT.toString(), HttpHeaderValues.CONTINUE);
            msg.setHeader("X-Status", "Positive");

            CountDownLatch latch = new CountDownLatch(1);
            listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running Test", e);
        }
    }

    private void thenRespShouldBeNormalResponse() {
        assertNotNull(listener.getHttpResponseMessage());
        String result = new BufferedReader(
                new InputStreamReader(new HttpMessageDataStreamer(listener.getHttpResponseMessage()).getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        assertEquals(result, testValue);
    }

    private void given100ContinueSupportingServer() throws InterruptedException {
        ServerBootstrapConfiguration serverBootstrapConfig = new ServerBootstrapConfiguration(new HashMap<>());
        ListenerConfiguration listenerConfiguration = Continue100Util.getListenerConfigs();
        serverConnector = connectorFactory.createServerConnector(serverBootstrapConfig, listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new Continue100Listener());
        serverConnectorFuture.sync();
    }

    private void givenNormalClient() {
        httpClientConnector = connectorFactory
                .createHttpClientConnector(new HashMap<>(), Continue100Util.getSenderConfigs());
    }
}
