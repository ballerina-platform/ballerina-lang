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
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * A test case for echo message from MessageProcessor level
 */
public class ContentAwareMessageProcessorTestCase {
    private static final Logger log = LoggerFactory.getLogger(ContentAwareMessageProcessorTestCase.class);

    private HTTPTransportListener httpTransportListener;

    private ListenerConfiguration listenerConfiguration;

    private SenderConfiguration senderConfiguration;

    private HTTPServer httpServer;
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8490));

//    @BeforeClass(groups = "contentaware")
    public void setUp() {
//        listenerConfiguration = new ListenerConfiguration();
//        listenerConfiguration.setHost(TestUtil.TEST_HOST);
//        listenerConfiguration.setId("test-listener");
//        listenerConfiguration.setPort(TestUtil.TEST_ESB_PORT);
//        senderConfiguration = new SenderConfiguration("passthrough-sender");
//        httpTransportListener = TestUtil
//         .startCarbonTransport(listenerConfiguration, senderConfiguration, new MessageEchoingMessageProcessor());
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_SERVER_PORT);
    }

//    @Test(groups = "contentaware")
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
            log.error("IO Exception occurred", e);
            assertTrue(false);
        }

    }

    //@Test(groups = "contentaware", dependsOnMethods = "messageEchoingFromProcessorTestCase")
    public void requestResponseTransformFromProcessorTestCase() {

        String requestValue = "XXXXXXXX";
        String responseValue = "YYYYYYY";
        String expectedValue = responseValue + ":" + requestValue;
        try {
            CarbonMessageProcessor carbonMessageProcessor = new RequestResponseTransformProcessor(responseValue);
            TestUtil.updateMessageProcessor(carbonMessageProcessor, senderConfiguration, listenerConfiguration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(expectedValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            log.error("IO Exception occurred", e);
            assertTrue(false);
        }
    }

    //@Test(groups = "contentaware", dependsOnMethods = "requestResponseTransformFromProcessorTestCase")
    public void requestResponseCreationFromProcessorTestCase() {

        String requestValue = "XXXXXXXX";
        String responseValue = "YYYYYYY";
        String expectedValue = responseValue + ":" + requestValue;
        try {
            CarbonMessageProcessor carbonMessageProcessor = new RequestResponseCreationProcessor(responseValue);
            TestUtil.updateMessageProcessor(carbonMessageProcessor, senderConfiguration, listenerConfiguration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(expectedValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            log.error("IO Exception occurred", e);
            assertTrue(false);
        }

    }

    //@Test(groups = "contentaware", dependsOnMethods = "requestResponseCreationFromProcessorTestCase")
    public void requestResponseStreamingFromProcessorTestCase() {

        String requestValue = "<A><B><C>Test Message</C></B></A>";
        try {
            CarbonMessageProcessor carbonMessageProcessor = new RequestResponseCreationStreamingProcessor();
            TestUtil.updateMessageProcessor(carbonMessageProcessor, senderConfiguration, listenerConfiguration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(requestValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            log.error("IO Exception occurred", e);
            assertTrue(false);
        }

    }

    //@Test(groups = "contentaware", dependsOnMethods = "requestResponseStreamingFromProcessorTestCase")
    public void requestResponseTransformStreamingFromProcessorTestCase() {

        String requestValue = "<A><B><C>Test Message</C></B></A>";
        try {
            CarbonMessageProcessor carbonMessageProcessor = new RequestResponseTransformStreamingProcessor();
            TestUtil.updateMessageProcessor(carbonMessageProcessor, senderConfiguration, listenerConfiguration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(requestValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            log.error("IO Exception occurred", e);
            assertTrue(false);
        }

    }

    //@Test(groups = "contentaware",  dependsOnMethods = "requestResponseTransformStreamingFromProcessorTestCase")
    public void responseStreamingWithoutBufferingTestCase() {

        String requestValue = "<A><B><C>Test Message</C></B></A>";
        try {
            CarbonMessageProcessor carbonMessageProcessor = new ResponseStreamingWithoutBufferingProcessor();
            TestUtil.updateMessageProcessor(carbonMessageProcessor, senderConfiguration, listenerConfiguration);
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, requestValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            assertEquals(requestValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            log.error("IO Exception occurred", e);
            assertTrue(false);
        }

    }

    //@AfterClass(groups = "contentaware", alwaysRun = true)
    public void cleanUp() {
        TestUtil.cleanUp(httpTransportListener, httpServer);
    }

}
