/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.netty.proxyserver;

import org.ballerinalang.net.netty.contentaware.listeners.EchoMessageListener;
import org.ballerinalang.net.netty.contract.HttpClientConnector;
import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contract.HttpWsConnectorFactory;
import org.ballerinalang.net.netty.contract.ServerConnector;
import org.ballerinalang.net.netty.contract.ServerConnectorFuture;
import org.ballerinalang.net.netty.contract.config.ListenerConfiguration;
import org.ballerinalang.net.netty.contract.config.ProxyServerConfiguration;
import org.ballerinalang.net.netty.contract.config.SenderConfiguration;
import org.ballerinalang.net.netty.contract.config.TransportsConfiguration;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.ballerinalang.net.netty.message.HttpConnectorUtil;
import org.ballerinalang.net.netty.message.HttpMessageDataStreamer;
import org.ballerinalang.net.netty.util.DefaultHttpConnectorListener;
import org.ballerinalang.net.netty.util.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.ballerinalang.net.netty.contract.Constants.HTTPS_SCHEME;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * A util class to use in both http and https proxy scenarios.
 */
public class ProxyServerUtil {
    private static HttpClientConnector httpClientConnector;
    private static ServerConnector serverConnector;
    private static HttpWsConnectorFactory httpWsConnectorFactory;
    private static final Logger LOG = LoggerFactory.getLogger(ProxyServerUtil.class);
    private static SenderConfiguration senderConfiguration;

    protected static void sendRequest(HttpCarbonMessage msg, String testValue) {

        try {
            CountDownLatch latch = new CountDownLatch(1);
            DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(5, TimeUnit.SECONDS);

            HttpCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            assertEquals(testValue, result);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running testProxyServer", e);
        }
    }

    static void setUpClientAndServerConnectors(ListenerConfiguration listenerConfiguration, String scheme)
            throws InterruptedException {

        ProxyServerConfiguration proxyServerConfiguration = null;
        try {
            proxyServerConfiguration = new ProxyServerConfiguration("localhost", TestUtil.SERVER_PORT2);
        } catch (UnknownHostException e) {
            TestUtil.handleException("Failed to resolve host", e);
        }

        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        Set<SenderConfiguration> senderConfig = transportsConfiguration.getSenderConfigurations();
        ProxyServerConfiguration finalProxyServerConfiguration = proxyServerConfiguration;
        setSenderConfigs(senderConfig, finalProxyServerConfiguration, scheme);
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();

        serverConnector = httpWsConnectorFactory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        httpClientConnector = httpWsConnectorFactory.createHttpClientConnector(new HashMap<>(), HttpConnectorUtil
                .getSenderConfiguration(transportsConfiguration, scheme));
    }

    static void shutDown() {
        httpClientConnector.close();
        serverConnector.stop();
        try {
            httpWsConnectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to close");
        }
    }

    private static void setSenderConfigs(Set<SenderConfiguration> senderConfig,
                                         ProxyServerConfiguration finalProxyServerConfiguration, String scheme) {
        senderConfig.forEach(config -> {
            if (scheme.equals(HTTPS_SCHEME)) {
                config.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
                config.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
                config.setScheme(HTTPS_SCHEME);
            }
            config.setProxyServerConfiguration(finalProxyServerConfiguration);
        });
    }
}

