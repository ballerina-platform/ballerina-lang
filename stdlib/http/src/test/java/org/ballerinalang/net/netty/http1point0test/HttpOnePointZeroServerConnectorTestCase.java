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

package org.ballerinalang.net.netty.http1point0test;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.ballerinalang.net.netty.contentaware.listeners.EchoStreamingMessageListener;
import org.ballerinalang.net.netty.contract.Constants;
import org.ballerinalang.net.netty.contract.HttpWsConnectorFactory;
import org.ballerinalang.net.netty.contract.ServerConnector;
import org.ballerinalang.net.netty.contract.ServerConnectorFuture;
import org.ballerinalang.net.netty.contract.config.ListenerConfiguration;
import org.ballerinalang.net.netty.contract.config.ServerBootstrapConfiguration;
import org.ballerinalang.net.netty.contract.exceptions.ServerConnectorException;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.ballerinalang.net.netty.util.TestUtil;
import org.ballerinalang.net.netty.util.client.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * This class tests for http 1.0 requests.
 */
public class HttpOnePointZeroServerConnectorTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(HttpOnePointZeroServerConnectorTestCase.class);

    protected ServerConnector serverConnector;
    protected ListenerConfiguration listenerConfiguration;
    protected HttpWsConnectorFactory httpWsConnectorFactory;

    HttpOnePointZeroServerConnectorTestCase() {
        this.listenerConfiguration = new ListenerConfiguration();
    }

    @BeforeClass
    public void setUp() {
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
    public void http1point0DefaultRequest() {
        try {
            HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);

            FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0,
                    HttpMethod.POST, "/", Unpooled.wrappedBuffer(TestUtil.largeEntity.getBytes()));
            FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);

            assertTrue(httpClient.waitForChannelClose());
            assertEquals(TestUtil.largeEntity, TestUtil.getEntityBodyFrom(httpResponse));
            assertNotNull(httpResponse.headers().get(HttpHeaderNames.CONTENT_LENGTH));
        } catch (Exception e) {
            TestUtil.handleException("IOException occurred while running largeHeaderTest", e);
        }
    }

    @Test
    public void http1point0KeepAliveRequest() {
        try {
            HttpClient httpClient = new HttpClient(TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT);

            FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0,
                    HttpMethod.POST, "/", Unpooled.wrappedBuffer(TestUtil.largeEntity.getBytes()));
            httpRequest.headers().set(HttpHeaderNames.CONNECTION, Constants.CONNECTION_KEEP_ALIVE);
            FullHttpResponse httpResponse = httpClient.sendRequest(httpRequest);

            assertFalse(httpClient.waitForChannelClose());
            assertEquals(TestUtil.largeEntity, TestUtil.getEntityBodyFrom(httpResponse));
            assertEquals(Constants.CONNECTION_KEEP_ALIVE, httpResponse.headers().get(HttpHeaderNames.CONNECTION));
            assertNotNull(httpResponse.headers().get(HttpHeaderNames.CONTENT_LENGTH));
        } catch (Exception e) {
            TestUtil.handleException("IOException occurred while running largeHeaderTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        serverConnector.stop();
        try {
            httpWsConnectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to close");
        }
    }
}
