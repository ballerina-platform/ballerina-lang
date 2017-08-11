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

import java.util.HashMap;
import java.util.Map;

/**
 * Test scenarios of WebSocket connection store.
 */
public class ConnectionStoreTest {

    private ProgramFile wsApp;
    private ProgramFile httpApp;

    // Client are identified here by Sessions
    private MockWebSocketSession session1 = new MockWebSocketSession("session1");
    private MockWebSocketSession session2 = new MockWebSocketSession("session2");
    private MockWebSocketSession session3 = new MockWebSocketSession("session3");
    private MockWebSocketSession session4 = new MockWebSocketSession("session4");

    private final String wsPath = "/chat-store/ws";
    private final String httpBasePath = "/data";

    @BeforeClass
    public void  setup() {
        wsApp = EnvironmentInitializer.setupProgramFile(
                "samples/websocket/connection_store_sample/websocketEndpoint.bal");
        httpApp = EnvironmentInitializer.setupProgramFile(
                "samples/websocket/connection_store_sample/httpService.bal");

        //Registering WebSocket clients
        Map<String, String> headers = new HashMap<>();
        headers.put("id", "1");
        Services.invoke(MessageUtils.generateWebSocketOnOpenMessage(session1, wsPath, headers));
        headers.put("id", "2");
        Services.invoke(MessageUtils.generateWebSocketOnOpenMessage(session2, wsPath, headers));
        headers.put("id", "3");
        Services.invoke(MessageUtils.generateWebSocketOnOpenMessage(session3, wsPath, headers));
        headers.put("id", "4");
        Services.invoke(MessageUtils.generateWebSocketOnOpenMessage(session4, wsPath, headers));
    }

    @Test(priority = 0)
    public void testStoringConnections() {
        String textSent;
        //Sending message to client 1 and check
        textSent = "Hi store 1";
        Services.invoke(MessageUtils.generateHTTPMessage(httpBasePath + "/1", "POST", textSent));
        Assert.assertEquals(session1.getTextReceived(), textSent);
        Assert.assertEquals(session2.getTextReceived(), null);
        Assert.assertEquals(session3.getTextReceived(), null);
        Assert.assertEquals(session4.getTextReceived(), null);

        //Sending message to client 2 and check
        textSent = "Hi store 2";
        Services.invoke(MessageUtils.generateHTTPMessage(httpBasePath + "/2", "POST", textSent));
        Assert.assertEquals(session1.getTextReceived(), null);
        Assert.assertEquals(session2.getTextReceived(), textSent);
        Assert.assertEquals(session3.getTextReceived(), null);
        Assert.assertEquals(session4.getTextReceived(), null);

        //Sending message to client 3 and check
        textSent = "Hi store 3";
        Services.invoke(MessageUtils.generateHTTPMessage(httpBasePath + "/3", "POST", textSent));
        Assert.assertEquals(session1.getTextReceived(), null);
        Assert.assertEquals(session2.getTextReceived(), null);
        Assert.assertEquals(session3.getTextReceived(), textSent);
        Assert.assertEquals(session4.getTextReceived(), null);

        //Sending message to client 4 and check
        textSent = "Hi store 4";
        Services.invoke(MessageUtils.generateHTTPMessage(httpBasePath + "/4", "POST", textSent));
        Assert.assertEquals(session1.getTextReceived(), null);
        Assert.assertEquals(session2.getTextReceived(), null);
        Assert.assertEquals(session3.getTextReceived(), null);
        Assert.assertEquals(session4.getTextReceived(), textSent);
    }

    @Test(priority = 1)
    public void removeConnection() {
        Services.invoke(MessageUtils.generateHTTPMessage(httpBasePath + "/rm/1", "GET"));
        Services.invoke(MessageUtils.generateHTTPMessage(httpBasePath + "/1", "POST", "test"));

        Assert.assertEquals(session1.getTextReceived(), null);
        Assert.assertEquals(session2.getTextReceived(), null);
        Assert.assertEquals(session3.getTextReceived(), null);
        Assert.assertEquals(session4.getTextReceived(), null);
    }

    @Test(priority = 2)
    public void testCloseConnection() {
        // Check pre conditions
        Assert.assertTrue(session1.isOpen());
        Assert.assertTrue(session2.isOpen());
        Assert.assertTrue(session3.isOpen());
        Assert.assertTrue(session4.isOpen());

        Services.invoke(MessageUtils.generateHTTPMessage(httpBasePath + "/close/2", "GET"));

        // Check post conditions
        Assert.assertTrue(session1.isOpen());
        Assert.assertFalse(session2.isOpen());
        Assert.assertTrue(session3.isOpen());
        Assert.assertTrue(session4.isOpen());
    }

    @AfterClass
    public void cleanUp() {
        EnvironmentInitializer.cleanup(wsApp);
        EnvironmentInitializer.cleanup(httpApp);
    }
}
