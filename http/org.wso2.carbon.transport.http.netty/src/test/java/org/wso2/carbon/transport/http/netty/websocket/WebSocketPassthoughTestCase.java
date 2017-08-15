/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contractimpl.HTTPConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.util.client.websocket.WebSocketClient;
import org.wso2.carbon.transport.http.netty.util.server.websocket.WebSocketRemoteServer;

import java.net.URISyntaxException;
import javax.net.ssl.SSLException;

/**
 * Test cases for WebSocket pass-through scenarios.
 */
public class WebSocketPassthoughTestCase extends WebSocketTestCase {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientTestCase.class);

    private HTTPConnectorFactoryImpl httpConnectorFactory = new HTTPConnectorFactoryImpl();
    private WebSocketRemoteServer remoteServer = new WebSocketRemoteServer(8490);

    private WebSocketClient webSocketClient = new WebSocketClient();
    private ServerConnector serverConnector;

    @BeforeClass
    public void setup() throws InterruptedException, ClientConnectorException {
        log.info(System.lineSeparator() +
                         "--------------------- WebSocket Pass Through Test Cases ---------------------");
        remoteServer.run();

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost("localhost");
        listenerConfiguration.setPort(9009);
        serverConnector = httpConnectorFactory.getServerConnector(ServerBootstrapConfiguration.getInstance(),
                                                                  listenerConfiguration);
        serverConnector.start().setWSConnectorListener(new WebSocketPassthroughServerConnectorListener());
    }

    @Test
    public void testTextPassthrough() throws InterruptedException, SSLException, URISyntaxException {
        webSocketClient.handhshake();
        String text = "hello-pass-through";
        webSocketClient.sendText(text);
        assertWebSocketClientTextMessage(webSocketClient, text);
        Assert.assertEquals(webSocketClient.getTextReceived(), text);
    }

    @AfterClass
    public void cleaUp() throws ServerConnectorException, InterruptedException {
        serverConnector.stop();
        remoteServer.stop();
    }
}
