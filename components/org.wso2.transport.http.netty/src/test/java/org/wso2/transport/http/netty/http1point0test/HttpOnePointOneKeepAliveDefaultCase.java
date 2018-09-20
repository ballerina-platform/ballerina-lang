/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.http1point0test;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contentaware.listeners.EchoStreamingMessageListener;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.http.HttpClient;

import java.util.HashMap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * This class tests for http 1.1 requests when KeepAliveConfig is set to default.
 */
public class HttpOnePointOneKeepAliveDefaultCase {

    private static final Logger LOG = LoggerFactory.getLogger(HttpOnePointOneKeepAliveDefaultCase.class);

    protected ServerConnector serverConnector;
    protected ListenerConfiguration listenerConfiguration;
    protected HttpWsConnectorFactory httpWsConnectorFactory;

    HttpOnePointOneKeepAliveDefaultCase() {
        this.listenerConfiguration = new ListenerConfiguration();
    }

    @BeforeClass
    private void setUp() {
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        listenerConfiguration.setServerHeader(TestUtil.TEST_SERVER);

        ServerBootstrapConfiguration serverBootstrapConfig = new ServerBootstrapConfiguration(new HashMap<>());
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();

        serverConnector = httpWsConnectorFactory.createServerConnector(serverBootstrapConfig, listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new EchoStreamingMessageListener());
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
            LOG.error("Thread Interrupted while sleeping ", e);
        }
    }

    @Test
    public void http1point1DefaultRequest() {
        try {
            HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);

            FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.POST, "/", Unpooled.wrappedBuffer(TestUtil.largeEntity.getBytes()));
            FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);

            assertFalse(httpClient.waitForChannelClose());
            assertEquals(TestUtil.largeEntity, TestUtil.getEntityBodyFrom(httpResponse));
            assertNotNull(httpResponse.headers().get(HttpHeaderNames.CONTENT_LENGTH));
        } catch (Exception e) {
            TestUtil.handleException("IOException occurred while running http1point1DefaultRequest test", e);
        }
    }

    @AfterClass
    public void cleanup() throws InterruptedException {
        serverConnector.stop();
        httpWsConnectorFactory.shutdown();
    }
}
