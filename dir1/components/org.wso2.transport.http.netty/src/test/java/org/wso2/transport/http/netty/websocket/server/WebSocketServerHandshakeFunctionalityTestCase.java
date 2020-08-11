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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.client.websocket.WebSocketTestClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test the expected functionality of server while handshaking with a client.
 */
public class WebSocketServerHandshakeFunctionalityTestCase {

    private static final int countdownLatchTimeout = 10;
    private DefaultHttpWsConnectorFactory httpConnectorFactory;
    private ServerConnector serverConnector;
    private WebSocketServerHandshakeFunctionalityListener listener;
    private ServerConnectorFuture serverConnectorFuture;

    @BeforeClass
    public void setup() throws InterruptedException {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost(Constants.LOCALHOST);
        listenerConfiguration.setPort(TestUtil.SERVER_CONNECTOR_PORT);
        listenerConfiguration.setWebSocketCompressionEnabled(true);
        httpConnectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = httpConnectorFactory.createServerConnector(TestUtil.getDefaultServerBootstrapConfig(),
                                                                     listenerConfiguration);
        serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setWebSocketConnectorListener(
                listener = new WebSocketServerHandshakeFunctionalityListener());
        serverConnectorFuture.sync();
    }

    @Test(description = "Check the successful handshake")
    public void testSuccessfulHandshake() throws URISyntaxException, InterruptedException {
        CountDownLatch serverHandshakeCountDownLatch = new CountDownLatch(1);
        listener.setHandshakeCompleteCountDownLatch(serverHandshakeCountDownLatch);
        WebSocketTestClient testClient = createClientAndHandshake("x-handshake", null);
        serverHandshakeCountDownLatch.await(TestUtil.WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);
        WebSocketConnection webSocketConnection = listener.getCurrentWebSocketConnection();

        Assert.assertNotNull(webSocketConnection);
        Assert.assertNotNull(webSocketConnection.getChannelId());
        Assert.assertNotNull(webSocketConnection.getHost()); // Host can be changed in different environments
        Assert.assertEquals(webSocketConnection.getPort(), TestUtil.SERVER_CONNECTOR_PORT);
        Assert.assertTrue(webSocketConnection.isOpen());
        Assert.assertFalse(webSocketConnection.isSecure());

        testClient.closeChannel();
    }

    @Test(description = "Check response for unsupported WebSocket version")
    public void testUnsupportedWebSocketVersion() throws UnirestException {
        String url = String.format("http://%s:%d/%s", TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT, "test");
        HttpResponse<String> response = Unirest.get(url)
                .header(HttpHeaderNames.UPGRADE.toString(), HttpHeaderValues.WEBSOCKET.toString())
                .header(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.UPGRADE.toString())
                .header(HttpHeaderNames.SEC_WEBSOCKET_VERSION.toString(), String.valueOf(15))
                .header("x-handshake", "true")
                .asString();
        Assert.assertEquals(response.getStatus(), 426);
        Assert.assertEquals(response.getStatusText(), "Upgrade Required");
    }

    @Test(description = "Tests if server responds with correct header for extension")
    public void testExtensionSupport() throws IOException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        URL url = URI.create(String.format("http://%s:%d/%s", TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT,
                                           "/test")).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("GET");
        urlConn.setRequestProperty("Connection", "Upgrade");
        urlConn.setRequestProperty("Upgrade", "websocket");
        urlConn.setRequestProperty("Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ==");
        urlConn.setRequestProperty("Sec-WebSocket-Version", "13");

        String deflateHeader = "permessage-deflate";
        urlConn.setRequestProperty("Sec-WebSocket-Extensions", deflateHeader);
        urlConn.setRequestProperty("x-handshake", "true");

        Assert.assertEquals(urlConn.getResponseCode(), 101);
        Assert.assertEquals(urlConn.getResponseMessage(), "Switching Protocols");
        String header = urlConn.getHeaderField("Sec-WebSocket-Extensions");
        Assert.assertNotNull(header);
        Assert.assertEquals(header, deflateHeader);

        urlConn.disconnect();
    }


    @Test
    public void testExtensionNotSupportedByClient() throws URISyntaxException, InterruptedException {
        CountDownLatch handshakeCompleteCountDownLatch = new CountDownLatch(1);
        listener.setHandshakeCompleteCountDownLatch(handshakeCompleteCountDownLatch);
        WebSocketTestClient testClient = createClientAndHandshake("x-handshake", null);
        handshakeCompleteCountDownLatch.await(countdownLatchTimeout, TimeUnit.SECONDS);
        testClient.sendFrameWithRSV("Hello");
        WebSocketConnection webSocketConnection = listener.getCurrentWebSocketConnection();

        Assert.assertNotNull(webSocketConnection);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        testClient.setCountDownLatch(countDownLatch);
        webSocketConnection.readNextFrame();
        countDownLatch.await(countdownLatchTimeout, TimeUnit.SECONDS);
        CloseWebSocketFrame closeFrame = testClient.getReceivedCloseFrame();

        Assert.assertNotNull(closeFrame);
        Assert.assertEquals(closeFrame.statusCode(), 1002);

        testClient.closeChannel();
    }


    @Test(description = "Check whether the correct sub protocol is chosen by the server with the given sequence.")
    public void testSuccessfulSubProtocolNegotiation() throws URISyntaxException, InterruptedException {
        WebSocketTestClient testClient =
                createClientAndHandshake("x-negotiate-sub-protocols", "dummy1, xml, dummy2, json");

        Assert.assertEquals(testClient.getHandshaker().actualSubprotocol(), "xml");

        testClient.closeChannel();
    }

    @Test(description = "Check whether no any sub protocol is negotiated when unsupported sub protocols are requested.",
          expectedExceptions = WebSocketHandshakeException.class,
          expectedExceptionsMessageRegExp = "Invalid subprotocol. Actual: null. Expected one of: dummy1, dummy2")
    public void testFailSubProtocolNegotiation() throws URISyntaxException, InterruptedException {
        createClientAndHandshake("x-negotiate-sub-protocols", "dummy1, dummy2");
    }

    @Test(description = "Check the capability of sending custom headers in handshake response.")
    public void testHandshakeWithCustomHeader() throws URISyntaxException, InterruptedException {
        WebSocketTestClient testClient = createClientAndHandshake("x-send-custom-header", null);

        Assert.assertEquals(testClient.getHandshakeResponse().headers().get("x-custom-header"), "custom-header-value");

        testClient.closeChannel();
    }

    @Test(description = "Check whether no any sub protocol is negotiated when unsupported sub protocols are requested.",
          expectedExceptions = WebSocketHandshakeException.class,
          expectedExceptionsMessageRegExp = "Invalid handshake response getStatus: 404 Not Found")
    public void testCancelHandshake() throws URISyntaxException, InterruptedException {
        WebSocketTestClient testClient = new WebSocketTestClient();
        testClient.handshake();

        testClient.closeChannel();
    }

    @Test(description = "Check if an error is returned if handshake is called after cancelling the handshake.")
    public void testCancelAndHandshake() throws URISyntaxException, InterruptedException {
        CountDownLatch handshakeCompleteCountDownLatch = new CountDownLatch(1);
        listener.setHandshakeCompleteCountDownLatch(handshakeCompleteCountDownLatch);
        try {
            createClientAndHandshake("x-cancel-and-handshake", null);
        } catch (WebSocketHandshakeException ex) {
            //Ignore
        }
        handshakeCompleteCountDownLatch.await(countdownLatchTimeout, TimeUnit.SECONDS);
        Throwable error = listener.getHandshakeError();
        Assert.assertTrue(error instanceof IllegalAccessException);
        Assert.assertEquals(error.getMessage(), "Handshake is already cancelled.");
    }

    @Test
    public void testReadNextFrame() throws URISyntaxException, InterruptedException {
        CountDownLatch handshakeCompleteCountDownLatch = new CountDownLatch(1);
        listener.setHandshakeCompleteCountDownLatch(handshakeCompleteCountDownLatch);
        WebSocketTestClient testClient = createClientAndHandshake("x-wait-for-frame-read", null);
        handshakeCompleteCountDownLatch.await(countdownLatchTimeout, TimeUnit.SECONDS);
        String[] testMsgArray = sendTextMessages(testClient, 10);
        WebSocketConnection webSocketConnection = listener.getCurrentWebSocketConnection();

        Assert.assertNotNull(webSocketConnection);

        for (String testMsg : testMsgArray) {
            Assert.assertEquals(readNextTextFrame(testClient, webSocketConnection), testMsg);
        }

        testClient.closeChannel();
    }

    @Test(description = "Test connection closure due to idle timeout in server.")
    public void testIdleTimeout() throws URISyntaxException, InterruptedException {
        WebSocketTestClient testClient = createClientAndHandshake("x-set-connection-timeout", null);
        CloseWebSocketFrame closeFrame = getReceivedCloseFrame(testClient);

        Assert.assertNotNull(closeFrame);
        Assert.assertEquals(closeFrame.statusCode(), 1001);
        Assert.assertEquals(closeFrame.reasonText(), "Connection Idle Timeout");

        testClient.sendCloseFrame(closeFrame.statusCode(), null).closeChannel();
        testClient.closeChannel();
    }


    @Test(description = "WebSocket server sends 400 Bad Request if a createClientAndHandshake request is " +
            "received with other than GET method")
    public void testHandshakeWithPostMethod() throws IOException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        URL url = URI.create(String.format("http://%s:%d/%s", TestUtil.TEST_HOST, TestUtil.SERVER_CONNECTOR_PORT,
                                           "/websocket")).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setRequestProperty("Connection", "Upgrade");
        urlConn.setRequestProperty("Upgrade", "websocket");
        urlConn.setRequestProperty("Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ==");
        urlConn.setRequestProperty("Sec-WebSocket-Version", "13");

        Assert.assertEquals(urlConn.getResponseCode(), 400);
        Assert.assertEquals(urlConn.getResponseMessage(), "Bad Request");
        Assert.assertNull(urlConn.getHeaderField("sec-websocket-accept"));

        urlConn.disconnect();
    }

    @Test(priority = 1, enabled = false)
    public void testListenerNotSetInMessageReading() throws URISyntaxException, InterruptedException {
        CountDownLatch serverHandshakeCountDownLatch = new CountDownLatch(1);
        listener.setHandshakeCompleteCountDownLatch(serverHandshakeCountDownLatch);
        WebSocketTestClient testClient = createClientAndHandshake("x-handshake", null);
        testClient.handshake();
        serverConnectorFuture.setWebSocketConnectorListener(null);
        serverHandshakeCountDownLatch.await(TestUtil.WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);
        listener.getCurrentWebSocketConnection().startReadingFrames();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        testClient.setCountDownLatch(countDownLatch);
        testClient.sendText("hi");
        countDownLatch.await(TestUtil.WEBSOCKET_TEST_IDLE_TIMEOUT, TimeUnit.SECONDS);
        CloseWebSocketFrame closeWebSocketFrame = testClient.getReceivedCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1011);
        Assert.assertEquals(closeWebSocketFrame.reasonText(), "Encountered an unexpected condition");

        testClient.closeChannel();
    }

    @Test(priority = 2,
          expectedExceptions = WebSocketHandshakeException.class,
          expectedExceptionsMessageRegExp = "Invalid handshake response getStatus: 500 Internal Server Error",
          enabled = false)
    public void testListenerNotSetInHandshake() throws URISyntaxException, InterruptedException {
        serverConnectorFuture.setWebSocketConnectorListener(null);
        createClientAndHandshake("x-handshake", null);
    }

    @AfterClass
    public void cleanup() throws InterruptedException {
        serverConnector.stop();
        httpConnectorFactory.shutdown();
    }

    private String readNextTextFrame(WebSocketTestClient testClient, WebSocketConnection webSocketConnection)
            throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        testClient.setCountDownLatch(countDownLatch);
        webSocketConnection.readNextFrame();
        countDownLatch.await(countdownLatchTimeout, TimeUnit.SECONDS);
        return testClient.getTextReceived();
    }

    private String[] sendTextMessages(WebSocketTestClient testClient, int noOfMessages) throws InterruptedException {
        String[] testMsgArray = new String[noOfMessages];
        for (int i = 0; i < noOfMessages; i++) {
            String testMsg = "testMessage" + i;
            testMsgArray[i] = testMsg;
            testClient.sendText(testMsg);
        }
        return testMsgArray;
    }

    private CloseWebSocketFrame getReceivedCloseFrame(WebSocketTestClient testClient) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        testClient.setCountDownLatch(countDownLatch);
        countDownLatch.await(countdownLatchTimeout, TimeUnit.SECONDS);
        return testClient.getReceivedCloseFrame();
    }

    private WebSocketTestClient createClientAndHandshake(String commandHeader, String subProtocols)
            throws URISyntaxException, InterruptedException {
        Map<String, String> headers = new HashMap<>();
        headers.put(commandHeader, "true");
        WebSocketTestClient testClient = new WebSocketTestClient(subProtocols, headers);
        testClient.handshake();
        return testClient;
    }
}
