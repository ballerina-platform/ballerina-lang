/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.services.dispatchers;

import org.ballerinalang.services.dispatchers.http.HTTPSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager to manage HTTP transport sessions.
 */
public class SessionManager {

    private static SessionManager instance = null;
    private Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    /**
     * Max number of sessions that can be active at a given time.
     */
    private static final int DEFAULT_MAX_ACTIVE_SESSIONS = 100_000;

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Session getHTTPSession(String sessionId) {
        HTTPSession session = (HTTPSession) sessionMap.get(sessionId);
        if (session == null) {

        } else {
            //sessionMap.put(sessionId,)
            return session;
        }
        return null;
    }

    public Session createHTTPSession() {
        if (sessionMap.size() >= DEFAULT_MAX_ACTIVE_SESSIONS) {
            throw new IllegalStateException("Too many active sessions");
        }
        HTTPSession session = new HTTPSession(sessionIdGenerator());
        session.setManager(this);
        sessionMap.put(session.getId(), session);
        return session;
    }


    public void invalidateSession(Session session) {
        sessionMap.remove(session.getId());

    }

    public String sessionIdGenerator() {
        return UUID.randomUUID().toString();
    }
}
