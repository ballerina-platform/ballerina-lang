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

package org.wso2.transport.http.netty.https;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpsServer;
import org.wso2.transport.http.netty.util.server.initializers.MockServerInitializer;

import java.util.HashMap;

import static org.wso2.transport.http.netty.contract.Constants.HTTPS_SCHEME;
import static org.wso2.transport.http.netty.contract.Constants.TEXT_PLAIN;

/**
 * Tests for HTTPS client connector
 */
public class HttpSClientTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSClientTestCase.class);

    private HttpsServer httpsServer;
    private HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory connectorFactory;
    private String testValue = "Test Message";

    @BeforeClass
    public void setup() {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.TRUST_STORE_FILE_PATH));
        senderConfiguration.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        senderConfiguration.setScheme(HTTPS_SCHEME);

        httpsServer = TestUtil.startHttpsServer(TestUtil.HTTPS_SERVER_PORT,
                new MockServerInitializer(testValue, TEXT_PLAIN, 200));
        connectorFactory = new DefaultHttpWsConnectorFactory();
        httpClientConnector = connectorFactory.createHttpClientConnector(new HashMap<>(), senderConfiguration);
    }

    @Test
    public void testHttpsGet() {
        TestUtil.testHttpsPost(httpClientConnector, TestUtil.HTTPS_SERVER_PORT);
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            httpsServer.shutdown();
            connectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.error("Failed to shutdown the test server");
        }
    }
}
