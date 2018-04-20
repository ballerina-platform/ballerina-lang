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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * WebSocket test class for WebSocket Connector Listener.
 */
public class WebSocketTestClientConnectorListener implements WebSocketConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketTestClientConnectorListener.class);

    private final CountDownLatch latch;
    private final Queue<String> textQueue = new LinkedList<>();
    private final Queue<ByteBuffer> bufferQueue = new LinkedList<>();
    private final Queue<Throwable> errorsQueue = new LinkedList<>();
    private static final String PING = "ping";
    private WebSocketCloseMessage closeMessage = null;
    private boolean isPongReceived = false;
    private boolean isPingReceived = false;
    private boolean isIdleTimeout = false;
    private boolean isClose = false;

    public WebSocketTestClientConnectorListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onMessage(WebSocketInitMessage initMessage) {
        // Not applicable
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        if (PING.equals(textMessage.getText())) {
            try {
                textMessage.getChannelSession().getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}));
            } catch (IOException e) {
                errorsQueue.add(e);
            }
        }
        textQueue.add(textMessage.getText());
        latch.countDown();
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        bufferQueue.add(binaryMessage.getByteBuffer());
        latch.countDown();
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        if (controlMessage.getControlSignal() == WebSocketControlSignal.PING) {
            isPingReceived = true;
            latch.countDown();
        }

        if (controlMessage.getControlSignal() == WebSocketControlSignal.PONG) {
            isPongReceived = true;
            latch.countDown();
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {
        isClose = true;
        this.closeMessage = closeMessage;
        latch.countDown();
    }

    @Override
    public void onError(Throwable throwable) {
        errorsQueue.add(throwable);
        log.error("Error handler received: " + throwable.getMessage());
        for (int i = 0; i < latch.getCount(); i++) {
            latch.countDown();
        }
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        isIdleTimeout = true;
        latch.countDown();
    }

    /**
     * Retrieve the latest text received to client.
     *
     * @return the latest text received to the client.
     */
    public String getReceivedTextToClient() throws Throwable {
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
    public ByteBuffer getReceivedByteBufferToClient() throws Throwable {
        if (errorsQueue.isEmpty()) {
            return bufferQueue.remove();
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
}
