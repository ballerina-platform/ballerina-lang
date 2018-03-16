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
package org.wso2.transport.http.netty.hostnameverfication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
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
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.https.SSLConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * A test for hostname verification. Contains two test scenarios to test certificates with CN included and not included.
 * CN included test should pass and the other should fail.
 */
public class HostnameVerificationTest {

    private static final Logger log = LoggerFactory.getLogger(HostnameVerificationTest.class);

    private HttpClientConnector httpClientConnector;
    private ServerConnector serverConnector;
    private HttpWsConnectorFactory factory;
    private String tlsStoreType = "PKCS12";

    @DataProvider(name = "configurations")
    private Object[][] configurations() {
        // true = expecting a SSL hand shake failure.
        // false = expecting no errors.
        String keyStoreFilePath = "/simple-test-config/localcrt.p12";
        String keyStorePassword = "localpwd";
        String trustStoreFilePath = "/simple-test-config/cacerts.p12";
        String trustStorePassword = "cacertspassword";
        String keyStoreFileWithCN = "/simple-test-config/wso2carbon.p12";
        String trustStoreFileWithCN = "/simple-test-config/client-truststore.p12";
        String password = "ballerina";
        return new Object[][] {
                { keyStoreFilePath, keyStorePassword, trustStoreFilePath, trustStorePassword, true, 9098 },
                { keyStoreFileWithCN, password, trustStoreFileWithCN, password, false, 9099 }
        };
    }

    @Test(dataProvider = "configurations")
    /**
     * Set up the client and the server.
     *
     * @param keyStoreFilePath Keystore file path
     * @param keyStorePassword Keystore password
     * @param trustStoreFilePath Truststore file path
     * @param trustStorePassword Truststore password
     * @param hasException Expecting an error(true/false)
     * @param serverPort port
     */
    public void testHostNameVerification(String keyStoreFilePath, String keyStorePassword, String trustStoreFilePath,
            String trustStorePassword, boolean hasException, int serverPort) throws InterruptedException {

        TransportsConfiguration transportsConfiguration = TestUtil
                .getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");
        Set<SenderConfiguration> senderConfig = transportsConfiguration.getSenderConfigurations();

        senderConfig.forEach(config -> {
            if (config.getId().contains(Constants.HTTPS_SCHEME)) {
                config.setTrustStoreFile(TestUtil.getAbsolutePath(trustStoreFilePath));
                config.setTrustStorePass(trustStorePassword);
                config.setTLSStoreType(tlsStoreType);
            }
        });

        factory = new DefaultHttpWsConnectorFactory();

        serverConnector = factory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(),
                setListenerConfiguration(serverPort, keyStoreFilePath, keyStorePassword));
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        httpClientConnector = factory
                .createHttpClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                        HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTPS_SCHEME));

        sendRequest(hasException, serverPort);
    }

    public void sendRequest(boolean hasException, int serverPort) {
        try {
            String testValue = "Test";
            HTTPCarbonMessage msg = TestUtil.createHttpsPostReq(serverPort, testValue, "");
            CountDownLatch latch = new CountDownLatch(1);
            SSLConnectorListener listener = new SSLConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(5, TimeUnit.SECONDS);
            HTTPCarbonMessage response = listener.getHttpResponseMessage();

            if (hasException) {
                assertNotNull(listener.getThrowables());
                boolean hasSSLException = false;
                for (Throwable throwable : listener.getThrowables()) {
                    if (throwable.getMessage() != null && (throwable.getMessage()
                            .contains("General SSLEngine problem"))) {
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
            TestUtil.handleException("Exception occurred while running testHostNameVerification", e);
        }
    }

    private ListenerConfiguration setListenerConfiguration(int port, String keyStore, String keyStorePassword) {
        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        listenerConfiguration.setPort(port);
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(keyStore));
        listenerConfiguration.setKeyStorePass(keyStorePassword);
        listenerConfiguration.setScheme(Constants.HTTPS_SCHEME);
        listenerConfiguration.setTLSStoreType(tlsStoreType);
        return listenerConfiguration;
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        httpClientConnector.close();
        serverConnector.stop();
        try {
            factory.shutdown();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for HttpWsFactory to shutdown", e);
        }
    }
}

