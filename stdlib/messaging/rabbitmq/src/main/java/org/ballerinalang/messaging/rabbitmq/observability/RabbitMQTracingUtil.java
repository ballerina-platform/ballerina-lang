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
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;

import java.util.Optional;

/**
 * Providing tracing functionality to RabbitMQ.
 *
 * @since 1.2.0
 */
public class RabbitMQTracingUtil {

    public static void traceResourceInvocation(Connection connection) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        setTags(getObserverContext(), connection);
    }

    public static void traceResourceInvocation(Channel channel) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        setTags(getObserverContext(), channel);
    }

    public static void traceQueueResourceInvocation(Channel channel, String queueName) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        ObserverContext observerContext = getObserverContext();
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_QUEUE, queueName);
        setTags(observerContext, channel);
    }

    public static void traceExchangeResourceInvocation(Channel channel, String exchangeName) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        ObserverContext observerContext = getObserverContext();
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_EXCHANGE, exchangeName);
        setTags(observerContext, channel);
    }

    public static void traceQueueBindResourceInvocation(Channel channel, String queueName, String exchangeName,
                                                        String routingKey) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        ObserverContext observerContext = getObserverContext();
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_QUEUE, queueName);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_EXCHANGE, exchangeName);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_ROUTING_KEY, routingKey);
        setTags(observerContext, channel);
    }

    public static void tracePublishResourceInvocation(Channel channel, String exchangeName, String routingKey) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        ObserverContext observerContext = getObserverContext();
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_EXCHANGE, exchangeName);
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_ROUTING_KEY, routingKey);
        setTags(observerContext, channel);
    }

    private static ObserverContext getObserverContext() {
        Strand strand = Scheduler.getStrand();
        ObserverContext observerContext;
        Optional<ObserverContext> observerContextOptional = ObserveUtils.getObserverContextOfCurrentFrame(strand);
        if (observerContextOptional.isPresent()) {
            observerContext = observerContextOptional.get();
        } else {
            observerContext = new ObserverContext();
            ObserveUtils.setObserverContextToCurrentFrame(strand, observerContext);
        }
        return observerContext;
    }

    private static void setTags(ObserverContext observerContext, Connection connection) {
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_URL,
                               RabbitMQObservabilityUtil.getServerUrl(connection));
    }

    private static void setTags(ObserverContext observerContext, Channel channel) {
        setTags(observerContext, channel.getConnection());
        observerContext.addTag(RabbitMQObservabilityConstants.TAG_CHANNEL, channel.toString());
    }

    private RabbitMQTracingUtil() {
    }
}
