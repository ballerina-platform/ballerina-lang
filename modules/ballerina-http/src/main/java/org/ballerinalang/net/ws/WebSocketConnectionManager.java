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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.websocket.Session;

/**
 * This contains all the sessions which are received via a {@link org.wso2.carbon.messaging.CarbonMessage}.
 */
public class WebSocketConnectionManager {

    private static final WebSocketConnectionManager CONNECTION_MANAGER = new WebSocketConnectionManager();

    // Map <sessionId, WebSocketConnectionStruct>
    private final Map<String, BStruct> wsConnecionsMap = new ConcurrentHashMap<>();

    private WebSocketConnectionManager() {
    }

    public static WebSocketConnectionManager getInstance() {
        return CONNECTION_MANAGER;
    }

    public BStruct getConnection(String connectionID) {
        return wsConnecionsMap.get(connectionID);
    }

    public void addConnection(String connectionID, BStruct wsConnection) {
        wsConnecionsMap.put(connectionID, wsConnection);
    }
}
