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

package org.wso2.carbon.transport.http.netty.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.websocket.WebSocketRemoteServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * Test cases for the WebSocket Client implementation.
 */
public class WebSocketClientTestCase extends WeSocketTestCase {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientTestCase.class);

    private HttpWsConnectorFactoryImpl httpConnectorFactory = new HttpWsConnectorFactoryImpl();
    private final String url = String.format("ws://%s:%d/%s", "localhost",
                                             TestUtil.TEST_REMOTE_WS_SERVER_PORT, "websocket");
    private final int threadSleepTime = 100;
    private WsClientConnectorConfig configuration = new WsClientConnectorConfig(url);
    private WebSocketClientConnector clientConnector;
    private WebSocketRemoteServer remoteServer = new WebSocketRemoteServer(TestUtil.TEST_REMOTE_WS_SERVER_PORT,
                                                                           "xml, json");

    @BeforeClass
    public void setup() throws InterruptedException, ClientConnectorException {
        remoteServer.run();
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
    }

    @Test(priority = 1, description = "Test the WebSocket handshake and sending and receiving text messages.")
    public void testTextReceived() throws ClientConnectorException, InterruptedException, IOException {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        HandshakeFuture handshakeFuture = handshake(connectorListener);
        handshakeFuture.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                try {
                    String text = "testText";
                    session.getBasicRemote().sendText(text);
                    assertWebSocketClientTextMessage(connectorListener, text);
                    shutDownClient(session);
                } catch (IOException | InterruptedException e) {
                    Assert.assertTrue(false, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                Assert.assertTrue(false, t.getMessage());
            }
        });
    }

    @Test(priority = 2, description = "Test binary message sending and receiving.")
    public void testBinaryReceived() throws ClientConnectorException, InterruptedException, IOException {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        HandshakeFuture handshakeFuture = handshake(connectorListener);
        handshakeFuture.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                try {
                    byte[] bytes = {1, 2, 3, 4, 5};
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    session.getBasicRemote().sendBinary(buffer);
                    assertWebSocketClientBinaryMessage(connectorListener, buffer);
                    shutDownClient(session);
                } catch (IOException | InterruptedException e) {
                    Assert.assertTrue(false, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                Assert.assertTrue(false, t.getMessage());
            }
        });
    }

    @Test(priority = 3, description = "Test ping pong messaging.")
    public void testPingPong() throws ClientConnectorException, InterruptedException, IOException {
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        HandshakeFuture handshakeFuture = handshake(connectorListener);
        handshakeFuture.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                try {
                    byte[] bytes = {1, 2, 3, 4, 5};
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    session.getBasicRemote().sendPing(buffer);
                    Thread.sleep(threadSleepTime);
                    assertWebSocketClientPongMessage(connectorListener);
                    shutDownClient(session);
                } catch (IOException | InterruptedException e) {
                    Assert.assertTrue(false, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                Assert.assertTrue(false, t.getMessage());
            }
        });
    }

    @Test(priority = 4, description = "Test multiple clients handling, sending and receiving text messages for them.")
    public void testMultipleClients() throws ClientConnectorException, InterruptedException, IOException {
        WebSocketTestClientConnectorListener connectorListener1 = new WebSocketTestClientConnectorListener();
        String text1 = "testText1";
        String text2 = "testText2";
        HandshakeFuture handshakeFuture1 = handshake(connectorListener1);
        handshakeFuture1.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                try {
                    session.getBasicRemote().sendText(text1);
                    assertWebSocketClientTextMessage(connectorListener1, text1);
                    shutDownClient(session);
                } catch (IOException | InterruptedException e) {
                    Assert.assertTrue(false, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                Assert.assertTrue(false, t.getMessage());
            }
        });

        WebSocketTestClientConnectorListener connectorListener2 = new WebSocketTestClientConnectorListener();
        HandshakeFuture handshakeFuture2 = handshake(connectorListener2);
        handshakeFuture2.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                try {
                    session.getBasicRemote().sendText(text2);
                    assertWebSocketClientTextMessage(connectorListener2, text2);

                    session.getBasicRemote().sendText(text1);
                    assertWebSocketClientTextMessage(connectorListener2, text1);

                    shutDownClient(session);
                } catch (IOException | InterruptedException e) {
                    Assert.assertTrue(false, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                Assert.assertTrue(false, t.getMessage());
            }
        });
    }

    @Test(priority = 5, description = "Test the idle timeout for WebSocket")
    public void testIdleTimeout() throws ClientConnectorException, InterruptedException, IOException {
        configuration.setIdleTimeoutInMillis(3000);
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        HandshakeFuture handshakeFuture = handshake(connectorListener);
        handshakeFuture.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                try {
                    Thread.sleep(5000);
                    Assert.assertTrue(connectorListener.isIdleTimeout());
                    session.close();
                } catch (InterruptedException | IOException e) {
                    Assert.assertTrue(false, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                Assert.assertTrue(false, t.getMessage());
            }
        });
    }

    @Test(priority = 6, description = "Test the sub protocol negotiation with the remote server")
    public void testSubProtocolNegotiation()  {

        // Try with a matching sub protocol.
        String[] subProtocolsSuccess = {"xmlx", "json"};
        configuration.setSubProtocols(subProtocolsSuccess);
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
        WebSocketTestClientConnectorListener connectorListenerSuccess = new WebSocketTestClientConnectorListener();
        HandshakeFuture handshakeFutureSuccess = handshake(connectorListenerSuccess);
        handshakeFutureSuccess.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                Assert.assertEquals(session.getNegotiatedSubprotocol(), "json");
            }

            @Override
            public void onError(Throwable t) {
                Assert.assertTrue(false, "Handshake failed: " + t.getMessage());
            }
        });

        // Try with unmatching sub protocol
        String[] subProtocolsFail = {"xmlx", "jsonx"};
        configuration.setSubProtocols(subProtocolsFail);
        clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
        WebSocketTestClientConnectorListener connectorListenerFail = new WebSocketTestClientConnectorListener();
        HandshakeFuture handshakeFutureFail = handshake(connectorListenerFail);
        handshakeFutureFail.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                Assert.assertFalse(true, "Should not negotiate");
            }

            @Override
            public void onError(Throwable t) {
                Assert.assertTrue(true, "Handshake failed: " + t.getMessage());
            }
        });
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException, InterruptedException {
        remoteServer.stop();
    }

    private HandshakeFuture handshake(WebSocketConnectorListener connectorListener) {
        return clientConnector.connect(connectorListener);
    }

    private void shutDownClient(Session session) throws IOException {
        session.close(new CloseReason(
                () -> 1000,
                "Normal Closure"
        ));
    }
}
