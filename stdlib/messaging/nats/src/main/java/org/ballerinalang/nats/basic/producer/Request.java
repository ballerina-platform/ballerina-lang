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

package org.ballerinalang.nats.basic.producer;

import io.nats.client.Connection;
import io.nats.client.Message;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.ballerinalang.nats.Utils.convertDataIntoByteArray;

/**
 * Extern function to publish message to a given subject.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "externRequest",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Producer", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Request {

    @SuppressWarnings("unused")
    public static Object externRequest(Strand strand, ObjectValue producerObject, String subject, Object data,
                                 Object duration) {
        Object connection = producerObject.get("connection");

        if (TypeChecker.getType(connection).getTag() == TypeTags.OBJECT_TYPE_TAG) {
            ObjectValue connectionObject = (ObjectValue) connection;
            Connection natsConnection = (Connection) connectionObject.getNativeData(Constants.NATS_CONNECTION);

            if (natsConnection == null) {
                return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error while publishing message to " +
                        "subject " + subject + ". NATS connection doesn't exist.");
            }
            byte[] byteContent = convertDataIntoByteArray(data);
            try {
                Message reply;
                Future<Message> incoming = natsConnection.request(subject, byteContent);
                if (TypeChecker.getType(duration).getTag() == TypeTags.INT_TAG) {
                    reply = incoming.get((Long) duration, TimeUnit.MILLISECONDS);
                } else {
                    reply = incoming.get();
                }
                ArrayValue msgData = new ArrayValue(reply.getData());
                ObjectValue msgObj = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE,
                        Constants.NATS_MESSAGE_OBJ_NAME, reply.getSubject(), msgData, reply.getReplyTo());
                msgObj.addNativeData(Constants.NATS_MSG, reply);
                return msgObj;
            } catch (IllegalArgumentException | IllegalStateException | InterruptedException
                    | ExecutionException | TimeoutException ex) {
                return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error while requesting message to " +
                        "subject " + subject + ". " + ex.getMessage());
            }
        } else {
            return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error while publishing message to " +
                    "subject " + subject + ". Producer is logically disconnected.");
        }
    }
}
