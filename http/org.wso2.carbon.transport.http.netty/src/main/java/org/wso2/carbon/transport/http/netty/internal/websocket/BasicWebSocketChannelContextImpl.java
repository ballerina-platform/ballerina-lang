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

package org.wso2.carbon.transport.http.netty.internal.websocket;

import org.wso2.carbon.connector.framework.websocket.WebSocketChannelContext;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link WebSocketChannelContext}.
 */
public class BasicWebSocketChannelContextImpl implements WebSocketChannelContext {

    private final Map<String, Object> properties = new HashMap<>();
    protected final String subProtocol;
    protected final String target;
    protected final String listenerPort;
    protected final String protocolVersion;
    protected final boolean isConnectionSecured;
    protected final boolean isServerMessage;
    protected final ConnectionManager connectionManager;
    protected final ListenerConfiguration listenerConfiguration;

    public BasicWebSocketChannelContextImpl(String subProtocol, String target, String listenerPort,
                                            String protocolVersion, boolean isConnectionSecured,
                                            boolean isServerMessage, ConnectionManager connectionManager,
                                            ListenerConfiguration listenerConfiguration) {
        this.subProtocol = subProtocol;
        this.target = target;
        this.listenerPort = listenerPort;
        this.protocolVersion = protocolVersion;
        this.isConnectionSecured = isConnectionSecured;
        this.isServerMessage = isServerMessage;
        this.connectionManager = connectionManager;
        this.listenerConfiguration = listenerConfiguration;
    }

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

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public ListenerConfiguration getListenerConfiguration() {
        return listenerConfiguration;
    }

    @Override
    public String getSubProtocol() {
        return subProtocol;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public String getListenerPort() {
        return listenerPort;
    }

    @Override
    public String getWebSocketProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public boolean isConnectionSecured() {
        return isConnectionSecured;
    }

    @Override
    public boolean isServerMessage() {
        return isServerMessage;
    }
}
