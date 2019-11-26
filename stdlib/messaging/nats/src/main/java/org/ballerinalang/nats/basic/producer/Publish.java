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
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;

import static org.ballerinalang.nats.Utils.convertDataIntoByteArray;

/**
 * Extern function to publish message to a given subject.
 *
 * @since 0.995
 */
public class Publish {

    public static Object externPublish(ObjectValue producerObject, String subject, Object data, Object replyTo) {
        Object connection = producerObject.get("conn");

        if (TypeChecker.getType(connection).getTag() == TypeTags.OBJECT_TYPE_TAG) {
            ObjectValue connectionObject = (ObjectValue) connection;
            Connection natsConnection = (Connection) connectionObject.getNativeData(Constants.NATS_CONNECTION);

            if (natsConnection == null) {
                return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, Constants.PRODUCER_ERROR + subject +
                        ". NATS connection doesn't exist.");
            }
            byte[] byteContent = convertDataIntoByteArray(data);
            try {
                if (TypeChecker.getType(replyTo).getTag() == TypeTags.STRING_TAG) {
                    natsConnection.publish(subject, (String) replyTo, byteContent);
                } else if (TypeChecker.getType(replyTo).getTag() == TypeTags.SERVICE_TAG) {
                    MapValue<String, Object> subscriptionConfig =
                            getSubscriptionConfig(((ObjectValue) replyTo).getType().getAnnotation(
                                    Constants.NATS_PACKAGE, Constants.SUBSCRIPTION_CONFIG));
                    if (subscriptionConfig == null) {
                        return Utils.createNatsError("Cannot find subscription configuration");
                    }
                    String replyToSubject = subscriptionConfig.getStringValue(Constants.SUBJECT);
                    natsConnection.publish(subject, replyToSubject, byteContent);
                } else {
                    natsConnection.publish(subject, byteContent);
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, Constants.PRODUCER_ERROR + subject +
                        ". " + ex.getMessage());
            }
        } else {
            return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, Constants.PRODUCER_ERROR + subject +
                    ". Producer is logically disconnected.");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static MapValue<String, Object> getSubscriptionConfig(Object annotationData) {
        MapValue annotationRecord = null;
        if (TypeChecker.getType(annotationData).getTag() == TypeTags.RECORD_TYPE_TAG) {
            annotationRecord = (MapValue) annotationData;
        }
        return annotationRecord;
    }
}
