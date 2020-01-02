/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats.connection;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.observability.NatsMetricsUtil;

import java.io.PrintStream;
import java.util.Arrays;


/**
 * ConnectionListener to track the status of a {@link Connection Connection}.
 *
 * @since 1.0.0
 */
public class DefaultConnectionListener implements ConnectionListener {

    private static final PrintStream console;
    private boolean printDisconnected = true;

    DefaultConnectionListener() {
    }

    @Override
    public void connectionEvent(Connection conn, Events type) {
        String url = conn.getConnectedUrl();
        switch (type) {
            case CONNECTED: {
                // The connection has successfully completed the handshake with the gnatsd
                printToConsole("Connection established with server " + url);
                NatsMetricsUtil.reportNewConnection(url);
                break;
            }
            case CLOSED: {
                // The connection is permanently closed, either by manual action or failed reconnects
                printToConsole(conn.getLastError() != null ? "Connection closed." + conn.getLastError() :
                        "Connection closed.");
                NatsMetricsUtil.reportConnectionClose(url);
                break;
            }
            case RECONNECTED: {
                // The connection was connected, lost its connection and successfully reconnected
                printToConsole("Connection reconnected with server " + conn.getConnectedUrl());
                printDisconnected = true;
                NatsMetricsUtil.reportNewConnection(url);
                break;
            }
            case DISCONNECTED: {
                // The connection lost its connection, but may try to reconnect if configured to.
                if (printDisconnected) {
                    printToConsole("Connection disconnected with server " + conn.getLastError());
                    printDisconnected = false;
                    NatsMetricsUtil.reportConnectionClose(url);
                }
                break;
            }
            case RESUBSCRIBED: {
                // The connection was reconnected and the server has been notified of all subscriptions
                printToConsole("Subscriptions re-established with server " + conn.getConnectedUrl());
                break;
            }
            case DISCOVERED_SERVERS: {
                // The connection was told about new servers from, from the current server.
                printToConsole("Server discovered. List of connected servers "
                        + Arrays.toString(conn.getServers().toArray()));
                break;
            }
        }
    }

    private void printToConsole(String message) {
        console.println(Constants.MODULE + message);
    }

    static {
        console = System.out;
    }
}
