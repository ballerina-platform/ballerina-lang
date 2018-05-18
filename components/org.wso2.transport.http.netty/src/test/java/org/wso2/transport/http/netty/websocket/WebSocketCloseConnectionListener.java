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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.util.concurrent.CountDownLatch;

public class WebSocketCloseConnectionListener implements WebSocketConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketCloseConnectionListener.class);
    private CountDownLatch returnFutureLatch;
    private CountDownLatch closeDoneLatch;
    private ChannelFuture closeFuture;

    public void setReturnFutureLatch(CountDownLatch returnFutureLatch) {
        this.returnFutureLatch = returnFutureLatch;
    }

    public ChannelFuture getCloseFuture() {
        if (closeFuture == null) {
            throw new IllegalStateException("Cannot investigate null close future");
        }
        return closeFuture;
    }

    public void setCloseDoneLatch(CountDownLatch closeDoneLatch) {
        this.closeDoneLatch = closeDoneLatch;
    }

    @Override
    public void onMessage(WebSocketInitMessage initMessage) {
        if (initMessage.isServerMessage()) {
            initMessage.handshake().setHandshakeListener(new ServerHandshakeListener() {
                @Override
                public void onSuccess(WebSocketConnection webSocketConnection) {
                    webSocketConnection.startReadingFrames();
                }

                @Override
                public void onError(Throwable t) {
                    log.error("Error in handshake", t);
                }
            });
        } else {
            throw new IllegalStateException("Cannot have a init message in client");
        }
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        int statusCode = 1001;
        String reason = "Going away";
        WebSocketConnection webSocketConnection = textMessage.getWebSocketConnection();
        if ("close-forcefully".equals(textMessage.getText())) {
            closeFuture = webSocketConnection.closeForcefully();
        } else if ("send-and-wait".equals(textMessage.getText())) {
            closeFuture = webSocketConnection.initiateConnectionClosure(statusCode, reason);
        } else {
            log.error("Valid command not found to close the connection");
            return;
        }

        if (returnFutureLatch != null) {
            returnFutureLatch.countDown();
        }

        closeFuture.addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                log.error("Error occurred when closing the connection", cause);
            } else {
                log.info("Closing handshake successful");
            }
            if (closeFuture.channel().isOpen()) {
                closeFuture.channel().close().sync();
            }
            if (closeDoneLatch != null) {
                closeDoneLatch.countDown();
            }
        });
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        // Do nothing
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        // Do nothing
    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {
        // Do nothing
    }

    @Override
    public void onError(Throwable throwable) {
        // Do nothing
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        // Do nothing
    }
}
