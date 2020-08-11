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

import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;
import org.wso2.transport.http.netty.util.client.http2.MessageSender;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.http2.Http2EchoServerInitializer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * This contains basic test cases for HTTP2 Client connector.
 */
public class Http2ClientConnectorBasicTestCase {

    private HttpServer http2Server;
    private HttpClientConnector httpClientConnector;
    private SenderConfiguration senderConfiguration;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() {
        http2Server = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new Http2EchoServerInitializer());
        connectorFactory = new DefaultHttpWsConnectorFactory();

        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        senderConfiguration = HttpConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTP_SCHEME);
        senderConfiguration.setHttpVersion(Constants.HTTP_2_0);

        httpClientConnector = connectorFactory.createHttpClientConnector(
                HttpConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration);
    }

    @Test
    public void testHttp2Get() {
        HttpCarbonMessage httpCarbonMessage = MessageGenerator.generateRequest(HttpMethod.GET, null);
        HttpCarbonMessage response = new MessageSender(httpClientConnector).sendMessage(httpCarbonMessage);
        assertNotNull(response, "Expected response not received");
    }

    @Test
    public void testHttp2Post() {
        String testValue = "Test Message";
        HttpCarbonMessage httpCarbonMessage = MessageGenerator.generateRequest(HttpMethod.POST, testValue);
        HttpCarbonMessage response = new MessageSender(httpClientConnector).sendMessage(httpCarbonMessage);
        assertNotNull(response);
        String result = TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        assertEquals(result, testValue, "Expected response not received");
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            senderConfiguration.setHttpVersion(String.valueOf(Constants.HTTP_1_1));
            http2Server.shutdown();
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            TestUtil.handleException("Failed to shutdown the test server", e);
        }
    }
}
