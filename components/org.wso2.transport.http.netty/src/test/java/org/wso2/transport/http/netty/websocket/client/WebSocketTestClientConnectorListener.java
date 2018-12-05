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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * WebSocket test class for WebSocket Connector Listener.
 */
public class WebSocketTestClientConnectorListener implements WebSocketConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketTestClientConnectorListener.class);

    private final Queue<WebSocketTextMessage> textQueue = new LinkedList<>();
    private final Queue<WebSocketBinaryMessage> binaryMessageQueue = new LinkedList<>();
    private final Queue<Throwable> errorsQueue = new LinkedList<>();
    private static final String PING = "ping";
    private WebSocketCloseMessage closeMessage = null;
    private boolean isPongReceived = false;
    private boolean isPingReceived = false;
    private boolean isIdleTimeout = false;
    private boolean isClose = false;
    private CountDownLatch latch;

    public WebSocketTestClientConnectorListener() {
    }

    public WebSocketTestClientConnectorListener(CountDownLatch latch) {
        this.latch = latch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.latch = countDownLatch;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        // Not applicable
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        if (PING.equals(textMessage.getText())) {
            try {
                textMessage.getWebSocketConnection().ping(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}))
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            errorsQueue.add(future.cause());
                        }
                    }).sync();
            } catch (InterruptedException e) {
                errorsQueue.add(e);
            }
        }
        textQueue.add(textMessage);
        countDownLatch();
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        binaryMessageQueue.add(binaryMessage);
        countDownLatch();
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        if (controlMessage.getControlSignal() == WebSocketControlSignal.PING) {
            isPingReceived = true;
            countDownLatch();
        }

        if (controlMessage.getControlSignal() == WebSocketControlSignal.PONG) {
            isPongReceived = true;
            countDownLatch();
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {
        isClose = true;
        this.closeMessage = closeMessage;
        countDownLatch();
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        //Do nothing
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        errorsQueue.add(throwable);
        LOG.error("Error handler received: " + throwable.getMessage());
        for (int i = 0; i < latch.getCount(); i++) {
            countDownLatch();
        }
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        isIdleTimeout = true;
        countDownLatch();
    }

    /**
     * Retrieve the latest text received to client.
     *
     * @return the latest text received to the client.
     */
    public WebSocketTextMessage getReceivedTextMessageToClient() throws Throwable {
        if (errorsQueue.isEmpty()) {
            return textQueue.remove();
        }
        throw errorsQueue.remove();
    }

    /**
     * Retrieve the latest {@link ByteBuffer} received to client.
     *
     * @return the latest {@link ByteBuffer} received to client.
     */
    public WebSocketBinaryMessage getReceivedBinaryMessageToClient() throws Throwable {
        if (errorsQueue.isEmpty()) {
            return binaryMessageQueue.remove();
        }
        throw errorsQueue.remove();
    }

    /**
     * Retrieve whether a pong is received client.
     *
     * @return true if a pong is received to client.
     */
    public boolean isPongReceived() throws Throwable {
        if (errorsQueue.isEmpty()) {
            boolean tmp = isPongReceived;
            isPongReceived = false;
            return tmp;
        }
        throw errorsQueue.remove();
    }

    /**
     * Retrieve whether a pong is received client.
     *
     * @return true if a pong is received to client.
     */
    public boolean isPingReceived() throws Throwable {
        if (errorsQueue.isEmpty()) {
            boolean tmp = isPingReceived;
            isPingReceived = false;
            return tmp;
        }
        throw errorsQueue.remove();
    }

    /**
     * Check whether any idle timeout triggered or not.
     *
     * @return true if idle timeout is triggered.
     */
    public boolean isIdleTimeout() throws Throwable {
       if (errorsQueue.isEmpty()) {
           boolean temp = isIdleTimeout;
           isIdleTimeout = false;
           return temp;
       }
       throw errorsQueue.remove();
    }

    /**
     * Return whether the connection is closed or not.
     *
     * @return true if the connection is closed.
     * @throws Throwable if any error occurred.
     */
    public boolean isClosed() throws Throwable {
        if (errorsQueue.isEmpty()) {
            boolean temp = isClose;
            isClose = false;
            return temp;
        }
        throw errorsQueue.remove();
    }

    /**
     * Return the close message.
     *
     * @return the close message received.
     */
    public WebSocketCloseMessage getCloseMessage() {
        return closeMessage;
    }

    /**
     * Retrieve the available error.
     *
     * @return the available error.
     */
    public Throwable getError() {
        return errorsQueue.remove();
    }

    private void countDownLatch() {
        if (latch != null) {
            latch.countDown();
        }
    }
}
