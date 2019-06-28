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

package org.ballerinalang.nats.basic.consumer;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;

/**
 * Creates a subscription with the NATS server.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "nats",
        functionName = "register",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "DefaultMessageHandler",
                structPackage = "ballerina/nats"),
        isPublic = true
)
public class Register extends BlockingNativeCallableUnit {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
    }

    public static Object register(Strand strand, ObjectValue listenerObject, ObjectValue service,
                                  Object annotationData) {
        Connection natsConnection =
                (Connection) ((ObjectValue) listenerObject.get(Constants.CONNECTION_OBJ))
                        .getNativeData(Constants.NATS_CONNECTION);
        MapValue<String, Object> subscriptionConfig = getSubscriptionConfig(service.getType().getAnnotation(
                "ballerina/nats", "SubscriptionConfig"));
        if (subscriptionConfig == null) {
            return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error while registering the subscriber. " +
                    "Cannot find subscription configuration");
        }
        String queueName = subscriptionConfig.getStringValue("queueName");
        String subject = subscriptionConfig.getStringValue("subject");
        Dispatcher dispatcher = natsConnection.createDispatcher(new DefaultMessageHandler(service));
        try {
            if (queueName != null) {
                dispatcher.subscribe(subject, queueName);
            } else {
                dispatcher.subscribe(subject);
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error while registering the subscriber. " +
                    ex.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static MapValue<String, Object> getSubscriptionConfig(Object annotationData) {

        ArrayValue annotationArray = null;
        if (annotationData instanceof ArrayValue) {
            annotationArray = (ArrayValue) annotationData;
        }
        if (annotationArray != null && annotationArray.size() > 0) {
            Object annotationValue = annotationArray.getRefValue(0);
            if (annotationValue instanceof MapValue) {
                return (MapValue) annotationValue;
            }
        }
        return null;
    }
}
