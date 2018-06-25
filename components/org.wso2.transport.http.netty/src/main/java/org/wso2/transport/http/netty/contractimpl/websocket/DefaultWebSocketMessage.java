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

package org.wso2.transport.http.netty.contractimpl.websocket;

import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link WebSocketMessage}.
 */
public class DefaultWebSocketMessage implements WebSocketMessage {

    private final Map<String, Object> properties = new HashMap<>();
    protected String target;
    protected String listenerInterface;
    protected boolean secureConnection;
    protected boolean isServerMessage;
    protected WebSocketConnection webSocketConnection;
    protected String sessionID;

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public void setProperties(Map<String, Object> properties) {
        properties.forEach(this.properties::put);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String getTarget() {
        return target;
    }

    public void setListenerInterface(String listenerInterface) {
        this.listenerInterface = listenerInterface;
    }

    @Override
    public String getListenerInterface() {
        return listenerInterface;
    }

    public void setIsSecureConnection(boolean isConnectionSecured) {
        this.secureConnection = isConnectionSecured;
    }

    @Override
    public boolean isSecureConnection() {
        return secureConnection;
    }

    public void setIsServerMessage(boolean isServerMessage) {
        this.isServerMessage = isServerMessage;
    }

    @Override
    public boolean isServerMessage() {
        return isServerMessage;
    }

    public void setWebSocketConnection(WebSocketConnection webSocketConnection) {
        this.webSocketConnection = webSocketConnection;
    }

    @Override
    public WebSocketConnection getWebSocketConnection() {
        return webSocketConnection;
    }

    @Override
    public String getSessionID() {
        return sessionID;
    }
}
