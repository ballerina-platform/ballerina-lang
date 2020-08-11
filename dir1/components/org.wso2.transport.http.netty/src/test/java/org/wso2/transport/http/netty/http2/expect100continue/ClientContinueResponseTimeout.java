/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.http2.expect100continue;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.EndpointTimeOutException;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.http2.listeners.Http2NoResponseListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This class is for testing the response timeout after 100-continue client sending the full request.
 */
public class ClientContinueResponseTimeout {
    private static final Logger LOG = LoggerFactory.getLogger(ClientContinue100ChunkingTestCase.class);

    private ServerConnector serverConnector;
    private HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory httpWsConnectorFactory;

    @BeforeClass
    public void setup() throws InterruptedException {
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        givenAHttpServerThatDoesNotSendAnyResponse();
        givenANormal100ContinueClient();
    }

    @Test
    public void testResponseTimeOut() {
        try {
            DefaultHttpConnectorListener listener = whenReqSentForPositiveResponse();
            thenRespShouldBeTimeoutError(listener);
        } catch (Exception e) {
            TestUtil.handleException(
                    "Exception occurred while running testResponseTimeOut in ClientContinueResponseTimeout", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        serverConnector.stop();
        try {
            httpWsConnectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.error("Interrupted while waiting for HttpWsFactory to shutdown", e);
        }
    }

    private void givenAHttpServerThatDoesNotSendAnyResponse() throws InterruptedException {
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        ListenerConfiguration listenerConfiguration = Continue100Util.getListenerConfigs();
        listenerConfiguration.setPort(TestUtil.HTTP_SERVER_PORT);
        //Set this to a value larger than client socket timeout value, to make sure that the client times out first
        listenerConfiguration.setSocketIdleTimeout(500000);
        serverConnector = httpWsConnectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        Http2NoResponseListener http2ServerConnectorListener = new Http2NoResponseListener();
        future.setHttpConnectorListener(http2ServerConnectorListener);
        future.sync();
    }

    private void givenANormal100ContinueClient() {
        SenderConfiguration senderConfiguration = Continue100Util.getSenderConfigs();
        senderConfiguration.setSocketIdleTimeout(3000);
        httpClientConnector = httpWsConnectorFactory.createHttpClientConnector(new HashMap<>(), senderConfiguration);
    }

    private void thenRespShouldBeTimeoutError(DefaultHttpConnectorListener connectorListener) {
        Throwable error = connectorListener.getHttpErrorMessage();
        AssertJUnit.assertNotNull(error);
        assertTrue(error instanceof EndpointTimeOutException,
                "Exception is not an instance of EndpointTimeOutException");
        String result = error.getMessage();
        assertEquals(result, Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE,
                "Expected error message not received");
    }

    private DefaultHttpConnectorListener whenReqSentForPositiveResponse() throws InterruptedException {
        HttpCarbonMessage request = MessageGenerator.generateRequest(HttpMethod.POST, "test");
        request.setHeader(HttpHeaderNames.EXPECT.toString(), HttpHeaderValues.CONTINUE);
        CountDownLatch latch = new CountDownLatch(1);
        DefaultHttpConnectorListener connectorListener = new DefaultHttpConnectorListener(latch);
        HttpResponseFuture responseFuture = httpClientConnector.send(request);
        responseFuture.setHttpConnectorListener(connectorListener);
        latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
        return connectorListener;
    }
}
