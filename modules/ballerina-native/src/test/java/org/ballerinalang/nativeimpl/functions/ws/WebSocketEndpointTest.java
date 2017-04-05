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
import org.ballerinalang.testutils.websocket.client.WebSocketClient;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import javax.net.ssl.SSLException;

/**
 * Test connected client scenarios. When the client connects, send  and receive text and closing the connection.
 */
public class WebSocketEndpointTest {

    private BLangProgram application;
    private WebSocketClient client1;
    private WebSocketClient client2;
    private final int sleepTime = 100;
    private String url = "ws://localhost:9090/test/websocket";

    @BeforeClass
    public void setup() {
        application = EnvironmentInitializer.setup("samples/websocket/endpointTest.bal");
    }

    @Test
    public void testClientConnected() throws InterruptedException, SSLException, URISyntaxException {
        client1.handhshake(url);
        client2.handhshake(url);
        String expectedText = "new client connected";
        Thread.sleep(sleepTime);
        Assert.assertEquals(client1.getTextReceived(), expectedText);
    }
}
