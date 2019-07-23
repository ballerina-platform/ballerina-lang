/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.http2.servertimeout;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
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
import org.wso2.transport.http.netty.http2.listeners.Http2NoResponseListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;
import org.wso2.transport.http.netty.util.client.http2.MessageSender;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test server timeout during request receive.
 */
public class TimeoutDuringRequestReceive {
    private static final Logger LOG = LoggerFactory.getLogger(TimeoutDuringRequestReceive.class);

    private HttpClientConnector h2ClientWithPriorKnowledge;
    private ServerConnector serverConnector;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() throws InterruptedException {
        connectorFactory = new DefaultHttpWsConnectorFactory();
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.HTTP_SERVER_PORT);
        listenerConfiguration.setScheme(Constants.HTTP_SCHEME);
        listenerConfiguration.setVersion(Constants.HTTP_2_0);
        listenerConfiguration.setSocketIdleTimeout(1000);
        serverConnector = connectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new Http2NoResponseListener());
        future.sync();

        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        SenderConfiguration senderConfiguration1 = getSenderConfiguration();
        senderConfiguration1.setForceHttp2(true);
        h2ClientWithPriorKnowledge = connectorFactory.createHttpClientConnector(
                HttpConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration1);
    }

    private SenderConfiguration getSenderConfiguration() {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setScheme(Constants.HTTP_SCHEME);
        senderConfiguration.setHttpVersion(Constants.HTTP_2_0);
        //Set this to a value larger than server socket timeout value, to make sure that the server times out first
        senderConfiguration.setSocketIdleTimeout(500000);
        return senderConfiguration;
    }

    @Test
    public void testServerTimeout() {
        HttpCarbonMessage httpMsg1 = MessageGenerator.generateDelayedRequest(HttpMethod.POST);
        verifyResult(httpMsg1, h2ClientWithPriorKnowledge);
    }

    private void verifyResult(HttpCarbonMessage httpCarbonMessage, HttpClientConnector http2ClientConnector) {
        byte[] data1 = "Content data part1".getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuff1 = ByteBuffer.wrap(data1);
        httpCarbonMessage.addHttpContent(new DefaultHttpContent(Unpooled.wrappedBuffer(byteBuff1)));
        HttpCarbonMessage response = new MessageSender(http2ClientConnector).sendMessage(httpCarbonMessage);
        assertNotNull(response);
        assertEquals(response.getHttpStatusCode().intValue(), HttpResponseStatus.REQUEST_TIMEOUT.code(),
                     "Expected response not received");
    }

    @AfterClass
    public void cleanUp() {
        h2ClientWithPriorKnowledge.close();
        serverConnector.stop();
        try {
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to close");
        }
    }
}
