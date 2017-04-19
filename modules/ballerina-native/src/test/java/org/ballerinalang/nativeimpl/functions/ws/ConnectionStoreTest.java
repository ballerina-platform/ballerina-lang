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

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.ws.MockWebSocketSession;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test scenarios of WebSocket connection store.
 */
public class ConnectionStoreTest {

    private BLangProgram wsApp;

    // Client are identified here by Sessions
    private MockWebSocketSession session1 = new MockWebSocketSession("session1");
    private MockWebSocketSession session2 = new MockWebSocketSession("session2");
    private MockWebSocketSession session3 = new MockWebSocketSession("session3");
    private MockWebSocketSession session4 = new MockWebSocketSession("session4");

    @BeforeClass
    public void  setup() {
        wsApp = EnvironmentInitializer.setup("samples/websocket/connectionGroupTest.bal");
    }

    @Test
    public void testStoringConnections() {

    }

    @AfterClass
    public void cleanUp() {
        EnvironmentInitializer.cleanup(wsApp);
    }
}
