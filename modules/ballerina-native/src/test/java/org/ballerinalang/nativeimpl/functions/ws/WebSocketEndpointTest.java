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

import javax.websocket.CloseReason;

/**
 * Test connected client scenarios. When the client connects, send  and receive text and closing the connection.
 */
public class WebSocketEndpointTest {

    private ProgramFile application;
    private final String uri = "/test/websocket";

    // Client properties
    MockWebSocketSession session1 = new MockWebSocketSession("session1");
    MockWebSocketSession session2 = new MockWebSocketSession("session2");
    MockWebSocketSession errorSession = new MockWebSocketSession("errorSession");

    @BeforeClass
    public void setup() throws InterruptedException {
        application = EnvironmentInitializer.setupProgramFile("samples/websocket/endpointTest.bal");
    }

    @Test(description = "Test the client connection establishment and broadcast.")
    public void testClientConnected() {
        String expectedText = "new client connected";
        CarbonMessage client1Message = MessageUtils.generateWebSocketOnOpenMessage(session1, uri);
        CarbonMessage client2Message = MessageUtils.generateWebSocketOnOpenMessage(session2, uri);
        CarbonMessage client3Message = MessageUtils.generateWebSocketOnOpenMessage(errorSession, uri + "/error");

        Services.invoke(client1Message);
        Services.invoke(client2Message);
        Services.invoke(client3Message);

        Assert.assertEquals(session1.getTextReceived(), expectedText);

        // Checking the Error handler response for invalid URL
        Assert.assertTrue(errorSession.isConnectionClose());
        CloseReason closeReason = errorSession.getCloseReason();
        Assert.assertTrue(closeReason != null);
        Assert.assertEquals(closeReason.getCloseCode().getCode(), 1001);
        Assert.assertEquals(closeReason.getReasonPhrase(),
                            "Server closing connection since no service found for URI: " + uri + "/error");
    }

    @Test(description = "Test the sending and receiving of client messages.")
    public void testPushText() {
        String expectedText = "Hi, Test";
        CarbonMessage client1Message = MessageUtils.generateWebSocketTextMessage(expectedText, session1, uri);
        Services.invoke(client1Message);
        Assert.assertEquals(session1.getTextReceived(), expectedText);
    }

    @Test(description = "Test closing a connection and broadcast.")
    public void textConnectionClose() {
        String expectedText = "client left";
        CarbonMessage client1Message = MessageUtils.generateWebSocketOnCloseMessage(session1, uri);
        Services.invoke(client1Message);
        Assert.assertEquals(session2.getTextReceived(), expectedText);
    }

    @AfterClass
    public void cleanUp() {
        EnvironmentInitializer.cleanup(application);
    }
}
