/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq.nativeimpl.listener;

import com.rabbitmq.client.Channel;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Detach services from the listener.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "detach",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.LISTENER_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ)
)
public class Detach {
    private static final PrintStream console;

    public static Object detach(Strand strand, ObjectValue listenerObjectValue, ObjectValue service) {
        ObjectValue channelObject = (ObjectValue) listenerObjectValue.get(RabbitMQConstants.CHANNEL_REFERENCE);
        Channel channel = (Channel) channelObject.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        ArrayList<ObjectValue> startedServices =
                (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.STARTED_SERVICES);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        ArrayList<ObjectValue> services =
                (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.CONSUMER_SERVICES);
        String serviceName = service.getType().getName();
        String queueName = (String) service.getNativeData(RabbitMQConstants.ALIAS_QUEUE_NAME);
        try {
            channel.basicCancel(serviceName);
            console.println("[ballerina/rabbitmq] Consumer service unsubscribed from the queue " + queueName);
        } catch (IOException e) {
            return RabbitMQUtils.returnErrorValue("Error occurred while detaching the service");
        }
        listenerObjectValue.addNativeData(RabbitMQConstants.CONSUMER_SERVICES,
                RabbitMQUtils.removeFromList(services, service));
        listenerObjectValue.addNativeData(RabbitMQConstants.STARTED_SERVICES,
                RabbitMQUtils.removeFromList(startedServices, service));
        return null;
    }

    static {
        console = System.out;
    }
}
