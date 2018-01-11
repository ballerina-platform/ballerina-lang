/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.transport.http.netty.pkcs;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.HTTPConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Test case for testing PKCS12 keystore and truststore.
 */
public class PKCSTest {

    private static HttpClientConnector httpClientConnector;
    private static String testValue = "Test";
    private String keyStoreFile = "/simple-test-config/wso2carbon.p12";
    private String trustStoreFile = "/simple-test-config/client-truststore.p12";
    private String password = "ballerina";
    private String scheme = "https";
    private String tlsStoreType = "PKCS12";
    private static int serverPort = 5431;

    @BeforeClass
    public void setup() throws InterruptedException {

        TransportsConfiguration transportsConfiguration = TestUtil
                .getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");

        Set<SenderConfiguration> senderConfig = transportsConfiguration.getSenderConfigurations();
        //set PKCS12 truststore to ballerina client.
        senderConfig.forEach(config -> {
            if (config.getId().contains(Constants.HTTPS_SCHEME)) {
                config.setTrustStoreFile(TestUtil.getAbsolutePath(trustStoreFile));
                config.setTrustStorePass(password);
                config.setTLSStoreType(tlsStoreType);
            }
        });

        HttpWsConnectorFactory factory = new HttpWsConnectorFactoryImpl();

        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        listenerConfiguration.setPort(serverPort);
        //set PKCS12 keystore to ballerina server.
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(keyStoreFile));
        listenerConfiguration.setKeyStorePass(password);
        listenerConfiguration.setCertPass(password);
        listenerConfiguration.setScheme(scheme);
        listenerConfiguration.setTLSStoreType(tlsStoreType);

        ServerConnector connector = factory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = connector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        httpClientConnector = factory
                .createHttpClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                        HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTPS_SCHEME));
    }

    @Test
    public void testPKCS12() {
        try {
            HTTPCarbonMessage msg = TestUtil.createHttpsPostReq(serverPort, testValue, "");

            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(5, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            assertNotNull(response);
            String result = new BufferedReader(
                    new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            assertEquals(testValue, result);
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running testPKCS12", e);
        }
    }
}
