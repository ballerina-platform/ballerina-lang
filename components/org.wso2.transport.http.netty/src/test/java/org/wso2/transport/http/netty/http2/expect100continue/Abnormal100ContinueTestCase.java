/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.http2.expect100continue;

import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;
import org.wso2.transport.http.netty.util.client.http2.MessageSender;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.http2.expect100continue.Abnormal100ContinueServerInitializer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.transport.http.netty.util.Http2Util.HTTP2_RESPONSE_PAYLOAD;
import static org.wso2.transport.http.netty.util.Http2Util.getHttp2Client;
import static org.wso2.transport.http.netty.util.TestUtil.HTTP_SCHEME;

/**
 * Test whether the HTTP/2 client gracefully handles the 100-continue response when the request doesn't contain the
 * expect header.
 */
public class Abnormal100ContinueTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(Abnormal100ContinueTestCase.class);
    private HttpClientConnector h2PriorOnClient;
    private HttpWsConnectorFactory connectorFactory;
    private HttpServer http2Server;

    @BeforeClass
    public void setup() throws InterruptedException {
        connectorFactory = new DefaultHttpWsConnectorFactory();
        h2PriorOnClient = getHttp2Client(connectorFactory, true, 3000);
        http2Server = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new Abnormal100ContinueServerInitializer());
    }

    @Test
    public void test100ContinueAbnormalWithPriorOn() {
        HttpCarbonMessage httpCarbonMessage = MessageGenerator.generateRequest(HttpMethod.POST, "test",
                                                                               TestUtil.HTTP_SERVER_PORT,
                                                                               HTTP_SCHEME);
        HttpCarbonMessage response = new MessageSender(h2PriorOnClient).sendMessage(httpCarbonMessage);
        assertNotNull(response);
        String finalResponsePayload = TestUtil.getStringFromInputStream(
                new HttpMessageDataStreamer(response).getInputStream());
        assertEquals(finalResponsePayload, HTTP2_RESPONSE_PAYLOAD);
    }

    @AfterClass
    public void cleanUp() {
        try {
            h2PriorOnClient.close();
            http2Server.shutdown();
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsConnectorFactory to close");
        }
    }
}
