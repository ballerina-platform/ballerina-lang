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

package org.ballerinalang.nativeimpl.connectors.ws.connectormanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This manage all the connectors for Incoming and outgoing WebSocket messages.
 */
public class ConnectorRegistry {

    private static final ConnectorRegistry CONNECTOR_MANAGER = new ConnectorRegistry();

    Map<String, ConnectorController> connectorControllers = new ConcurrentHashMap<>();

    private ConnectorRegistry() {
    }

    public static ConnectorRegistry getInstance() {
        return CONNECTOR_MANAGER;
    }

    public void addConnectorController(String connectorID) {
        connectorControllers.put(connectorID, new ConnectorController(connectorID));
    }

    public ConnectorController removeConnectorManager(String connectorID) {
        return connectorControllers.remove(connectorID);
    }

    public ConnectorController getConnectorController(String connectorID) {
        return connectorControllers.get(connectorID);
    }
}
