/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.util;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.ForwardedExtensionConfig;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.Parameter;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.wso2.transport.http.netty.contract.Constants.HTTPS_SCHEME;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_2_0;
import static org.wso2.transport.http.netty.contract.Constants.OPTIONAL;

/**
 * Utilities required for HTTP/2 test cases.
 *
 * @since 6.0.273
 */
public class Http2Util {

    public static final String HTTP2_RESPONSE_PAYLOAD = "Final Response";

    public static ListenerConfiguration getH2ListenerConfigs() {
        Parameter paramServerCiphers = new Parameter("ciphers", "TLS_RSA_WITH_AES_128_CBC_SHA");
        List<Parameter> serverParams = new ArrayList<>(1);
        serverParams.add(paramServerCiphers);
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setParameters(serverParams);
        listenerConfiguration.setPort(TestUtil.SERVER_PORT1);
        listenerConfiguration.setScheme(HTTPS_SCHEME);
        listenerConfiguration.setVersion(HTTP_2_0);
        listenerConfiguration.setVerifyClient(OPTIONAL);
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        listenerConfiguration.setKeyStorePass(TestUtil.KEY_STORE_PASSWORD);
        return listenerConfiguration;
    }

    public static SenderConfiguration getSenderConfigs(String httpVersion) {
        Parameter paramClientCiphers = new Parameter("ciphers", "TLS_RSA_WITH_AES_128_CBC_SHA");
        List<Parameter> clientParams = new ArrayList<>(1);
        clientParams.add(paramClientCiphers);
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setParameters(clientParams);
        senderConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        senderConfiguration.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        senderConfiguration.setHttpVersion(httpVersion);
        senderConfiguration.setScheme(HTTPS_SCHEME);
        return senderConfiguration;
    }

    public static SenderConfiguration getForwardSenderConfigs(ForwardedExtensionConfig extensionConfig,
            boolean forceHttp) {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setHttpVersion("2.0");
        senderConfiguration.setForceHttp2(forceHttp);
        senderConfiguration.setForwardedExtensionConfig(extensionConfig);
        return senderConfiguration;
    }

    /**
     * Get the test client. Each test client has their own connection manager and does not use source pools.
     *
     * @param withPriorKnowledge a boolean indicating whether the prior knowledge support is expected
     * @return HttpClientConnector
     */
    public static HttpClientConnector getTestHttp2Client(HttpWsConnectorFactory httpWsConnectorFactory,
                                                         boolean withPriorKnowledge) {
        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        SenderConfiguration senderConfiguration = HttpConnectorUtil.getSenderConfiguration(transportsConfiguration,
                                                                                           Constants.HTTP_SCHEME);
        senderConfiguration.setHttpVersion(HTTP_2_0);
        if (withPriorKnowledge) {
            senderConfiguration.setForceHttp2(true);       // Force to use HTTP/2 without an upgrade
        }
        return httpWsConnectorFactory.createHttpClientConnector(
            HttpConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration);
    }

    public static HttpClientConnector getTestHttp1Client(HttpWsConnectorFactory httpWsConnectorFactory) {
        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        SenderConfiguration h1SenderConfiguration = HttpConnectorUtil.getSenderConfiguration(transportsConfiguration,
                                                                                             Constants.HTTP_SCHEME);
        h1SenderConfiguration.setHttpVersion(String.valueOf(Constants.HTTP_1_1));
        return httpWsConnectorFactory.createHttpClientConnector(
                HttpConnectorUtil.getTransportProperties(transportsConfiguration), h1SenderConfiguration);
    }

    public static void assertResult(String response1, String response2, String response3, String response4) {
        assertNotEquals(response1, response2,
                        "Client uses two different pools, hence response 1 and 2 should not be equal");
        assertNotEquals(response3, response4,
                        "Client uses two different pools, hence response 3 and 4 should not be equal");
        assertEquals(response1, response3, "Client uses the same pool, hence response 1 and 3 should be equal");
        assertEquals(response2, response4, "Client uses the same pool, hence response 2 and 4 should be equal");
    }

    public static HttpClientConnector getHttp2Client(HttpWsConnectorFactory connectorFactory, boolean priorOn,
                                               int socketIdleTimeout) {
        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        SenderConfiguration senderConfiguration = HttpConnectorUtil.getSenderConfiguration(transportsConfiguration,
                                                                                           Constants.HTTP_SCHEME);
        senderConfiguration.setSocketIdleTimeout(socketIdleTimeout);
        senderConfiguration.setHttpVersion(Constants.HTTP_2_0);
        if (priorOn) {
            senderConfiguration.setForceHttp2(true);
        }
        return connectorFactory.createHttpClientConnector(
                HttpConnectorUtil.getTransportProperties(transportsConfiguration), senderConfiguration);
    }

    public static Http2Headers http1HeadersToHttp2Headers(FullHttpRequest request) {
        CharSequence host = request.headers().get(HttpHeaderNames.HOST);
        Http2Headers http2Headers = new DefaultHttp2Headers()
                .method(HttpMethod.GET.asciiName())
                .path(request.uri())
                .scheme(HttpScheme.HTTP.name());
        if (host != null) {
            http2Headers.authority(host);
        }
        return http2Headers;
    }

    public static ListenerConfiguration getH2CListenerConfiguration() {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.HTTP_SERVER_PORT);
        listenerConfiguration.setScheme(Constants.HTTP_SCHEME);
        listenerConfiguration.setVersion(Constants.HTTP_2_0);
        return listenerConfiguration;
    }

    public static SenderConfiguration getH2CSenderConfiguration() {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setScheme(Constants.HTTP_SCHEME);
        senderConfiguration.setHttpVersion(Constants.HTTP_2_0);
        senderConfiguration.setForceHttp2(true);
        return senderConfiguration;
    }
}
