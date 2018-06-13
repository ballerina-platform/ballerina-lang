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

package org.ballerinalang.test.service.websocket;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test WebSocket Path and Query Parameters.
 */
public class WebSocketQueryAndPathParamSupportTestCase extends WebSocketIntegrationTest {

    @BeforeClass(description = "Initializes Ballerina")
    public void setup() throws BallerinaTestException {
        initBallerinaServer("query_and_path_param_support.bal");
    }

    @Test(description = "Tests path and query parameters support for WebSockets in Ballerina")
    public void testPathAndQueryParams() throws URISyntaxException, InterruptedException {
        String path1 = "path1";
        String path2 = "path2";
        String query1 = "query1";
        String query2 = "query2";
        String url = String.format("ws://localhost:9090/simple/%s/%s?q1=%s&q2=%s", path1, path2, query1, query2);
        WebSocketTestClient client = new WebSocketTestClient(url);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.handshake();
        client.sendText("send");
        String expectedMsg = String.format("path-params: %s, %s; query-params: %s, %s", path1, path2, query1, query2);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), expectedMsg);
        client.shutDown();
    }

    @AfterClass(description = "Stops Ballerina")
    public void cleanup() throws BallerinaTestException {
        stopBallerinaServerInstance();
    }
}
