/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.Parameter;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static org.wso2.transport.http.netty.contract.Constants.HTTPS_SCHEME;

/**
 * Tests for different cipher suites provided by client and server with certs and keys.
 */
public class CipherSuiteswithCertsTest {
    private static HttpClientConnector httpClientConnector;
    private List<Parameter> clientParams = new ArrayList<>(1);
    private ServerConnector serverConnector;
    private HttpWsConnectorFactory factory;
    private static final Logger LOG = LoggerFactory.getLogger(CipherSuitesTest.class);

    @DataProvider(name = "ciphers")
    public static Object[][] cipherSuites() {
        return new Object[][] {
                // true = expecting a SSL hand shake failure.
                // false = expecting no errors.
                { "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", false,
                        TestUtil.SERVER_CONNECTOR_PORT },
                { "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", true, TestUtil.HTTPS_SERVER_PORT } };
    }

    /**
     * Set up the client and the server
     * @param clientCiphers ciphers given by client
     * @param serverCiphers ciphers supported by server
     * @param hasException expecting an exception true/false
     * @param serverPort port
     */
    @Test(dataProvider = "ciphers")
    public void setup(String clientCiphers, String serverCiphers, boolean hasException, int serverPort)
            throws InterruptedException {

        Parameter paramClientCiphers = new Parameter("ciphers", clientCiphers);
        clientParams.add(paramClientCiphers);
        clientParams.add(new Parameter("shareSession", "true"));

        Parameter paramServerCiphers = new Parameter("ciphers", serverCiphers);
        List<Parameter> serverParams = new ArrayList<>(1);
        serverParams.add(paramServerCiphers);
        serverParams.add(new Parameter("shareSession", "true"));

        factory = new DefaultHttpWsConnectorFactory();
        serverConnector = factory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(),
                getListenerConfiguration(serverPort, serverParams));
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        httpClientConnector = factory.createHttpClientConnector(new HashMap<>(), getSenderConfigs());

        testCiphersuitesWithCertsAndKeys(hasException, serverPort);
        serverConnector.stop();
    }

    private void testCiphersuitesWithCertsAndKeys(boolean hasException, int serverPort) {
        try {
            String testValue = "successful";
            HttpCarbonMessage msg = TestUtil.createHttpsPostReq(serverPort, testValue, "");

            CountDownLatch latch = new CountDownLatch(1);
            SSLConnectorListener listener = new SSLConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(5, TimeUnit.SECONDS);
            HttpCarbonMessage response = listener.getHttpResponseMessage();

            if (hasException) {
                assertNotNull(listener.getThrowables());
                boolean hasSSLException = false;
                for (Throwable throwable : listener.getThrowables()) {
                    if (throwable.getMessage() != null && throwable.getMessage().contains("handshake_failure")) {
                        hasSSLException = true;
                        break;
                    }
                }
                assertTrue(hasSSLException);
            } else {
                assertNotNull(response);
                String result = new BufferedReader(
                        new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream())).lines()
                        .collect(Collectors.joining("\n"));
                assertEquals(testValue, result);
            }
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running testCiphersuites", e);
        }
    }

    private ListenerConfiguration getListenerConfiguration(int serverPort, List<Parameter> serverParams) {
        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        listenerConfiguration.setPort(serverPort);
        String verifyClient = "require";
        listenerConfiguration.setVerifyClient(verifyClient);
        listenerConfiguration.setServerKeyFile(TestUtil.getAbsolutePath(TestUtil.KEY_FILE));
        listenerConfiguration.setServerCertificates(TestUtil.getAbsolutePath(TestUtil.CERT_FILE));
        listenerConfiguration.setServerTrustCertificates(TestUtil.getAbsolutePath(TestUtil.TRUST_CERT_CHAIN));
        listenerConfiguration.setScheme(HTTPS_SCHEME);
        listenerConfiguration.setParameters(serverParams);
        return listenerConfiguration;
    }

    private SenderConfiguration getSenderConfigs() {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setClientKeyFile(TestUtil.getAbsolutePath(TestUtil.KEY_FILE));
        senderConfiguration.setClientCertificates(TestUtil.getAbsolutePath(TestUtil.CERT_FILE));
        senderConfiguration.setClientTrustCertificates(TestUtil.getAbsolutePath(TestUtil.TRUST_CERT_CHAIN));
        senderConfiguration.setScheme(HTTPS_SCHEME);
        senderConfiguration.setHostNameVerificationEnabled(false);
        senderConfiguration.setParameters(clientParams);
        return senderConfiguration;
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            serverConnector.stop();
            httpClientConnector.close();
            factory.shutdown();
        } catch (Exception e) {
            LOG.warn("Interrupted while waiting for response", e);
        }
    }
}

