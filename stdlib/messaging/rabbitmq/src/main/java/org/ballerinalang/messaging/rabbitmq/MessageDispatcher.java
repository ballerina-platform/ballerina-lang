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

package org.ballerinalang.messaging.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.Scheduler;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.Executor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * Handles and dispatched messages with data binding.
 *
 * @since 0.995
 */
public class MessageDispatcher {
    private RabbitMQTransactionContext rabbitMQTransactionContext;
    private Channel channel;
    private boolean autoAck;
    private ObjectValue service;
    private Scheduler scheduler;

    public MessageDispatcher(RabbitMQTransactionContext rabbitMQTransactionContext, ObjectValue service,
                             Channel channel, boolean autoAck, Scheduler scheduler) {
        this.rabbitMQTransactionContext = rabbitMQTransactionContext;
        this.channel = channel;
        this.autoAck = autoAck;
        this.service = service;
        this.scheduler = scheduler;
    }

    /**
     * Dispatch messages.
     *
     * @param message     Message content to be dispatched to the resource function.
     * @param deliveryTag Delivery tag of the message.
     * @param properties  Basic properties of the message.
     */
    public void handleDispatch(byte[] message, long deliveryTag, AMQP.BasicProperties properties) {
        AttachedFunction[] attachedFunctions = service.getType().getAttachedFunctions();
        AttachedFunction onMessageFunction;
        if (RabbitMQConstants.FUNC_ON_MESSAGE.equals(attachedFunctions[0].getName())) {
            onMessageFunction = attachedFunctions[0];
        } else if (RabbitMQConstants.FUNC_ON_MESSAGE.equals(attachedFunctions[1].getName())) {
            onMessageFunction = attachedFunctions[1];
        } else {
            return;
        }
        BType[] paramTypes = onMessageFunction.paramTypes;
        int paramSize = paramTypes.length;
        if (paramSize > 1) {
            dispatchMessageWithDataBinding(message, deliveryTag, onMessageFunction, properties);
        } else {
            dispatchMessage(message, deliveryTag, properties);
        }
    }

    /**
     * Dispatch messages.
     *
     * @param message     Message content to be dispatched to the resource function.
     * @param deliveryTag Delivery tag of the message.
     */
    private void dispatchMessage(byte[] message, long deliveryTag, AMQP.BasicProperties properties) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            Executor.submit(scheduler, service, RabbitMQConstants.FUNC_ON_MESSAGE,
                    new RabbitMQResourceCallback(countDownLatch),
                    null, getMessageObjectValue(message, deliveryTag, properties), true);
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RabbitMQConnectorException(RabbitMQConstants.THREAD_INTERRUPTED);
        } catch (AlreadyClosedException | BallerinaConnectorException exception) {
            handleError(message, deliveryTag, RabbitMQConstants.DISPATCH_ERROR, properties);
        }
    }

    /**
     * Dispatches messages with data binding.
     *
     * @param message     Message content to be dispatched to the resource function.
     * @param deliveryTag Delivery tag of the message.
     * @param properties  Basic properties of the message.
     */
    private void dispatchMessageWithDataBinding(byte[] message, long deliveryTag, AttachedFunction onMessage,
                                                AMQP.BasicProperties properties) {
        BType[] paramTypes = onMessage.paramTypes;
        try {
            Object forContent = getMessageContentForType(message, paramTypes[1]);
            ObjectValue messageObjectValue = getMessageObjectValue(message, deliveryTag, properties);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Executor.submit(scheduler, service, RabbitMQConstants.FUNC_ON_MESSAGE,
                    new RabbitMQResourceCallback(countDownLatch), null,
                    messageObjectValue, true, forContent, true);
            countDownLatch.await();
        } catch (BallerinaConnectorException | UnsupportedEncodingException exception) {
            handleError(message, deliveryTag, RabbitMQConstants.DISPATCH_ERROR, properties);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RabbitMQConnectorException(RabbitMQConstants.THREAD_INTERRUPTED);
        }
    }

    /**
     * Retrieve messages in the given type.
     *
     * @param message  The message body in bytes.
     * @param dataType The data type of the message for data binding.
     * @return Message in the given type.
     */
    private Object getMessageContentForType(byte[] message, BType dataType) throws UnsupportedEncodingException {
        int dataTypeTag = dataType.getTag();
        switch (dataTypeTag) {
            case TypeTags.STRING_TAG:
                return new String(message, StandardCharsets.UTF_8.name());
            case TypeTags.JSON_TAG:
                return JSONParser.parse(new String(message, StandardCharsets.UTF_8.name()));
            case TypeTags.XML_TAG:
                return XMLFactory.parse(new String(message, StandardCharsets.UTF_8.name()));
            case TypeTags.FLOAT_TAG:
                return Float.parseFloat(new String(message, StandardCharsets.UTF_8.name()));
            case TypeTags.INT_TAG:
                return Integer.parseInt(new String(message, StandardCharsets.UTF_8.name()));
            case TypeTags.RECORD_TYPE_TAG:
                return JSONUtils.convertJSONToRecord(JSONParser.parse(new String(message,
                        StandardCharsets.UTF_8.name())), (BStructureType) dataType);
            case TypeTags.ARRAY_TAG:
                if (((BArrayType) dataType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                    return message;
                } else {
                    throw new RabbitMQConnectorException("Only type byte[] is supported in data binding.");
                }
            default:
                throw new RabbitMQConnectorException(
                        "The content type of the message received does not match the resource signature type.");
        }

    }

    /**
     * Create and get message BMap.
     *
     * @param message    Message content received from the RabbitMQ server.
     * @param properties Basic properties of the message.
     * @return Ballerina RabbitMQ message BValue.
     */
    private ObjectValue getMessageObjectValue(byte[] message, long deliveryTag, AMQP.BasicProperties properties) {
        ObjectValue messageObjectValue = BallerinaValues.createObjectValue(RabbitMQConstants.PACKAGE_RABBITMQ,
                RabbitMQConstants.MESSAGE_OBJECT);
        messageObjectValue.addNativeData(RabbitMQConstants.DELIVERY_TAG, deliveryTag);
        messageObjectValue.addNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT, channel);
        messageObjectValue.addNativeData(RabbitMQConstants.MESSAGE_CONTENT, message);
        messageObjectValue.addNativeData(RabbitMQConstants.AUTO_ACK_STATUS, autoAck);
        if (!Objects.isNull(rabbitMQTransactionContext)) {
            messageObjectValue.addNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT,
                    rabbitMQTransactionContext);
        }
        messageObjectValue.addNativeData(RabbitMQConstants.BASIC_PROPERTIES, properties);
        messageObjectValue.addNativeData(RabbitMQConstants.MESSAGE_ACK_STATUS, false);
        return messageObjectValue;
    }

    /**
     * Triggers onError resource function upon error.
     *
     * @param message      Message content received from the RabbitMQ server.
     * @param deliveryTag  Delivery tag of the message.
     * @param errorMessage Error message.
     * @param properties   Basic properties of the message.
     */
    public void handleError(byte[] message, long deliveryTag, String errorMessage, AMQP.BasicProperties properties) {
        ErrorValue error = RabbitMQUtils.returnErrorValue(errorMessage);
        ObjectValue messageObjectValue = getMessageObjectValue(message, deliveryTag, properties);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            Executor.submit(scheduler, service, RabbitMQConstants.FUNC_ON_ERROR,
                    new RabbitMQResourceCallback(countDownLatch), null,
                    messageObjectValue, true, error, true);
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RabbitMQConnectorException(RabbitMQConstants.THREAD_INTERRUPTED);
        } catch (AlreadyClosedException | BallerinaConnectorException exception) {
            throw new RabbitMQConnectorException("Error occurred in RabbitMQ service. ");
        }
    }
}
