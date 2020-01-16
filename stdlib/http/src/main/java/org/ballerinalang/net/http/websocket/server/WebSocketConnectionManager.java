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

package org.ballerinalang.net.http.websocket.server;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains a map of connectionId and ConnectionInfo objects of a successfully established connection.
 */
public class WebSocketConnectionManager {

    private final Map<String, WebSocketConnectionInfo> wsConnectionsMap = new ConcurrentHashMap<>();

    public WebSocketConnectionInfo getConnectionInfo(String connectionID) {
        return wsConnectionsMap.get(connectionID);
    }

    public void addConnection(String connectionID, WebSocketConnectionInfo wsConnection) {
        wsConnectionsMap.put(connectionID, wsConnection);
    }

    public WebSocketConnectionInfo removeConnectionInfo(String connectionID) {
        return wsConnectionsMap.remove(connectionID);
    }
}
