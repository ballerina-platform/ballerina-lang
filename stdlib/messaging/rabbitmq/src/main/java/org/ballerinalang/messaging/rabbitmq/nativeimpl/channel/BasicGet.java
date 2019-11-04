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

package org.ballerinalang.messaging.rabbitmq.nativeimpl.channel;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.IOException;

/**
 * Retrieves a message from a queue using RabbitMQ pull API.
 *
 * @since 1.0.3
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "basicGet",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.CHANNEL_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ),
        isPublic = true
)
public class BasicGet {

    public static Object basicGet(Strand strand, ObjectValue channelObjectValue, String queueName, Object ackMode) {
        Channel channel = (Channel) channelObjectValue.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        boolean autoAck = false;
        if (ackMode.toString().equals("auto")) {
            autoAck = true;
        }
        try {
            GetResponse response = channel.basicGet(queueName, autoAck);
            return createAndPopulateMessageObjectValue(response, channel, autoAck);
        } catch (IOException e) {
            return RabbitMQUtils.returnErrorValue("Error occurred while retrieving the message: " +
                    e.getMessage());
        }
    }

    private static ObjectValue createAndPopulateMessageObjectValue(GetResponse response, Channel channel,
                                                                   boolean autoAck) {
        ObjectValue messageObjectValue = BallerinaValues.createObjectValue(RabbitMQConstants.PACKAGE_ID_RABBITMQ,
                RabbitMQConstants.MESSAGE_OBJECT);
        messageObjectValue.addNativeData(RabbitMQConstants.DELIVERY_TAG, response.getEnvelope().getDeliveryTag());
        messageObjectValue.addNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT, channel);
        messageObjectValue.addNativeData(RabbitMQConstants.MESSAGE_CONTENT, response.getBody());
        messageObjectValue.addNativeData(RabbitMQConstants.AUTO_ACK_STATUS, autoAck);
        messageObjectValue.addNativeData(RabbitMQConstants.BASIC_PROPERTIES, response.getProps());
        messageObjectValue.addNativeData(RabbitMQConstants.MESSAGE_ACK_STATUS, false);
        return messageObjectValue;
    }
}
