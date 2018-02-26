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
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.Http2ClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageSender;
import org.wso2.transport.http.netty.util.client.http2.RequestGenerator;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.Http2EchoServerInitializer;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/* This contains basic test cases for HTTP2 Client connector */
public class Http2ClientConnectorBasicTestCase {

    private HttpServer http2Server;
    private Http2ClientConnector http2ClientConnector;

    @BeforeClass
    public void setup() {
        TransportsConfiguration transportsConfiguration = TestUtil.getConfiguration(
                "/simple-test-config" + File.separator + "netty-transports.yml");

        http2Server = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new Http2EchoServerInitializer());
        HttpWsConnectorFactory connectorFactory = new DefaultHttpWsConnectorFactory();
        http2ClientConnector = connectorFactory.createHttp2ClientConnector(
                HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTP_SCHEME));
    }

    @Test
    public void testHttp2Get() {
        HTTPCarbonMessage httpCarbonMessage = RequestGenerator.generateRequest(HttpMethod.GET, null);
        HTTPCarbonMessage response = MessageSender.sendMessage(httpCarbonMessage, http2ClientConnector);
        assertNotNull(response);
    }

    @Test
    public void testHttp2Post() {
        String testValue = "Test Message";
        HTTPCarbonMessage httpCarbonMessage = RequestGenerator.generateRequest(HttpMethod.POST, testValue);
        HTTPCarbonMessage response = MessageSender.sendMessage(httpCarbonMessage, http2ClientConnector);
        assertNotNull(response);
        String result = TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
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

}

