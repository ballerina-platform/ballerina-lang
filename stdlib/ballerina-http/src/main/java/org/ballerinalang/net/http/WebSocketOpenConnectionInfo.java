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

package org.ballerinalang.net.http;

import org.ballerinalang.model.values.BStruct;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

/**
 * This class represent already opened WebSocket connection. Which include all necessary details needed after for
 * dispatching after a successful handshake.
 */
public class WebSocketOpenConnectionInfo {

    private final WebSocketService webSocketService;
    private final BStruct webSocketEndpoint;
    private boolean readingStarted = false;

    public WebSocketOpenConnectionInfo(WebSocketService webSocketService, BStruct webSocketEndpoint) {
        this.webSocketService = webSocketService;
        this.webSocketEndpoint = webSocketEndpoint;
    }

    public WebSocketService getService() {
        return webSocketService;
    }

    public BStruct getWebSocketEndpoint() {
        return webSocketEndpoint;
    }

    public WebSocketConnection getWebSocketConnection() {
        BStruct webSocketConnector = (BStruct) webSocketEndpoint.getRefField(1);
        return (WebSocketConnection) webSocketConnector
                .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION);
    }

    public void setReadingStarted(boolean readingStarted) {
        this.readingStarted = readingStarted;
    }

    public boolean isReadingStarted() {
        return readingStarted;
    }
}
