/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.httppipelining;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test HTTP pipelining properties.
 *
 * @since 6.0.228
 */
public class HttpPipeliningTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(HttpPipeliningTestCase.class);
    private final DefaultHttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();

    @BeforeClass
    public void setUp() {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        listenerConfiguration.setPipeliningEnabled(true);
        ServerConnector serverConnector = httpConnectorFactory.createServerConnector(
                new ServerBootstrapConfiguration(new HashMap<>()), listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new HttpPipeliningListener());
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
            LOG.error("Thread Interrupted while sleeping ", e);
        }
    }

    @Test(description = "Test whether the server behaves normally when pipelining is enabled")
    public void testRespponse() {
        String testValue = "Test Message";
        try {
            URI baseURI = URI.create(String.format("http://%s:%d", "localhost", TestUtil.SERVER_CONNECTOR_PORT));
            HttpResponse<String> response = Unirest.post(baseURI.resolve("/").toString()).body(testValue).asString();
            assertEquals(200, response.getStatus());
            assertEquals(testValue, response.getBody());
            assertEquals("Sequence number of the response should be 1", "1",
                    response.getHeaders().get("x-sequence-number").get(0));
        } catch (UnirestException e) {
            TestUtil.handleException("IOException occurred while running HttpPipeliningTestCase", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        try {
            Unirest.shutdown();
            Options.refresh();
            httpConnectorFactory.shutdown();
        } catch (IOException e) {
            LOG.warn("IOException occurred while waiting for Unirest connection to shutdown", e);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to shutdown", e);
        }
    }
}
