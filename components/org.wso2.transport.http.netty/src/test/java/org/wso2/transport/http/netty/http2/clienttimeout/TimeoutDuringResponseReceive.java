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

package org.wso2.transport.http.netty.http2.clienttimeout;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.http2.listeners.Http2ServerWaitDuringDataWrite;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http2.MessageGenerator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.wso2.transport.http.netty.util.Http2Util.getHttp2Client;

/**
 * {@code TimeoutDuringResponseReceive} contains test cases for HTTP/2 client timeout during response receive state.
 */
public class TimeoutDuringResponseReceive {
    private static final Logger LOG = LoggerFactory.getLogger(TimeoutDuringResponseReceive.class);

    private HttpClientConnector h2PriorOffClient;
    private HttpClientConnector h2PriorOnClient;
    private ServerConnector serverConnector;
    private HttpWsConnectorFactory connectorFactory;

    @BeforeClass
    public void setup() throws InterruptedException {
        connectorFactory = new DefaultHttpWsConnectorFactory();
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.HTTP_SERVER_PORT);
        listenerConfiguration.setScheme(Constants.HTTP_SCHEME);
        listenerConfiguration.setVersion(Constants.HTTP_2_0);
        //Set this to a value larger than client socket timeout value, to make sure that the client times out first
        listenerConfiguration.setSocketIdleTimeout(500000);
        serverConnector = connectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        Http2ServerWaitDuringDataWrite http2ServerConnectorListener = new Http2ServerWaitDuringDataWrite(12000);
        future.setHttpConnectorListener(http2ServerConnectorListener);
        future.sync();

        h2PriorOnClient = getHttp2Client(connectorFactory, true, 1000);
        h2PriorOffClient = getHttp2Client(connectorFactory, false, 1000);
    }

    @Test(expectedExceptions = DecoderException.class,
          expectedExceptionsMessageRegExp = "Idle timeout triggered while reading inbound response entity body*")
    public void testHttp2ClientTimeoutWithPriorOff() throws DecoderException {
        testH2ClientTimeout(h2PriorOffClient);
    }

    @Test(expectedExceptions = DecoderException.class,
          expectedExceptionsMessageRegExp = "Idle timeout triggered while reading inbound response entity body*")
    public void testHttp2ClientTimeoutWithPriorOn() throws DecoderException {
        testH2ClientTimeout(h2PriorOnClient);
    }

    private void testH2ClientTimeout(HttpClientConnector h2Client) {
        HttpCarbonMessage request = MessageGenerator.generateRequest(HttpMethod.POST, "test");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = h2Client.send(request);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
            Thread.sleep(10000); //Wait for the idle timeout
            HttpCarbonMessage response = listener.getHttpResponseMessage();
            TestUtil.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        } catch (InterruptedException e) {
            TestUtil.handleException("Exception occurred while running testHttp2ClientTimeout test case", e);
        }
    }

    @AfterClass
    public void cleanUp() {
        h2PriorOffClient.close();
        h2PriorOnClient.close();
        serverConnector.stop();
        try {
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to close");
        }
    }
}
