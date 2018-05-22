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
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.websocket.WebSocketRemoteServer;

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test cases for the WebSocket Client implementation.
 */
public class WebSocketClientHandshakeFunctionalityTestCase {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientHandshakeFunctionalityTestCase.class);

    private DefaultHttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
    private final String url = String.format("ws://%s:%d/%s", "localhost",
                                             TestUtil.REMOTE_WS_SERVER_PORT, "websocket");
    private static final String PING = "ping";
    private final int latchWaitTimeInSeconds = 10;
    private WebSocketClientConnector clientConnector;
    private WebSocketRemoteServer remoteServer;

    @BeforeClass
    public void setup() throws InterruptedException {
        WsClientConnectorConfig configuration = new WsClientConnectorConfig(url);
        remoteServer = new WebSocketRemoteServer(TestUtil.REMOTE_WS_SERVER_PORT, "xml, json");
        remoteServer.run();
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
    }

    @Test(description = "Test the idle timeout for WebSocket")
    public void testIdleTimeout() throws Throwable {
        WsClientConnectorConfig configuration = new WsClientConnectorConfig(url);
        configuration.setIdleTimeoutInMillis(1000);
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener(latch);
        ClientHandshakeFuture handshakeFuture = handshake(connectorListener);

        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>();
        handshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                throwableAtomicReference.set(t);
            }
        });
        latch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);

        Assert.assertNull(throwableAtomicReference.get());
        Assert.assertTrue(connectorListener.isIdleTimeout(), "Should reach idle timeout");
    }

    @Test(description = "Test the sub protocol negotiation with the remote server")
    public void testSubProtocolNegotiationSuccessful() throws InterruptedException {
        WsClientConnectorConfig configuration = new WsClientConnectorConfig(url);
        String[] subProtocolsSuccess = {"xmlx", "json"};
        configuration.setSubProtocols(subProtocolsSuccess);
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
        CountDownLatch latchSuccess = new CountDownLatch(1);
        WebSocketTestClientConnectorListener connectorListenerSuccess =
                new WebSocketTestClientConnectorListener(latchSuccess);
        ClientHandshakeFuture handshakeFutureSuccess = handshake(connectorListenerSuccess);

        AtomicReference<WebSocketConnection> connectionAtomicReference = new AtomicReference<>();
        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>();
        handshakeFutureSuccess.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                connectionAtomicReference.set(webSocketConnection);
                latchSuccess.countDown();
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                throwableAtomicReference.set(t);
                latchSuccess.countDown();
            }
        });
        latchSuccess.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);

        WebSocketConnection webSocketConnection = connectionAtomicReference.get();

        Assert.assertNull(throwableAtomicReference.get());
        Assert.assertNotNull(webSocketConnection);
        Assert.assertEquals(webSocketConnection.getSession().getNegotiatedSubprotocol(), "json");
    }

    @Test(description = "Test the sub protocol negotiation with the remote server")
    public void testSubProtocolNegotiationFail() throws InterruptedException {
        WsClientConnectorConfig configuration = new WsClientConnectorConfig(url);
        String[] subProtocolsFail = {"xmlx", "jsonx"};
        configuration.setSubProtocols(subProtocolsFail);
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
        CountDownLatch latchFail = new CountDownLatch(1);
        WebSocketTestClientConnectorListener connectorListenerFail =
                new WebSocketTestClientConnectorListener(latchFail);
        ClientHandshakeFuture handshakeFutureFail = handshake(connectorListenerFail);

        AtomicReference<WebSocketConnection> connectionAtomicReference = new AtomicReference<>();
        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>();
        handshakeFutureFail.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                connectionAtomicReference.set(webSocketConnection);
                latchFail.countDown();
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                throwableAtomicReference.set(t);
                latchFail.countDown();
            }
        });
        latchFail.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);
        Throwable throwable = throwableAtomicReference.get();

        Assert.assertNull(connectionAtomicReference.get());
        Assert.assertNotNull(throwable);
        Assert.assertEquals(throwable.getMessage(), "Invalid subprotocol. Actual: null. Expected one of: xmlx,jsonx");
    }

    @Test(description = "Test whether client can send custom headers and receive.")
    public void testSendAndReceiveCustomHeaders() throws InterruptedException {
        WsClientConnectorConfig configuration = new WsClientConnectorConfig(url);
        configuration.addHeader("x-ack-custom-header", "true");
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
        CountDownLatch latchSuccess = new CountDownLatch(1);
        WebSocketTestClientConnectorListener connectorListenerSuccess =
                new WebSocketTestClientConnectorListener(latchSuccess);
        ClientHandshakeFuture handshakeFutureSuccess = handshake(connectorListenerSuccess);

        AtomicReference<WebSocketConnection> connectionAtomicReference = new AtomicReference<>();
        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>();
        AtomicReference<HttpCarbonResponse> responseAtomicReference = new AtomicReference<>();
        handshakeFutureSuccess.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                connectionAtomicReference.set(webSocketConnection);
                responseAtomicReference.set(response);
                latchSuccess.countDown();
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                throwableAtomicReference.set(t);
                latchSuccess.countDown();
            }
        });
        latchSuccess.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);

        HttpCarbonResponse response = responseAtomicReference.get();

        Assert.assertNull(throwableAtomicReference.get());
        Assert.assertNotNull(connectionAtomicReference.get());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getHeader("x-custom-header-return"), "custom-header");
    }

    @Test(description = "Test the behavior of client connector when auto read is false.")
    public void testReadNextFrame() throws Throwable {
        WsClientConnectorConfig configuration = new WsClientConnectorConfig(url);
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        configuration.setAutoRead(false);
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
        ClientHandshakeFuture handshakeFuture = handshake(connectorListener);

        CountDownLatch handshakeSuccessLatch = new CountDownLatch(1);
        AtomicReference<WebSocketConnection> webSocketConnectionAtomicReference = new AtomicReference<>();
        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>();
        handshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                webSocketConnectionAtomicReference.set(webSocketConnection);
                handshakeSuccessLatch.countDown();
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                log.error(t.getMessage());
                throwableAtomicReference.set(t);
                handshakeSuccessLatch.countDown();
            }
        });
        handshakeSuccessLatch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);

        Assert.assertNull(throwableAtomicReference.get());

        // All sent messages will get echoed back.
        WebSocketConnection webSocketConnection = webSocketConnectionAtomicReference.get();
        int noOfMsgs = 10;
        String testMsgArray[] = new String[noOfMsgs];
        for (int i = 0; i < noOfMsgs; i++) {
            testMsgArray[i] = "Test Message " + i;
            webSocketConnection.pushText(testMsgArray[i]).sync();
        }

        String textReceived = null;
        try {
            textReceived = connectorListener.getReceivedTextToClient();
            Assert.fail("Expected exception");
        } catch (NoSuchElementException ex) {
            Assert.assertTrue(true, "Expected exception thrown");
        }
        Assert.assertNull(textReceived);

        for (String testMsg: testMsgArray) {
            CountDownLatch latch = new CountDownLatch(1);
            connectorListener.setCountDownLatch(latch);
            webSocketConnection.readNextFrame();
            latch.await(latchWaitTimeInSeconds, TimeUnit.SECONDS);
            textReceived = connectorListener.getReceivedTextToClient();

            Assert.assertEquals(textReceived, testMsg);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException, InterruptedException {
        remoteServer.stop();
    }

    private ClientHandshakeFuture handshake(WebSocketConnectorListener connectorListener) {
        return clientConnector.connect(connectorListener);
    }
}
