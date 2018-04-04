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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.exception.EndpointTimeOutException;
import org.wso2.transport.http.netty.http2.listeners.Http2NoResponseListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.util.HTTPConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.RequestGenerator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * {@code Http2ClientTimeoutTestCase} contains test cases for HTTP/2 Client request timeout.
 */
public class Http2ClientTimeoutTestCase {

    private static Logger log = LoggerFactory.getLogger(Http2ClientTimeoutTestCase.class);

    private HttpClientConnector httpClientConnector;
    private ServerConnector serverConnector;
    private SenderConfiguration senderConfiguration;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() throws InterruptedException {
        connectorFactory = new DefaultHttpWsConnectorFactory();
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.HTTP_SERVER_PORT);
        listenerConfiguration.setScheme(Constants.HTTP_SCHEME);
        listenerConfiguration.setVersion(String.valueOf(Constants.HTTP_2_0));
        serverConnector = connectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        Http2NoResponseListener http2ServerConnectorListener = new Http2NoResponseListener();
        future.setHttpConnectorListener(http2ServerConnectorListener);
        future.sync();

        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        senderConfiguration =
                HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTP_SCHEME);
        senderConfiguration.setSocketIdleTimeout(3000);
        senderConfiguration.setHttpVersion(String.valueOf(Constants.HTTP_2_0));

        httpClientConnector = connectorFactory.createHttpClientConnector(
                HTTPConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration);
    }

    @Test
    public void testHttp2ClientTimeout() {
        HTTPCarbonMessage request = RequestGenerator.generateRequest(HttpMethod.POST, "test");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(request);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
            Throwable error = listener.getHttpErrorMessage();
            AssertJUnit.assertNotNull(error);
            assertTrue(error instanceof EndpointTimeOutException,
                       "Exception is not an instance of EndpointTimeOutException");
            String result = error.getMessage();
            assertEquals(result, Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_READING_INBOUND_RESPONSE,
                         "Expected error message not received");
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running testHttp2ClientTimeout test case", e);
        }
    }

    @AfterClass
    public void cleanUp() {
        senderConfiguration.setHttpVersion(String.valueOf(Constants.HTTP_1_1));
        httpClientConnector.close();
        serverConnector.stop();
        try {
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for HttpWsFactory to close");
        }
    }
}
