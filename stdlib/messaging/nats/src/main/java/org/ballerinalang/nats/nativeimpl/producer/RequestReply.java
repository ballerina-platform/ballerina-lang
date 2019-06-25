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

import io.nats.client.Message;
import io.nats.streaming.StreamingConnection;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.nativeimpl.Constants;
import org.ballerinalang.nats.nativeimpl.Utils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Sends message to a given subject and receive a response.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "sendRequestReplyMsg",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Producer", structPackage = "ballerina/nats"),
        isPublic = true
)
public class RequestReply implements NativeCallableUnit {

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
            CompletableFuture<Message> future = connection.getNatsConnection().request(subject, content);
            // this will be a blocking call, given we will be rewriting this in ballerina
            // will not invest to write java non blocking version
            Message message = future.get();
            BMap<String, BValue> msgObj = BLangConnectorSPIUtil.createBStruct(context,
                    Constants.NATS_PACKAGE,
                    Constants.NATS_MESSAGE_OBJ_NAME);
            msgObj.addNativeData(Constants.NATS_MSG, message);
            msgObj.put(Constants.MSG_CONTENT_NAME, new BString(new String(message.getData(), StandardCharsets.UTF_8)));
            context.setReturnValues(msgObj);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            context.setReturnValues(Utils.createError(context, Constants.NATS_ERROR_CODE, e.getMessage()));
        } finally {
            callback.notifySuccess();
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
