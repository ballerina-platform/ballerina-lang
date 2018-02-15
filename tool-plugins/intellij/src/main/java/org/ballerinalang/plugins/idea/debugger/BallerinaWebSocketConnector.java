/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.debugger;

import com.intellij.openapi.diagnostic.Logger;
import org.ballerinalang.plugins.idea.debugger.client.WebSocketClient;
import org.ballerinalang.plugins.idea.debugger.protocol.Command;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

/**
 * Used to communicate with debug server.
 */
public class BallerinaWebSocketConnector {

    private static final Logger LOGGER = Logger.getInstance(BallerinaWebSocketConnector.class);

    private static final String DEBUG_PROTOCOL = "ws://";
    private static final String DEBUG_WEB_SOCKET_PATH = "/debug";

    private WebSocketClient client;
    private String myAddress;
    private ConnectionState myConnectionState;

    public BallerinaWebSocketConnector(@NotNull String address) {
        myAddress = address;
        myConnectionState = ConnectionState.NOT_CONNECTED;
    }

    void createConnection(Callback callback) {
        client = new WebSocketClient(getUri());
        try {
            client.handshake(callback);
        } catch (InterruptedException | URISyntaxException | SSLException e) {
            myConnectionState = ConnectionState.CONNECTING;
            LOGGER.debug(e);
        }
    }

    @NotNull
    private String getUri() {
        return DEBUG_PROTOCOL + myAddress + DEBUG_WEB_SOCKET_PATH;
    }

    @NotNull
    public String getDebugServerAddress() {
        return myAddress;
    }

    void sendCommand(Command command) {
        if (isConnected()) {
            client.sendText(generateRequest(command));
        }
    }

    void sendCommand(Command command, String threadId) {
        if (isConnected()) {
            client.sendText(generateRequest(command, threadId));
        }
    }

    private String generateRequest(Command command) {
        return "{\"command\":\"" + command + "\"}";
    }

    private String generateRequest(Command command, String threadId) {
        return "{\"command\":\"" + command + "\", \"threadId\":\"" + threadId + "\"}";
    }

    void send(String json) {
        if (isConnected()) {
            client.sendText(json);
        }
    }

    boolean isConnected() {
        return client != null && client.isConnected();
    }

    void close() {
        try {
            if (client != null) {
                client.shutDown();
            }
        } catch (InterruptedException e) {
            LOGGER.debug(e);
        }
    }

    String getState() {
        if (myConnectionState == ConnectionState.NOT_CONNECTED) {
            return "Not connected. Waiting for a connection.";
        } else if (myConnectionState == ConnectionState.CONNECTED) {
            return "Connected to " + getUri() + ".";
        } else if (myConnectionState == ConnectionState.DISCONNECTED) {
            return "Disconnected.";
        } else if (myConnectionState == ConnectionState.CONNECTING) {
            return "Connecting to " + getUri() + ".";
        }
        return "Unknown";
    }

    private enum ConnectionState {
        NOT_CONNECTED, CONNECTING, CONNECTED, DISCONNECTED
    }
}
