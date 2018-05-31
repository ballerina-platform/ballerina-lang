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

package org.wso2.transport.http.netty.websocket.server;

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

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Test class for WebSocket protocol.
 */
public class WebSocketServerFunctionalityTestCase {

    private final int latchCountDownInSecs = 10;
    private final int defaultStatusCode = 1001;
    private final String defaultCloseReason = "Going away";
    private DefaultHttpWsConnectorFactory httpConnectorFactory;
    private ServerConnector serverConnector;
    private WebSocketTestServerConnectorListener serverConnectorListener;

    @BeforeClass
    public void setup() throws InterruptedException {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost(TestUtil.TEST_HOST);
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        httpConnectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = httpConnectorFactory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(),
                listenerConfiguration);
        ServerConnectorFuture connectorFuture = serverConnector.start();
        connectorFuture.setWSConnectorListener(serverConnectorListener = new WebSocketTestServerConnectorListener());
        connectorFuture.sync();
    }

    @Test
    public void testTextReceiveAndEchoBack() throws URISyntaxException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClient client = new WebSocketTestClient();
        client.handshake();
        client.setCountDownLatch(latch);
        String textSent = "test";
        client.sendText(textSent);
        latch.await(latchCountDownInSecs, SECONDS);

        Assert.assertEquals(client.getTextReceived(), textSent);

        client.sendCloseFrame(defaultStatusCode, defaultCloseReason).closeChannel();
    }

    @Test
    public void testBinaryReceiveAndEchoBack() throws URISyntaxException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClient client = new WebSocketTestClient();
        client.handshake();
        client.setCountDownLatch(latch);
        byte[] bytes = {1, 2, 3, 4, 5};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        client.sendBinary(bufferSent);
        latch.await(latchCountDownInSecs, SECONDS);

        Assert.assertEquals(client.getBufferReceived(), bufferSent);

        client.sendCloseFrame(defaultStatusCode, defaultCloseReason).closeChannel();
    }

    @Test
    public void testPingToTestClient() throws InterruptedException, URISyntaxException {
        final String ping = "ping";
        CountDownLatch pingLatch = new CountDownLatch(1);
        WebSocketTestClient pingCheckClient = new WebSocketTestClient();
        pingCheckClient.handshake();
        pingCheckClient.setCountDownLatch(pingLatch);
        pingCheckClient.sendText(ping);
        pingLatch.await(latchCountDownInSecs, SECONDS);
        ByteBuffer expectedBuffer = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});

        Assert.assertTrue(pingCheckClient.isPingReceived(), "Should receive a ping from the server");
        Assert.assertEquals(pingCheckClient.getBufferReceived(), expectedBuffer);

        pingCheckClient.sendCloseFrame(defaultStatusCode, defaultCloseReason).closeChannel();
    }

    //TODO: Description
    @Test
    public void testPong()
            throws InterruptedException, URISyntaxException {
        CountDownLatch pongLatch = new CountDownLatch(1);
        WebSocketTestClient pongCheckClient = new WebSocketTestClient();
        pongCheckClient.handshake();
        pongCheckClient.setCountDownLatch(pongLatch);
        byte[] bytes = {6, 7, 8, 9, 10, 11};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        pongCheckClient.sendPing(bufferSent);
        pongLatch.await(latchCountDownInSecs, SECONDS);

        Assert.assertTrue(pongCheckClient.isPongReceived(), "Should receive a pong from the server");
        Assert.assertEquals(pongCheckClient.getBufferReceived(), bufferSent);

        pongCheckClient.sendCloseFrame(defaultStatusCode, defaultCloseReason).closeChannel();
    }

    @Test
    public void testForceFulConnectionClosure() throws InterruptedException, URISyntaxException {
        CountDownLatch clientLatch = new CountDownLatch(1);
        CountDownLatch serverLatch = new CountDownLatch(1);
        serverConnectorListener.setReturnFutureLatch(serverLatch);
        WebSocketTestClient client = new WebSocketTestClient();
        client.handshake();
        client.setCountDownLatch(clientLatch);
        client.sendText("close-forcefully");
        serverLatch.await(latchCountDownInSecs, SECONDS);
        clientLatch.await(latchCountDownInSecs, SECONDS);

        Assert.assertNull(client.getReceivedCloseFrame());
        Assert.assertFalse(client.isOpen());
        Assert.assertFalse(serverConnectorListener.getCloseFuture().channel().isOpen());
    }

    @Test
    public void testServerInitiatedClosure() throws InterruptedException, URISyntaxException {
        WebSocketTestClient client = commandServerInitiatedClosure();
        CloseWebSocketFrame closeFrame = client.getReceivedCloseFrame();

        // Assert the pre conditions after receiving a close frame from the server and
        // before client echoing the close frame.
        Assert.assertNotNull(closeFrame);
        Assert.assertEquals(closeFrame.statusCode(), 1001);
        Assert.assertEquals(closeFrame.reasonText(), "Going away");
        Assert.assertFalse(serverConnectorListener.getCloseFuture().isDone());

        acknowledgeServerClosure(client, closeFrame.statusCode());

        // Assert the post conditions after receiving a close frame from the server and
        // after client echoing the close frame.
        Assert.assertTrue(serverConnectorListener.getCloseFuture().isSuccess());

        closeFrame.release();
    }

    private void acknowledgeServerClosure(WebSocketTestClient client, int statusCode) throws InterruptedException {
        CountDownLatch closeDoneLatch = new CountDownLatch(1);
        serverConnectorListener.setCloseDoneLatch(closeDoneLatch);
        client.sendCloseFrame(statusCode, null);
        closeDoneLatch.await(latchCountDownInSecs, SECONDS);
    }

    private WebSocketTestClient commandServerInitiatedClosure() throws URISyntaxException, InterruptedException {
        CountDownLatch clientLatch = new CountDownLatch(1);
        CountDownLatch serverLatch = new CountDownLatch(1);
        serverConnectorListener.setReturnFutureLatch(serverLatch);
        WebSocketTestClient client = new WebSocketTestClient();
        client.handshake();
        client.setCountDownLatch(clientLatch);
        client.sendText("send-and-wait");
        clientLatch.await(latchCountDownInSecs, SECONDS);
        serverLatch.await(latchCountDownInSecs, SECONDS);
        return client;
    }

    @Test(description = "As per spec typically the remote endpoint should echo back the same status code " +
            "sent by this endpoint. This tests the error for not receiving the same status code.")
    public void testSendAndReceiveDifferentStatusCode() throws InterruptedException, URISyntaxException {
        WebSocketTestClient client = commandServerInitiatedClosure();
        CloseWebSocketFrame closeFrame = client.getReceivedCloseFrame();

        // Assert the pre conditions after receiving a close frame from the server and
        // before client echoing the close frame.
        Assert.assertNotNull(closeFrame);
        Assert.assertEquals(closeFrame.statusCode(), 1001);
        Assert.assertEquals(closeFrame.reasonText(),  "Going away");
        Assert.assertFalse(serverConnectorListener.getCloseFuture().isDone());

        acknowledgeServerClosure(client, closeFrame.statusCode() + 1);
        Throwable cause = serverConnectorListener.getCloseFuture().cause();

        // Assert the post conditions after receiving a close frame from the server and
        // after client echoing the close frame.
        Assert.assertTrue(serverConnectorListener.getCloseFuture().isDone());
        Assert.assertFalse(serverConnectorListener.getCloseFuture().isSuccess());
        Assert.assertNotNull(cause);
        Assert.assertTrue(cause instanceof IllegalStateException);
        Assert.assertEquals(cause.getMessage(),
                            String.format("Expected status code %d but found %d in echoed close frame from remote " +
                                                  "endpoint",
                                          closeFrame.statusCode(), closeFrame.statusCode() + 1));
        closeFrame.release();
    }

    @AfterClass
    public void cleanUp() throws InterruptedException {
        serverConnector.stop();
        httpConnectorFactory.shutdown();
    }
}
