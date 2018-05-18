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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.websocket.WebSocketRemoteServer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Test cases for WebSocket client connection close scenarios.
 */
public class WebSocketClientCloseConnectionTestCase {

    private static final int latchCountDownInSecs = 10;
    private static final String URL = String.format("ws://%s:%d/%s", "localhost",
                                                    TestUtil.REMOTE_WS_SERVER_PORT, "websocket");
    private WebSocketRemoteServer remoteServer;
    private WebSocketClientConnector clientConnector;

    @BeforeClass
    public void setup() throws InterruptedException {
        remoteServer = new WebSocketRemoteServer(TestUtil.REMOTE_WS_SERVER_PORT);
        remoteServer.run();
        DefaultHttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
        WsClientConnectorConfig clientConnectorConfig = new WsClientConnectorConfig(URL);
        clientConnector = httpConnectorFactory.createWsClientConnector(clientConnectorConfig);
    }

    @Test
    public void testCloseForcefully() throws Throwable {
        WebSocketConnection webSocketConnection = getWebSocketConnection();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        webSocketConnection.closeForcefully().addListener(future -> countDownLatch.countDown());
        countDownLatch.await(latchCountDownInSecs, SECONDS);
        Assert.assertFalse(webSocketConnection.getSession().isOpen());
    }

    @Test
    public void testSendAndWaitForCloseFrameEchoBack() throws Throwable {
        WebSocketConnection webSocketConnection = getWebSocketConnection();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CloseFutureListener closeFutureListener = new CloseFutureListener(countDownLatch);
        webSocketConnection.initiateConnectionClosure(1001, "Going away")
                .addListener(closeFutureListener);
        countDownLatch.await(latchCountDownInSecs, SECONDS);
        ChannelFuture future = closeFutureListener.future();
        Assert.assertNotNull(future);
        Assert.assertNull(future.cause(), future.cause() != null ? future.cause().getMessage() : "");
        Assert.assertTrue(future.isDone());
        Assert.assertTrue(future.isSuccess());
    }

    @AfterClass
    public void cleanup() {
        remoteServer.stop();
    }

    private WebSocketConnection getWebSocketConnection() throws Throwable {
        ClientHandshakeFuture handshakeFuture = clientConnector.connect(new WebSocketCloseConnectionListener());
        AtomicReference<WebSocketConnection> webSocketConnectionAtomicReference = new AtomicReference<>();
        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>();
        CountDownLatch clientCountDownLatch = new CountDownLatch(1);
        handshakeFuture.setClientHandshakeListener(new ClientHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection, HttpCarbonResponse response) {
                webSocketConnectionAtomicReference.set(webSocketConnection);
                clientCountDownLatch.countDown();
            }

            @Override
            public void onError(Throwable t, HttpCarbonResponse response) {
                throwableAtomicReference.set(t);
                clientCountDownLatch.countDown();
            }
        });
        clientCountDownLatch.await(latchCountDownInSecs, SECONDS);
        if (throwableAtomicReference.get() != null) {
            throw throwableAtomicReference.get();
        }
        return webSocketConnectionAtomicReference.get();
    }

    /**
     * {@link ChannelFutureListener} to identify if there are any throwable.
     */
    private static class CloseFutureListener implements ChannelFutureListener {

        private final CountDownLatch countDownLatch;
        private ChannelFuture future;

        private CloseFutureListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            this.future = future;
            countDownLatch.countDown();
        }

        private ChannelFuture future() {
            return future;
        }
    }
}
