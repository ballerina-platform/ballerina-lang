/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.http2.trailer;

import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.http2.listeners.Http2EchoServerWithTrailerHeader;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.Http2Util;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;
import org.wso2.transport.http.netty.util.client.http2.MessageSender;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test case for H2 trailer headers come along with inbound intended response.
 *
 * @since 6.3.0
 */
public class H2ListenerIntendedResponseTrailerHeaderTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(H2ListenerIntendedResponseTrailerHeaderTestCase.class);

    private HttpWsConnectorFactory httpWsConnectorFactory;
    private HttpClientConnector h2ClientWithPriorKnowledge;
    private ServerConnector serverConnector;

    @BeforeClass
    public void setup() {
        ListenerConfiguration listenerConfiguration = Http2Util.getH2CListenerConfiguration();
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = httpWsConnectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();

        Http2EchoServerWithTrailerHeader http2ConnectorListener = new Http2EchoServerWithTrailerHeader();
        HttpHeaders trailers = new DefaultLastHttpContent().trailingHeaders();
        trailers.add("foo", "bar");
        trailers.add("baz", "ballerina");
        http2ConnectorListener.setTrailer(trailers);
        http2ConnectorListener.setMessageType(Http2EchoServerWithTrailerHeader.MessageType.RESPONSE);
        serverConnectorFuture.setHttpConnectorListener(http2ConnectorListener);

        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for server connector to start");
        }

        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        SenderConfiguration senderConfiguration = Http2Util.getH2CSenderConfiguration();
        h2ClientWithPriorKnowledge = httpWsConnectorFactory.createHttpClientConnector(
                HttpConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration);
    }

    @Test
    public void testNoPayload() {
        String testValue = "";
        HttpCarbonMessage httpMsg = MessageGenerator.generateRequest(HttpMethod.GET, testValue);
        verifyResult(httpMsg, h2ClientWithPriorKnowledge, testValue);
    }

    @Test
    public void testSmallPayload() {
        String testValue = "Test Http2 Message";
        HttpCarbonMessage httpMsg = MessageGenerator.generateRequest(HttpMethod.POST, testValue);
        verifyResult(httpMsg, h2ClientWithPriorKnowledge, testValue);
    }

    @Test
    public void testLargePayload() {
        String testValue = TestUtil.largeEntity;
        HttpCarbonMessage httpMsg = MessageGenerator.generateRequest(HttpMethod.POST, testValue);
        verifyResult(httpMsg, h2ClientWithPriorKnowledge, testValue);
    }

    private void verifyResult(HttpCarbonMessage httpCarbonMessage, HttpClientConnector http2ClientConnector,
                              String expectedValue) {
        HttpCarbonMessage response = new MessageSender(http2ClientConnector).sendMessage(httpCarbonMessage);
        assertNotNull(response);
        String result = TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        assertEquals(result, expectedValue, "Expected response not received");
        assertEquals(response.getHeaders().get("Trailer"), "foo,baz");
        assertEquals(response.getTrailerHeaders().get("foo"), "bar");
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            serverConnector.stop();
            httpWsConnectorFactory.shutdown();
        } catch (Exception e) {
            LOG.warn("Resource clean up is interrupted", e);
        }
    }
}
