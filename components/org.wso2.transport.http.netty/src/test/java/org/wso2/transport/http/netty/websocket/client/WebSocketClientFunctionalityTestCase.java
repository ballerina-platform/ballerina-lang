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

package org.wso2.transport.http.netty.websocket.client;

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
import org.wso2.transport.http.netty.util.server.websocket.WebSocketRemoteServer;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.wso2.transport.http.netty.util.TestUtil.WEBSOCKET_REMOTE_SERVER_PORT;
import static org.wso2.transport.http.netty.util.TestUtil.WEBSOCKET_REMOTE_SERVER_URL;
import static org.wso2.transport.http.netty.util.TestUtil.WEBSOCKET_TEST_IDLE_TIMEOUT;

/**
 * Test cases for the WebSocket Client implementation.
 */
public class WebSocketClientFunctionalityTestCase {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientFunctionalityTestCase.class);

    private DefaultHttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
    private WebSocketClientConnector clientConnector;
    private WebSocketRemoteServer remoteServer;

    @BeforeClass
    public void setup() throws InterruptedException {
        remoteServer = new WebSocketRemoteServer(WEBSOCKET_REMOTE_SERVER_PORT, "xml, json");
        remoteServer.run();
        WsClientConnectorConfig configuration = new WsClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
    }

    @Test(description = "Test the WebSocket handshake and sending and receiving text messages.")
    public void testTextSendAndReceive() throws Throwable {
        String textSent = "testText";
        WebSocketTestClientConnectorListener connectorListener = handshakeAndSendText(textSent);
        String textReceived = connectorListener.getReceivedTextToClient();

        Assert.assertEquals(textReceived, textSent);
    }

    @Test
    public void testConnectionClosureFromServerSide() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = handshakeAndSendText("close");
        WebSocketCloseMessage closeMessage = connectorListener.getCloseMessage();

        Assert.assertTrue(connectorListener.isClosed());
        Assert.assertEquals(closeMessage.getCloseCode(), 1000);
        Assert.assertEquals(closeMessage.getCloseReason(), "Close on request");
    }

    @Test
    public void testConnectionClosureFromServerSideWithoutCloseFrame() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = handshakeAndSendText("close-without-frame");
        WebSocketCloseMessage closeMessage = connectorListener.getCloseMessage();

        Assert.assertEquals(closeMessage.getCloseCode(), 1006);
        Assert.assertNull(closeMessage.getCloseReason());
        Assert.assertTrue(connectorListener.isClosed());
    }

    @Test(description = "Test ping received from the server.")
    public void testPing() throws Throwable {
        WebSocketTestClientConnectorListener pingConnectorListener = handshakeAndSendText("ping");

        Assert.assertTrue(pingConnectorListener.isPingReceived(), "Ping message should be received");
    }

    private WebSocketTestClientConnectorListener handshakeAndSendText(String textSent) throws InterruptedException {
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
        latch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);
        return connectorListener;
    }

    @Test(description = "Test binary message sending and receiving.")
    public void testBinarySendAndReceive() throws Throwable {
        byte[] bytes = {1, 2, 3, 4, 5};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        WebSocketTestClientConnectorListener connectorListener = handshakeAndSendBinaryMessage(bufferSent);
        ByteBuffer bufferReceived = connectorListener.getReceivedByteBufferToClient();

        Assert.assertEquals(bufferReceived, bufferSent);
    }

    private WebSocketTestClientConnectorListener handshakeAndSendBinaryMessage(ByteBuffer bufferSent)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
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
        latch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);
        return connectorListener;
    }

    @Test(description = "Test pong received from the server after pinging the server.")
    public void testPong() throws Throwable {
        WebSocketTestClientConnectorListener pongConnectorListener = handshakeAndPing();

        Assert.assertTrue(pongConnectorListener.isPongReceived(), "Pong message should be received");
    }

    private WebSocketTestClientConnectorListener handshakeAndPing() throws InterruptedException {
        CountDownLatch pongLatch = new CountDownLatch(1);
        WebSocketTestClientConnectorListener pongConnectorListener =
                new WebSocketTestClientConnectorListener(pongLatch);
        handshake(pongConnectorListener).setClientHandshakeListener(new ClientHandshakeListener() {
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
        pongLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);
        return pongConnectorListener;
    }

    @Test(description = "Test connection termination using WebSocketConnection without sending a close frame.")
    public void testConnectionTermination() throws Throwable {
        WebSocketConnection webSocketConnection =
                getWebSocketConnectionSync(new WebSocketTestClientConnectorListener());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ChannelFuture closeFuture = webSocketConnection.terminateConnection().addListener(
                future -> countDownLatch.countDown());
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);

        Assert.assertNull(closeFuture.cause());
        Assert.assertTrue(closeFuture.isDone());
        Assert.assertTrue(closeFuture.isSuccess());
    }

    @Test
    public void testClientInitiatedClosure() throws Throwable {
        WebSocketConnection webSocketConnection =
                getWebSocketConnectionSync(new WebSocketTestClientConnectorListener());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ChannelFuture closeFuture = webSocketConnection.initiateConnectionClosure(1001, "Going away").addListener(
                future -> countDownLatch.countDown());
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);

        Assert.assertNull(closeFuture.cause());
        Assert.assertTrue(closeFuture.isDone());
        Assert.assertTrue(closeFuture.isSuccess());
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException, InterruptedException {
        remoteServer.stop();
        httpConnectorFactory.shutdown();
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
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);
        if (throwableAtomicReference.get() != null) {
            throw throwableAtomicReference.get();
        }
        return webSocketConnectionAtomicReference.get();
    }
}
