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
 *
 */

package org.wso2.transport.http.netty.websocket;

import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.websocket.WebSocketRemoteServer;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test cases for the WebSocket Client implementation.
 */
public class WebSocketClientFunctionalityTestCase {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientFunctionalityTestCase.class);

    private DefaultHttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
    private final String url = String.format("ws://%s:%d/%s", "localhost",
                                             TestUtil.REMOTE_WS_SERVER_PORT, "websocket");
    private static final String PING = "ping";
    private final int latchWaitTimeInSeconds = 10;
    private WsClientConnectorConfig configuration = new WsClientConnectorConfig(url);
    private WebSocketClientConnector clientConnector;
    private WebSocketRemoteServer remoteServer;

    @BeforeClass
    public void setup() throws InterruptedException {
        remoteServer = new WebSocketRemoteServer(TestUtil.REMOTE_WS_SERVER_PORT, "xml, json");
        remoteServer.run();
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
    }

    @Test(description = "Test the WebSocket handshake and sending and receiving text messages.")
    public void testTextSendAndReceive() throws Throwable {
        String textSent = "testText";
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener(latch);
        ClientHandshakeFuture handshakeFuture = handshake(connectorListener);
        handshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                webSocketConnection.pushText(textSent);
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                Assert.fail(t.getMessage());
            }
        });
        latch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);
        String textReceived = connectorListener.getReceivedTextToClient();

        Assert.assertEquals(textReceived, textSent);
    }

    @Test(description = "Test binary message sending and receiving.")
    public void testBinarySendAndReceive() throws Throwable {
        byte[] bytes = {1, 2, 3, 4, 5};
        CountDownLatch latch = new CountDownLatch(1);
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener(latch);
        ClientHandshakeFuture handshakeFuture = handshake(connectorListener);
        handshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                webSocketConnection.pushBinary(bufferSent);
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                Assert.fail(t.getMessage());
            }
        });
        latch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);
        ByteBuffer bufferReceived = connectorListener.getReceivedByteBufferToClient();

        Assert.assertEquals(bufferReceived, bufferSent);
    }

    @Test(description = "Test ping received from the server.")
    public void testPingReceive() throws Throwable {
        CountDownLatch pingLatch = new CountDownLatch(1);
        WebSocketTestClientConnectorListener pingConnectorListener =
                new WebSocketTestClientConnectorListener(pingLatch);
        ClientHandshakeFuture pingHandshakeFuture = handshake(pingConnectorListener);
        pingHandshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                webSocketConnection.pushText(PING);
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                Assert.fail(t.getMessage());
            }
        });
        pingLatch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);

        Assert.assertTrue(pingConnectorListener.isPingReceived(), "Ping message should be received");
    }

    @Test(description = "Test pong received from the server after pinging the server.")
    public void testPongReceiveForPingSent() throws Throwable {
        CountDownLatch pongLatch = new CountDownLatch(1);
        WebSocketTestClientConnectorListener pongConnectorListener =
                new WebSocketTestClientConnectorListener(pongLatch);
        ClientHandshakeFuture pongHandshakeFuture = handshake(pongConnectorListener);
        pongHandshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                byte[] bytes = {1, 2, 3, 4, 5};
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                webSocketConnection.ping(buffer);
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                Assert.fail(t.getMessage());
            }
        });
        pongLatch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);

        Assert.assertTrue(pongConnectorListener.isPongReceived(), "Pong message should be received");
    }

    @Test
    public void testConnectionClosureFromServerSide() throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener(latch);
        ClientHandshakeFuture handshakeFuture = handshake(connectorListener);
        handshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                webSocketConnection.pushText("close");
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                Assert.fail(t.getMessage());
            }
        });
        latch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);
        WebSocketCloseMessage closeMessage = connectorListener.getCloseMessage();

        Assert.assertTrue(connectorListener.isClosed());
        Assert.assertEquals(closeMessage.getCloseCode(), 1000);
        Assert.assertEquals(closeMessage.getCloseReason(), "Close on request");
    }

    @Test
    public void testConnectionClosureFromServerSideWithoutCloseFrame() throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener(latch);
        ClientHandshakeFuture handshakeFuture = handshake(connectorListener);
        handshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                webSocketConnection.pushText("close-without-frame");
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                Assert.fail(t.getMessage());
            }
        });
        latch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);
        WebSocketCloseMessage closeMessage = connectorListener.getCloseMessage();

        Assert.assertEquals(closeMessage.getCloseCode(), 1006);
        Assert.assertNull(closeMessage.getCloseReason());
        Assert.assertTrue(connectorListener.isClosed());
    }

    @Test
    public void testForceFulConnectionClosure() throws Throwable {
        WebSocketConnection webSocketConnection =
                getWebSocketConnectionSync(new WebSocketTestClientConnectorListener());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ChannelFuture closeFuture = webSocketConnection.closeForcefully().addListener(
                future -> countDownLatch.countDown());
        countDownLatch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);

        Assert.assertNull(closeFuture.cause());
        Assert.assertTrue(closeFuture.isDone());
        Assert.assertTrue(closeFuture.isSuccess());
    }

    @Test
    public void testClientWaitAfterSendingCloseFrame() throws Throwable {
        WebSocketConnection webSocketConnection =
                getWebSocketConnectionSync(new WebSocketTestClientConnectorListener());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ChannelFuture closeFuture = webSocketConnection.initiateConnectionClosure(1001, "Going away").addListener(
                future -> countDownLatch.countDown());
        countDownLatch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);

        Assert.assertNull(closeFuture.cause());
        Assert.assertTrue(closeFuture.isDone());
        Assert.assertTrue(closeFuture.isSuccess());
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException, InterruptedException {
        remoteServer.stop();
    }

    private ClientHandshakeFuture handshake(WebSocketConnectorListener connectorListener) {
        return clientConnector.connect(connectorListener);
    }

    private WebSocketConnection getWebSocketConnectionSync(WebSocketConnectorListener connectorListener)
            throws Throwable {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<WebSocketConnection> webSocketConnectionAtomicReference = new AtomicReference<>();
        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>();
        handshake(connectorListener).setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                webSocketConnectionAtomicReference.set(webSocketConnection);
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable throwable, HttpCarbonResponse response) {
                throwableAtomicReference.set(throwable);
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);
        if (throwableAtomicReference.get() != null) {
            throw throwableAtomicReference.get();
        }
        return webSocketConnectionAtomicReference.get();
    }
}
