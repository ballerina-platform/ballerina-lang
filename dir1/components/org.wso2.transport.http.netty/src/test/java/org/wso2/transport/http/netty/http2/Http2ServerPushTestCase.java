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

package org.wso2.transport.http.netty.http2;

import io.netty.handler.codec.http.HttpMethod;
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
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.http2.listeners.Http2ServerConnectorListener;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.message.ResponseHandle;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;
import org.wso2.transport.http.netty.util.client.http2.MessageSender;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * {@code Http2ServerPushTestCase} contains test cases for HTTP2 Server push functionality.
 */
public class Http2ServerPushTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(Http2ServerPushTestCase.class);

    private HttpClientConnector httpClientConnector;
    private ServerConnector serverConnector;
    private SenderConfiguration senderConfiguration;
    private HttpWsConnectorFactory connectorFactory;

    private final String expectedResource = "/main";
    private final String promisedResource1 = "/resource1";
    private final String promisedResource2 = "/resource2";

    @BeforeClass
    public void setup() throws InterruptedException {
        connectorFactory = new DefaultHttpWsConnectorFactory();
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.HTTP_SERVER_PORT);
        listenerConfiguration.setScheme(Constants.HTTP_SCHEME);
        listenerConfiguration.setVersion(Constants.HTTP_2_0);
        serverConnector = connectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        Http2ServerConnectorListener http2ServerConnectorListener = new Http2ServerConnectorListener();
        http2ServerConnectorListener.
                setExpectedResource(expectedResource).setPromisedResources(promisedResource1, promisedResource2);
        future.setHttpConnectorListener(http2ServerConnectorListener);
        future.sync();

        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        senderConfiguration = HttpConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTP_SCHEME);
        senderConfiguration.setHttpVersion(Constants.HTTP_2_0);

        httpClientConnector = connectorFactory.createHttpClientConnector(
                HttpConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration);
    }

    @Test
    public void testHttp2ServerPush() {
        MessageSender msgSender = new MessageSender(httpClientConnector);

        HttpCarbonMessage request = MessageGenerator.generateRequest(HttpMethod.POST, expectedResource);
        // Submit a request and get the handle
        ResponseHandle handle = msgSender.submitMessage(request);
        assertNotNull(handle, "Response handle not found");

        // Look for 1st promise
        assertTrue(msgSender.checkPromiseAvailability(handle), "Promises not available");
        // Get the 1st promise
        Http2PushPromise promise1 = msgSender.getNextPromise(handle);
        assertNotNull(promise1, "Promise 1 not available");
        String path = promise1.getPath();
        boolean promise1Received = false;
        if (path.equals(promisedResource1)) {
            promise1Received = true;
        } else if (!path.equals(promisedResource2)) {
            fail("Invalid Promise received");
        }

        // Look for 2nd promise
        assertTrue(msgSender.checkPromiseAvailability(handle), "Promises not available");
        // Get the 2nd promise
        Http2PushPromise promise2 = msgSender.getNextPromise(handle);
        assertNotNull(promise2, "Promise 2 not available");
        path = promise2.getPath();
        if (promise1Received) {
            assertEquals(path, promisedResource2, "Invalid Promise received");
        } else {
            assertEquals(path, promisedResource1, "Invalid Promise received");
        }

        // Get the expected response
        HttpCarbonMessage response = msgSender.getResponse(handle);
        assertNotNull(response);
        String result = TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        assertEquals(result, expectedResource, "Expected response not received");

        // Get the 1st promised response
        HttpCarbonMessage promisedResponse = msgSender.getPushResponse(promise1);
        assertNotNull(promisedResponse);
        result = TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(promisedResponse).getInputStream());
        assertTrue(result.contains(promisedResource1), "Promised response 1 not received");

        // Get the 2nd promised response
        promisedResponse = msgSender.getPushResponse(promise2);
        assertNotNull(promisedResponse);
        result = TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(promisedResponse).getInputStream());
        assertTrue(result.contains(promisedResource2), "Promised response 2 not received");
    }

    @AfterClass
    public void cleanUp() {
        senderConfiguration.setHttpVersion(String.valueOf(Constants.HTTP_1_1));
        httpClientConnector.close();
        serverConnector.stop();
        try {
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to close");
        }
    }
}
