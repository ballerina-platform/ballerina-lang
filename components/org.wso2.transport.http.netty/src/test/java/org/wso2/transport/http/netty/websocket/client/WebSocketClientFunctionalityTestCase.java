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
import io.netty.handler.codec.CorruptedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.util.server.websocket.WebSocketRemoteServer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.wso2.transport.http.netty.util.TestUtil.WEBSOCKET_REMOTE_SERVER_PORT;
import static org.wso2.transport.http.netty.util.TestUtil.WEBSOCKET_REMOTE_SERVER_URL;
import static org.wso2.transport.http.netty.util.TestUtil.WEBSOCKET_TEST_IDLE_TIMEOUT;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Test cases for the WebSocket Client implementation.
 */
public class WebSocketClientFunctionalityTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClientFunctionalityTestCase.class);

    private DefaultHttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
    private WebSocketClientConnector clientConnector;
    private WebSocketRemoteServer remoteServer;

    @BeforeClass
    public void setup() throws InterruptedException {
        remoteServer = new WebSocketRemoteServer(WEBSOCKET_REMOTE_SERVER_PORT, "xml, json");
        remoteServer.run();
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        configuration.setAutoRead(true);
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
    }

    @Test(description = "Test the WebSocket handshake and sending and receiving text messages.")
    public void testTextSendAndReceive() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);
        String textSent = "testText";
        sendTextMessageAndReceiveResponse(textSent, connectorListener, webSocketConnection);
        WebSocketTextMessage textMessage = connectorListener.getReceivedTextMessageToClient();

        assertMessageProperties(textMessage);
        Assert.assertEquals(textMessage.getText(), textSent);
        Assert.assertTrue(textMessage.isFinalFragment());
    }

    @Test(description = "This is to test whether the text frame continuation is working as expected.")
    public void testFrameContinuationForText() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);

        String continuationText1 = "continuation text 1";
        String continuationText2 = "continuation text 2";
        String finalText = "final text";

        int loopCount = 2;

        for (int i = 0; i < loopCount; i++) {
            WebSocketTextMessage textMessage;

            sendTextMessageAndReceiveResponse(continuationText1, false, connectorListener, webSocketConnection);
            textMessage = connectorListener.getReceivedTextMessageToClient();
            Assert.assertEquals(textMessage.getText(), continuationText1);
            Assert.assertFalse(textMessage.isFinalFragment());

            sendTextMessageAndReceiveResponse(continuationText2, false, connectorListener, webSocketConnection);
            textMessage = connectorListener.getReceivedTextMessageToClient();
            Assert.assertEquals(textMessage.getText(), continuationText2);
            Assert.assertFalse(textMessage.isFinalFragment());

            sendTextMessageAndReceiveResponse(finalText, true, connectorListener, webSocketConnection);
            textMessage = connectorListener.getReceivedTextMessageToClient();
            Assert.assertEquals(textMessage.getText(), finalText);
            Assert.assertTrue(textMessage.isFinalFragment());
        }
    }

    @Test(description = "Test binary message sending and receiving.")
    public void testBinarySendAndReceive() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);
        byte[] bytes = {1, 2, 3, 4, 5};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        sendAndReceiveBinaryMessage(bufferSent, connectorListener, webSocketConnection);
        WebSocketBinaryMessage receivedBinaryMessage = connectorListener.getReceivedBinaryMessageToClient();

        assertMessageProperties(receivedBinaryMessage);
        Assert.assertEquals(receivedBinaryMessage.getByteBuffer(), bufferSent);
        Assert.assertEquals(receivedBinaryMessage.getByteArray(), bytes);
    }

    @Test(description = "This is to test whether the binary frame continuation is working as expected.")
    public void testFrameContinuationForBinary() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);

        ByteBuffer continuationBuffer1 = ByteBuffer.wrap("continuation text 1".getBytes(StandardCharsets.UTF_8));
        ByteBuffer continuationBuffer2 = ByteBuffer.wrap("continuation text 2".getBytes(StandardCharsets.UTF_8));
        ByteBuffer finalBuffer = ByteBuffer.wrap("final text".getBytes(StandardCharsets.UTF_8));

        int loopCount = 2;

        for (int i = 0; i < loopCount; i++) {
            WebSocketBinaryMessage binaryMessage;

            sendAndReceiveBinaryMessage(continuationBuffer1, false, connectorListener, webSocketConnection);
            binaryMessage = connectorListener.getReceivedBinaryMessageToClient();
            Assert.assertEquals(binaryMessage.getByteBuffer(), continuationBuffer1);
            Assert.assertFalse(binaryMessage.isFinalFragment());

            sendAndReceiveBinaryMessage(continuationBuffer2, false, connectorListener, webSocketConnection);
            binaryMessage = connectorListener.getReceivedBinaryMessageToClient();
            Assert.assertEquals(binaryMessage.getByteBuffer(), continuationBuffer2);
            Assert.assertFalse(binaryMessage.isFinalFragment());

            sendAndReceiveBinaryMessage(finalBuffer, true, connectorListener, webSocketConnection);
            binaryMessage = connectorListener.getReceivedBinaryMessageToClient();
            Assert.assertEquals(binaryMessage.getByteBuffer(), finalBuffer);
            Assert.assertTrue(binaryMessage.isFinalFragment());
        }
    }

    @Test(description = "See if an error is thrown if a binary message is sent during text continuation.",
          expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Cannot interrupt WebSocket text frame continuation")
    public void testIllegalTextFrameContinuation() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);

        ByteBuffer buffer = ByteBuffer.wrap("continuation text 1".getBytes(StandardCharsets.UTF_8));
        String text = "continuation text 2";

        sendTextMessageAndReceiveResponse(text, false, connectorListener, webSocketConnection);
        sendAndReceiveBinaryMessage(buffer, false, connectorListener, webSocketConnection);
    }

    @Test(description = "See if an error is thrown if a text message is sent during binary continuation.",
          expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Cannot interrupt WebSocket binary frame continuation")
    public void testIllegalBinaryFrameContinuation() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);

        ByteBuffer buffer = ByteBuffer.wrap("continuation text 1".getBytes(StandardCharsets.UTF_8));
        String text = "continuation text 2";

        sendAndReceiveBinaryMessage(buffer, false, connectorListener, webSocketConnection);
        sendTextMessageAndReceiveResponse(text, false, connectorListener, webSocketConnection);
    }

    @Test(description = "Push text after connection closure should throw an exception.",
          expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Close frame already sent. Cannot push binary data.")
    public void testPushBinaryAfterConnectionClosure() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);

        webSocketConnection.terminateConnection(1000, "");

        ByteBuffer buffer = ByteBuffer.wrap("continuation text 1".getBytes(StandardCharsets.UTF_8));
        sendAndReceiveBinaryMessage(buffer, true, connectorListener, webSocketConnection);
    }

    @Test(description = "Push text after connection closure should throw an exception.",
          expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Close frame already sent. Cannot push text data.")
    public void testPushTextAfterConnectionClosure() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);

        webSocketConnection.terminateConnection(1000, "");

        String text = "continuation text 1";
        sendTextMessageAndReceiveResponse(text, true, connectorListener, webSocketConnection);
    }

    @Test(description = "Closing connection twice leads to illegal state.",
          expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Close frame already sent. Cannot send close frame again.")
    public void testConnectionClosureTwice() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);

        webSocketConnection.terminateConnection(1000, "");
        webSocketConnection.initiateConnectionClosure(1000, "");
    }

    @Test(description = "Closing connection twice leads to illegal state.",
          expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Close frame already sent. Cannot send close frame again.")
    public void testTerminateConnectionClosureTwice() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);

        webSocketConnection.terminateConnection(1000, "");
        webSocketConnection.terminateConnection(1000, "");
    }

    private void assertMessageProperties(WebSocketMessage webSocketMessage) {
        Assert.assertEquals(webSocketMessage.getTarget(), "ws://localhost:9010/websocket");
        Assert.assertFalse(webSocketMessage.isServerMessage());
    }


    private void sendAndReceiveBinaryMessage(ByteBuffer bufferSent,
                                             WebSocketTestClientConnectorListener connectorListener,
                                             WebSocketConnection webSocketConnection)
            throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        connectorListener.setCountDownLatch(countDownLatch);
        webSocketConnection.pushBinary(bufferSent);
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);
    }

    private void sendAndReceiveBinaryMessage(ByteBuffer bufferSent, boolean finalFragment,
                                             WebSocketTestClientConnectorListener connectorListener,
                                             WebSocketConnection webSocketConnection)
            throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        connectorListener.setCountDownLatch(countDownLatch);
        webSocketConnection.pushBinary(bufferSent, finalFragment);
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);
    }

    @Test(description = "Test ping received from the server.")
    public void testPing() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);
        sendTextMessageAndReceiveResponse("ping", connectorListener, webSocketConnection);
        Assert.assertTrue(connectorListener.isPingReceived(), "Ping message should be received");
    }

    private void sendTextMessageAndReceiveResponse(String textSent,
                                                   WebSocketTestClientConnectorListener connectorListener,
                                                   WebSocketConnection webSocketConnection)
            throws Throwable {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        connectorListener.setCountDownLatch(countDownLatch);
        webSocketConnection.pushText(textSent);
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);
    }

    private void sendTextMessageAndReceiveResponse(String textSent, boolean finalFragment,
                                                   WebSocketTestClientConnectorListener connectorListener,
                                                   WebSocketConnection webSocketConnection)
            throws Throwable {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        connectorListener.setCountDownLatch(countDownLatch);
        webSocketConnection.pushText(textSent, finalFragment);
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);
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
                LOG.error(t.getMessage());
                Assert.fail(t.getMessage());
            }
        });
        pongLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);
        return pongConnectorListener;
    }

    @Test
    public void testConnectionClosureFromServerSide() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);
        sendTextMessageAndReceiveResponse("close", connectorListener, webSocketConnection);
        WebSocketCloseMessage closeMessage = connectorListener.getCloseMessage();

        Assert.assertTrue(connectorListener.isClosed());
        Assert.assertEquals(closeMessage.getCloseCode(), 1000);
        Assert.assertEquals(closeMessage.getCloseReason(), "Close on request");

        webSocketConnection.finishConnectionClosure(closeMessage.getCloseCode(), null).sync();
        Assert.assertFalse(webSocketConnection.isOpen());
    }

    @Test
    public void testConnectionClosureWithoutCloseCodeFromServerSide() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);
        sendTextMessageAndReceiveResponse("close-without-status-code", connectorListener, webSocketConnection);
        WebSocketCloseMessage closeMessage = connectorListener.getCloseMessage();

        Assert.assertTrue(connectorListener.isClosed());
        Assert.assertEquals(closeMessage.getCloseCode(), 1005);
        Assert.assertEquals(closeMessage.getCloseReason(), "");

        webSocketConnection.finishConnectionClosure().sync();
        Assert.assertFalse(webSocketConnection.isOpen());
    }

    @Test
    public void testConnectionClosureFromServerSideWithoutCloseFrame() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);
        sendTextMessageAndReceiveResponse("close-without-frame", connectorListener, webSocketConnection);
        WebSocketCloseMessage closeMessage = connectorListener.getCloseMessage();

        Assert.assertTrue(connectorListener.isClosed());
        Assert.assertEquals(closeMessage.getCloseCode(), 1000);
        Assert.assertEquals(closeMessage.getCloseReason(), "Bye");
    }

    @Test(description = "Test connection termination using WebSocketConnection without sending a close frame.")
    public void testConnectionTerminationWithoutCloseFrame() throws Throwable {
        WebSocketConnection webSocketConnection =
                getWebSocketConnection(new WebSocketTestClientConnectorListener());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ChannelFuture closeFuture = webSocketConnection.terminateConnection().addListener(
                future -> countDownLatch.countDown());
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);

        Assert.assertNull(closeFuture.cause());
        Assert.assertTrue(closeFuture.isDone());
        Assert.assertTrue(closeFuture.isSuccess());
        Assert.assertFalse(webSocketConnection.isOpen());
    }

    @Test(description = "Test connection termination using WebSocketConnection with a close frame.")
    public void testConnectionTerminationWithCloseFrame() throws Throwable {
        WebSocketConnection webSocketConnection =
                getWebSocketConnection(new WebSocketTestClientConnectorListener());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ChannelFuture closeFuture = webSocketConnection.terminateConnection(1011, "Unexpected failure").addListener(
                future -> countDownLatch.countDown());
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);

        Assert.assertNull(closeFuture.cause());
        Assert.assertTrue(closeFuture.isDone());
        Assert.assertTrue(closeFuture.isSuccess());
        Assert.assertFalse(webSocketConnection.isOpen());
    }

    @Test
    public void testClientInitiatedClosure() throws Throwable {
        WebSocketConnection webSocketConnection =
                getWebSocketConnection(new WebSocketTestClientConnectorListener());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ChannelFuture closeFuture = webSocketConnection.initiateConnectionClosure(1001, "Going away").addListener(
                future -> countDownLatch.countDown());
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);

        Assert.assertNull(closeFuture.cause());
        Assert.assertTrue(closeFuture.isDone());
        Assert.assertTrue(closeFuture.isSuccess());
    }

    @Test
    public void testClientInitiatedClosureWithoutCloseCode() throws Throwable {
        WebSocketConnection webSocketConnection =
                getWebSocketConnection(new WebSocketTestClientConnectorListener());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ChannelFuture closeFuture = webSocketConnection.initiateConnectionClosure().addListener(
                future -> countDownLatch.countDown());
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);

        Assert.assertNull(closeFuture.cause());
        Assert.assertTrue(closeFuture.isDone());
        Assert.assertTrue(closeFuture.isSuccess());
    }

    @Test
    public void testExceptionCaught() throws Throwable {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        WebSocketConnection webSocketConnection = getWebSocketConnection(connectorListener);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        connectorListener.setCountDownLatch(countDownLatch);
        webSocketConnection.pushText("send-corrupted-frame");
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);
        Throwable throwable = connectorListener.getError();

        Assert.assertNotNull(webSocketConnection);
        Assert.assertNotNull(throwable);
        Assert.assertTrue(throwable instanceof CorruptedFrameException);
        Assert.assertEquals(throwable.getMessage(), "received continuation data frame outside fragmented message");
        Assert.assertFalse(webSocketConnection.isOpen());
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException, InterruptedException {
        remoteServer.stop();
        httpConnectorFactory.shutdown();
    }

    private ClientHandshakeFuture handshake(WebSocketConnectorListener connectorListener) {
        ClientHandshakeFuture clientHandshakeFuture = clientConnector.connect();
        clientHandshakeFuture.setWebSocketConnectorListener(connectorListener);
        return clientHandshakeFuture;
    }

    private WebSocketConnection getWebSocketConnection(WebSocketConnectorListener connectorListener)
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
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, SECONDS);
        if (throwableAtomicReference.get() != null) {
            throw throwableAtomicReference.get();
        }
        return webSocketConnectionAtomicReference.get();
    }
}
