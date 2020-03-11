/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.messaging.rabbitmq.observability;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.metrics.DefaultMetricRegistry;
import org.ballerinalang.jvm.observability.metrics.MetricId;
import org.ballerinalang.jvm.observability.metrics.MetricRegistry;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;

import java.util.ArrayList;

/**
 * Providing metrics functionality to the RabbitMQ connector.
 *
 * @since 1.2.0
 */
public class RabbitMQMetricsUtil {

    private static final MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

    /**
     * Reports a new connection.
     *
     * @param connection RabbitMQ connection.
     */
    public static void reportNewConnection(Connection connection) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementGauge(new RabbitMQObserverContext(connection), RabbitMQObservabilityConstants.METRIC_CONNECTIONS[0],
                       RabbitMQObservabilityConstants.METRIC_CONNECTIONS[1]);
    }

    /**
     * Reports a connection closure.
     *
     * @param connection RabbitMQ connection.
     */
    public static void reportConnectionClose(Connection connection) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        decrementGauge(new RabbitMQObserverContext(connection), RabbitMQObservabilityConstants.METRIC_CONNECTIONS[0],
                       RabbitMQObservabilityConstants.METRIC_CONNECTIONS[1]);
    }

    /**
     * Reports a new channel.
     *
     * @param channel RabbitMQ channel.
     */
    public static void reportNewChannel(Channel channel) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementGauge(new RabbitMQObserverContext(channel), RabbitMQObservabilityConstants.METRIC_CHANNELS[0],
                       RabbitMQObservabilityConstants.METRIC_CHANNELS[1]);
    }

    /**
     * Reports a channel closure.
     *
     * @param channel RabbitMQ channel.
     */
    public static void reportChannelClose(Channel channel) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        decrementGauge(new RabbitMQObserverContext(channel), RabbitMQObservabilityConstants.METRIC_CHANNELS[0],
                       RabbitMQObservabilityConstants.METRIC_CHANNELS[1]);
    }

    /**
     * Reports a new queue.
     *
     * @param channel   RabbitMQ channel.
     * @param queueName Name of the queue.
     */
    public static void reportNewQueue(Channel channel, String queueName) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_QUEUE, queueName);
        incrementGauge(observerContext, RabbitMQObservabilityConstants.METRIC_QUEUES[0],
                       RabbitMQObservabilityConstants.METRIC_QUEUES[1]);
    }

    /**
     * Reports the deletion of a queue.
     *
     * @param channel   RabbitMQ channel.
     * @param queueName Name of the queue.
     */
    public static void reportQueueDeletion(Channel channel, String queueName) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_QUEUE, queueName);
        decrementGauge(observerContext, RabbitMQObservabilityConstants.METRIC_QUEUES[0],
                       RabbitMQObservabilityConstants.METRIC_QUEUES[1]);
    }

    /**
     * Reports a new exchange.
     *
     * @param channel      RabbitMQ channel.
     * @param exchangeName Exchange name.
     */
    public static void reportNewExchange(Channel channel, String exchangeName) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_EXCHANGE, exchangeName);
        incrementGauge(observerContext, RabbitMQObservabilityConstants.METRIC_EXCHANGES[0],
                       RabbitMQObservabilityConstants.METRIC_EXCHANGES[1]);
    }

    /**
     * Reports the deletion of an exchange.
     *
     * @param channel      RabbitMQ channel.
     * @param exchangeName Exchange name.
     */
    public static void reportExchangeDeletion(Channel channel, String exchangeName) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_EXCHANGE, exchangeName);
        decrementGauge(observerContext, RabbitMQObservabilityConstants.METRIC_EXCHANGES[0],
                       RabbitMQObservabilityConstants.METRIC_EXCHANGES[1]);
    }

    /**
     * Reports a new consumer.
     *
     * @param channel RabbitMQ channel.
     */
    public static void reportNewConsumer(Channel channel) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementGauge(new RabbitMQObserverContext(channel), RabbitMQObservabilityConstants.METRIC_CONSUMERS[0],
                       RabbitMQObservabilityConstants.METRIC_CONSUMERS[1]);
    }

    /**
     * Reports a consumer closure.
     *
     * @param channel RabbitMQ channel.
     */
    public static void reportConsumerClose(Channel channel) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        decrementGauge(new RabbitMQObserverContext(channel), RabbitMQObservabilityConstants.METRIC_CONSUMERS[0],
                       RabbitMQObservabilityConstants.METRIC_CONSUMERS[1]);
    }

    /**
     * Reports a subscription.
     *
     * @param channel RabbitMQ channel.
     * @param service Service (queueName can be obtained).
     */
    public static void reportSubscription(Channel channel, ObjectValue service) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_QUEUE,
                               RabbitMQObservabilityUtil.getQueueName(service));
        incrementGauge(observerContext, RabbitMQObservabilityConstants.METRIC_SUBSCRIPTION[0],
                       RabbitMQObservabilityConstants.METRIC_SUBSCRIPTION[1]);
    }

    /**
     * Reports an unsubscription.
     *
     * @param channel RabbitMQ channel.
     * @param service Service (queueName can be obtained).
     */
    public static void reportUnsubscription(Channel channel, ObjectValue service) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_QUEUE,
                               RabbitMQObservabilityUtil.getQueueName(service));
        decrementGauge(observerContext, RabbitMQObservabilityConstants.METRIC_SUBSCRIPTION[0],
                       RabbitMQObservabilityConstants.METRIC_SUBSCRIPTION[1]);
    }

    /**
     * Reports a bulk unsubscription.
     *
     * @param channel             RabbitMQ channel.
     * @param listenerObjectValue Listener ObjectValue.
     */
    public static void reportBulkUnsubscription(Channel channel, ObjectValue listenerObjectValue) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        ArrayList<ObjectValue> services =
                (ArrayList<ObjectValue>) listenerObjectValue.getNativeData(RabbitMQConstants.CONSUMER_SERVICES);
        for (ObjectValue service : services) {
            reportUnsubscription(channel, service);
        }
    }

    /**
     * Reports publishing of a message.
     *
     * @param channel      RabbitMQ channel.
     * @param exchangeName Exchange name.
     * @param routingKey   Routing key.
     * @param size         Size of the message.
     */
    public static void reportPublish(Channel channel, String exchangeName, String routingKey, int size) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        if (exchangeName == null) {
            exchangeName = RabbitMQObservabilityConstants.UNKNOWN;
        }
        if (exchangeName.equals("")) {
            exchangeName = RabbitMQObservabilityConstants.DEFAULT;
        }
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_EXCHANGE, exchangeName);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_ROUTING_KEY, routingKey);
        incrementCounter(observerContext, RabbitMQObservabilityConstants.METRIC_PUBLISHED[0],
                         RabbitMQObservabilityConstants.METRIC_PUBLISHED[1]);
        incrementCounter(observerContext, RabbitMQObservabilityConstants.METRIC_PUBLISHED_SIZE[0],
                         RabbitMQObservabilityConstants.METRIC_PUBLISHED_SIZE[1], size);
    }

    /**
     * Reports consuming of a message.
     *
     * @param channel     RabbitMQ channel.
     * @param queueName   Queue name.
     * @param size        Size of the message.
     * @param consumeType Context from which the message was consumed (service or channel).
     */
    public static void reportConsume(Channel channel, String queueName, int size, String consumeType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_QUEUE, queueName);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_CONSUME_TYPE, consumeType);
        incrementCounter(observerContext, RabbitMQObservabilityConstants.METRIC_CONSUMED[0],
                         RabbitMQObservabilityConstants.METRIC_CONSUMED[1]);
        incrementCounter(observerContext, RabbitMQObservabilityConstants.METRIC_CONSUMED_SIZE[0],
                         RabbitMQObservabilityConstants.METRIC_CONSUMED_SIZE[1], size);
    }

    /**
     * Reports acknowledgement of a message.
     *
     * @param channel RabbitMQ channel.
     * @param ackType Type of acknowledgment (ACK or NACK).
     */
    public static void reportAcknowledgement(Channel channel, String ackType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_ACK_TYPE, ackType);
        incrementCounter(observerContext, RabbitMQObservabilityConstants.METRIC_ACK[0],
                         RabbitMQObservabilityConstants.METRIC_ACK[1]);
    }

    public static void reportError(String errorType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext();
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_ERROR_TYPE, errorType);
        incrementCounter(observerContext, RabbitMQObservabilityConstants.METRIC_ERRORS[0],
                         RabbitMQObservabilityConstants.METRIC_ERRORS[1]);
    }


    public static void reportError(Connection connection, String errorType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(connection);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_ERROR_TYPE, errorType);
        incrementCounter(observerContext, RabbitMQObservabilityConstants.METRIC_ERRORS[0],
                         RabbitMQObservabilityConstants.METRIC_ERRORS[1]);
    }

    public static void reportError(Channel channel, String errorType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        RabbitMQObserverContext observerContext = new RabbitMQObserverContext(channel);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_ERROR_TYPE, errorType);
        incrementCounter(observerContext, RabbitMQObservabilityConstants.METRIC_ERRORS[0],
                         RabbitMQObservabilityConstants.METRIC_ERRORS[1]);
    }

    private static void incrementCounter(RabbitMQObserverContext observerContext, String name, String desc) {
        incrementCounter(observerContext, name, desc, 1);
    }

    private static void incrementCounter(RabbitMQObserverContext observerContext, String name, String desc,
                                         int amount) {
        if (metricRegistry == null) {
            return;
        }
        metricRegistry.counter(new MetricId(
                RabbitMQObservabilityConstants.CONNECTOR_NAME + "_" + name, desc, observerContext.getAllTags()))
                .increment(amount);
    }

    private static void incrementGauge(RabbitMQObserverContext observerContext, String name, String desc) {
        if (metricRegistry == null) {
            return;
        }
        metricRegistry.gauge(new MetricId(
                RabbitMQObservabilityConstants.CONNECTOR_NAME + "_" + name, desc, observerContext.getAllTags()))
                .increment();
    }

    private static void decrementGauge(RabbitMQObserverContext observerContext, String name, String desc) {
        if (metricRegistry == null) {
            return;
        }
        metricRegistry.gauge(new MetricId(
                RabbitMQObservabilityConstants.CONNECTOR_NAME + "_" + name, desc, observerContext.getAllTags()))
                .decrement();
    }

    private RabbitMQMetricsUtil() {
    }
}
