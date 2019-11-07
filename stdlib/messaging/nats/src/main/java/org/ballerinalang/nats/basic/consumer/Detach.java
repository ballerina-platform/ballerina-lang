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

import io.nats.client.Dispatcher;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unsubscribe the consumer from the subject.
 *
 * @since 1.0.4
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.NATS,
        functionName = "detach",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = Constants.NATS_LISTENER,
                structPackage = Constants.NATS_PACKAGE),
        isPublic = true
)
public class Detach {
    private static final PrintStream console;

    public static Object detach(Strand strand, ObjectValue listenerObject, ObjectValue service) {
        @SuppressWarnings("unchecked")
        List<ObjectValue> serviceList =
                (List<ObjectValue>) ((ObjectValue) listenerObject.get(Constants.CONNECTION_OBJ))
                        .getNativeData(Constants.SERVICE_LIST);
        MapValue<String, Object> subscriptionConfig = Utils.getSubscriptionConfig(service.getType()
                .getAnnotation(Constants.NATS_PACKAGE, Constants.SUBSCRIPTION_CONFIG));
        if (subscriptionConfig == null) {
            return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error occurred while un-subscribing, " +
                    "Cannot find subscription configuration");
        }
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Dispatcher> dispatcherList = (ConcurrentHashMap<String, Dispatcher>)
                listenerObject.getNativeData(Constants.DISPATCHER_LIST);
        String subject = subscriptionConfig.getStringValue(Constants.SUBJECT);
        Dispatcher dispatcher = dispatcherList.get(service.getType().getName());
        try {
            dispatcher.unsubscribe(subject);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, "Error occurred while un-subscribing "
                    + ex.getMessage());
        }
        serviceList.remove(service);
        String sOutput = "subject " + subject;
        console.println(Constants.NATS_CLIENT_UNSUBSCRIBED + sOutput);
        dispatcherList.remove(service.getType().getName());
        listenerObject.addNativeData(Constants.DISPATCHER_LIST, dispatcherList);
        listenerObject.addNativeData(Constants.SERVICE_LIST, serviceList);
        return null;
    }

    static {
        console = System.out;
    }
}
