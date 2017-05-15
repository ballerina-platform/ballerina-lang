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

package org.ballerinalang.nativeimpl.actions.ws;

import org.ballerinalang.model.values.BConnector;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 * This manage all the connectors for Incoming and outgoing WebSocket messages.
 */
public class ConnectorControllerRegistry {

    private static final ConnectorControllerRegistry CONNECTOR_MANAGER = new ConnectorControllerRegistry();

    // Map <serviceName, List<BConnector>>
    Map<String, List<BConnector>> serviceToBConnectorListMap = new ConcurrentHashMap<>();
    // Map <BConnector, connectorID>
    Map<BConnector, String>  bConnectorToConnectorIDMap= new ConcurrentHashMap<>();
    // Map <connectorID, ConnectorController>
    Map<String, ConnectorController> connectorIDToConnectorControllerMap= new ConcurrentHashMap<>();

    private ConnectorControllerRegistry() {
    }

    public static ConnectorControllerRegistry getInstance() {
        return CONNECTOR_MANAGER;
    }

    public void addConnectorController(BConnector bConnector, String connectorID, String parentServiceName) {
        connectorIDToConnectorControllerMap.put(
                connectorID, new ConnectorController(bConnector, connectorID, parentServiceName));
        bConnectorToConnectorIDMap.put(bConnector, connectorID);

        if (serviceToBConnectorListMap.containsKey(parentServiceName)) {
            serviceToBConnectorListMap.get(parentServiceName).add(bConnector);
        } else {
            List<BConnector> connectors = new LinkedList<>();
            connectors.add(bConnector);
            serviceToBConnectorListMap.put(parentServiceName, connectors);
        }
    }

    public void addConnectionToConnectorController(String parentServiceName, Session session) {
        serviceToBConnectorListMap.get(parentServiceName).forEach(
                bConnector -> {
                    getConnectorController(bConnector).addConnection(session);
                }
        );
    }

    public ConnectorController removeConnectorController(BConnector bConnector) {
        return connectorIDToConnectorControllerMap.remove(bConnectorToConnectorIDMap.remove(bConnector));
    }

    public ConnectorController getConnectorController(BConnector bConnector) {
        return connectorIDToConnectorControllerMap.get(bConnectorToConnectorIDMap.get(bConnector));
    }
}
