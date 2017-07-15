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

package org.wso2.carbon.transport.http.netty.contentaware;

import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.passthrough.PassthroughMessageProcessor;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * A test case for echo message from MessageProcessor level.
 */
public class ContentAwareMessageProcessorTestCase {
    private static final Logger log = LoggerFactory.getLogger(ContentAwareMessageProcessorTestCase.class);

    private List<HTTPServerConnector> serverConnectors;
    private CarbonMessageProcessor carbonMessageProcessor;
    private TransportsConfiguration configuration;

    private HTTPServer httpServer;
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8490));

    @BeforeClass
    public void setUp() {
        configuration = YAMLTransportConfigurationBuilder
                .build("src/test/resources/simple-test-config/netty-transports.yml");
        serverConnectors = TestUtil.startConnectors(configuration, new PassthroughMessageProcessor());
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_SERVER_PORT);
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
        } finally {
            TestUtil.removeMessageProcessor(carbonMessageProcessor);
        }

    }

    @Test
    public void requestResponseTransformFromProcessorTestCase() {

        String requestValue = "XXXXXXXX";
        String responseValue = "YYYYYYY";
        String expectedValue = responseValue + ":" + requestValue;
        try {
            carbonMessageProcessor = new RequestResponseTransformProcessor(responseValue);
            TestUtil.updateMessageProcessor(carbonMessageProcessor, configuration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(expectedValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException(
                    "IOException occurred while running requestResponseTransformFromProcessorTestCase", e);
        } finally {
            TestUtil.removeMessageProcessor(carbonMessageProcessor);
        }
    }

    @Test
    public void requestResponseCreationFromProcessorTestCase() {
        String requestValue = "XXXXXXXX";
        String responseValue = "YYYYYYY";
        String expectedValue = responseValue + ":" + requestValue;
        try {
            carbonMessageProcessor = new RequestResponseCreationProcessor(responseValue);
            TestUtil.updateMessageProcessor(carbonMessageProcessor, configuration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(expectedValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException(
                    "IOException occurred while running requestResponseCreationFromProcessorTestCase", e);
        } finally {
            TestUtil.removeMessageProcessor(carbonMessageProcessor);
        }

    }

    @Test
    public void requestResponseStreamingFromProcessorTestCase() {
        String requestValue = "<A><B><C>Test Message</C></B></A>";
        try {
            carbonMessageProcessor = new RequestResponseCreationStreamingProcessor();
            TestUtil.updateMessageProcessor(carbonMessageProcessor, configuration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(requestValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException(
                    "IOException occurred while running requestResponseStreamingFromProcessorTestCase", e);
        } finally {
            TestUtil.removeMessageProcessor(carbonMessageProcessor);
        }

    }

    @Test
    public void requestResponseTransformStreamingFromProcessorTestCase() {

        String requestValue = "<A><B><C>Test Message</C></B></A>";
        try {
            carbonMessageProcessor = new RequestResponseTransformStreamingProcessor();
            TestUtil.updateMessageProcessor(carbonMessageProcessor, configuration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(requestValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException(
                    "IOException occurred while running requestResponseTransformStreamingFromProcessorTestCase", e);
        } finally {
            TestUtil.removeMessageProcessor(carbonMessageProcessor);
        }
    }

    @Test
    public void responseStreamingWithoutBufferingTestCase() {

        String requestValue = "<A><B><C>Test Message</C></B></A>";
        try {
            carbonMessageProcessor = new ResponseStreamingWithoutBufferingProcessor();
            TestUtil.updateMessageProcessor(carbonMessageProcessor, configuration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            urlConn.setChunkedStreamingMode(-1); // Enable Chunking
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(requestValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running responseStreamingWithoutBufferingTestCase", e);
        } finally {
            TestUtil.removeMessageProcessor(carbonMessageProcessor);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        TestUtil.cleanUp(serverConnectors, httpServer);
    }
}
