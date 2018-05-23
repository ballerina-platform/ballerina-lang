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
 *
 */

package org.wso2.transport.http.netty.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebSocket connector listener for  the Protocol switch from HTTP to WebSocket test case.
 */
public class HttpToWsProtocolSwitchWebSocketMulthreadListener implements WebSocketConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(
            HttpToWsProtocolSwitchWebSocketMulthreadListener.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    public void onMessage(WebSocketInitMessage initMessage) {
        executor.execute(initMessage::handshake);
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
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
    public void onError(Throwable throwable) {
        handleError(throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
    }

    private void handleError(Throwable throwable) {
        logger.error(throwable.getMessage());
    }

}
