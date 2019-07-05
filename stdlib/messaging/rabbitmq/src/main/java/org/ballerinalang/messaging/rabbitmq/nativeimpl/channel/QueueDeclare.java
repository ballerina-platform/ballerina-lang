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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConnectorException;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQTransactionContext;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.messaging.rabbitmq.util.ChannelUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;


/**
 * Declares a queue.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "queueDeclare",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.CHANNEL_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ),
        isPublic = true
)
public class QueueDeclare extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static Object queueDeclare(Strand strand, ObjectValue channelObjectValue, Object queueConfig) {

        Channel channel = (Channel) channelObjectValue.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        RabbitMQTransactionContext transactionContext = (RabbitMQTransactionContext) channelObjectValue.
                getNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT);
        try {
            if (queueConfig == null) {
                return ChannelUtils.queueDeclare(channel);
            } else {
                @SuppressWarnings(RabbitMQConstants.UNCHECKED)
                MapValue<String, Object> config = (MapValue<String, Object>) queueConfig;
                String queueName = config.getStringValue(RabbitMQConstants.ALIAS_QUEUE_NAME);
                boolean durable = config.getBooleanValue(RabbitMQConstants.ALIAS_QUEUE_DURABLE);
                boolean exclusive = config.getBooleanValue(RabbitMQConstants.ALIAS_QUEUE_EXCLUSIVE);
                boolean autoDelete = config.getBooleanValue(RabbitMQConstants.ALIAS_QUEUE_AUTODELETE);
                ChannelUtils.queueDeclare(channel, queueName, durable, exclusive, autoDelete);
            }
            if (transactionContext != null) {
                transactionContext.handleTransactionBlock();
            }
        } catch (RabbitMQConnectorException exception) {
            return RabbitMQUtils.returnErrorValue(RabbitMQConstants.RABBITMQ_CLIENT_ERROR
                    + "I/O exception while declaring a queue; " + exception.getDetail());
        }
        return null;
    }
}
