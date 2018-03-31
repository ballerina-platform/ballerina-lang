/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.contentaware;

import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.transport.http.netty.contentaware.listeners.RequestResponseCreationListener;
import org.wso2.transport.http.netty.contentaware.listeners.RequestResponseCreationStreamingListener;
import org.wso2.transport.http.netty.contentaware.listeners.RequestResponseTransformListener;
import org.wso2.transport.http.netty.contentaware.listeners.RequestResponseTransformStreamingListener;
import org.wso2.transport.http.netty.contentaware.listeners.ResponseStreamingWithoutBufferingListener;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.passthrough.PassthroughMessageProcessorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.EchoServerInitializer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * A test case for echo message from MessageProcessor level.
 */
public class ContentAwareMessageProcessorTestCase {

    private List<ServerConnector> serverConnectors;
    private HttpConnectorListener httpConnectorListener;
    private TransportsConfiguration configuration;

    private HttpServer httpServer;
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", TestUtil.SERVER_CONNECTOR_PORT));

    @BeforeClass
    public void setUp() {
        configuration = YAMLTransportConfigurationBuilder
                .build("src/test/resources/simple-test-config/netty-transports.yml");
        serverConnectors = TestUtil.startConnectors(
                configuration, new PassthroughMessageProcessorListener(new SenderConfiguration()));
        httpServer = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new EchoServerInitializer());
    }

    @Test
    public void messageEchoingFromProcessorTestCase() {
        String testValue = "Test Message";
        try {
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, testValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(testValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running messageEchoingFromProcessorTestCase", e);
        }
    }

    @Test
    public void requestResponseTransformFromProcessorTestCase() {

        String requestValue = "XXXXXXXX";
        String responseValue = "YYYYYYY";
        String expectedValue = responseValue + ":" + requestValue;
        try {
            httpConnectorListener = new RequestResponseTransformListener(responseValue, configuration);
            TestUtil.updateMessageProcessor(httpConnectorListener);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(expectedValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException(
                    "IOException occurred while running requestResponseTransformFromProcessorTestCase", e);
        }
    }

    @Test
    public void requestResponseCreationFromProcessorTestCase() {
        String requestValue = "XXXXXXXX";
        String responseValue = "YYYYYYY";
        String expectedValue = responseValue + ":" + requestValue;
        try {
            httpConnectorListener = new RequestResponseCreationListener(responseValue, configuration);
            TestUtil.updateMessageProcessor(httpConnectorListener);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(expectedValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException(
                    "IOException occurred while running requestResponseCreationFromProcessorTestCase", e);
        }
    }

    @Test
    public void requestResponseStreamingFromProcessorTestCase() {
        String requestValue = "<A><B><C>Test Message</C></B></A>";
        try {
            httpConnectorListener = new RequestResponseCreationStreamingListener(configuration);
            TestUtil.updateMessageProcessor(httpConnectorListener);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(requestValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException(
                    "IOException occurred while running requestResponseStreamingFromProcessorTestCase", e);
        }
    }

    @Test
    public void requestResponseTransformStreamingFromProcessorTestCase() {
        String requestValue = "<A><B><C>Test Message</C></B></A>";
        try {
            httpConnectorListener = new RequestResponseTransformStreamingListener(configuration);
            TestUtil.updateMessageProcessor(httpConnectorListener);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(requestValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException(
                    "IOException occurred while running requestResponseTransformStreamingFromProcessorTestCase", e);
        }
    }

    @Test
    public void responseStreamingWithoutBufferingTestCase() {
        String requestValue = "<A><B><C>Test Message</C></B></A>";
        try {
            httpConnectorListener = new ResponseStreamingWithoutBufferingListener();
            TestUtil.updateMessageProcessor(httpConnectorListener);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            urlConn.setChunkedStreamingMode(-1); // Enable Chunking
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(requestValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running responseStreamingWithoutBufferingTestCase", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        TestUtil.cleanUp(serverConnectors, httpServer);
    }
}
