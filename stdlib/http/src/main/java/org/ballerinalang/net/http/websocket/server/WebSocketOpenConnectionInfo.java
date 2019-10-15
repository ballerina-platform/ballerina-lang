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

package org.ballerinalang.net.http.websocket.server;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

/**
 * This class represent already opened WebSocket connection. Which include all necessary details needed after for
 * dispatching after a successful handshake.
 */
public class WebSocketOpenConnectionInfo {

    private final WebSocketService webSocketService;
    private final ObjectValue webSocketCaller;
    private final WebSocketConnection webSocketConnection;
    private String aggregateString = "";

    public WebSocketOpenConnectionInfo(WebSocketService webSocketService, WebSocketConnection webSocketConnection,
                                       ObjectValue webSocketCaller) {
        this.webSocketService = webSocketService;
        this.webSocketConnection = webSocketConnection;
        this.webSocketCaller = webSocketCaller;
    }

    public WebSocketService getService() {
        return webSocketService;
    }

    public ObjectValue getWebSocketCaller() {
        return webSocketCaller;
    }

    public WebSocketConnection getWebSocketConnection() throws IllegalAccessException {
        if (webSocketConnection != null) {
            return webSocketConnection;
        } else {
            throw new IllegalAccessException(WebSocketConstants.THE_WEBSOCKET_CONNECTION_HAS_NOT_BEEN_MADE);
        }
    }

    public String getAggregateString() {
        return aggregateString;
    }

    public void appendAggregateString(String aggregateString) {
        this.aggregateString += aggregateString;
    }

    public void resetAggregateString() {
        this.aggregateString = "";
    }
}
