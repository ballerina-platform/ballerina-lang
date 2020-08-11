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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.util.server.websocket.WebSocketRemoteServer;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.wso2.transport.http.netty.util.TestUtil.WEBSOCKET_REMOTE_SERVER_PORT;
import static org.wso2.transport.http.netty.util.TestUtil.WEBSOCKET_REMOTE_SERVER_URL;
import static org.wso2.transport.http.netty.util.TestUtil.WEBSOCKET_TEST_IDLE_TIMEOUT;

/**
 * Test cases for the WebSocket Client implementation.
 */
public class WebSocketClientHandshakeFunctionalityTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClientHandshakeFunctionalityTestCase.class);

    private DefaultHttpWsConnectorFactory httpConnectorFactory;
    private WebSocketRemoteServer remoteServer;

    @BeforeClass
    public void setup() throws InterruptedException {
        remoteServer = new WebSocketRemoteServer(WEBSOCKET_REMOTE_SERVER_PORT, "xml, json");
        remoteServer.run();
        httpConnectorFactory = new DefaultHttpWsConnectorFactory();
    }

    @Test(description = "Test the idle timeout for WebSocket")
    public void testIdleTimeout() throws Throwable {
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        configuration.setIdleTimeoutInMillis(3000);
        HandshakeResult result = connectAndGetHandshakeResult(configuration);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        result.getConnectorListener().setCountDownLatch(countDownLatch);
        countDownLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);

        Assert.assertNull(result.getThrowable());
        Assert.assertTrue(result.getConnectorListener().isIdleTimeout(), "Should reach idle timeout");
    }

    @Test(description = "Test the sub protocol negotiation with the remote server")
    public void testSubProtocolNegotiationSuccessful() throws InterruptedException {
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        String[] subProtocolsSuccess = {"xmlx", "json"};
        configuration.setSubProtocols(subProtocolsSuccess);
        HandshakeResult result = connectAndGetHandshakeResult(configuration);

        WebSocketConnection webSocketConnection = result.getWebSocketConnection();

        Assert.assertNull(result.getThrowable());
        Assert.assertNotNull(webSocketConnection);
        Assert.assertEquals(webSocketConnection.getNegotiatedSubProtocol(), "json");
        Assert.assertTrue(webSocketConnection.isOpen());
    }

    @Test(description = "Test the sub protocol negotiation with the remote server")
    public void testSubProtocolNegotiationFail() throws InterruptedException {
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        String[] subProtocolsFail = {"xmlx", "jsonx"};
        configuration.setSubProtocols(subProtocolsFail);
        HandshakeResult result = connectAndGetHandshakeResult(configuration);
        Throwable throwable = result.getThrowable();

        Assert.assertNull(result.getWebSocketConnection());
        Assert.assertNotNull(throwable);
        Assert.assertEquals(throwable.getMessage(), "Invalid subprotocol. Actual: null. Expected one of: xmlx,jsonx");
    }

    @Test(description = "Test the case when the compression is enabled")
    public void testCompressionEnabled() throws InterruptedException {
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        configuration.setWebSocketCompressionEnabled(true);
        HandshakeResult result = connectAndGetHandshakeResult(configuration);
        HttpCarbonResponse response = result.getHandshakeResponse();
        Assert.assertNotNull(response);
        String compressionHeader = response.getHeader(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS.toString());
        Assert.assertNotNull(compressionHeader);
        Assert.assertEquals(compressionHeader, "permessage-deflate");
    }

    @Test(description = "Test the case when the compression is disabled")
    public void testCompressionDisabled() throws InterruptedException {
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        configuration.setWebSocketCompressionEnabled(false);
        HandshakeResult result = connectAndGetHandshakeResult(configuration);
        HttpCarbonResponse response = result.getHandshakeResponse();
        Assert.assertNotNull(response);
        String compressionHeader = response.getHeader(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS.toString());
        Assert.assertNull(compressionHeader);
    }

    @Test(description = "Test the sub protocol negotiation with the remote server with 0 length of sub protocols")
    public void testConnectToServerWithoutSubProtocols() throws InterruptedException {
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        String[] zeroLengthSubProtocols = {};
        configuration.setSubProtocols(zeroLengthSubProtocols);
        HandshakeResult result = connectAndGetHandshakeResult(configuration);
        Throwable throwable = result.getThrowable();

        Assert.assertNotNull(result.getWebSocketConnection());
        Assert.assertNull(throwable);
        Assert.assertTrue(result.webSocketConnection.isOpen());
    }

    @Test(description = "Test whether client can send custom headers and receive.")
    public void testSendAndReceiveCustomHeaders() throws InterruptedException {
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        int maxHeadersCount = 3;
        String customHeaderKey = "x-custom-header-key-";
        String customHeaderValue = "x-custom-header-value-";

        // Adding headers using addHeader()
        configuration.addHeader(customHeaderKey + 0, customHeaderValue + 0);

        // Adding custom headers using addHeaders()
        Map<String, String> customHeaderMap = new HashMap<>();
        for (int i = 1; i < maxHeadersCount; i++) {
            customHeaderMap.put(customHeaderKey + i, customHeaderValue + i);
        }
        configuration.addHeaders(customHeaderMap);

        // Check all the headers are avaliable in the configuration
        for (int i = 0; i < maxHeadersCount; i++) {
            Assert.assertTrue(configuration.getHeaders().contains(customHeaderKey + i));
        }

        Assert.assertTrue(configuration.containsHeader(customHeaderKey + 0));

        HandshakeResult result = connectAndGetHandshakeResult(configuration);
        HttpCarbonResponse response = result.getHandshakeResponse();

        Assert.assertNull(result.getThrowable());
        Assert.assertNotNull(result.getWebSocketConnection());
        Assert.assertNotNull(response);

        // Check received custom headers
        String returnStr = "-return";
        for (int i = 0; i < maxHeadersCount; i++) {
            Assert.assertEquals(response.getHeader(customHeaderKey + i + returnStr), customHeaderValue + i + returnStr);
        }
    }

    @Test(description = "Test the behavior of client connector when auto read is false.")
    public void testReadNextFrame() throws Throwable {
        WebSocketClientConnectorConfig configuration = new WebSocketClientConnectorConfig(WEBSOCKET_REMOTE_SERVER_URL);
        configuration.setAutoRead(false);
        HandshakeResult result = connectAndGetHandshakeResult(configuration);

        Assert.assertNull(result.getThrowable());

        // All sent messages will get echoed back.
        WebSocketConnection webSocketConnection = result.getWebSocketConnection();
        String[] testMsgArray = sendTextMessages(webSocketConnection, 10);

        WebSocketTestClientConnectorListener connectorListener = result.getConnectorListener();
        String textReceived = null;
        try {
            textReceived = connectorListener.getReceivedTextMessageToClient().getText();
            Assert.fail("Expected exception");
        } catch (NoSuchElementException ex) {
            Assert.assertTrue(true, "Expected exception thrown");
        }
        Assert.assertNull(textReceived);

        for (String testMsg : testMsgArray) {
            Assert.assertEquals(readNextTextMsg(connectorListener, webSocketConnection), testMsg);
        }
    }

    @Test
    public void testInvalidUrl() throws InterruptedException {
        String invalidUrl = "myUrl";
        connectAndAssertInvalidUrl(invalidUrl);

        String urlWithWrongScheme = "http://localhost:9090/websocket";
        connectAndAssertInvalidUrl(urlWithWrongScheme);
    }

    private void connectAndAssertInvalidUrl(String url) throws InterruptedException {
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(url);
        HandshakeResult result = connectAndGetHandshakeResult(clientConnectorConfig);
        Throwable throwable = result.getThrowable();

        Assert.assertNull(result.getWebSocketConnection());
        Assert.assertNull(result.getHandshakeResponse());
        Assert.assertNotNull(throwable);
        Assert.assertTrue(throwable instanceof URISyntaxException);
        Assert.assertEquals(throwable.getMessage(), "WebSocket client supports only WS(S) scheme: " + url);
    }

    @Test
    public void testNonExistingValidUrl() throws InterruptedException {
        String nonExistingUrl = "ws://localhost:14900/websocket";
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(nonExistingUrl);
        HandshakeResult result = connectAndGetHandshakeResult(clientConnectorConfig);
        Throwable throwable = result.getThrowable();

        Assert.assertNull(result.getWebSocketConnection());
        Assert.assertNull(result.getHandshakeResponse());
        Assert.assertNotNull(throwable);
        Assert.assertEquals(throwable.getMessage(), "Connection refused: localhost/127.0.0.1:14900");
    }

    @Test
    public void testWssCallWithoutSslConfig() throws InterruptedException {
        String url = "wss://localhost:9090/websocket";
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(url);
        HandshakeResult result = connectAndGetHandshakeResult(clientConnectorConfig);
        Throwable throwable = result.getThrowable();

        Assert.assertNull(result.getWebSocketConnection());
        Assert.assertNull(result.getHandshakeResponse());
        Assert.assertNotNull(throwable);
        Assert.assertTrue(throwable instanceof IllegalArgumentException);
        Assert.assertEquals(throwable.getMessage(),
                "TrustStoreFile or TrustStorePassword not defined for HTTPS/WS scheme");
    }

    private String readNextTextMsg(WebSocketTestClientConnectorListener connectorListener,
                                   WebSocketConnection webSocketConnection) throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);
        connectorListener.setCountDownLatch(latch);
        webSocketConnection.readNextFrame();
        latch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);
        return connectorListener.getReceivedTextMessageToClient().getText();
    }

    private String[] sendTextMessages(WebSocketConnection webSocketConnection, int noOfMsgs)
            throws InterruptedException {
        String[] testMsgArray = new String[noOfMsgs];
        for (int i = 0; i < noOfMsgs; i++) {
            String testMsg = "Test Message " + i;
            testMsgArray[i] = testMsg;
            webSocketConnection.pushText(testMsg).sync();
        }
        return testMsgArray;
    }

    private HandshakeResult connectAndGetHandshakeResult(WebSocketClientConnectorConfig configuration)
            throws InterruptedException {
        WebSocketClientConnector clientConnector = httpConnectorFactory.createWsClientConnector(configuration);
        WebSocketTestClientConnectorListener connectorListener = new WebSocketTestClientConnectorListener();
        ClientHandshakeFuture handshakeFuture = handshake(clientConnector, connectorListener);

        CountDownLatch handshakeFutureLatch = new CountDownLatch(1);
        AtomicReference<WebSocketConnection> connectionAtomicReference = new AtomicReference<>();
        AtomicReference<HttpCarbonResponse> handshakeResponseAtomicReference = new AtomicReference<>();
        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>();
        handshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                connectionAtomicReference.set(webSocketConnection);
                handshakeResponseAtomicReference.set(response);
                handshakeFutureLatch.countDown();
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                LOG.error(t.getMessage());
                throwableAtomicReference.set(t);
                handshakeFutureLatch.countDown();
            }
        });
        handshakeFutureLatch.await(WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);
        return new HandshakeResult(connectionAtomicReference.get(), handshakeResponseAtomicReference.get(),
                                   throwableAtomicReference.get(), connectorListener);
    }

    private static class HandshakeResult {
        private final WebSocketConnection webSocketConnection;
        private final HttpCarbonResponse handshakeResponse;
        private final Throwable throwable;
        private final WebSocketTestClientConnectorListener connectorListener;

        private HandshakeResult(WebSocketConnection webSocketConnection, HttpCarbonResponse handshakeResponse,
                               Throwable throwable, WebSocketTestClientConnectorListener connectorListener) {
            this.webSocketConnection = webSocketConnection;
            this.handshakeResponse = handshakeResponse;
            this.throwable = throwable;
            this.connectorListener = connectorListener;
        }

        private WebSocketConnection getWebSocketConnection() {
            return webSocketConnection;
        }

        private Throwable getThrowable() {
            return throwable;
        }

        private WebSocketTestClientConnectorListener getConnectorListener() {
            return connectorListener;
        }

        private HttpCarbonResponse getHandshakeResponse() {
            return handshakeResponse;
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException, InterruptedException {
        remoteServer.stop();
        httpConnectorFactory.shutdown();
    }

    private ClientHandshakeFuture handshake(WebSocketClientConnector clientConnector,
            WebSocketConnectorListener connectorListener) {
        ClientHandshakeFuture clientHandshakeFuture = clientConnector.connect();
        clientHandshakeFuture.setWebSocketConnectorListener(connectorListener);
        return clientHandshakeFuture;
    }
}
