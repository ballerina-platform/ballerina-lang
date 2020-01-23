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

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
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
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.http2.listeners.Http2EchoServerWithTrailerHeader;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.Http2Util;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageSender;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.transport.http.netty.util.TestUtil.HTTP_SCHEME;

/**
 * Test case for H2 trailer headers come along with inbound request.
 *
 * @since 6.3.0
 */
public class H2ClientRequestTrailerHeaderTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(H2ClientRequestTrailerHeaderTestCase.class);

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
        http2ConnectorListener.setMessageType(Http2EchoServerWithTrailerHeader.MessageType.REQUEST);
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
    public void testSmallPayload() {
        String testValue = "Test Http2 Message";
        HttpHeaders trailers = new DefaultLastHttpContent().trailingHeaders();
        trailers.add("foo", "xyz");
        trailers.add("bar", "ballerina");

        HttpCarbonMessage httpMsg = generateRequestWithTrailers(HttpMethod.POST, testValue, trailers);
        HttpHeaders httpHeaders = verifyResult(httpMsg, h2ClientWithPriorKnowledge, testValue);
        assertEquals(httpHeaders.get("Request-trailer"), "foo,bar");
        assertEquals(httpHeaders.get("foo"), "xyz");
        assertEquals(httpHeaders.get("bar"), "ballerina");
    }

    @Test
    public void testLargePayload() {
        String testValue = TestUtil.largeEntity;
        HttpHeaders trailers = new DefaultLastHttpContent().trailingHeaders();
        trailers.add("foo", "baz");
        trailers.add("xyz", "ballerina");

        HttpCarbonMessage httpMsg = generateRequestWithTrailers(HttpMethod.POST, testValue, trailers);
        HttpHeaders httpHeaders = verifyResult(httpMsg, h2ClientWithPriorKnowledge, testValue);
        assertEquals(httpHeaders.get("Request-trailer"), "foo,xyz");
        assertEquals(httpHeaders.get("foo"), "baz");
        assertEquals(httpHeaders.get("xyz"), "ballerina");
    }

    private HttpCarbonMessage generateRequestWithTrailers(HttpMethod httpMethod, String payload,
                                                          HttpHeaders trailers) {
        HttpCarbonMessage httpCarbonMessage = new HttpCarbonRequest(new DefaultHttpRequest(
                new HttpVersion(Constants.DEFAULT_VERSION_HTTP_1_1, true), httpMethod,
                HTTP_SCHEME + TestUtil.TEST_HOST + ":" + TestUtil.HTTP_SERVER_PORT));
        httpCarbonMessage.setHttpMethod(httpMethod.toString());
        httpCarbonMessage.setProperty(Constants.HTTP_HOST, TestUtil.TEST_HOST);
        httpCarbonMessage.setProperty(Constants.HTTP_PORT, TestUtil.HTTP_SERVER_PORT);
        httpCarbonMessage.setHeader("Host", TestUtil.TEST_HOST + ":" + TestUtil.HTTP_SERVER_PORT);

        String trailerHeaderValue = String.join(",", trailers.names());
        httpCarbonMessage.setHeader(HttpHeaderNames.TRAILER.toString(), trailerHeaderValue);

        DefaultLastHttpContent lastHttpContent;
        if (payload != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(payload.getBytes(Charset.forName("UTF-8")));
            lastHttpContent = new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer));
        } else {
            lastHttpContent = new DefaultLastHttpContent();
        }
        lastHttpContent.trailingHeaders().add(trailers);
        httpCarbonMessage.addHttpContent(lastHttpContent);
        return httpCarbonMessage;
    }

    private HttpHeaders verifyResult(HttpCarbonMessage httpCarbonMessage, HttpClientConnector http2ClientConnector,
                                     String expectedValue) {
        HttpCarbonMessage response = new MessageSender(http2ClientConnector).sendMessage(httpCarbonMessage);
        assertNotNull(response);
        String result = TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        assertEquals(result, expectedValue, "Expected response not received");
        return response.getHeaders();
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
