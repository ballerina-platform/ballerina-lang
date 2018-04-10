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

package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;

/**
 * Test WebSocket Path and Query Parameters.
 */
public class WebSocketQueryAndPathParamSupportTestCase {

    private ServerInstance ballerinaServerInstance;

    @BeforeClass
    public void setup() throws InterruptedException, BallerinaTestException {

        String balPath = new File("src/test/resources/websocket/QueryAndPathParamSupport.bal").getAbsolutePath();
        ballerinaServerInstance = ServerInstance.initBallerinaServer();
        ballerinaServerInstance.startBallerinaServer(balPath);
    }

    @Test
    public void testPathAndQueryParams() throws URISyntaxException, InterruptedException, SSLException {
        String path1 = "path1";
        String path2 = "path2";
        String query1 = "query1";
        String query2 = "query2";
        String url = String.format("ws://localhost:9090/simple/%s/%s?q1=%s&q2=%s", path1, path2, query1, query2);
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("send");
        String expectedMsg = String.format("path-params: %s, %s; query-params: %s, %s", path1, path2, query1, query2);
        countDownLatch.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), expectedMsg);
        client.shutDown();
    }

    @AfterClass
    public void cleanup() throws BallerinaTestException {
        ballerinaServerInstance.stopServer();
    }
}
