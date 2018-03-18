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
 */

package org.ballerinalang.net.http;

import org.ballerinalang.model.values.BConnector;

/**
 * This class represent already opened WebSocket connection. Which include all necessary details needed after for
 * dispatching after a successful handshake.
 */
public class WebSocketOpenConnectionInfo {

    private final WebSocketService webSocketService;
    private final BConnector wsConnection;

    public WebSocketOpenConnectionInfo(WebSocketService webSocketService, BConnector wsConnection) {
        this.webSocketService = webSocketService;
        this.wsConnection = wsConnection;
    }

    public WebSocketService getService() {
        return webSocketService;
    }

    public BConnector getWsConnection() {
        return wsConnection;
    }

}
