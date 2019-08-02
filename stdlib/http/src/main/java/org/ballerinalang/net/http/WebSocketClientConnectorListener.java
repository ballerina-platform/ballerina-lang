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

package org.ballerinalang.net.http;

import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.exception.WebSocketException;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_WEBSOCKET_CLIENT;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_FAILOVDER_RECONNECT;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_FAILOVER;
import static org.ballerinalang.net.http.WebSocketConstants.STATEMENT_FOR_RECONNECT;
import static org.ballerinalang.net.http.WebSocketConstants.SUB_TARGET_URLS;
import static org.ballerinalang.net.http.WebSocketConstants.TARGET_URLS;
import static org.ballerinalang.net.http.WebSocketUtil.doFailover;
import static org.ballerinalang.net.http.WebSocketUtil.reconnect;
import static org.ballerinalang.net.http.WebSocketUtil.reconnectForFailoverClient;
import static org.ballerinalang.net.http.WebSocketUtil.removeUrlInTarget;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class WebSocketClientConnectorListener implements WebSocketConnectorListener {
    private WebSocketOpenConnectionInfo connectionInfo;
    private static final PrintStream console = System.out;

    public void setConnectionInfo(WebSocketOpenConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        throw new WebSocketException("onHandshake and onOpen is not supported for WebSocket client service");
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        try {
            WebSocketDispatcher.dispatchTextMessage(connectionInfo, webSocketTextMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        try {
            WebSocketDispatcher.dispatchBinaryMessage(connectionInfo, webSocketBinaryMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        try {
            WebSocketDispatcher.dispatchControlMessage(connectionInfo, webSocketControlMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        MapValueImpl clientConfig = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG);
        int statusCode = webSocketCloseMessage.getCloseCode();
        if (!(statusCode == 1006 || statusCode == 1000)  && webSocketClient.getType().getName().
                equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT)) {
            removeUrlInTarget(webSocketClient);
        }
        if (statusCode == 1006 && !webSocketClient.getType().getName().
                equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) && clientConfig.getMapValue(RETRY_CONFIG) != null) {
            if (!reconnect(connectionInfo)) {
                console.println(STATEMENT_FOR_RECONNECT +
                        webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
                setError(webSocketCloseMessage);
            }
        }  else if (statusCode != 1006 && !webSocketClient.getType().getName().
                equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) && clientConfig.getMapValue(RETRY_CONFIG) != null) {
            console.println("The given server: " + webSocketClient.
                    getStringValue(WebSocketConstants.CLIENT_URL_CONFIG) + " is not in the connection state");
            setError(webSocketCloseMessage);
        } else if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                clientConfig.getMapValue(RETRY_CONFIG) == null) {
            if (((List) webSocketClient.getNativeData(SUB_TARGET_URLS)).size() == 0) {
                console.println("All given servers: " +  webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).
                        getArrayValue(WebSocketConstants.TARGET_URLS) + " are not in the connection state");
            } else {
                if (!doFailover(connectionInfo)) {
                    console.println(STATEMENT_FOR_FAILOVER + clientConfig.getArrayValue(TARGET_URLS));
                    setError(webSocketCloseMessage);
                }
            }
        } else if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                clientConfig.getMapValue(RETRY_CONFIG) != null) {
            if (!reconnectForFailoverClient(connectionInfo)) {
                if (((List) webSocketClient.getNativeData(SUB_TARGET_URLS)).size() == 0) {
                    console.println("All given servers: " +  webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).
                            getArrayValue(WebSocketConstants.TARGET_URLS) + " are not in the connection state");
                } else {
                    console.println(STATEMENT_FOR_FAILOVDER_RECONNECT +
                            webSocketClient.getNativeData(SUB_TARGET_URLS));
                }
                setError(webSocketCloseMessage);
            }
        } else {
            setError(webSocketCloseMessage);
        }
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        if (throwable instanceof IOException || throwable.getMessage().contains("Unexpected error")) {
            ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
            MapValueImpl clientConfig = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG);
            if (!webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                    clientConfig.getMapValue(RETRY_CONFIG) != null) {
                if (!reconnect(connectionInfo)) {
                    console.println(STATEMENT_FOR_RECONNECT +
                            webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
                    WebSocketDispatcher.dispatchError(connectionInfo, throwable);
                }
            } else if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                    clientConfig.getMapValue(RETRY_CONFIG) == null) {
                if (!doFailover(connectionInfo)) {
                    if (((List) webSocketClient.getNativeData(SUB_TARGET_URLS)).size() == 0) {
                        console.println("All given servers: " + clientConfig.getArrayValue(TARGET_URLS) +
                                " are not in the connection state");
                    }
                    console.println(STATEMENT_FOR_FAILOVER + clientConfig.getArrayValue(TARGET_URLS));
                    WebSocketDispatcher.dispatchError(connectionInfo, throwable);
                }
            } else if (webSocketClient.getType().getName().equalsIgnoreCase(FAILOVER_WEBSOCKET_CLIENT) &&
                    clientConfig.getMapValue(RETRY_CONFIG) != null) {
                if (!reconnectForFailoverClient(connectionInfo)) {
                    if (((List) webSocketClient.getNativeData(SUB_TARGET_URLS)).size() == 0) {
                        console.println("All given servers: " + clientConfig.getArrayValue(TARGET_URLS) +
                                " are not in the connection state");
                    } else {
                        console.println(STATEMENT_FOR_FAILOVDER_RECONNECT +
                                webSocketClient.getNativeData(SUB_TARGET_URLS));
                    }
                    WebSocketDispatcher.dispatchError(connectionInfo, throwable);
                }
            } else {
                WebSocketDispatcher.dispatchError(connectionInfo, throwable);
            }
        } else {
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        try {
            WebSocketDispatcher.dispatchIdleTimeout(connectionInfo);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
        try {
            WebSocketUtil.setListenerOpenField(connectionInfo);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    public void setError(WebSocketCloseMessage webSocketCloseMessage) {
        try {
            WebSocketDispatcher.dispatchCloseMessage(connectionInfo, webSocketCloseMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }
}
