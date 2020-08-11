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

package org.wso2.transport.http.netty.http2.clientchannelclose;

import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.http2.listeners.Http2NoResponseListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.transport.http.netty.util.Http2Util.getH2ListenerConfigs;
import static org.wso2.transport.http.netty.util.Http2Util.getTestHttp2Client;
import static org.wso2.transport.http.netty.util.TestUtil.HTTP_SCHEME;
import static org.wso2.transport.http.netty.util.TestUtil.SERVER_PORT1;

/**
 * When the remote channel close the connection due to a SSL handshake issue, client should be notified of it.
 */
public class RemoteChannelCloseWithSSLError {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteChannelCloseWithSSLError.class);
    private HttpClientConnector h2PriorOnClient;
    private ServerConnector serverConnector;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() throws InterruptedException {
        connectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = connectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), getH2ListenerConfigs());
        ServerConnectorFuture future = serverConnector.start();
        Http2NoResponseListener http2ServerConnectorListener = new Http2NoResponseListener();
        future.setHttpConnectorListener(http2ServerConnectorListener);
        future.sync();
        h2PriorOnClient = getTestHttp2Client(connectorFactory, true);
    }

    //TODO:Change the assertion state once the issue https://githubcom/ballerina-platform/ballerina-lang/issues/17539
    // is fixed.
    @Test
    public void testRemoteChannelClose() {
        HttpCarbonMessage request = MessageGenerator.generateRequest(HttpMethod.POST, "test", SERVER_PORT1,
                                                                     HTTP_SCHEME);
        try {
            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = h2PriorOnClient.send(request);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
            Throwable error = listener.getHttpErrorMessage();
            AssertJUnit.assertNotNull(error);
            assertTrue(error instanceof ServerConnectorException,
                       "Exception is not an instance of ServerConnectorException");
            String result = error.getMessage();
            assertEquals(result, Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE,
                         "Expected error message not received");
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running testRemoteChannelClose test case", e);
        }
    }

    @AfterClass
    public void cleanUp() {
        h2PriorOnClient.close();
        serverConnector.stop();
        try {
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to close");
        }
    }
}
