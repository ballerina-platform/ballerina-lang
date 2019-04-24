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

package org.ballerinalang.nats.nativeimpl.producer;

import io.nats.streaming.StreamingConnection;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.nativeimpl.Constants;
import org.ballerinalang.nats.nativeimpl.Utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Sends message to a given subject.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "sendMsg",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Producer", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Send implements NativeCallableUnit {
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            BMap<String, BValue> publisher = Utils.getReceiverObject(context);
            String subject = context.getStringArgument(0);
            byte[] content = ((BValueArray) context.getRefArgument(1)).getBytes();
            StreamingConnection connection = (StreamingConnection) ((BMap) publisher.get(Constants.CONNECTION_OBJ)).
                    getNativeData(Constants.NATS_CONNECTION);
            Acknowledgment acknowledgment = new Acknowledgment(context, callback);
            connection.publish(subject, content, acknowledgment);
        } catch (IOException | TimeoutException e) {
            context.setReturnValues(Utils.createError(context, Constants.NATS_ERROR_CODE, e.getMessage()));
            callback.notifySuccess();
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBlocking() {
        return false;
    }
}
