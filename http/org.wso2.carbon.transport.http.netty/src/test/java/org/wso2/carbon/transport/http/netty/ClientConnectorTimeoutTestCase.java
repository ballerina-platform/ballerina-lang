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

package org.wso2.carbon.transport.http.netty;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HttpClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.carbon.transport.http.netty.util.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.HttpServer;
import org.wso2.carbon.transport.http.netty.util.server.initializers.DumbServerInitializer;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Tests for HTTP client connector timeout
 */
public class ClientConnectorTimeoutTestCase {

    private static Logger logger = LoggerFactory.getLogger(ClientConnectorTimeoutTestCase.class);

    private HttpServer httpServer;
    private HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();

    @BeforeClass
    public void setup() {
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_HTTPS_SERVER_PORT, new DumbServerInitializer());

        TransportsConfiguration transportsConfiguration = TestUtil.getConfiguration(
                        "/simple-test-config" + File.separator + "netty-transports.yml");
        SenderConfiguration senderConfiguration = HTTPConnectorUtil
                .getSenderConfiguration(transportsConfiguration, Constants.HTTP_SCHEME);
        senderConfiguration.setSocketIdleTimeout(3000);

        httpClientConnector = connectorFactory.createHttpClientConnector(
                HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                senderConfiguration);
    }

    @Test
    public void testHttpsGet() {
        try {
            HTTPCarbonMessage msg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.GET, ""));
            msg.setProperty("PORT", TestUtil.TEST_HTTPS_SERVER_PORT);
            msg.setProperty("PROTOCOL", "https");
            msg.setProperty("HOST", "localhost");
            msg.setProperty("HTTP_METHOD", "GET");
            msg.setEndOfMsgAdded(true);

            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(6, TimeUnit.SECONDS);

            Throwable response = listener.getHttpErrorMessage();
            assertNotNull(response);
            String result = response.getMessage();

            assertEquals(HttpResponseStatus.GATEWAY_TIMEOUT.reasonPhrase(), result);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running httpsGetTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            httpServer.shutdown();
        } catch (InterruptedException e) {
            logger.error("Failed to shutdown the test server");
        }
    }
}
