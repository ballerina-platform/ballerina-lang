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

package org.ballerinalang.net.netty.http2.http2forwardedextension;

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.net.netty.contentaware.listeners.EchoMessageListener;
import org.ballerinalang.net.netty.contract.Constants;
import org.ballerinalang.net.netty.contract.HttpClientConnector;
import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contract.HttpWsConnectorFactory;
import org.ballerinalang.net.netty.contract.ServerConnector;
import org.ballerinalang.net.netty.contract.ServerConnectorFuture;
import org.ballerinalang.net.netty.contract.config.ListenerConfiguration;
import org.ballerinalang.net.netty.contract.config.SenderConfiguration;
import org.ballerinalang.net.netty.contract.exceptions.ServerConnectorException;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.ballerinalang.net.netty.util.DefaultHttpConnectorListener;
import org.ballerinalang.net.netty.util.TestUtil;
import org.testng.annotations.AfterClass;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Util class for Http2 Forwarded tests.
 */
public class Http2ForwardedTestUtil {

    protected HttpClientConnector clientConnector;
    protected ServerConnector serverConnector;
    protected SenderConfiguration senderConfiguration;

    public void setUp(SenderConfiguration senderConfiguration) throws InterruptedException {
        this.senderConfiguration = senderConfiguration;
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.HTTP_SERVER_PORT);
        listenerConfiguration.setScheme(Constants.HTTP_SCHEME);
        listenerConfiguration.setVersion(Constants.HTTP_2_0);
        HttpWsConnectorFactory connectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = connectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();
        HttpWsConnectorFactory httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        clientConnector = httpWsConnectorFactory.createHttpClientConnector(new HashMap<>(), senderConfiguration);
    }

    public HttpCarbonMessage send(HttpHeaders headers) {
        try {
            String testValue = "Test Message";
            HttpCarbonMessage msg = TestUtil.createHttpPostReq(TestUtil.HTTP_SERVER_PORT, testValue, "");
            msg.setHeaders(headers);

            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = clientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(30, TimeUnit.SECONDS);

            return listener.getHttpResponseMessage();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running Test", e);
        }
        return null;
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        serverConnector.stop();
    }
}
