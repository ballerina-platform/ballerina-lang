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

package org.wso2.transport.http.netty;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.EndpointTimeOutException;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.DumbServerInitializer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Tests for HTTP client connector timeout
 */
public class ClientTimeoutWhileWritingBodyTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientTimeoutWhileWritingBodyTestCase.class);

    private HttpServer httpServer;
    private HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() {
        httpServer = TestUtil.startHTTPServer(TestUtil.HTTPS_SERVER_PORT, new DumbServerInitializer());

        connectorFactory = new DefaultHttpWsConnectorFactory();
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setSocketIdleTimeout(3000);
        httpClientConnector = connectorFactory.createHttpClientConnector(new HashMap<>(), senderConfiguration);
    }

    @Test
    public void testHttpPost() {
        try {
            HttpCarbonMessage msg = TestUtil.createHttpsPostReq(TestUtil.HTTPS_SERVER_PORT, "", "");
            msg.getHttpContent().release();

            ByteBuffer byteBuffer = ByteBuffer.wrap("Test-Value".getBytes(Charset.forName("UTF-8")));
            msg.addHttpContent(new DefaultHttpContent(Unpooled.wrappedBuffer(byteBuffer)));

            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(6, TimeUnit.SECONDS);

            Throwable response = listener.getHttpErrorMessage();
            assertNotNull(response);
            assertTrue(response instanceof EndpointTimeOutException,
                    "Exception is not an instance of EndpointTimeOutException");
            String result = response.getMessage();

            assertEquals(Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY, result);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running httpsGetTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            httpServer.shutdown();
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.error("Failed to shutdown the test server");
        }
    }
}
