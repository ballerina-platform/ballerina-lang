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
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.websocket.Session;

/**
 * This contains all the sessions which are received via a {@link org.wso2.carbon.messaging.CarbonMessage}.
 */
public class WebSocketConnectionManager {

    // Map<serviceName, Map<sessionId, session>>
    private final Map<String, Map<String, Session>> broadcastSessions = new HashMap<>();
    // Map<groupName, Map<sessionId, session>>
    private final Map<String, Map<String, Session>> connectionGroups = new HashMap<>();
    // Map<NameToStoreConnection, Session>
    private final Map<String, Session> connectionStore = new HashMap<>();

    // Map <parentServiceName, Connector>
    private final Map<String, List<BConnector>> parentServiceToClientConnectorsMap = new HashMap<>();

    // Map<Connector, Map<serverSessionID, ClientSession>>
    private final Map<BConnector, Map<String, Session>> clientConnectorSessionsMap = new HashMap<>();
    // Map<serverSessionID, clientSessionList>
    private final Map<String, List<Session>> serverSessionToClientSessionsMap = new HashMap<>();

    private final HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();
    private static final WebSocketConnectionManager sessionManager = new WebSocketConnectionManager();

    private WebSocketConnectionManager() {
    }

    public static WebSocketConnectionManager getInstance() {
        return sessionManager;
    }

    /**
     * Adding new connection to the connection manager.
     * This adds the server session to the related maps and create necessary client connections to the client
     * connectors.
     *
     * @param service {@link ServiceInfo} of the service which session is related.
     * @param session Session of the connection.
     * @param carbonMessage carbon message received.
     */
    public synchronized void addServerSession(ServiceInfo service, Session session, CarbonMessage carbonMessage) {
        /*
            If service is in the parentServiceToClientConnectorsMap means it might have client connectors relates to
            the service. So for each client connector new connections should be created for remote server.
         */
        if (parentServiceToClientConnectorsMap.containsKey(service.getName())) {
            for (BConnector bConnector : parentServiceToClientConnectorsMap.get(service.getName())) {
                Session clientSession = initializeClientConnection(bConnector, carbonMessage);
                Session serverSession = (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
                clientConnectorSessionsMap.get(bConnector).put(serverSession.getId(), clientSession);
                addClinetSessionForServerSession(serverSession, clientSession);
            }
        }

        // Adding connection to broadcast group.
        addConnectionToBroadcast(service, session);
    }

    /**
     * Add {@link Session} to session the broadcast group of a given service.
     *
     * @param service {@link ServiceInfo} of the service.
     * @param session {@link Session} to add to the broadcast group.
     */
    private synchronized void addConnectionToBroadcast(ServiceInfo service, Session session) {
        if (broadcastSessions.containsKey(service.getName())) {
            broadcastSessions.get(service.getName()).put(session.getId(), session);
        } else {
            Map<String, Session> sessionMap = new HashMap<>();
            sessionMap.put(session.getId(), session);
            broadcastSessions.put(service.getName(), sessionMap);
        }
    }

    /**
     * Get a list of Sessions for broadcasting for a given service.
     *
     * @param service name of the service.
     * @return the list of sessions which are connected to a given service.
     */
    public synchronized List<Session> getBroadcastConnectionList(ServiceInfo service) {
        if (broadcastSessions.containsKey(service.getName())) {
            return broadcastSessions.get(service.getName()).entrySet().stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * Add {@link Session} to session the session group.
     *
     * @param groupName name of the group.
     * @param session {@link Session} to remove from the broadcast group.
     */
    public synchronized void addConnectionToGroup(String groupName, Session session) {
        if (connectionGroups.containsKey(groupName)) {
            connectionGroups.get(groupName).put(session.getId(), session);
        } else {
            Map<String, Session> sessionMap = new HashMap<>();
            sessionMap.put(session.getId(), session);
            connectionGroups.put(groupName, sessionMap);
        }
    }

    /**
     * Remove {@link Session} from a session group. This should be mostly called when a WebSocket connection is
     * Closed.
     *
     * @param groupName name of the broadcast.
     * @param session {@link Session} to remove from the group.
     * @return true if the connection name was found and connection is removed.
     */
    public synchronized boolean removeConnectionFromGroup(String groupName, Session session) {
        if (connectionGroups.containsKey(groupName)) {
            connectionGroups.get(groupName).remove(session.getId());
            return true;
        }
        return false;
    }

    /**
     * Remove the whole group from the groups map.
     *
     * @param groupName name of the group.
     * @return true if connection group name exists and connection group is removed successfully.
     */
    public synchronized boolean removeConnectionGroup(String groupName) {
        if (connectionGroups.containsKey(groupName)) {
            connectionGroups.remove(groupName);
            return true;
        }
        return false;
    }

    /**
     * Get the list of the connections which belongs to a specific group.
     *
     * @param groupName name of the connection group.
     * @return a list of connections belongs to the mentioned group name.
     */
    public synchronized List<Session> getConnectionGroup(String groupName) {
        if (connectionGroups.containsKey(groupName)) {
            return connectionGroups.get(groupName).entrySet().stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * Store connection with the name given by the user for future usages.
     *
     * @param connectionName name of the connection given by the user.
     * @param session {@link Session} to store.
     */
    public synchronized void storeConnection(String connectionName, Session session) {
        connectionStore.put(connectionName, session);
    }

    /**
     * Remove connection from the connection store.
     *
     * @param connectionName connection name which should be removed from the store.
     * @return true if connection name was found in connection store and removed successfully.
     */
    public synchronized boolean removeConnectionFromStore(String connectionName) {
        if (connectionStore.containsKey(connectionName)) {
            connectionStore.remove(connectionName);
            return true;
        }
        return false;
    }

    /**
     * Get the stored connection from the connection store.
     *
     * @param connectionName name of the connection which should be removed.
     * @return Session of the stored connection if connections is found else return null.
     */
    public synchronized Session getStoredConnection(String connectionName) {
        return connectionStore.get(connectionName);
    }

    private synchronized void addClinetSessionForServerSession(Session serverSession, Session clientSession) {
        String serverSessionID = serverSession.getId();
        if (serverSessionToClientSessionsMap.containsKey(serverSessionID)) {
            serverSessionToClientSessionsMap.get(serverSessionID).add(clientSession);
        } else {
            List<Session> clientSessions = new LinkedList<>();
            clientSessions.add(clientSession);
            serverSessionToClientSessionsMap.put(serverSessionID, clientSessions);
        }
    }

    /**
     * Add ballerina level client connector to the connection manager and initialize related maps.
     * <b>This method should be only used if the WebSocket parent service exists.</b>
     *
     * @param parentService {@link ServiceInfo} where the connector is created.
     * @param connector {@link BConnector} which should be added to the connection manager.
     * @param clientSession newly created client session for the client connector.
     */
    public synchronized void addClientConnector(ServiceInfo parentService, BConnector connector,
                                                Session clientSession) {
        if (parentServiceToClientConnectorsMap.containsKey(parentService.getName())) {
            parentServiceToClientConnectorsMap.get(parentService.getName()).add(connector);
        } else {
            // Adding connector against parent service.
            List<BConnector> connectors = new LinkedList<>();
            connectors.add(connector);
            parentServiceToClientConnectorsMap.put(parentService.getName(), connectors);
        }

        // Initiating clientConnectorSessionsMap list for the given connector.
        Map<String, Session> sessions = new HashMap<>();
        sessions.put(Constants.DEFAULT, clientSession);
        clientConnectorSessionsMap.put(connector, sessions);
    }

    /**
     * <p>This method should be carefully used. This is only used when creating the initial connection to the
     * remote server by the client connector which is created by the the services other than WebSocket.</p>
     * This also add the initial connection to the the same mapping but using "Default" keyword as the
     * parent service name.
     *
     * @param bConnector {@link BConnector} which created in other than WebSocket endpoint.
     * @param session The default WebSocket session for the Connector.
     */
    public synchronized void addClientConnectorWithoutParentService(BConnector bConnector, Session session) {
        Map<String, Session> sessions = new HashMap<>();
        sessions.put(Constants.DEFAULT, session);
        clientConnectorSessionsMap.put(bConnector, sessions);
    }

    /**
     * Retrieve the client session related to the connector and the server session.
     *
     * @param bConnector {@link BConnector} related to the session.
     * @param serverSession server session of the {@link BConnector} related to the client session.
     * @return the client session of relate to the {@link BConnector} and the relevant server session.
     */
    public synchronized Session getClientSessionForConnector(BConnector bConnector, Session serverSession) {
        return clientConnectorSessionsMap.get(bConnector).get(serverSession.getId());
    }

    /**
     * Connections can be saved in multiple places according to there need of use.
     * Once the connection is closed or because of the reason if it has to be removed from all the places
     * that it is referred to use this method.
     *
     * @param session {@link Session} which should be removed from all the places.
     */
    public synchronized void removeSessionFromAll(Session session) {

        // Remove all the server session related client sessions.
        if (serverSessionToClientSessionsMap.containsKey(session.getId())) {
            for (Session clientSession : serverSessionToClientSessionsMap.remove(session.getId())) {
                if (clientSession.isOpen()) {
                    try {
                        clientSession.close();
                    } catch (IOException e) {
                        throw new BallerinaException("Internal error occurred when closing client connection");
                    }
                }
            }
        }

        // Removing session from connector map
        clientConnectorSessionsMap.entrySet().forEach(
                connectorEntry -> connectorEntry.getValue().remove(session.getId())
        );

        // Removing session from broadcast sessions map
        broadcastSessions.entrySet().forEach(
                entry -> entry.getValue().entrySet().removeIf(
                        innerEntry -> innerEntry.getKey().equals(session.getId())
                )
        );
        //Removing session from groups map
        connectionGroups.entrySet().forEach(
                entry -> entry.getValue().entrySet().removeIf(
                        innerEntry -> innerEntry.getKey().equals(session.getId())
                )
        );
        // Removing session from connection store
        connectionStore.entrySet().removeIf(
                entry -> entry.getValue().equals(session)
        );
    }

    private Session initializeClientConnection(BConnector bConnector, CarbonMessage carbonMessage) {
        String remoteUrl = bConnector.getStringField(0);
        String clientServiceName = bConnector.getStringField(1);

        WebSocketMessage websocketMessage =
                (WebSocketMessage) carbonMessage.getProperty(Constants.WEBSOCKET_MESSAGE);

        // Initializing a client connection.
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.REMOTE_ADDRESS, remoteUrl);
        properties.put(Constants.TO, clientServiceName);
        properties.put(Constants.WEBSOCKET_MESSAGE, websocketMessage);
        WebSocketClientConnector clientConnector = BallerinaConnectorManager.getInstance().
                getWebSocketClientConnector(properties);
        Map<String, String> customHeaders = new HashMap<>();
        Headers headers = carbonMessage.getHeaders();
        headers.getAll().forEach(
                header -> customHeaders.put(header.getName(), header.getValue())
        );
        Session clientSession;
        try {
            clientSession = clientConnector.connect(new BallerinaWebSocketConnectorListener(), customHeaders);
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Error occurred during initializing the connection to " + remoteUrl);
        }

        return clientSession;
    }
}
