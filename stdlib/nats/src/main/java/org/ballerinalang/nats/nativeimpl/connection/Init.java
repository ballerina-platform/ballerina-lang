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
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.nativeimpl.Constants;
import org.ballerinalang.nats.nativeimpl.Utils;

import java.io.IOException;

/**
 * Initialize the topic producer.
 *
 * @since 0.966
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Connection", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Init implements NativeCallableUnit {

    /**
     * Initializes NATS streaming connection.
     *
     * @param serverConfig Holds values related to establishing a NATS connection.
     * @return Streaming connection created.
     */
    private StreamingConnection connect(BMap<String, BValue> serverConfig) throws IOException, InterruptedException {
        String host = ((BString) serverConfig.get("host")).value();
        int port = ((BInteger) serverConfig.get("port")).value().intValue();
        String clusterId = ((BString) serverConfig.get("clusterId")).value();
        String clientId = ((BString) serverConfig.get("clientId")).value();
        Options.Builder opts = new Options.Builder().natsUrl("nats://" + host + ":" + port);
        return NatsStreaming.connect(clusterId, clientId, opts.build());
    }

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            BMap<String, BValue> serverConfig = (BMap<String, BValue>) context.getRefArgument(1);
            Struct connectorEndpointStruct = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            StreamingConnection connection = connect(serverConfig);
            connectorEndpointStruct.addNativeData(Constants.NATS_CONNECTION, connection);
        } catch (IOException e) {
            Utils.throwBallerinaException("Error occurred while establishing connection.", context, e);
        } catch (InterruptedException ignore) {
            //ignore
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            Utils.throwBallerinaException("Could not establish connection.", context, e);
        }
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
