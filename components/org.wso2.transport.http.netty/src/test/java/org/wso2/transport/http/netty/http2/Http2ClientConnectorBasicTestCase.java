/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.http2;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.Http2ClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.transport.http.netty.message.EmptyLastHttpContent;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.HTTPConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.Http2EchoServerInitializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class Http2ClientConnectorBasicTestCase {

    private HttpServer http2Server;
    private Http2ClientConnector http2ClientConnector;

    @BeforeClass
    public void setup() {
        TransportsConfiguration transportsConfiguration = TestUtil.getConfiguration(
                "/simple-test-config" + File.separator + "netty-transports.yml");

        http2Server = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new Http2EchoServerInitializer());
        HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();
        http2ClientConnector = connectorFactory.createHttp2ClientConnector(
                HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTP_SCHEME));
    }

    @Test
    public void testHttp2Get() {
        HTTPCarbonMessage httpCarbonMessage = generateRequest(HttpMethod.GET, null);
        HTTPCarbonMessage response = sendMessage(httpCarbonMessage);
        assertNotNull(response);
    }

    @Test
    public void testHttp2Post() {
        String testValue = "Test Message";
        HTTPCarbonMessage httpCarbonMessage = generateRequest(HttpMethod.POST, testValue);
        HTTPCarbonMessage response = sendMessage(httpCarbonMessage);
        assertNotNull(response);
        String result = new BufferedReader(
                new InputStreamReader(new HttpMessageDataStreamer(
                        response).getInputStream())).lines().collect(Collectors.joining("\n"));
        assertEquals(testValue, result);
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            http2Server.shutdown();
        } catch (InterruptedException e) {
            TestUtil.handleException("Failed to shutdown the test server", e);
        }
    }

    private HTTPCarbonMessage sendMessage(HTTPCarbonMessage httpCarbonMessage) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            HttpResponseFuture responseFuture = http2ClientConnector.send(httpCarbonMessage);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(5, TimeUnit.SECONDS);
            return listener.getHttpResponseMessage();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while sending a message", e);
        }
        return null;
    }

    private HTTPCarbonMessage generateRequest(HttpMethod httpMethod, String payload) {
        HTTPCarbonMessage httpCarbonMessage = new HttpCarbonRequest(
                new DefaultHttpRequest(new HttpVersion(Constants.HTTP_VERSION_2_0, true), httpMethod,
                                       "http://" + TestUtil.TEST_HOST + ":" + TestUtil.HTTP_SERVER_PORT));
        httpCarbonMessage.setProperty(Constants.HTTP_METHOD, httpMethod.toString());
        httpCarbonMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
        httpCarbonMessage.setProperty(Constants.PORT, TestUtil.HTTP_SERVER_PORT);
        httpCarbonMessage.setHeader("Host", TestUtil.TEST_HOST + ":" + TestUtil.HTTP_SERVER_PORT);
        if (payload != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(payload.getBytes(Charset.forName("UTF-8")));
            httpCarbonMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
        } else {
            httpCarbonMessage.addHttpContent(new EmptyLastHttpContent());
        }
        return httpCarbonMessage;
    }

}

