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

package org.ballerinalang.net.netty.http2;

import io.netty.handler.codec.http.HttpMethod;
import org.ballerinalang.net.netty.contentaware.listeners.EchoMessageListener;
import org.ballerinalang.net.netty.contract.Constants;
import org.ballerinalang.net.netty.contract.HttpClientConnector;
import org.ballerinalang.net.netty.contract.HttpWsConnectorFactory;
import org.ballerinalang.net.netty.contract.ServerConnector;
import org.ballerinalang.net.netty.contract.ServerConnectorFuture;
import org.ballerinalang.net.netty.contract.config.ListenerConfiguration;
import org.ballerinalang.net.netty.contract.config.SenderConfiguration;
import org.ballerinalang.net.netty.contract.config.TransportsConfiguration;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.ballerinalang.net.netty.message.HttpConnectorUtil;
import org.ballerinalang.net.netty.message.HttpMessageDataStreamer;
import org.ballerinalang.net.netty.util.TestUtil;
import org.ballerinalang.net.netty.util.client.http2.MessageGenerator;
import org.ballerinalang.net.netty.util.client.http2.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * This contains test case for handling requests send by a client with a prior knowledge of server support HTTP/2.
 * That means communication happens over HTTP/2 without a connection upgrade from HTTP/1.x.
 */
public class Http2WithPriorKnowledgeTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(Http2WithPriorKnowledgeTestCase.class);

    private HttpClientConnector httpClientConnector;
    private ServerConnector serverConnector;
    private SenderConfiguration senderConfiguration;
    private HttpWsConnectorFactory connectorFactory;

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
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        senderConfiguration = HttpConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTP_SCHEME);
        senderConfiguration.setHttpVersion(Constants.HTTP_2_0);
        senderConfiguration.setForceHttp2(true);       // Force to use HTTP/2 without an upgrade
        httpClientConnector = connectorFactory.createHttpClientConnector(
                HttpConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration);
    }

    @Test
    public void testHttp2WithPriorKnowledge() {
        String testValue = "Test Message";
        HttpCarbonMessage httpCarbonMessage = MessageGenerator.generateRequest(HttpMethod.POST, testValue);
        HttpCarbonMessage response = new MessageSender(httpClientConnector).sendMessage(httpCarbonMessage);
        assertNotNull(response, "Expected response not received");
        String result = TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        assertEquals(result, testValue, "Expected response not received");
    }

    @AfterClass
    public void cleanUp() {
        senderConfiguration.setHttpVersion(String.valueOf(Constants.HTTP_1_1));
        senderConfiguration.setForceHttp2(false);
        httpClientConnector.close();
        serverConnector.stop();
        try {
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to close");
        }
    }
}
