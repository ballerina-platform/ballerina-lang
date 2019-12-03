/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests an ssl WebSocket echo server.
 *
 * @since 0.990.1
 */
@Test(groups = {"websocket-test"})
public class AttachDetachTest extends WebSocketTestCommons {

    private static final String URL = "ws://localhost:21029/attach/detach";
    private static final String URL_NO_PATH = "ws://localhost:21029";
    private static final String URL_WITH_PATH = "ws://localhost:21029/hello";
    private static final String ATTACH_TEXT = "attach";
    private static final String DETACH_TEXT = "detach";

    private WebSocketTestClient client;

    // Related file 29_attach_detach.bal
    @BeforeClass
    public void setup() throws URISyntaxException, InterruptedException {
        client = new WebSocketTestClient(URL);
        client.handshake();
    }

    @Test(description = "Try attaching a WebSocket Client service")
    public void attachClientService() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText("client_attach");
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "Client service cannot be attached to the Listener");
    }

    @Test(description = "Detach the service first")
    public void detachFirst() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(DETACH_TEXT);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "Cannot detach service. Service has not been registered");
    }

    @Test(description = "Tests echoed text message from the attached servers", priority = 1)
    public void attachSuccess() throws InterruptedException, URISyntaxException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(ATTACH_TEXT);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);

        // send to the no path service
        String msg = "message";
        WebSocketTestClient attachClient = new WebSocketTestClient(URL_NO_PATH);
        countDownLatch = new CountDownLatch(1);
        attachClient.setCountDownLatch(countDownLatch);
        attachClient.handshake();
        attachClient.sendText(msg);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(attachClient.getTextReceived(), msg, "Attach with no path failed");

        // send to service with path
        msg = "path message";
        WebSocketTestClient pathClient = new WebSocketTestClient(URL_WITH_PATH);
        countDownLatch = new CountDownLatch(1);
        pathClient.setCountDownLatch(countDownLatch);
        pathClient.handshake();
        pathClient.sendText(msg);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(pathClient.getTextReceived(), msg, "Attach with path failed");
    }

    @Test(description = "Tests detach", expectedExceptions = WebSocketHandshakeException.class,
          expectedExceptionsMessageRegExp = "Invalid handshake response getStatus: 404 Not Found", priority = 2)
    public void detachSuccess() throws InterruptedException, URISyntaxException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(DETACH_TEXT);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);

        WebSocketTestClient attachClient = new WebSocketTestClient(URL_NO_PATH);
        countDownLatch = new CountDownLatch(1);
        attachClient.setCountDownLatch(countDownLatch);
        attachClient.handshake();
    }


    @Test(description = "Attach twice to the service", priority = 3)
    public void attachTwice() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(ATTACH_TEXT);
        client.sendText(ATTACH_TEXT);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "Two services have the same addressable URI");
    }


    @Test(description = "Detach from the service twice", priority = 4)
    public void detachTwice() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(DETACH_TEXT);
        client.sendText(DETACH_TEXT);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), "Cannot detach service. Service has not been registered");
    }

    @AfterClass
    public void cleanUp() throws InterruptedException {
        client.shutDown();
    }
}
