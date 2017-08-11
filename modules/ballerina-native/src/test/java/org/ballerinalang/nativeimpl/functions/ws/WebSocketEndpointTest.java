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
 */

package org.ballerinalang.nativeimpl.functions.ws;

import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.MessageUtils;
import org.ballerinalang.testutils.Services;
import org.ballerinalang.testutils.ws.MockWebSocketSession;
import org.ballerinalang.util.codegen.ProgramFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Test connected client scenarios. When the client connects, send  and receive text and closing the connection.
 */
public class WebSocketEndpointTest {

    private ProgramFile application;
    private final String uri = "/test/websocket";

    // Client properties
    MockWebSocketSession session1 = new MockWebSocketSession("session1");
    MockWebSocketSession session2 = new MockWebSocketSession("session2");
    MockWebSocketSession session3 = new MockWebSocketSession("session2");

    @BeforeClass
    public void setup() throws InterruptedException {
        application = EnvironmentInitializer.setupProgramFile("samples/websocket/endpointTest.bal");
    }

    @Test(description = "Test the client connection establishment and broadcast.",
          priority = 0)
    public void testClientConnected() {
        String expectedText = "new client connected";
        CarbonMessage client1Message = MessageUtils.generateWebSocketOnOpenMessage(session1, uri);
        CarbonMessage client2Message = MessageUtils.generateWebSocketOnOpenMessage(session2, uri);

        Services.invoke(client1Message);
        Services.invoke(client2Message);

        Assert.assertEquals(session1.getTextReceived(), expectedText);
    }

    @Test(description = "Test the sending and receiving of client messages.",
          priority = 1)
    public void testPushText() {
        String expectedText = "Hi, Test";
        CarbonMessage client1Message = MessageUtils.generateWebSocketTextMessage(expectedText, session1, uri);
        Services.invoke(client1Message);
        Assert.assertEquals(session1.getTextReceived(), expectedText);
    }

    @Test(description = "Test closing a connection and broadcast.",
          priority = 2)
    public void textConnectionClose() {
        String expectedText = "client left";
        CarbonMessage client1Message = MessageUtils.generateWebSocketOnCloseMessage(session1, uri);
        Services.invoke(client1Message);
        Assert.assertEquals(session2.getTextReceived(), expectedText);
    }

    @Test(description = "Test the connection closure by server",
          priority = 4)
    public void testConnectionClosureByServer() {
        CarbonMessage client3Message = MessageUtils.generateWebSocketOnOpenMessage(session2, uri);
        Services.invoke(client3Message);

        //Check pre conditions
        Assert.assertTrue(session3.isOpen());

        String sentText = "closeMe";
        client3Message = MessageUtils.generateWebSocketTextMessage(sentText, session3, uri);
        Services.invoke(client3Message);

        // Check post conditions
        Assert.assertFalse(session3.isOpen());

    }

    @AfterClass
    public void cleanUp() {
        EnvironmentInitializer.cleanup(application);
    }
}
