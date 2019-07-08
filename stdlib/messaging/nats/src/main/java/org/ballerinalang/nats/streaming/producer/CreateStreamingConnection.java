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
package org.ballerinalang.nats.streaming.producer;

import io.nats.client.Connection;
import io.nats.streaming.StreamingConnection;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.streaming.BallerinaNatsStreamingConnectionFactory;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Remote function implementation for NATS Streaming Connection creation.
 */
@BallerinaFunction(orgName = "ballerina",
                   packageName = "nats",
                   functionName = "createStreamingConnection",
                   receiver = @Receiver(type = TypeKind.OBJECT,
                                        structType = "StreamingProducer",
                                        structPackage = "ballerina/nats"),
                   isPublic = true)
public class CreateStreamingConnection implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {

    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static void createStreamingConnection(Strand strand, ObjectValue streamingProducer, ObjectValue conn,
            String clusterId, String clientId, Object streamingConfig) {
        Connection natsConnection = (Connection) conn.getNativeData(Constants.NATS_CONNECTION);
        BallerinaNatsStreamingConnectionFactory streamingConnectionFactory =
                new BallerinaNatsStreamingConnectionFactory(
                natsConnection, clusterId, clientId, (MapValue<String, Object>) streamingConfig);
        try {
            StreamingConnection streamingConnection = streamingConnectionFactory.createConnection();
            streamingProducer.addNativeData(Constants.NATS_STREAMING_CONNECTION, streamingConnection);
            ((AtomicInteger) conn.getNativeData(Constants.CONNECTED_CLIENTS)).incrementAndGet();
        } catch (IOException e) {
            throw new BallerinaException(e.getMessage());
        } catch (InterruptedException e) {
            throw new BallerinaException("Internal error while creating streaming connection");
        }
    }
}
