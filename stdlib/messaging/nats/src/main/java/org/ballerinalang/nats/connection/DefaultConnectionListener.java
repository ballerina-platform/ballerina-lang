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
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.nats.basic.consumer.DefaultMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.nats.Constants.NATS_ERROR_CODE;
import static org.ballerinalang.nats.Constants.ON_ERROR_RESOURCE;
import static org.ballerinalang.nats.Utils.getMessageObject;

/**
 * ConnectionListener to track the status of a {@link Connection Connection}.
 *
 * @since 1.0.0
 */
public class DefaultConnectionListener implements ConnectionListener {

    private static final PrintStream console;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConnectionListener.class);
    private final Scheduler scheduler;
    private final List<ObjectValue> serviceList;

    DefaultConnectionListener(Scheduler scheduler, List<ObjectValue> serviceList) {
        this.scheduler = scheduler;
        this.serviceList = serviceList;
    }

    @Override
    public void connectionEvent(Connection conn, Events type) {
        switch (type) {
            case CONNECTED : {
                console.println("[ballerina/nats] Connection established with server "
                        + conn.getConnectedUrl());
                break;
            }
            case CLOSED: {
                String message = conn.getLastError() != null ? "Connection closed." + conn.getLastError() :
                        "Connection closed.";
                ErrorValue errorValue = BallerinaErrors.createError(NATS_ERROR_CODE, message);
                LOG.warn(message);
                for (ObjectValue service : serviceList) {
                    boolean onErrorResourcePresent = Arrays.stream(service.getType().getAttachedFunctions())
                            .anyMatch(resource -> resource.getName().equals(ON_ERROR_RESOURCE));
                    if (onErrorResourcePresent) {
                        Executor.submit(scheduler, service, ON_ERROR_RESOURCE,
                                new DefaultMessageHandler.ResponseCallback(), null, getMessageObject(null),
                                Boolean.TRUE, errorValue, Boolean.TRUE);
                    }
                }
                break;
            }
            case RECONNECTED: {
                LOG.debug("Connection reconnected with server " + conn.getConnectedUrl());
                break;
            }
            case DISCONNECTED: {
                LOG.debug("Connection disconnected with server " + conn.getConnectedUrl());
                break;
            }
            case RESUBSCRIBED: {
                LOG.debug("Subscriptions reestablished with server " + conn.getConnectedUrl());
                break;
            }
            case DISCOVERED_SERVERS: {
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
