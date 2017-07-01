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

package org.ballerinalang.services.dispatchers.ws;

import org.ballerinalang.model.values.BConnector;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This manage all the connectors for Incoming and outgoing WebSocket messages.
 */
public class    ConnectorControllerRegistry {

    private static final ConnectorControllerRegistry CONNECTOR_MANAGER = new ConnectorControllerRegistry();

    // Map <serviceName, List<BConnector>>
    Map<String, List<BConnector>> serviceNameToConnectorListMap = new ConcurrentHashMap<>();
    // Map <BConnector, connectorID>
    Map<BConnector, String> connectorToConnectorIDMap = new ConcurrentHashMap<>();
    // Map <connectorID, ConnectorController>
    Map<String, ConnectorController> connectorIDToConnectorControllerMap = new ConcurrentHashMap<>();

    private ConnectorControllerRegistry() {
    }

    public static ConnectorControllerRegistry getInstance() {
        return CONNECTOR_MANAGER;
    }

    public void addConnectorController(BConnector bConnector, String connectorID,
                                       String parentServiceName, String clientServiceName, String url) {
        // Mapping connector ID to connector controller
        connectorIDToConnectorControllerMap.put(connectorID,
                                                new ConnectorController(bConnector, connectorID,
                                                                        parentServiceName, clientServiceName, url));

        // Mapping connector to connector ID
        connectorToConnectorIDMap.put(bConnector, connectorID);

        // Adding connector to the list of connectors against there parent service
        if (serviceNameToConnectorListMap.containsKey(parentServiceName)) {
            serviceNameToConnectorListMap.get(parentServiceName).add(bConnector);
        } else {
            List<BConnector> connectors = new LinkedList<>();
            connectors.add(bConnector);
            serviceNameToConnectorListMap.put(parentServiceName, connectors);
        }
    }

    public boolean contains(BConnector bConnector) {
        return connectorToConnectorIDMap.containsKey(bConnector);
    }

    public List<ConnectorController> getConnectorControllersForService(String serviceName) {
        if (!serviceNameToConnectorListMap.containsKey(serviceName)) {
            List<ConnectorController> tempList = new LinkedList<>();
            return tempList;
        } else {
            List<ConnectorController> connectorControllers = new LinkedList<>();
            for (BConnector connector: serviceNameToConnectorListMap.get(serviceName)) {
                connectorControllers.add(connectorIDToConnectorControllerMap.
                        get(connectorToConnectorIDMap.get(connector)));
            }
            return connectorControllers;
        }
    }

    public ConnectorController getConnectorController(BConnector bConnector) {
        return connectorIDToConnectorControllerMap.get(connectorToConnectorIDMap.get(bConnector));
    }

    public ConnectorController getConnectorController(String clientID) {
        String connectorID = Utils.getConnectorIDFromClientID(clientID);
        return connectorIDToConnectorControllerMap.get(connectorID);
    }

    public ConnectorController removeConnectorController(BConnector bConnector) {
        return connectorIDToConnectorControllerMap.remove(connectorToConnectorIDMap.remove(bConnector));
    }

}
