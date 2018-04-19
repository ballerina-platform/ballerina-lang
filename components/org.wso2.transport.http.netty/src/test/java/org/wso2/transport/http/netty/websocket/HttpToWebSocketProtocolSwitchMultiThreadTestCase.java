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
 *
 */

package org.wso2.transport.http.netty.websocket;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Test cases to check the Protocol switch from HTTP to WebSocket.
 */
public class HttpToWebSocketProtocolSwitchMultiThreadTestCase {

    private DefaultHttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", TestUtil.SERVER_CONNECTOR_PORT));
    private ServerConnector serverConnector;

    @BeforeClass
    public void setup() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost("localhost");
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        serverConnector = httpConnectorFactory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(),
                                                                     listenerConfiguration);
        ServerConnectorFuture connectorFuture = serverConnector.start();
        connectorFuture.setWSConnectorListener(new HttpToWsProtocolSwitchWebSocketMulthreadListener());
        connectorFuture.setHttpConnectorListener(new HttpToWsProtocolSwitchHttpListener());
        connectorFuture.sync();
    }

    @Test
    public void testWebSocketGetUpgrade() throws IOException, InterruptedException {
        URL url = baseURI.resolve("/websocket").toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("GET");
        urlConn.setRequestProperty("connection", "upgrade");
        urlConn.setRequestProperty("upgrade", "websocket");
        urlConn.setRequestProperty("Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ==");
        urlConn.setRequestProperty("Sec-WebSocket-Version", "13");


        System.out.println("Sent message");
        Assert.assertEquals(urlConn.getResponseCode(), 101);
        Assert.assertEquals(urlConn.getResponseMessage(), "Switching Protocols");
        Assert.assertEquals(urlConn.getHeaderField("upgrade"), "websocket");
        Assert.assertEquals(urlConn.getHeaderField("connection"), "upgrade");
        Assert.assertTrue(urlConn.getHeaderField("sec-websocket-accept") != null);
        urlConn.disconnect();
        System.out.println("Done test");
    }

    @AfterClass
    public void cleaUp() throws ServerConnectorException, InterruptedException {
        serverConnector.stop();
    }

}
