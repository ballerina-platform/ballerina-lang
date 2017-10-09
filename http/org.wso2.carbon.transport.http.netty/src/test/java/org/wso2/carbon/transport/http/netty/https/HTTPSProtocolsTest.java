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

package org.wso2.carbon.transport.http.netty.https;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.Parameter;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contentaware.EchoMessageListener;
import org.wso2.carbon.transport.http.netty.contract.HttpClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.carbon.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Tests for SSL protocols.
 */
public class HTTPSProtocolsTest {

    private static HttpClientConnector httpClientConnector;
    private static ServerConnector serverConnector;
    private static String testValue = "Test";
    private String keyStoreFile = "src/test/resources/simple-test-config/wso2carbon.jks";
    private String trustStoreFile = "src/test/resources/simple-test-config/client-truststore.jks";
    private String password = "wso2carbon";
    private String scheme = "https";
    private String verifyClient = "require";

    @DataProvider(name = "protocols")

    public static Object[][] cipherSuites() {

        // true = expecting a SSL hand shake failure.
        // false = expecting no errors.
        return new Object[][] { { "TLSv1.1", "TLSv1.1", false, 9009 }, { "TLSv1.2", "TLSv1.1", true, 9008 },
                { "TLSv1.1", "TLSv1.2", true, 9007 } };
    }

    @Test(dataProvider = "protocols")
    /**
     * Set up the client and the server
     * @param clientProtocol SSL enabled protocol of client
     * @param serverProtocol SSL enabled protocol of server
     * @param hasException expecting an exception true/false
     * @param serverPort port
     */
    public void setup(String clientProtocol, String serverProtocol, boolean hasException, int serverPort)
            throws InterruptedException {

        Parameter clientprotocols = new Parameter("sslEnabledProtocols", clientProtocol);
        List<Parameter> clientParams = new ArrayList<>();
        clientParams.add(clientprotocols);

        Parameter serverProtocols = new Parameter("sslEnabledProtocols", serverProtocol);
        List<Parameter> severParams = new ArrayList<>();
        severParams.add(serverProtocols);

        TransportsConfiguration transportsConfiguration = TestUtil
                .getConfiguration("/simple-test-config" + File.separator + "netty-transports.yml");
        Set<SenderConfiguration> senderConfig = transportsConfiguration.getSenderConfigurations();
        senderConfig.forEach(config -> {
            config.setKeyStoreFile(keyStoreFile);
            config.setKeyStorePassword(password);
            config.setParameters(clientParams);
        });

        HttpWsConnectorFactory factory = new HttpWsConnectorFactoryImpl();
        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        listenerConfiguration.setPort(serverPort);
        listenerConfiguration.setVerifyClient(verifyClient);
        listenerConfiguration.setTrustStoreFile(trustStoreFile);
        listenerConfiguration.setKeyStoreFile(keyStoreFile);
        listenerConfiguration.setTrustStorePass(password);
        listenerConfiguration.setKeyStorePass(password);
        listenerConfiguration.setCertPass(password);
        listenerConfiguration.setScheme(scheme);
        listenerConfiguration.setParameters(severParams);
        serverConnector = factory
                .createServerConnector(ServerBootstrapConfiguration.getInstance(), listenerConfiguration);
        ServerConnectorFuture future = serverConnector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();
        HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();
        httpClientConnector = connectorFactory
                .createHttpClientConnector(HTTPConnectorUtil.getTransportProperties(transportsConfiguration),
                        HTTPConnectorUtil.getSenderConfiguration(transportsConfiguration, Constants.HTTPS_SCHEME));
        testHttpsProtocols(hasException, serverPort);
    }

    public void testHttpsProtocols(boolean hasException, int serverPort) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(testValue.getBytes(Charset.forName("UTF-8")));
            HTTPCarbonMessage msg = new HTTPCarbonMessage(
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, ""));
            msg.setProperty("PORT", serverPort);
            msg.setProperty("PROTOCOL", scheme);
            msg.setProperty("HOST", TestUtil.TEST_HOST);
            msg.setProperty("HTTP_METHOD", "GET");
            msg.addMessageBody(byteBuffer);
            msg.setEndOfMsgAdded(true);

            CountDownLatch latch = new CountDownLatch(1);
            SSLConnectorListener listener = new SSLConnectorListener(latch);
            HttpResponseFuture responseFuture = httpClientConnector.send(msg);
            responseFuture.setHttpConnectorListener(listener);

            latch.await(5, TimeUnit.SECONDS);

            HTTPCarbonMessage response = listener.getHttpResponseMessage();
            if (hasException) {
                assertNotNull(listener.getThrowables());
                boolean hasSSLException = false;
                for (Throwable t : listener.getThrowables()) {
                    if (t.getMessage() != null && (t.getMessage().contains("javax.net.ssl.SSLHandshakeException") || t
                            .getMessage().contains("handshake_failure"))) {
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
            TestUtil.handleException("Exception occurred while running testHttpsProtocols", e);
        }
    }
}
