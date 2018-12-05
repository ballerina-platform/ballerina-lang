/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.transport.http.netty.method.head;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;

import java.net.URI;
import java.util.HashMap;

/**
 * This tests head request handling of the HTTP server.
 */
public class HeadRequestTestCase {

    private HttpWsConnectorFactory httpWsConnectorFactory;
    private ServerConnector serverConnector;
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", TestUtil.SERVER_CONNECTOR_PORT));

    @BeforeClass
    public void setup() throws InterruptedException {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        ServerBootstrapConfiguration serverBootstrapConfig = new ServerBootstrapConfiguration(new HashMap<>());
        serverConnector = httpWsConnectorFactory.createServerConnector(serverBootstrapConfig, listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new HeadRequestMessageProcessorListener());
        serverConnectorFuture.sync();
    }

    @Test
    public void testHeadRequestForSmallPayload() throws UnirestException {
        HttpResponse<String> response = Unirest.head(baseURI.resolve("/").toString()).asString();
        assertBasicResponse(response);
        Assert.assertEquals(response.getHeaders().getFirst("content-length"),
                String.valueOf(TestUtil.smallEntity.length()));
    }

    @Test(description = "By x-large-payload custom header, client ask server to respond with a large payload")
    public void testHeadRequestForLargePayload() throws UnirestException {
        HttpResponse<String> response = Unirest.head(baseURI.resolve("/").toString())
                .header("x-large-payload", "true").asString();
        assertBasicResponse(response);
        Assert.assertEquals(response.getHeaders().getFirst("transfer-encoding"), "chunked");
    }

    private void assertBasicResponse(HttpResponse<String> response) {
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(response.getHeaders().getFirst("content-type"), "text/plain");
        Assert.assertNull(response.getBody());
    }

    @AfterClass
    public void cleanup() throws InterruptedException {
        serverConnector.stop();
        httpWsConnectorFactory.shutdown();
    }
}
