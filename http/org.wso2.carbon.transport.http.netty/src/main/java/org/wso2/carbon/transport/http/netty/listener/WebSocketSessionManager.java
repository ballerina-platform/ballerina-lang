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

package org.wso2.carbon.transport.http.netty.listener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.websocket.Session;

/**
 * Internal session manager for WebSocket messages.
 * This is a singleton class which is used to store all the {@link Session} details in a single place.
 */

public class WebSocketSessionManager {

    private static WebSocketSessionManager webSocketSessionManager = new WebSocketSessionManager();

    //Map<uri, Map<sessionId, session>> sessionMap
    private Map<String, Map<String, Session>> sessionMap = new ConcurrentHashMap<>();

    private WebSocketSessionManager() {
    }

    public static WebSocketSessionManager getInstance() {
        return webSocketSessionManager;
    }

    /**
     * @param uri uri of the WebSocket endpoint.
     * @param sessionId session id for the given channel.
     * @return requested {@link Session} for given channel.
     */
    public Session getSession(String uri, String sessionId) {
        return sessionMap.get(uri).get(sessionId);
    }

    /**
     * This method creates session for a given channel.
     * Here unlike http channel ID was taken as the session ID.
     * @param uri uri of the WebSocket endpoint.
     * @param session session to add for the given channel.
     * @return Created new {@link Session}.
     */
    public Session add(String uri, Session session) {
        if (sessionMap.containsKey(uri)) {
            sessionMap.get(uri).put(session.getId(), session);
        } else {
            Map<String, Session> sessions = new ConcurrentHashMap<>();
            sessions.put(session.getId(), session);
            sessionMap.put(uri, sessions);
        }
        return session;
    }


    /**
     * @param uri of the sessions which are mapped.
     * @return the list of session which are mapped to a specific uri.
     */
    public List<Session> getAllSessions(String uri) {
        Map<String, Session> sessions = sessionMap.get(uri);
        return sessions.entrySet().stream().
                map(Map.Entry::getValue).
                collect(Collectors.toList());
    }

    /**
     * Checks whether the session is contained in the session manager.
     * @param uri uri of the WebSocket endpoint.
     * @param sessionId session id for the given channel.
     * @return true if the session is in the {@link WebSocketSessionManager}.
     */
    public boolean containsSession(String uri, String sessionId) {
        return sessionMap.containsKey(uri) && sessionMap.get(uri).containsKey(sessionId);
    }

    /**
     * Remove session from the session manager.
     * @param uri uri of the WebSocket endpoint.
     * @param sessionId session id for the given channel.
     * @return removed session from the session map.
     */
    public Session removeSession(String uri, String sessionId) {
        return sessionMap.get(uri).remove(sessionId);
    }

}
