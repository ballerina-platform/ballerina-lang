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
import io.nats.client.Nats;
import io.nats.client.Options;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Establish a connection with NATS server.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Connection", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {

    private static final String RECONNECT_WAIT = "reconnectWait";
    private static final String SERVER_URL_SEPARATOR = ",";
    private static final String CONNECTION_NAME = "connectionName";
    private static final String MAX_RECONNECT = "maxReconnect";
    private static final String CONNECTION_TIMEOUT = "connectionTimeout";
    private static final String PING_INTERVAL = "pingInterval";
    private static final String MAX_PINGS_OUT = "maxPingsOut";
    private static final String INBOX_PREFIX = "inboxPrefix";
    private static final String NO_ECHO = "noEcho";

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
    }

    public static void init(Strand strand, ObjectValue connectionObject, String urlString,
                            MapValue connectionConfig) {
        Options.Builder opts = new Options.Builder();

        // Add server endpoint urls.
        String[] serverUrls;
        if (urlString != null && urlString.contains(SERVER_URL_SEPARATOR)) {
            serverUrls = urlString.split(SERVER_URL_SEPARATOR);
        } else {
            serverUrls = new String[]{urlString};
        }
        opts.servers(serverUrls);

        // Add connection name.
        opts.connectionName(connectionConfig.getStringValue(CONNECTION_NAME));

        // Add max reconnect.
        opts.maxReconnects(Math.toIntExact(connectionConfig.getIntValue(MAX_RECONNECT)));

        // Add reconnect wait.
        opts.reconnectWait(Duration.ofSeconds(connectionConfig.getIntValue(RECONNECT_WAIT)));


        // Add connection timeout.
        opts.connectionTimeout(Duration.ofSeconds(connectionConfig.getIntValue(CONNECTION_TIMEOUT)));

        // Add ping interval.
        opts.pingInterval(Duration.ofMinutes(connectionConfig.getIntValue(PING_INTERVAL)));

        // Add max ping out.
        opts.maxPingsOut(Math.toIntExact(connectionConfig.getIntValue(MAX_PINGS_OUT)));

        // Add inbox prefix.
        opts.inboxPrefix(connectionConfig.getStringValue(INBOX_PREFIX));

        // Add noEcho.
        if (connectionConfig.getBooleanValue(NO_ECHO)) {
            opts.noEcho();
        }
        // TODO secure socket and authentication support.

        try {
            Connection natsConnection = Nats.connect(opts.build());
            connectionObject.addNativeData(Constants.NATS_CONNECTION, natsConnection);
            List clientslist = new ArrayList();
            connectionObject.addNativeData(Constants.CONNECTED_CLIENTS, clientslist);

        } catch (IOException | InterruptedException e) {
            throw new BallerinaConnectorException(e);
        }
    }
}
