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

import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.util.concurrent.CountDownLatch;

public class WebSocketServerHandshakeFunctionalityListener implements WebSocketConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketServerHandshakeFunctionalityListener.class);

    private WebSocketConnection currentWebSocketConnection;
    private Throwable handshakeError;
    private CountDownLatch handshakeCompleteCountDownLatch;

    private static final String[] supportingSubProtocols = {"json", "xml"};

    public WebSocketConnection getCurrentWebSocketConnection() {
        return currentWebSocketConnection;
    }

    public Throwable getHandshakeError() {
        return handshakeError;
    }

    public void setHandshakeCompleteCountDownLatch(CountDownLatch handshakeCompleteCountDownLatch) {
        this.handshakeCompleteCountDownLatch = handshakeCompleteCountDownLatch;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        ServerHandshakeFuture handshakeFuture = null;
        if (getBooleanValueOfHeader(webSocketHandshaker, "x-negotiate-sub-protocols")) {
            handshakeFuture = webSocketHandshaker.handshake(supportingSubProtocols);
        } else if (getBooleanValueOfHeader(webSocketHandshaker, "x-send-custom-header")) {
            DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
            httpHeaders.add("x-custom-header", "custom-header-value");
            handshakeFuture = webSocketHandshaker.handshake(null, -1, httpHeaders);
        } else if (getBooleanValueOfHeader(webSocketHandshaker, "x-set-connection-timeout")) {
            handshakeFuture = webSocketHandshaker.handshake(null, 4000);
        } else if (getBooleanValueOfHeader(webSocketHandshaker, "x-wait-for-frame-read")) {
            handshakeFuture = webSocketHandshaker.handshake();
        } else if (getBooleanValueOfHeader(webSocketHandshaker, "x-handshake")) {
            handshakeFuture = webSocketHandshaker.handshake();
        } else if (getBooleanValueOfHeader(webSocketHandshaker, "x-cancel-and-handshake")) {
            webSocketHandshaker.cancelHandshake(404, "Not Found").addListener(future -> {
                if (!future.isSuccess() && future.cause() != null) {
                    LOG.error("Error canceling handshake", future.cause());
                }
            }).channel().close();
            handshakeFuture = webSocketHandshaker.handshake();
        } else {
            webSocketHandshaker.cancelHandshake(404, "Not Found").addListener(future -> {
                if (!future.isSuccess() && future.cause() != null) {
                    LOG.error("Error canceling handshake", future.cause());
                }
            }).channel().close();
        }

        if (handshakeFuture != null) {
            handshakeFuture.setHandshakeListener(new ServerHandshakeListener() {
                @Override
                public void onSuccess(WebSocketConnection webSocketConnection) {
                    currentWebSocketConnection = webSocketConnection;
                    completeHandshakeCountDown();
                }

                @Override
                public void onError(Throwable t) {
                    handshakeError = t;
                    LOG.error("Error in handshake: ", t);
                    completeHandshakeCountDown();
                }
            });
        }
    }

    private Boolean getBooleanValueOfHeader(WebSocketHandshaker webSocketHandshaker, String s) {
        return Boolean.valueOf(webSocketHandshaker.getHttpCarbonRequest().getHeader(s));
    }

    private void completeHandshakeCountDown() {
        if (handshakeCompleteCountDownLatch != null) {
            handshakeCompleteCountDownLatch.countDown();
            handshakeCompleteCountDownLatch = null;
        }
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        textMessage.getWebSocketConnection().pushText(textMessage.getText());
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {

    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {

    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {

    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        //Do nothing
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {

    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        controlMessage.getWebSocketConnection().initiateConnectionClosure(1001, "Connection Idle Timeout");
    }
}
