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

package org.ballerinalang.net.netty.expect100continue;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.ballerinalang.net.netty.contract.HttpClientConnector;
import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contract.HttpWsConnectorFactory;
import org.ballerinalang.net.netty.contract.config.ChunkConfig;
import org.ballerinalang.net.netty.contract.config.SenderConfiguration;
import org.ballerinalang.net.netty.contract.exceptions.ServerConnectorException;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.ballerinalang.net.netty.util.DefaultHttpConnectorListener;
import org.ballerinalang.net.netty.util.TestUtil;
import org.ballerinalang.net.netty.util.server.HttpServer;
import org.ballerinalang.net.netty.util.server.initializers.CloseWithoutRespondingInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.net.netty.contract.Constants.REMOTE_SERVER_CLOSED_BEFORE_READING_100_CONTINUE_RESPONSE;
import static org.testng.Assert.assertEquals;

/**
 * This class test client 100-continue implementation for connection closure. To be specific, connection closure
 * after receiving 100-continue response from the sever.
 */
public class ClientContinue100ClosureTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientContinue100ClosureTestCase.class);

    private HttpServer httpServer;
    private HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory httpWsConnectorFactory;
    private DefaultHttpConnectorListener listener;

    @BeforeClass
    public void setup() throws InterruptedException {
        givenServerClosingWithoutResponding();
        givenChunkingNeverClient();
    }

    @Test
    public void test100Continue() {
        try {
            whenReqSentWithExpectContinue();
            thenRespShouldBeWithMessage(REMOTE_SERVER_CLOSED_BEFORE_READING_100_CONTINUE_RESPONSE);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running httpsGetTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            httpServer.shutdown();
            httpWsConnectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.error("Interrupted while waiting for HttpWsFactory to shutdown", e);
        }
    }

    private void thenRespShouldBeWithMessage(String msg) {
        Throwable error = listener.getHttpErrorMessage();
        assertEquals(error.getLocalizedMessage(), msg);
    }

    private void whenReqSentWithExpectContinue() throws InterruptedException {
        HttpCarbonMessage msg = TestUtil.createHttpsPostReq(TestUtil.HTTP_SERVER_PORT, TestUtil.largeEntity, "");
        msg.setHeader(HttpHeaderNames.EXPECT.toString(), HttpHeaderValues.CONTINUE);

        CountDownLatch latch = new CountDownLatch(1);
        listener = new DefaultHttpConnectorListener(latch);
        HttpResponseFuture responseFuture = httpClientConnector.send(msg);
        responseFuture.setHttpConnectorListener(listener);

        latch.await(6, TimeUnit.SECONDS);
    }

    private void givenChunkingNeverClient() {
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setChunkingConfig(ChunkConfig.NEVER);
        senderConfiguration.setSocketIdleTimeout(3000);
        httpClientConnector = httpWsConnectorFactory.createHttpClientConnector(new HashMap<>(), senderConfiguration);
    }

    private void givenServerClosingWithoutResponding() {
        httpServer = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new CloseWithoutRespondingInitializer());
    }
}
