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

package org.wso2.transport.http.netty.https;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ClientConnectorException;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.ServerCloseTcpConnectionInitializer;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static org.wso2.transport.http.netty.contract.Constants.HTTPS_SCHEME;

/**
 * A test case for testing the scenario of server closing the tcp connection during the SSL handshake.
 */
public class ServerCloseConnectionDuringSslTest {

    private HttpClientConnector httpClientConnector;
    private HttpServer httpServer;
    private HttpWsConnectorFactory factory;
    private static final Logger LOG = LoggerFactory.getLogger(ServerCloseConnectionDuringSslTest.class);
    private DefaultHttpConnectorListener listener;

    @BeforeClass
    public void setup() {
        givenServerThatClosesConnection();
        givenANormalHttpsClient();
    }

    @Test
    public void testServerCloseChannelDuringSslHandshake() {
        try {
            whenANormalHttpsRequestSent();
            thenRespShouldBeAServerCloseException();
        } catch (Exception ex) {
            TestUtil.handleException("Exception occurred while running testServerCloseChannelDuringSslHandshake", ex);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            httpServer.shutdown();
            httpClientConnector.close();
            factory.shutdown();
        } catch (Exception e) {
            LOG.warn("Interrupted while waiting for response", e);
        }
    }

    private void thenRespShouldBeAServerCloseException() {
        Throwable response = listener.getHttpErrorMessage();
        assertNotNull(response);
        assertTrue(response instanceof ClientConnectorException,
                "Exception is not an instance of ClientConnectorException");
        String result = response.getMessage();

        assertEquals("Remote host: localhost/127.0.0.1:9000 closed the connection while SSL handshake", result);
    }

    private void whenANormalHttpsRequestSent() throws InterruptedException {
        HttpCarbonMessage msg = TestUtil.createHttpsPostReq(TestUtil.HTTP_SERVER_PORT, "", "");

        CountDownLatch latch = new CountDownLatch(1);
        listener = new DefaultHttpConnectorListener(latch);
        HttpResponseFuture responseFuture = httpClientConnector.send(msg);
        responseFuture.setHttpConnectorListener(listener);

        latch.await(5, TimeUnit.SECONDS);
    }

    private SenderConfiguration getSenderConfigs() {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        String trustStoreFile = "/simple-test-config/client-truststore.p12";
        senderConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(trustStoreFile));
        String password = "ballerina";
        senderConfiguration.setTrustStorePass(password);
        String tlsStoreType = "PKCS12";
        senderConfiguration.setTLSStoreType(tlsStoreType);
        senderConfiguration.setScheme(HTTPS_SCHEME);
        return senderConfiguration;
    }

    private void givenANormalHttpsClient() {
        factory = new DefaultHttpWsConnectorFactory();
        httpClientConnector = factory.createHttpClientConnector(new HashMap<>(), getSenderConfigs());
    }

    private void givenServerThatClosesConnection() {
        httpServer = TestUtil
                .startHTTPServer(TestUtil.HTTP_SERVER_PORT, new ServerCloseTcpConnectionInitializer());
    }
}
