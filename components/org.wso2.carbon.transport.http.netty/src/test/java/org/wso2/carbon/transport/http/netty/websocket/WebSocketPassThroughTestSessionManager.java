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
 *
 */

package org.wso2.carbon.transport.http.netty.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 * Session manager for WebSocket pass through test
 */
public class WebSocketPassThroughTestSessionManager {

    private static final WebSocketPassThroughTestSessionManager
            SESSION_MANAGER = new WebSocketPassThroughTestSessionManager();

    // Map <serverSessionID, clientSession>
    private final Map<String, Session> serverKeySessionMap = new ConcurrentHashMap<>();
    // Map <clientSessionID, serverSession>
    private final Map<String, Session> clientKeySessionMap = new ConcurrentHashMap<>();

    private WebSocketPassThroughTestSessionManager() {
    }

    public static WebSocketPassThroughTestSessionManager getInstance() {
        return SESSION_MANAGER;
    }

    public void interRelateSessions(Session serverSession, Session clientSession) {
        serverKeySessionMap.put(serverSession.getId(), clientSession);
        clientKeySessionMap.put(clientSession.getId(), serverSession);
    }

    public Session getServerSession(Session clientSession) {
        return clientKeySessionMap.get(clientSession.getId());
    }

    public Session getClientSession(Session serverSession) {
        return serverKeySessionMap.get(serverSession.getId());
    }

}
