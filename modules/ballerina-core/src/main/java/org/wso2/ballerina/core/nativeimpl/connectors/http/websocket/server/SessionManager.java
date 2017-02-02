/*
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.core.nativeimpl.connectors.http.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 * Internal session manager for WebSocket messages
 */
public class SessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    private static SessionManager sessionManager = new SessionManager();
    private Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return sessionManager;
    }

    /**
     * @return requested {@link Session} for given channel
     */
    public Session getSession(String sessionId) {
        if (sessionMap.containsKey(sessionId)) {
            return sessionMap.get(sessionId);
        } else {
            throw new NullPointerException("No session found.");
        }
    }

    /**
     * This method creates session for a given channel.
     * Here unlike http channel ID was taken as the session ID.
     * @return Created new {@link Session}.
     */
    public Session add(Session session) {
        sessionMap.put(session.getId(), session);
        LOGGER.info("Added session for channel " + session.getId());
        return session;
    }

    /**
     * Checks whether the session is contained in the session manager.
     * @return true if the session is in the {@link SessionManager}.
     */
    public boolean containsSession(String sessionId) {
        return sessionMap.containsKey(sessionId);
    }

    /**
     * Close the channel for given session.
     * Remove session from the session manager.
     */
    public void removeSession(String sessionId) throws IOException {
        if (sessionMap.containsKey(sessionId)) {
            sessionMap.remove(sessionId).close();
            LOGGER.info("Removed session for channel " + sessionId);
        } else {
            LOGGER.info("There is no session created to remove for channel " + sessionId);
        }
    }

}
