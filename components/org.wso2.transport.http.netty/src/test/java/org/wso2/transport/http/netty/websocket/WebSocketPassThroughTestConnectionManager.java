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

import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Session manager for WebSocket pass through test
 */
public class WebSocketPassThroughTestConnectionManager {

    private static final WebSocketPassThroughTestConnectionManager
            SESSION_MANAGER = new WebSocketPassThroughTestConnectionManager();

    // Map <serverSessionID, clientSession>
    private final ConcurrentMap<String, WebSocketConnection> serverKeyConnectionMap = new ConcurrentHashMap<>();
    // Map <clientSessionID, serverSession>
    private final ConcurrentMap<String, WebSocketConnection> clientKeyConnectionMap = new ConcurrentHashMap<>();

    private WebSocketPassThroughTestConnectionManager() {
    }

    public static WebSocketPassThroughTestConnectionManager getInstance() {
        return SESSION_MANAGER;
    }

    public void interRelateSessions(WebSocketConnection serverConnection, WebSocketConnection clientConnection) {
        serverKeyConnectionMap.put(serverConnection.getId(), clientConnection);
        clientKeyConnectionMap.put(clientConnection.getId(), serverConnection);
    }

    public WebSocketConnection getServerConnection(WebSocketConnection clientConnection) {
        return clientKeyConnectionMap.get(clientConnection.getId());
    }

    public WebSocketConnection getClientConnection(WebSocketConnection serverConnection) {
        return serverKeyConnectionMap.get(serverConnection.getId());
    }

}
