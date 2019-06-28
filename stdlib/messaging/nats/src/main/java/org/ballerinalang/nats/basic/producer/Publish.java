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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;

/**
 * Extern function to publish message to a given subject.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "publish",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Producer", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Publish extends BlockingNativeCallableUnit {
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
    }

    public static Object publish(Strand strand, ObjectValue producerObject, String subject, Object replyTo,
                                 ArrayValue message) {
        Object connection = producerObject.get("connection");

        if (TypeChecker.getType(connection).getTag() == TypeTags.OBJECT_TYPE_TAG) {
            ObjectValue connectionObject = (ObjectValue) connection;
            Connection natsConnection = (Connection) connectionObject.getNativeData(Constants.NATS_CONNECTION);

            if (natsConnection == null) {
                return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error while publishing message to " +
                        "subject " + subject + ". NATS connection doesn't exist.");
            }
            byte[] byteContent = message.getBytes();
            try {
                if (TypeChecker.getType(replyTo).getTag() == TypeTags.STRING_TAG) {
                    natsConnection.publish(subject, (String) replyTo, byteContent);
                } else {
                    natsConnection.publish(subject, byteContent);
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error while publishing message to " +
                        "subject " + subject + ". " + ex.getMessage());
            }
        } else {
            return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error while publishing message to " +
                    "subject " + subject + ". Producer is logically disconnected.");
        }
        return null;
    }
}
