/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.passthrough;

import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.MockServerInitializer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * A test class for passthrough transport.
 */
public class PassThroughHttpTestCase {

    private List<ServerConnector> serverConnectors;
    private static final String testValue = "Test Message";
    private HttpServer httpServer;

    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", TestUtil.SERVER_CONNECTOR_PORT));

    @BeforeClass
    public void setUp() {
        TransportsConfiguration configuration = YAMLTransportConfigurationBuilder
                .build(TestUtil.getAbsolutePath("/simple-test-config/netty-transports.yml"));
        serverConnectors = TestUtil.startConnectors(
                configuration, new PassthroughMessageProcessorListener(new SenderConfiguration()));
        httpServer = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT,
                new MockServerInitializer(testValue, Constants.TEXT_PLAIN, 200));
    }

    @Test
    public void passthroughGetTest() {
        try {
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.GET.name(), true);
            String content = TestUtil.getContent(urlConn);
            assertEquals(testValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running passthroughGetTest", e);
        }
    }

    @Test
    public void passthroughPostTest() {
        try {
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            String content = TestUtil.getContent(urlConn);
            assertEquals(testValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running passthroughPostTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        TestUtil.cleanUp(serverConnectors, httpServer);
    }
}
