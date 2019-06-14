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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQTransactionContext;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.messaging.rabbitmq.util.ChannelUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class CreateChannel extends BlockingNativeCallableUnit {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateChannel.class);

    @Override
    public void execute(Context context) {
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        BMap<String, BValue> channelBObject = (BMap<String, BValue>) context.getRefArgument(0);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        BMap<String, BValue> connectionBObject = (BMap<String, BValue>) context.getRefArgument(1);
        Connection connection = RabbitMQUtils.getNativeObject(connectionBObject,
                RabbitMQConstants.CONNECTION_NATIVE_OBJECT,
                Connection.class, context);
        try {
            Channel channel = ChannelUtils.createChannel(connection);
            channelBObject.addNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT, channel);
            RabbitMQTransactionContext rabbitMQTransactionContext = new RabbitMQTransactionContext(channelBObject,
                    UUID.randomUUID().toString());
            channelBObject.addNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT,
                    rabbitMQTransactionContext);
            rabbitMQTransactionContext.handleTransactionBlock(context);
        } catch (BallerinaException exception) {
            LOGGER.error("I/O exception while creating the channel", exception);
            RabbitMQUtils.returnError("RabbitMQ Client Error:", context, exception);
        }
    }
}
