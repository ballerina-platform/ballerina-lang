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

import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * WebSocket test class for WebSocket Connector Listener.
 */
public class WebSocketTestServerConnectorListener implements WebSocketConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketTestServerConnectorListener.class);

    private static final String PING = "ping";
    private static final String CLOSE_FORCEFULLY = "close-forcefully";
    private static final String CLOSE_AND_WAIT = "send-and-wait";
    private CountDownLatch returnFutureLatch;
    private CountDownLatch methodDoneLatch;
    private ChannelFuture closeFuture;
    private Throwable currentError;


    public WebSocketTestServerConnectorListener() {
    }

    public void setReturnFutureLatch(CountDownLatch returnFutureLatch) {
        this.returnFutureLatch = returnFutureLatch;
    }

    public ChannelFuture getCloseFuture() {
        if (closeFuture == null) {
            throw new IllegalStateException("Cannot investigate null close future");
        }
        return closeFuture;
    }

    public Throwable getCurrentError() {
        return currentError;
    }

    public void setMethodDoneLatch(CountDownLatch methodDoneLatch) {
        this.methodDoneLatch = methodDoneLatch;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        ServerHandshakeFuture future = webSocketHandshaker.handshake(null, 3000);
        future.setHandshakeListener(new ServerHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection) {
                webSocketConnection.startReadingFrames();
            }

            @Override
            public void onError(Throwable throwable) {
                LOG.error(throwable.getMessage());
                Assert.fail("Error: " + throwable.getMessage());
            }
        });
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        WebSocketConnection webSocketConnection = textMessage.getWebSocketConnection();
        String receivedTextToClient = textMessage.getText();
        LOG.debug("text: " + receivedTextToClient);
        switch (receivedTextToClient) {
            case PING:
                webSocketConnection.ping(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}));
                break;
            case CLOSE_FORCEFULLY:
                closeFuture = webSocketConnection.terminateConnection();
                handleCloseFuture(returnFutureLatch, closeFuture);
                break;
            case CLOSE_AND_WAIT:
                closeFuture = webSocketConnection.initiateConnectionClosure(1001, "Going away");
                handleCloseFuture(returnFutureLatch, closeFuture);
                break;
            default:
                webSocketConnection.pushText(receivedTextToClient);
        }
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        WebSocketConnection webSocketConnection = binaryMessage.getWebSocketConnection();
        ByteBuffer receivedByteBufferToClient = binaryMessage.getByteBuffer();
        LOG.debug("ByteBuffer: " + receivedByteBufferToClient);
        webSocketConnection.pushBinary(receivedByteBufferToClient);
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        if (controlMessage.getControlSignal() == WebSocketControlSignal.PING) {
            WebSocketConnection webSocketConnection = controlMessage.getWebSocketConnection();
            webSocketConnection.pong(controlMessage.getByteBuffer()).addListener(future -> {
                if (!future.isSuccess()) {
                    Assert.fail("Could not send the message. "
                            + future.cause().getMessage());
                }
            });
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {
        LOG.info("Close frame received: " + closeMessage.getCloseCode() + " , " + closeMessage.getCloseReason());
        closeMessage.getWebSocketConnection().finishConnectionClosure(closeMessage.getCloseCode(),
                closeMessage.getCloseReason());
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        //Do nothing
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        handleError(throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        WebSocketConnection webSocketConnection = controlMessage.getWebSocketConnection();
        ChannelFuture channelFuture = webSocketConnection.initiateConnectionClosure(1001, "Connection timeout");
        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                LOG.error("Error occurred while closing the connection: " + future.cause().getMessage());
            }
            if (channelFuture.channel().isOpen()) {
                channelFuture.channel().close();
            }
        });
    }

    private void handleError(Throwable throwable) {
        LOG.error(throwable.getMessage());
        currentError = throwable;
        countDownMethodDoneLatch();
    }

    private void handleCloseFuture(CountDownLatch returnFutureLatch, ChannelFuture closeFuture) {
        if (returnFutureLatch != null) {
            returnFutureLatch.countDown();
        }

        closeFuture.addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                LOG.error("Error occurred when closing the connection" + cause.getMessage());
            } else {
                LOG.info("Closing handshake successful");
            }
            if (closeFuture.channel().isOpen()) {
                closeFuture.channel().close().sync();
            }
            countDownMethodDoneLatch();
        });
    }

    private void countDownMethodDoneLatch() {
        if (methodDoneLatch != null) {
            methodDoneLatch.countDown();
        }
    }

}
