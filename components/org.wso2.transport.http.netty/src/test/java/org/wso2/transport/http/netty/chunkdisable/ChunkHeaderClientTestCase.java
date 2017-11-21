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

package org.wso2.transport.http.netty.chunkdisable;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.HTTPConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.ChunkBasedServerInitializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Tests for enable/disable chunk.
 */
public class ChunkHeaderClientTestCase {

    private static Logger logger = LoggerFactory.getLogger(ChunkHeaderClientTestCase.class);

    private HttpServer httpServer;
    private HttpClientConnector httpClientConnector;
    private String testValue = "Test Message";
    private TransportsConfiguration transportsConfiguration;
    private HttpWsConnectorFactory connectorFactory;
    private SenderConfiguration senderConfiguration;

    @BeforeClass
    public void setup() {
        transportsConfiguration =
                TestUtil.getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_HTTP_SERVER_PORT,
                new ChunkBasedServerInitializer(testValue, "text/plain", 200));
    }

    @Test
    public void chunkDisabledTestCase() {
        HTTPCarbonMessage msg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, ""));
        msg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(testValue.length()));
        HTTPCarbonMessage response = getResponse(msg, true);
        String result = new BufferedReader(new InputStreamReader(new HttpMessageDataStreamer(response)
                .getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        assertEquals(testValue, result);
        assertEquals(response.getHeader(Constants.HTTP_CONTENT_LENGTH), String.valueOf(testValue.length()));
        assertNull(response.getHeader(Constants.HTTP_TRANSFER_ENCODING));
    }

    @Test
    public void chunkEnabledTestCase() {
        HTTPCarbonMessage msg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, ""));
        HTTPCarbonMessage response = getResponse(msg, false);
        String result = new BufferedReader(new InputStreamReader(new HttpMessageDataStreamer(response)
                .getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        assertEquals(testValue, result);
        assertEquals(response.getHeader(Constants.HTTP_TRANSFER_ENCODING), Constants.CHUNKED);
        assertNull(response.getHeader(Constants.HTTP_CONTENT_LENGTH));
    }


    private HTTPCarbonMessage getResponse(HTTPCarbonMessage msg, Boolean isDisabled) {
        HTTPCarbonMessage response = null;
        connectorFactory = new HttpWsConnectorFactoryImpl();
        senderConfiguration = HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTP_SCHEME);
        senderConfiguration.setChunkDisabled(isDisabled);
        httpClientConnector = connectorFactory.createHttpClientConnector(
                HTTPConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration);

        ByteBuffer byteBuffer = ByteBuffer.wrap(testValue.getBytes(Charset.forName("UTF-8")));
        msg.setProperty("PORT", TestUtil.TEST_HTTP_SERVER_PORT);
        msg.setProperty("PROTOCOL", "http");
        msg.setProperty("HOST", "localhost");
        msg.setProperty("HTTP_METHOD", "GET");
        msg.addMessageBody(byteBuffer);
        msg.setEndOfMsgAdded(true);
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(5, TimeUnit.SECONDS);

            response = listener.getHttpResponseMessage();
            assertNotNull(response);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running test ", e);
        }
        return response;
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
