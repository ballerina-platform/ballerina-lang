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
import javax.websocket.Session;

/**
 * Implementation of {@link WebSocketMessage}.
 */
public class WebSocketMessageImpl implements WebSocketMessage {

    private final Map<String, Object> properties = new HashMap<>();
    protected String subProtocol;
    protected String target;
    protected String listenerInterface;
    protected boolean isConnectionSecured;
    protected boolean isServerMessage;
    protected WebSocketConnection webSocketConnection;
    protected String sessionlID;
    protected Map<String, String> headers = new HashMap<>();

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public void setProperties(Map<String, Object> properties) {
        properties.entrySet().forEach(
                entry -> this.properties.put(entry.getKey(), entry.getValue())
        );
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }


    // setters and getters of common properties.

    public void setSubProtocol(String subProtocol) {
        this.subProtocol = subProtocol;
    }

    public void setSessionlID(String sessionlID) {
        this.sessionlID = sessionlID;
    }

    @Override
    public String getSubProtocol() {
        return subProtocol;
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

    public void setIsConnectionSecured(boolean isConnectionSecured) {
        this.isConnectionSecured = isConnectionSecured;
    }
    @Override
    public boolean isConnectionSecured() {
        return isConnectionSecured;
    }

    public void  setIsServerMessage(boolean isServerMessage) {
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
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        headers.entrySet().forEach(
                entry -> this.headers.put(entry.getKey(), entry.getValue())
        );
    }

    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getSessionID() {
        return sessionlID;
    }
}
