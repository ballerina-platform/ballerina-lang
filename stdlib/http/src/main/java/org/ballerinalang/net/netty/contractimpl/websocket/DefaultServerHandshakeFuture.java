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

package org.ballerinalang.net.netty.contractimpl.websocket;

import org.ballerinalang.net.netty.contract.websocket.ServerHandshakeFuture;
import org.ballerinalang.net.netty.contract.websocket.ServerHandshakeListener;
import org.ballerinalang.net.netty.contract.websocket.WebSocketConnection;

/**
 * Implementation of WebSocket handshake future.
 */
public class DefaultServerHandshakeFuture implements
                                          org.ballerinalang.net.netty.contract.websocket.ServerHandshakeFuture {

    private Throwable throwable = null;
    private org.ballerinalang.net.netty.contract.websocket.WebSocketConnection webSocketConnection = null;
    private org.ballerinalang.net.netty.contract.websocket.ServerHandshakeListener serverHandshakeListener;

    @Override
    public ServerHandshakeFuture setHandshakeListener(ServerHandshakeListener serverHandshakeListener) {
        this.serverHandshakeListener = serverHandshakeListener;
        if (throwable != null) {
            serverHandshakeListener.onError(throwable);
        }
        if (webSocketConnection != null) {
            serverHandshakeListener.onSuccess(webSocketConnection);
        }
        return this;
    }

    @Override
    public void notifySuccess(WebSocketConnection webSocketConnection) {
        this.webSocketConnection = webSocketConnection;
        if (serverHandshakeListener == null || throwable != null) {
            return;
        }
        serverHandshakeListener.onSuccess(webSocketConnection);
    }

    @Override
    public void notifyError(Throwable throwable) {
        this.throwable = throwable;
        if (serverHandshakeListener == null) {
            return;
        }
        serverHandshakeListener.onError(throwable);
    }


}
