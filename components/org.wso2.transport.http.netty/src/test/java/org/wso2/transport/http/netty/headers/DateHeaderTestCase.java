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

package org.wso2.transport.http.netty.headers;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Options;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contentaware.listeners.EchoStreamingMessageListener;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;

/**
 * Test case for ensuring that the Date header is correctly set.
 */
public class DateHeaderTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(DateHeaderTestCase.class);

    private ServerConnector serverConnector;
    private HttpWsConnectorFactory httpWsConnectorFactory;

    @BeforeClass
    public void setup() throws InterruptedException {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        ServerBootstrapConfiguration serverBootstrapConfig = new ServerBootstrapConfiguration(new HashMap<>());

        httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = httpWsConnectorFactory.createServerConnector(serverBootstrapConfig, listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new EchoStreamingMessageListener());

        serverConnectorFuture.sync();
    }

    @Test
    public void testDateHeaderFormatAndExistence() {
        try {
            URI baseURI = URI.create(String.format("http://%s:%d", "localhost", TestUtil.SERVER_CONNECTOR_PORT));
            HttpResponse<String> response = Unirest.post(baseURI.resolve("/").toString())
                    .header(CONNECTION, CLOSE.toString()).body(TestUtil.smallEntity).asString();

            String date = response.getHeaders().getFirst(HttpHeaderNames.DATE.toString());
            Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
            Assert.assertNotNull(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date));
        } catch (UnirestException e) {
            TestUtil.handleException("Exception occurred while running postTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        serverConnector.stop();
        try {
            Unirest.shutdown();
            Options.refresh();
            httpWsConnectorFactory.shutdown();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for HttpWsFactory to shutdown", e);
        } catch (IOException e) {
            LOG.warn("IOException occurred while waiting for Unirest connection to shutdown", e);
        }
    }
}
