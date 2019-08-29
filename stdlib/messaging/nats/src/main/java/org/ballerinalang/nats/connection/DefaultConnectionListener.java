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
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;


/**
 * ConnectionListener to track the status of a {@link Connection Connection}.
 *
 * @since 1.0.0
 */
public class DefaultConnectionListener implements ConnectionListener {

    private static final PrintStream console;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConnectionListener.class);

    DefaultConnectionListener() {
    }

    @Override
    public void connectionEvent(Connection conn, Events type) {
        switch (type) {
            case CONNECTED: {
                // The connection has successfully completed the handshake with the gnatsd
                console.println("[ballerina/nats] Connection established with server "
                        + conn.getConnectedUrl());
                break;
            }
            case CLOSED: {
                // The connection is permanently closed, either by manual action or failed reconnects
                String message = conn.getLastError() != null ? "Connection closed." + conn.getLastError() :
                        "Connection closed.";
                console.println("[ballerina/nats] " + message);
                break;
            }
            case RECONNECTED: {
                // The connection was connected, lost its connection and successfully reconnected
                String message = "Connection reconnected with server " + conn.getConnectedUrl();
                console.println("[ballerina/nats] " + message);
                break;
            }
            case DISCONNECTED: {
                // The connection lost its connection, but may try to reconnect if configured to.
                String message = "Connection disconnected with server " + conn.getConnectedUrl();
                console.println("[ballerina/nats] " + message);
                break;
            }
            case RESUBSCRIBED: {
                // The connection was reconnected and the server has been notified of all subscriptions
                String message = "Subscriptions reestablished with server " + conn.getConnectedUrl();
                console.println("[ballerina/nats] " + message);
                break;
            }
            case DISCOVERED_SERVERS: {
                // The connection was told about new servers from, from the current server.
                LOG.debug("Server discovered. List of connected servers "
                        + Arrays.toString(conn.getServers().toArray()));
                break;
            }
        }
    }

    static {
        console = System.out;
    }
}
