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

package org.ballerinalang.nats.nativeimpl.connection;

import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.nativeimpl.Constants;
import org.ballerinalang.nats.nativeimpl.Utils;

import java.io.IOException;

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
        args = {@Argument(name = "config", type = TypeKind.RECORD, structType = "ConnectionConfig",
                structPackage = "ballerina/nats")},
        isPublic = true
)
public class Init implements NativeCallableUnit {

    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String CLUSTER_ID = "clusterId";
    private static final String CLIENT_ID = "clientId";
    private static final String NATS_URL_PREFIX = "nats://";
    private static final String PROTOCOL_PREFIX = ":";

    /**
     * Initializes NATS_URL_PREFIX streaming connection.
     *
     * @param serverConfig Holds values related to establishing a NATS_URL_PREFIX connection.
     * @return Streaming connection created.
     * @throws IOException if a failure occurs while establishing a connection.
     */
    private StreamingConnection connect(BMap<String, BValue> serverConfig) throws IOException, InterruptedException {
        String host = ((BString) serverConfig.get(HOST)).value();
        int port = ((BInteger) serverConfig.get(PORT)).value().intValue();
        String clusterId = ((BString) serverConfig.get(CLUSTER_ID)).value();
        String clientId = ((BString) serverConfig.get(CLIENT_ID)).value();
        Options.Builder opts = new Options.Builder().natsUrl(NATS_URL_PREFIX + host + PROTOCOL_PREFIX + port);
        return NatsStreaming.connect(clusterId, clientId, opts.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            BMap<String, BValue> serverConfig = (BMap<String, BValue>) context.getRefArgument(1);
            Struct connectorEndpointStruct = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            StreamingConnection connection = connect(serverConfig);
            connectorEndpointStruct.addNativeData(Constants.NATS_CONNECTION, connection);
        } catch (IOException e) {
            Utils.throwBallerinaException("Error occurred while establishing connection", context, e);
        } catch (InterruptedException ignore) {
            // ignore
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            Utils.throwBallerinaException("Could not establish connection", context, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBlocking() {
        return true;
    }
}
