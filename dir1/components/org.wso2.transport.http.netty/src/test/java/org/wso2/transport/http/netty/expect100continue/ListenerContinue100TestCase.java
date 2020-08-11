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

package org.wso2.transport.http.netty.expect100continue;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http.HttpClient;
import org.wso2.transport.http.netty.util.server.listeners.Continue100Listener;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

/**
 * The responsibility of this tescase it validate the listener side implementation related to handling Expect-Continue
 * header
 */
public class ListenerContinue100TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(ListenerContinue100TestCase.class);

    private ServerConnector serverConnector;
    private HttpWsConnectorFactory httpWsConnectorFactory;

    @BeforeClass
    public void setup() throws InterruptedException {
        givenNormalListener();
    }

    @Test
    public void test100Continue() {
        List<FullHttpResponse> responses = whenReqSentWithExpectContinue();
        thenFirstRespShouldBe100Continue(responses);
        thenSecondRespShouldBeNormalResponse(responses);
    }

    @Test
    public void test100ContinueNegative() {
        List<FullHttpResponse> responses = whenNegativeReqSentWithExpectContinue();
        thenRespShouldBeWithMessage("Do not send me any payload", responses);
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

    private void givenNormalListener() throws InterruptedException {
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        listenerConfiguration.setServerHeader(TestUtil.TEST_SERVER);
        ServerBootstrapConfiguration serverBootstrapConfig = new ServerBootstrapConfiguration(new HashMap<>());
        serverConnector = httpWsConnectorFactory.createServerConnector(serverBootstrapConfig, listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new Continue100Listener());
        serverConnectorFuture.sync();
    }

    private void thenSecondRespShouldBeNormalResponse(List<FullHttpResponse> responses) {
        // Actual response
        String responsePayload = TestUtil.getEntityBodyFrom(responses.get(1));
        Assert.assertEquals(responses.get(1).status(), HttpResponseStatus.OK);
        Assert.assertEquals(responsePayload, TestUtil.largeEntity);
        Assert.assertEquals(responsePayload.getBytes().length, TestUtil.largeEntity.getBytes().length);
        Assert.assertEquals((responses.get(1).headers().get(HttpHeaderNames.TRANSFER_ENCODING)),
                            Constants.CHUNKED);
    }

    private List<FullHttpResponse> whenReqSentWithExpectContinue() {
        HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);

        DefaultHttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        DefaultLastHttpContent reqPayload = new DefaultLastHttpContent(
                Unpooled.wrappedBuffer(TestUtil.largeEntity.getBytes()));

        httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, TestUtil.largeEntity.getBytes().length);
        httpRequest.headers().set("X-Status", "Positive");

        List<FullHttpResponse> responses = httpClient.sendExpectContinueRequest(httpRequest, reqPayload);

        Assert.assertFalse(httpClient.waitForChannelClose());
        return responses;
    }

    private void thenFirstRespShouldBe100Continue(List<FullHttpResponse> responses) {
        // 100-continue response
        Assert.assertEquals(responses.get(0).status(), HttpResponseStatus.CONTINUE);
    }

    private void thenRespShouldBeWithMessage(String msg, List<FullHttpResponse> responses) {
        // 417 Expectation Failed response
        Assert.assertEquals(responses.get(0).status(), HttpResponseStatus.EXPECTATION_FAILED);
        int length = Integer.valueOf(responses.get(0).headers().get(HttpHeaderNames.CONTENT_LENGTH));
        Assert.assertEquals(length, 26);
        Assert.assertEquals(responses.get(0).content()
                                    .readCharSequence(length, Charset.defaultCharset()).toString(), msg);
        // Actual response
        Assert.assertEquals(responses.size(), 1,
                            "Multiple responses received when only a 417 response was expected");
    }

    private List<FullHttpResponse> whenNegativeReqSentWithExpectContinue() {
        HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);

        DefaultHttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        DefaultLastHttpContent reqPayload = new DefaultLastHttpContent(
                Unpooled.wrappedBuffer(TestUtil.largeEntity.getBytes()));

        httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, TestUtil.largeEntity.getBytes().length);
        httpRequest.headers().set("X-Status", "Negative");

        List<FullHttpResponse> responses = httpClient.sendExpectContinueRequest(httpRequest, reqPayload);

        Assert.assertFalse(httpClient.waitForChannelClose());
        return responses;
    }
}
