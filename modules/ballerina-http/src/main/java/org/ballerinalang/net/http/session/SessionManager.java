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

package org.ballerinalang.net.http.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SessionManager to manage all transport sessions.
 *
 * @since 0.89
 */
public class SessionManager {

    private static SessionManager instance = new SessionManager();
    private Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private SessionIdGenerator sessionIdGenerator = new SessionIdGenerator();
    private ScheduledExecutorService sessionExpiryChecker;

    /**
     * Max number of sessions that can be active at a given time.
     */
    private static final int DEFAULT_MAX_ACTIVE_SESSIONS = 100_000;

    private static final int DEFAULT_MAX_INACTIVE_INTERVAL = 900;  // In seconds

    /**
     * The session id length of Sessions created by this Manager.
     */
    private static final int SESSION_ID_LENGTH = 16;

    private SessionManager() {
        sessionIdGenerator.setSessionIdLength(SESSION_ID_LENGTH);
        // Session expiry scheduled task
        sessionExpiryChecker = Executors.newScheduledThreadPool(1);
        sessionExpiryChecker.scheduleAtFixedRate(() ->
                        sessionMap.values().parallelStream()
                                .filter(session ->
                                        (session.getMaxInactiveInterval() >= 0 &&
                                                System.currentTimeMillis() - session.getLastAccessedTime() >=
                                                        session.getMaxInactiveInterval() * 1000))
                                .forEach(Session::invalidate),
                30, 30, TimeUnit.SECONDS);
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public Session getHTTPSession(String sessionId) {
        HTTPSession session = (HTTPSession) sessionMap.get(sessionId);
        if (session != null) {
            return session;
        }
        return null;
    }

    public Session createHTTPSession(String path) {
        if (sessionMap.size() >= DEFAULT_MAX_ACTIVE_SESSIONS) {
            throw new IllegalStateException("Failed to create session: Too many active sessions");
        }
        HTTPSession session = new HTTPSession(sessionIdGenerator.generateSessionId(),
                DEFAULT_MAX_INACTIVE_INTERVAL, path);
        session.setManager(this);
        sessionMap.put(session.getId(), session);
        return session;
    }

    /**
     * Invalidate a session.
     *
     * @param session The session to be invalidated.
     */
    public void invalidateSession(Session session) {
        sessionMap.remove(session.getId());
    }

    /**
     * Stop ScheduledExecutorService.
     *
     */
    public void stop() {
        sessionExpiryChecker.shutdown();
    }

}
