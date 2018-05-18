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

package org.wso2.transport.http.netty.websocket;

import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.websocket.WebSocketTestClient;

import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import javax.net.ssl.SSLException;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Test cases for WebSocket server connection close scenarios.
 */
public class WebSocketServerCloseConnectionTestCase {

    private final int latchCountDownInSecs = 10;
    private ServerConnector serverConnector;
    private WebSocketCloseConnectionListener closeConnectionListener;

    @BeforeClass
    public void setup() throws InterruptedException {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost("localhost");
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        DefaultHttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = httpConnectorFactory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(),
                                                                     listenerConfiguration);
        ServerConnectorFuture connectorFuture = serverConnector.start();
        connectorFuture.setWSConnectorListener(closeConnectionListener = new WebSocketCloseConnectionListener());
        connectorFuture.sync();
    }

    @Test
    public void testCloseForcefully()
            throws InterruptedException, ProtocolException, SSLException, URISyntaxException {
        CountDownLatch clientLatch = new CountDownLatch(1);
        CountDownLatch serverLatch = new CountDownLatch(1);
        closeConnectionListener.setReturnFutureLatch(serverLatch);
        WebSocketTestClient client = new WebSocketTestClient(clientLatch);
        client.handhshake();
        client.sendText("close-forcefully");
        serverLatch.await(latchCountDownInSecs, SECONDS);
        clientLatch.await(latchCountDownInSecs, SECONDS);

        Assert.assertEquals(client.getReceivedCloseFrame(), null);
        Assert.assertFalse(client.isOpen());
        Assert.assertFalse(closeConnectionListener.getCloseFuture().channel().isOpen());
    }

    @Test
    public void testSendAndWaitForCloseFrameEchoBack()
            throws InterruptedException, ProtocolException, SSLException, URISyntaxException {
        int expectedStatusCode = 1001;
        String expectedReason = "Going away";
        CountDownLatch clientLatch = new CountDownLatch(1);
        CountDownLatch serverLatch = new CountDownLatch(1);
        closeConnectionListener.setReturnFutureLatch(serverLatch);
        WebSocketTestClient client = new WebSocketTestClient(clientLatch);
        client.handhshake();
        client.sendText("send-and-wait");
        clientLatch.await(latchCountDownInSecs, SECONDS);
        serverLatch.await(latchCountDownInSecs, SECONDS);
        CloseWebSocketFrame closeFrame = client.getReceivedCloseFrame();

        // Assert the pre conditions after receiving a close frame from the server and
        // before client echoing the close frame.
        Assert.assertNotNull(closeFrame);
        Assert.assertEquals(closeFrame.statusCode(), expectedStatusCode);
        Assert.assertEquals(closeFrame.reasonText(), expectedReason);
        Assert.assertFalse(closeConnectionListener.getCloseFuture().isDone());

        CountDownLatch closeDoneLatch = new CountDownLatch(1);
        closeConnectionListener.setCloseDoneLatch(closeDoneLatch);
        client.sendCloseFrame(closeFrame.statusCode(), null);
        closeDoneLatch.await(latchCountDownInSecs, SECONDS);

        // Assert the post conditions after receiving a close frame from the server and
        // after client echoing the close frame.
        Assert.assertTrue(closeConnectionListener.getCloseFuture().isSuccess());

        closeFrame.release();
    }

    @Test(description = "As per spec typically the remote endpoint should echo back the same status code " +
            "sent by this endpoint. This tests the error for not receiving the same status code.")
    public void testSendAndReceiveReceiveDifferentStatusCode()
            throws InterruptedException, ProtocolException, SSLException, URISyntaxException {
        int expectedStatusCode = 1001;
        String expectedReason = "Going away";
        CountDownLatch clientLatch = new CountDownLatch(1);
        CountDownLatch serverLatch = new CountDownLatch(1);
        closeConnectionListener.setReturnFutureLatch(serverLatch);
        WebSocketTestClient client = new WebSocketTestClient(clientLatch);
        client.handhshake();
        client.sendText("send-and-wait");
        clientLatch.await(latchCountDownInSecs, SECONDS);
        serverLatch.await(latchCountDownInSecs, SECONDS);
        CloseWebSocketFrame closeFrame = client.getReceivedCloseFrame();

        // Assert the pre conditions after receiving a close frame from the server and
        // before client echoing the close frame.
        Assert.assertNotNull(closeFrame);
        Assert.assertEquals(closeFrame.statusCode(), expectedStatusCode);
        Assert.assertEquals(closeFrame.reasonText(), expectedReason);
        Assert.assertFalse(closeConnectionListener.getCloseFuture().isDone());

        CountDownLatch closeDoneLatch = new CountDownLatch(1);
        closeConnectionListener.setCloseDoneLatch(closeDoneLatch);
        client.sendCloseFrame(closeFrame.statusCode() + 1, null);
        closeDoneLatch.await(latchCountDownInSecs, SECONDS);
        Throwable cause = closeConnectionListener.getCloseFuture().cause();

        // Assert the post conditions after receiving a close frame from the server and
        // after client echoing the close frame.
        Assert.assertTrue(closeConnectionListener.getCloseFuture().isDone());
        Assert.assertFalse(closeConnectionListener.getCloseFuture().isSuccess());
        Assert.assertNotNull(cause);
        Assert.assertTrue(cause instanceof IllegalStateException);
        Assert.assertEquals(cause.getMessage(),
                            String.format("Expected status code %d but found %d in echoed close frame from remote " +
                                                  "backend",
                                          closeFrame.statusCode(), closeFrame.statusCode() + 1));
        closeFrame.release();
    }

    @AfterClass
    public void cleanup() {
        serverConnector.stop();
    }
}
