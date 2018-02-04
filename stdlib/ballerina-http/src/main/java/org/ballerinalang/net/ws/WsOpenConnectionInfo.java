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

package org.ballerinalang.net.ws;

import org.ballerinalang.model.values.BStruct;

import java.util.Map;

/**
 * This class represent already opened WebSocket connection. Which include all necessary details needed after for
 * dispatching after a successful handshake.
 */
public class WsOpenConnectionInfo {

    private final WebSocketService webSocketService;
    private final BStruct wsConnection;
    private final Map<String, String> varialbles;

    public WsOpenConnectionInfo(WebSocketService webSocketService, BStruct wsConnection,
                                Map<String, String> varialbles) {
        this.webSocketService = webSocketService;
        this.wsConnection = wsConnection;
        this.varialbles = varialbles;
    }

    public WebSocketService getService() {
        return webSocketService;
    }

    public BStruct getWsConnection() {
        return wsConnection;
    }

    public Map<String, String> getVarialbles() {
        return varialbles;
    }
}
