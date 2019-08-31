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

package org.wso2.transport.http.netty.expect100continue;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.Send100ContinueAfterReceivingEntityBodyInitializer;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * This class test client 100-continue implementation for scenario in which 100 continue response is received after
 * sending entity body of the request. Ideally 100 continue response must be received after request headers are
 * sent. Therefore, this testcase is testing and abnormal scenario.
 */
public class ClientContinue100AfterSendingReqTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientContinue100AfterSendingReqTestCase.class);

    private HttpServer httpServer;
    private HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory httpWsConnectorFactory;
    private DefaultHttpConnectorListener listener;

    @BeforeClass
    public void setup() throws InterruptedException {
        givenServerSimulating100AfterFullRespReceived();
        givenNormalHttpClient();
    }

    @Test
    public void test100Continue() {
        try {
            whenReqSentWithExpectContinue();
            thenRespShouldBeNormalResponse();
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


    private void givenNormalHttpClient() {
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setSocketIdleTimeout(10000);
        httpClientConnector = httpWsConnectorFactory.createHttpClientConnector(new HashMap<>(), senderConfiguration);
    }

    private void givenServerSimulating100AfterFullRespReceived() {
        httpServer = TestUtil
                .startHTTPServer(TestUtil.HTTP_SERVER_PORT, new Send100ContinueAfterReceivingEntityBodyInitializer());
    }

    private void thenRespShouldBeNormalResponse() {
        String responseBody = TestUtil.getStringFromInputStream(
                new HttpMessageDataStreamer(listener.getHttpResponseMessage()).getInputStream());
        assertEquals(responseBody, "inbound response entity body");
    }

    private void whenReqSentWithExpectContinue() throws InterruptedException {
        HttpCarbonMessage requestMsg = new HttpCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                                                                                    HttpMethod.POST, ""));

        requestMsg.setProperty(Constants.HTTP_PORT, TestUtil.HTTP_SERVER_PORT);
        requestMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        requestMsg.setProperty(Constants.HTTP_HOST, TestUtil.TEST_HOST);
        requestMsg.setHttpMethod(Constants.HTTP_POST_METHOD);
        requestMsg.setHeader(HttpHeaderNames.EXPECT.toString(), HttpHeaderValues.CONTINUE);
        requestMsg.addHttpContent(
                new DefaultLastHttpContent(Unpooled.wrappedBuffer(TestUtil.smallEntity.getBytes())));

        CountDownLatch latch = new CountDownLatch(1);
        listener = new DefaultHttpConnectorListener(latch);
        httpClientConnector.send(requestMsg).setHttpConnectorListener(listener);
        latch.await(30, TimeUnit.SECONDS);
    }
}
