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

package org.ballerinalang.net.netty.https;

import org.ballerinalang.net.netty.contentaware.listeners.EchoMessageListener;
import org.ballerinalang.net.netty.contract.HttpClientConnector;
import org.ballerinalang.net.netty.contract.HttpWsConnectorFactory;
import org.ballerinalang.net.netty.contract.ServerConnector;
import org.ballerinalang.net.netty.contract.ServerConnectorFuture;
import org.ballerinalang.net.netty.contract.config.ListenerConfiguration;
import org.ballerinalang.net.netty.contract.config.SenderConfiguration;
import org.ballerinalang.net.netty.contract.exceptions.ServerConnectorException;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.ballerinalang.net.netty.util.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.ballerinalang.net.netty.contract.Constants.HTTPS_SCHEME;

/**
 * Tests for mutual ssl.
 */
public class MutualSSLTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(MutualSSLTestCase.class);

    private static HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory factory;
    private ServerConnector connector;

    @BeforeClass
    public void setup() throws InterruptedException {

        factory = new DefaultHttpWsConnectorFactory();

        ListenerConfiguration listenerConfiguration = getListenerConfiguration();

        connector = factory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = connector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        httpClientConnector = factory.createHttpClientConnector(new HashMap<>(), getSenderConfigs());
    }

    private ListenerConfiguration getListenerConfiguration() {
        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        listenerConfiguration.setPort(TestUtil.SERVER_PORT3);
        String verifyClient = "require";
        listenerConfiguration.setVerifyClient(verifyClient);
        listenerConfiguration.setSslSessionTimeOut(TestUtil.SSL_SESSION_TIMEOUT);
        listenerConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.TRUST_STORE_FILE_PATH));
        listenerConfiguration.setSslHandshakeTimeOut(TestUtil.SSL_HANDSHAKE_TIMEOUT);
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        listenerConfiguration.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        listenerConfiguration.setKeyStorePass(TestUtil.KEY_STORE_PASSWORD);
        listenerConfiguration.setScheme(HTTPS_SCHEME);
        return listenerConfiguration;
    }

    private SenderConfiguration getSenderConfigs() {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        senderConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.TRUST_STORE_FILE_PATH));
        senderConfiguration.setKeyStorePass(TestUtil.KEY_STORE_PASSWORD);
        senderConfiguration.setSslSessionTimeOut(TestUtil.SSL_SESSION_TIMEOUT);
        senderConfiguration.setSslHandshakeTimeOut(TestUtil.SSL_HANDSHAKE_TIMEOUT);
        senderConfiguration.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        senderConfiguration.setScheme(HTTPS_SCHEME);
        return senderConfiguration;
    }

    @Test
    public void testHttpsPost() {
        TestUtil.testHttpsPost(httpClientConnector, TestUtil.SERVER_PORT3);
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        connector.stop();
        httpClientConnector.close();
        try {
            factory.shutdown();
        } catch (Exception e) {
            LOG.warn("Interrupted while waiting for response two", e);
        }
    }
}

