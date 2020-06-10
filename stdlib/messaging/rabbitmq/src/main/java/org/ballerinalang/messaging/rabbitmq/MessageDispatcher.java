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
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQMetricsUtil;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQObservabilityConstants;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQObserverContext;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Handles and dispatched messages with data binding.
 *
 * @since 0.995
 */
public class MessageDispatcher {
    private String consumerTag;
    private static final PrintStream console;
    private Channel channel;
    private boolean autoAck;
    private ObjectValue service;
    private String queueName;
    private BRuntime runtime;

    public MessageDispatcher(ObjectValue service, Channel channel, boolean autoAck, BRuntime runtime) {
        this.channel = channel;
        this.autoAck = autoAck;
        this.service = service;
        this.queueName = getQueueNameFromConfig(service);
        this.consumerTag = service.getType().getName();
        this.runtime = runtime;
    }

    private String getQueueNameFromConfig(ObjectValue service) {
        MapValue serviceConfig = (MapValue) service.getType().getAnnotation(RabbitMQConstants.PACKAGE_RABBITMQ_FQN,
                                                                            RabbitMQConstants.SERVICE_CONFIG);
        @SuppressWarnings(RabbitMQConstants.UNCHECKED)
        MapValue<Strand, Object> queueConfig =
                (MapValue) serviceConfig.getMapValue(RabbitMQConstants.ALIAS_QUEUE_CONFIG);
        return queueConfig.getStringValue(RabbitMQConstants.QUEUE_NAME).getValue();
    }

    /**
     * Start receiving messages and dispatch the messages to the attached service.
     *
     * @param listener Listener object value.
     */
    public void receiveMessages(ObjectValue listener) {
        console.println("[ballerina/rabbitmq] Consumer service started for queue " + queueName);
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) {
                handleDispatch(body, envelope.getDeliveryTag(), properties);
            }
        };
        try {
            channel.basicConsume(queueName, autoAck, consumerTag, consumer);
        } catch (IOException exception) {
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_CONSUME);
            throw RabbitMQUtils.returnErrorValue("Error occurred while consuming messages; " +
                                                         exception.getMessage());
        }
        ArrayList<ObjectValue> startedServices =
                (ArrayList<ObjectValue>) listener.getNativeData(RabbitMQConstants.STARTED_SERVICES);
        startedServices.add(service);
        service.addNativeData(RabbitMQConstants.QUEUE_NAME.getValue(), queueName);
    }

    private void handleDispatch(byte[] message, long deliveryTag, AMQP.BasicProperties properties) {
        AttachedFunction[] attachedFunctions = service.getType().getAttachedFunctions();
        AttachedFunction onMessageFunction;
        if (RabbitMQConstants.FUNC_ON_MESSAGE.equals(attachedFunctions[0].getName())) {
            onMessageFunction = attachedFunctions[0];
        } else if (RabbitMQConstants.FUNC_ON_MESSAGE.equals(attachedFunctions[1].getName())) {
            onMessageFunction = attachedFunctions[1];
        } else {
            return;
        }
        BType[] paramTypes = onMessageFunction.getParameterType();
        int paramSize = paramTypes.length;
        if (paramSize > 1) {
            dispatchMessageWithDataBinding(message, deliveryTag, onMessageFunction, properties);
        } else {
            dispatchMessage(message, deliveryTag, properties);
        }
    }

    private void dispatchMessage(byte[] message, long deliveryTag, AMQP.BasicProperties properties) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            CallableUnitCallback callback = new RabbitMQResourceCallback(countDownLatch, channel, queueName,
                                                                         message.length);
            ObjectValue messageObjectValue = getMessageObjectValue(message, deliveryTag, properties);
            executeResource(RabbitMQConstants.FUNC_ON_MESSAGE, callback, messageObjectValue, true);
            countDownLatch.await();
        } catch (InterruptedException e) {
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_CONSUME);
            Thread.currentThread().interrupt();
            throw new RabbitMQConnectorException(RabbitMQConstants.THREAD_INTERRUPTED);
        } catch (AlreadyClosedException | BallerinaConnectorException exception) {
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_CONSUME);
            handleError(message, deliveryTag, properties);
        }
    }

    private void dispatchMessageWithDataBinding(byte[] message, long deliveryTag, AttachedFunction onMessage,
                                                AMQP.BasicProperties properties) {
        BType[] paramTypes = onMessage.getParameterType();
        try {
            Object forContent = getMessageContentForType(message, paramTypes[1]);
            ObjectValue messageObjectValue = getMessageObjectValue(message, deliveryTag, properties);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            CallableUnitCallback callback = new RabbitMQResourceCallback(countDownLatch, channel, queueName,
                                                                         message.length);
            executeResource(RabbitMQConstants.FUNC_ON_MESSAGE, callback, messageObjectValue,
                            true, forContent, true);
            countDownLatch.await();
        } catch (BallerinaConnectorException | UnsupportedEncodingException exception) {
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_CONSUME);
            handleError(message, deliveryTag, properties);
        } catch (InterruptedException e) {
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_CONSUME);
            Thread.currentThread().interrupt();
            throw new RabbitMQConnectorException(RabbitMQConstants.THREAD_INTERRUPTED);
        }
    }

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
                                                                                 StandardCharsets.UTF_8.name())),
                                                     (BStructureType) dataType);
            case TypeTags.ARRAY_TAG:
                if (((BArrayType) dataType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                    return message;
                } else {
                    RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_CONSUME);
                    throw new RabbitMQConnectorException("Only type byte[] is supported in data binding.");
                }
            default:
                RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_CONSUME);
                throw new RabbitMQConnectorException(
                        "The content type of the message received does not match the resource signature type.");
        }
    }

    private ObjectValue getMessageObjectValue(byte[] message, long deliveryTag, AMQP.BasicProperties properties) {
        ObjectValue messageObjectValue = BallerinaValues.createObjectValue(RabbitMQConstants.PACKAGE_ID_RABBITMQ,
                                                                           RabbitMQConstants.MESSAGE_OBJECT);
        messageObjectValue.set(RabbitMQConstants.DELIVERY_TAG, deliveryTag);
        messageObjectValue.set(RabbitMQConstants.JAVA_CLIENT_CHANNEL, new HandleValue(channel));
        messageObjectValue.set(RabbitMQConstants.MESSAGE_CONTENT, BValueCreator.createArrayValue(message));
        messageObjectValue.set(RabbitMQConstants.AUTO_ACK_STATUS, autoAck);
        messageObjectValue.set(RabbitMQConstants.MESSAGE_ACK_STATUS, false);
        if (properties != null) {
            String replyTo = properties.getReplyTo();
            String contentType = properties.getContentType();
            String contentEncoding = properties.getContentEncoding();
            String correlationId = properties.getCorrelationId();
            MapValue<BString, Object> basicProperties =
                    BallerinaValues.createRecordValue(RabbitMQConstants.PACKAGE_ID_RABBITMQ,
                                                      RabbitMQConstants.RECORD_BASIC_PROPERTIES);
            Object[] values = new Object[4];
            values[0] = replyTo;
            values[1] = contentType;
            values[2] = contentEncoding;
            values[3] = correlationId;
            messageObjectValue.set(RabbitMQConstants.BASIC_PROPERTIES,
                                   BallerinaValues.createRecord(basicProperties, values));
        }
        return messageObjectValue;
    }

    private void handleError(byte[] message, long deliveryTag, AMQP.BasicProperties properties) {
        ErrorValue error = RabbitMQUtils.returnErrorValue(RabbitMQConstants.DISPATCH_ERROR);
        ObjectValue messageObjectValue = getMessageObjectValue(message, deliveryTag, properties);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            CallableUnitCallback callback = new RabbitMQErrorResourceCallback(countDownLatch);
            executeResource(RabbitMQConstants.FUNC_ON_ERROR, callback, messageObjectValue, true, error, true);
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_CONSUME);
            throw new RabbitMQConnectorException(RabbitMQConstants.THREAD_INTERRUPTED);
        } catch (AlreadyClosedException | BallerinaConnectorException exception) {
            RabbitMQMetricsUtil.reportError(channel, RabbitMQObservabilityConstants.ERROR_TYPE_CONSUME);
            throw new RabbitMQConnectorException("Error occurred in RabbitMQ service. ");
        }
    }

    private void executeResource(String function, CallableUnitCallback callback, Object... args) {
        if (ObserveUtils.isTracingEnabled()) {
            runtime.invokeMethodAsync(service, function, callback, getNewObserverContextInProperties(), args);
            return;
        }
        runtime.invokeMethodAsync(service, function, callback, args);
    }

    private Map<String, Object> getNewObserverContextInProperties() {
        Map<String, Object> properties = new HashMap<>();
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_QUEUE, queueName);
        properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
        return properties;
    }

    static {
        console = System.out;
    }
}
