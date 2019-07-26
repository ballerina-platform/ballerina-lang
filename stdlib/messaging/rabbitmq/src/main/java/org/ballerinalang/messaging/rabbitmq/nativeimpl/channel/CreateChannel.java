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
import com.rabbitmq.client.Connection;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQTransactionContext;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.IOException;
import java.util.UUID;

/**
 * Creates a RabbitMQ AMQ Channel.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "createChannel",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.CHANNEL_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ),
        isPublic = true
)
public class CreateChannel {

    public static void createChannel(Strand strand, ObjectValue channelObjectValue, Object connectionObject) {
        boolean isInTransaction = strand.isInTransaction();
        ObjectValue connectionObjectValue;
        if (connectionObject != null) {
            connectionObjectValue = (ObjectValue) connectionObject;
            Connection connection =
                    (Connection) connectionObjectValue.getNativeData(RabbitMQConstants.CONNECTION_NATIVE_OBJECT);
            try {
                Channel channel = connection.createChannel();
                channelObjectValue.addNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT, channel);
                RabbitMQTransactionContext rabbitMQTransactionContext =
                        new RabbitMQTransactionContext(channelObjectValue, UUID.randomUUID().toString());
                channelObjectValue.addNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT,
                        rabbitMQTransactionContext);
                if (isInTransaction) {
                    rabbitMQTransactionContext.handleTransactionBlock(strand);
                }
            } catch (IOException exception) {
                throw new BallerinaException(RabbitMQConstants.RABBITMQ_CLIENT_ERROR
                        + exception.getMessage());
            }
        }
    }

    private CreateChannel() {
    }
}
