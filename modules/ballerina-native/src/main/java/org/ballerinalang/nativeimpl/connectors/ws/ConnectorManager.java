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

package org.ballerinalang.nativeimpl.connectors.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 * This manage all the connectors for Incoming and outgoing WebSocket messages.
 */
public class ConnectorManager {

    private static final ConnectorManager CONNECTOR_MANAGER = new ConnectorManager();

    Map<String, String> sessionIDToConnectorIDMap = new ConcurrentHashMap<>();
    Map<String, Session> connectorIDtoSessionMap = new ConcurrentHashMap<>();

    private ConnectorManager() {
    }

    public static ConnectorManager getInstance() {
        return CONNECTOR_MANAGER;
    }

    public void storeConnector(Session session, String connectorID) {
        sessionIDToConnectorIDMap.put(session.getId(), connectorID);
        connectorIDtoSessionMap.put(connectorID, session);
    }

    public String getConnectorID(String sessionID) {
        return sessionIDToConnectorIDMap.get(sessionID);
    }

    public Session getSessionByConnectorID(String connectorID) {
        return connectorIDtoSessionMap.get(connectorID);
    }

    public void removeConnectorBySession(Session session) {
        connectorIDtoSessionMap.remove(sessionIDToConnectorIDMap.remove(session.getId()));
    }
}
