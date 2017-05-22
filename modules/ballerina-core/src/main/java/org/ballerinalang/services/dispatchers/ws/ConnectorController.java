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
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import javax.websocket.Session;

/**
 * This manages single client connector which include multiple connections per connector.
 */
public class ConnectorController {

    private int connectionSubCount = 0;
    private final String connectorID;
    private final BConnector bConnector;
    private final String parentServiceName;
    private final String clientServiceName;
    private final String remoteUrl;
    private final String parentClientID;
    private final Queue<String> connectionIDPool = new LinkedList<>();
    // Map<clientID, session>
    private final Map<String, Session> clientIDToSessionMap = new HashMap<>();

    public ConnectorController(BConnector bConnector, String connectorID, String parentServiceName,
                               String clientServiceName, String url) {
        this.bConnector = bConnector;
        this.connectorID = connectorID;
        this.parentServiceName = parentServiceName;
        this.clientServiceName = clientServiceName;
        this.remoteUrl = url;
        //Creating an initial connection
        this.parentClientID = Utils.generateWebSocketClientID(connectorID, connectionSubCount);
        initiateConnection(parentClientID);
        connectionSubCount = connectionSubCount + 1;
    }

    /**
     * Finds if the client exists.
     *
     * @param session The session to find whether the client exists.
     * @return true if client exists.
     */
    public boolean clientExists(Session session) {
        for (Map.Entry<String , Session> sessionEntry: clientIDToSessionMap.entrySet()) {
            if (sessionEntry.getValue().equals(session)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve the client ID from session.
     *
     * @param session to find the client ID.
     * @return the client id if exist. If session is null that means it is not a WebSocket endpoint. So returns the
     * parentClientID. If both those are not true then return null.
     */
    public String getClientID(Session session) {
        if (session == null) {
            return parentClientID;
        }
        for (Map.Entry<String , Session> sessionEntry: clientIDToSessionMap.entrySet()) {
            if (sessionEntry.getValue().equals(session)) {
                return sessionEntry.getKey();
            }
        }
        return null;
    }

    /**
     * Remove the connection from the connector manager.
     *
     * @param clientID ID of the client to be removed.
     */
    public void removeClient(String clientID) {
        clientIDToSessionMap.remove(clientID);
    }

    /**
     * Add connections to connector controller.
     *
     * @param session the session which should be added with unique identifier.
     */
    public void addConnection(Session session) {
        String clientID;
        if (connectionIDPool.isEmpty()) {
            connectionSubCount = connectionSubCount + 1;
            clientID = Utils.generateWebSocketClientID(connectorID, connectionSubCount);
        } else {
            clientID = connectionIDPool.remove();
        }
        initiateConnection(clientID);
        clientIDToSessionMap.put(clientID, session);
    }

    public Session getConnection(String clientID) {
        return clientIDToSessionMap.get(clientID);
    }

    public String getParentServiceName() {
        return parentServiceName;
    }

    public String getClientServiceName() {
        return clientServiceName;
    }

    public List<String> getAllClientIDs() {
        return clientIDToSessionMap.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public BConnector getBConnector() {
        return bConnector;
    }

    private void initiateConnection(String clientID) {
        ClientConnector clientConnector =
                BallerinaConnectorManager.getInstance().getClientConnector(Constants.PROTOCOL_WEBSOCKET);
        if (clientConnector == null) {
            throw new BallerinaException("Cannot initiate the connection since not found the connector support for WS");
        }
        ControlCarbonMessage controlCarbonMessage = new ControlCarbonMessage(
                org.wso2.carbon.messaging.Constants.CONTROL_SIGNAL_OPEN);
        controlCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientID);
        controlCarbonMessage.setProperty(Constants.TO, remoteUrl);
        try {
            clientConnector.send(controlCarbonMessage, null);
        } catch (ClientConnectorException e) {
            connectionSubCount = connectionSubCount - 1;
            throw new BallerinaException("Internal error occurred establishing connection for remote server");
        }
    }
}
