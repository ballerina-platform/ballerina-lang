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

package org.ballerinalang.messaging.rabbitmq.nativeimpl.message;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQTransactionContext;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.messaging.rabbitmq.util.ChannelUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.IOException;

/**
 * Reject one or several received messages.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "basicNack",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.MESSAGE_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ),
        isPublic = true
)
public class BasicNack {

    public static Object basicNack(Strand strand, ObjectValue messageObjectValue, Object multiple, Object requeue) {
        boolean defaultMultiple = false;
        boolean defaultRequeue = true;
        boolean isInTransaction = strand.isInTransaction();
        Channel channel = (Channel) messageObjectValue.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
        RabbitMQTransactionContext transactionContext = (RabbitMQTransactionContext) messageObjectValue.
                getNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT);
        long deliveryTag = (long) messageObjectValue.getNativeData(RabbitMQConstants.DELIVERY_TAG);
        boolean multipleAck = ChannelUtils.validateMultipleAcknowledgements(messageObjectValue);
        boolean ackMode = ChannelUtils.validateAckMode(messageObjectValue);
        if (isInTransaction) {
            transactionContext.handleTransactionBlock(strand);
        }
        if (multiple != null && RabbitMQUtils.checkIfBoolean(multiple)) {
            defaultMultiple = Boolean.valueOf(multiple.toString());
        }
        if (requeue != null && RabbitMQUtils.checkIfBoolean(requeue)) {
            defaultRequeue = Boolean.valueOf(requeue.toString());
        }
        if (!multipleAck && ackMode) {
            try {
                channel.basicNack(deliveryTag, defaultMultiple, defaultRequeue);
            } catch (IOException exception) {
                return RabbitMQUtils.returnErrorValue(RabbitMQConstants.NACK_ERROR
                        + exception.getMessage());
            } catch (AlreadyClosedException exception) {
                return RabbitMQUtils.returnErrorValue(RabbitMQConstants.CHANNEL_CLOSED_ERROR);
            }
        } else if (multipleAck && ackMode) {
            return RabbitMQUtils.returnErrorValue(RabbitMQConstants.MULTIPLE_ACK_ERROR);
        } else {
            return RabbitMQUtils.returnErrorValue(RabbitMQConstants.ACK_MODE_ERROR);
        }
        return null;
    }

    private BasicNack() {
    }
}
