/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.urilengthvalidation;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contentaware.listeners.EchoStreamingMessageListener;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http.HttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;

import static org.testng.AssertJUnit.assertEquals;

/**
 * This class tests for 414 and 413 responses
 */
public class Status414And413ResponseTest {

    private static final Logger log = LoggerFactory.getLogger(Status414And413ResponseTest.class);

    protected ServerConnector serverConnector;
    protected ListenerConfiguration listenerConfiguration;
    private static final String testValue = "Test Message";
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", TestUtil.SERVER_CONNECTOR_PORT));

    Status414And413ResponseTest() {
        this.listenerConfiguration = new ListenerConfiguration();
    }

    @BeforeClass
    public void setUp() {
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        listenerConfiguration.setServerHeader(TestUtil.TEST_SERVER);
        listenerConfiguration.getRequestSizeValidationConfig().setMaxEntityBodySize(1024);

        ServerBootstrapConfiguration serverBootstrapConfig = new ServerBootstrapConfiguration(new HashMap<>());
        HttpWsConnectorFactory httpWsConnectorFactory = new HttpWsConnectorFactoryImpl();

        serverConnector = httpWsConnectorFactory.createServerConnector(serverBootstrapConfig, listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new EchoStreamingMessageListener());
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
            log.error("Thread Interrupted while sleeping ", e);
        }
    }

    @Test
    public void largeUriTest() {
        try {
            HttpURLConnection urlConn;

            urlConn = sendExtraLongUri();
            assertEquals(HttpResponseStatus.REQUEST_URI_TOO_LONG.code(), urlConn.getResponseCode());
            assertEquals(TestUtil.TEST_SERVER, urlConn.getHeaderField(Constants.HTTP_SERVER_HEADER));

            urlConn = sendShortUri();
            String content = TestUtil.getContent(urlConn);
            assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
            assertEquals(TestUtil.TEST_SERVER, urlConn.getHeaderField(Constants.HTTP_SERVER_HEADER));
            assertEquals(testValue, content);

            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running largeUriTest", e);
        }
    }

    @Test
    public void largeHeaderTest() {
        try {
            HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);

            FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.POST, "/", Unpooled.wrappedBuffer(testValue.getBytes()));
            httpRequest.headers().set("X-Test", getLargeHeader());
            FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);

            assertEntityTooLargeResponse(httpResponse);

            httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);
            httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.POST, "/", Unpooled.wrappedBuffer(testValue.getBytes()));
            httpResponse = httpClient.sendRequest(httpRequest);
            String payload = TestUtil.getEntityBodyFrom(httpResponse);

            assertEquals(testValue, payload);
            assertEquals(HttpResponseStatus.OK.code(), httpResponse.status().code());
            assertEquals(TestUtil.TEST_SERVER, httpResponse.headers().get(Constants.HTTP_SERVER_HEADER));

        } catch (Exception e) {
            TestUtil.handleException("IOException occurred while running largeHeaderTest", e);
        }
    }

    @Test
    public void largeEntityBodyTest() {
        try {
            HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);

            FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.POST, "/", Unpooled.wrappedBuffer(TestUtil.largeEntity.getBytes()));
            FullHttpResponse httpResponse = httpClient.sendChunkRequest(httpRequest);
            assertEntityTooLargeResponse(httpResponse);

            httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);
            httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.POST, "/", Unpooled.wrappedBuffer(TestUtil.largeEntity.getBytes()));
            httpResponse = httpClient.sendRequest(httpRequest);
            assertEntityTooLargeResponse(httpResponse);

            httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);
            httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.POST, "/", Unpooled.wrappedBuffer(TestUtil.smallEntity.getBytes()));
            httpResponse = httpClient.sendRequest(httpRequest);
            assertEquals(HttpResponseStatus.OK.code(), httpResponse.status().code());
            assertEquals(TestUtil.TEST_SERVER, httpResponse.headers().get(Constants.HTTP_SERVER_HEADER));

        } catch (Exception e) {
            TestUtil.handleException("IOException occurred while running largeEntityBodyTest", e);
        }
    }

    private void assertEntityTooLargeResponse(FullHttpResponse httpResponse) {
        assertEquals(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE.code(), httpResponse.status().code());
        assertEquals(Constants.CONNECTION_CLOSE, httpResponse.headers().get(Constants.HTTP_CONNECTION));
        assertEquals(TestUtil.TEST_SERVER, httpResponse.headers().get(Constants.HTTP_SERVER_HEADER));
    }

    private String getLargeHeader() {
        StringBuilder header = new StringBuilder("x");
        for (int i = 0; i < 9000; i++) {
            header.append("x");
        }
        return header.toString();
    }

    private HttpURLConnection sendShortUri() throws IOException {
        HttpURLConnection urlConn;
        urlConn = TestUtil
                .request(baseURI, getUriWithLengthOf(900), HttpMethod.POST.name(), true);
        urlConn.getOutputStream().write(testValue.getBytes());
        urlConn.getOutputStream().flush();
        return urlConn;
    }

    private HttpURLConnection sendExtraLongUri() throws IOException {
        HttpURLConnection urlConn = TestUtil
                .request(baseURI, getUriWithLengthOf(9000), HttpMethod.POST.name(), true);
        urlConn.getOutputStream().write(testValue.getBytes());
        urlConn.getOutputStream().flush();
        return urlConn;
    }

    private String getUriWithLengthOf(int length) {
        StringBuilder uri = new StringBuilder("/");
        for (int i = 0; i < length; i++) {
            uri.append("x");
        }
        return uri.toString();
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        serverConnector.stop();
    }
}
