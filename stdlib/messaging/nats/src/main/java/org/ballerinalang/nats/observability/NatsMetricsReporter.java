/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nats.observability;

import io.nats.client.Connection;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.metrics.DefaultMetricRegistry;
import org.ballerinalang.jvm.observability.metrics.MetricId;
import org.ballerinalang.jvm.observability.metrics.MetricRegistry;

import java.util.ArrayList;

/**
 * Providing metrics functionality to NATS.
 *
 * @since 1.1.0
 */
public class NatsMetricsReporter {

    private static final MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();
    private Connection connection;

    public NatsMetricsReporter(Connection connection) {
        this.connection = connection;
    }

    /**
     * Reports a new consumer connection.
     *
     * @param url URL of the NATS server that the client is connecting to.
     */
    public static void reportNewConnection(String url) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementGauge(new NatsObserverContext(url), NatsObservabilityConstants.METRIC_CONNECTIONS[0],
                       NatsObservabilityConstants.METRIC_CONNECTIONS[1]);
    }

    /**
     * Reports a disconnection.
     *
     * @param url URL of the NATS server that the client is disconnecting from.
     */
    public static void reportConnectionClose(String url) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        decrementGauge(new NatsObserverContext(url), NatsObservabilityConstants.METRIC_CONNECTIONS[0],
                       NatsObservabilityConstants.METRIC_CONNECTIONS[1]);
    }

    /**
     * Reports a new producer connection.
     */
    public void reportNewProducer() {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementGauge(
                new NatsObserverContext(NatsObservabilityConstants.CONTEXT_PRODUCER, connection.getConnectedUrl()),
                NatsObservabilityConstants.METRIC_PUBLISHERS[0],
                NatsObservabilityConstants.METRIC_PUBLISHERS[1]);

    }

    /**
     * Reports a producer disconnection.
     */
    public void reportProducerClose() {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        decrementGauge(
                new NatsObserverContext(NatsObservabilityConstants.CONTEXT_PRODUCER, connection.getConnectedUrl()),
                NatsObservabilityConstants.METRIC_PUBLISHERS[0],
                NatsObservabilityConstants.METRIC_PUBLISHERS[1]);

    }

    /**
     * Reports a message being published by a NATS producer.
     *
     * @param subject Subject the message is published to.
     * @param size    Size in bytes of the message.
     */
    public void reportPublish(String subject, int size) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        reportPublish(new NatsObserverContext(NatsObservabilityConstants.CONTEXT_PRODUCER, connection.getConnectedUrl(),
                                              subject), size);
    }


    /**
     * Reports a message being successfully received and handled.
     *
     * @param subject Subject the message is received to.
     */
    public void reportDelivery(String subject) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementCounter(
                new NatsObserverContext(NatsObservabilityConstants.CONTEXT_PRODUCER, connection.getConnectedUrl(),
                                        subject),
                NatsObservabilityConstants.METRIC_DELIVERED[0],
                NatsObservabilityConstants.METRIC_DELIVERED[1]);

    }


    /**
     * Reports a request by a NATS producer.
     *
     * @param subject Subject the request message is published to.
     * @param size    Size in bytes of the request message.
     */
    public void reportRequest(String subject, int size) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        NatsObserverContext observerContext = new NatsObserverContext(
                NatsObservabilityConstants.CONTEXT_PRODUCER, connection.getConnectedUrl(), subject);
        incrementCounter(observerContext,
                         NatsObservabilityConstants.METRIC_REQUEST[0],
                         NatsObservabilityConstants.METRIC_REQUEST[1]);
        //Since a request also includes a message being published, that metric must be reported as well.
        reportPublish(observerContext, size);
    }

    /**
     * Reports a response received by a NATS producer after a request is published.
     *
     * @param subject Subject of the request message from which the response was derived.
     */
    public void reportResponse(String subject) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementCounter(
                new NatsObserverContext(NatsObservabilityConstants.CONTEXT_PRODUCER, connection.getConnectedUrl(),
                                        subject),
                NatsObservabilityConstants.METRIC_RESPONSE[0],
                NatsObservabilityConstants.METRIC_RESPONSE[1]);
    }

    /**
     * Reports a consumer subscribing to a subject.
     *
     * @param url     URL of the NATS server that the consumer is connected to.
     * @param subject Subject that the consumer subscribes to.
     */
    public static void reportSubscription(String url, String subject) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        NatsObserverContext observerContext = new NatsObserverContext(
                NatsObservabilityConstants.CONTEXT_PRODUCER, url, subject);
        incrementGauge(observerContext, NatsObservabilityConstants.METRIC_SUBSCRIPTION[0],
                       NatsObservabilityConstants.METRIC_SUBSCRIPTION[1]);
    }

    /**
     * Reports a consumer unsubscribing from a subject.
     *
     * @param subject Subject that the consumer unsubscribes from.
     */
    public void reportUnsubscription(String subject) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        decrementGauge(
                new NatsObserverContext(NatsObservabilityConstants.CONTEXT_PRODUCER, connection.getConnectedUrl(),
                                        subject),
                NatsObservabilityConstants.METRIC_SUBSCRIPTION[0],
                NatsObservabilityConstants.METRIC_SUBSCRIPTION[1]);
    }

    /**
     * Reports a consumer unsubscribing from a subject.
     *
     * @param url     URL of the NATS server that the consumer is connected to.
     * @param subject Subject that the consumer unsubscribes from.
     */
    public static void reportStreamingUnsubscription(String url, String subject) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        decrementGauge(
                new NatsObserverContext(NatsObservabilityConstants.CONTEXT_PRODUCER, url, subject),
                NatsObservabilityConstants.METRIC_SUBSCRIPTION[0],
                NatsObservabilityConstants.METRIC_SUBSCRIPTION[1]);
    }

    /**
     * Reports a consumer unsubscribing from multiple subjects.
     *
     * @param subjects Subjects that the consumer unsubscribes from.
     */
    public void reportBulkUnsubscription(ArrayList<String> subjects) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        for (String subject : subjects) {
            this.reportUnsubscription(subject);
        }

    }

    /**
     * Reports an acknowledgement.
     *
     * @param subject Subject that the consumer subscribes to.
     */
    public void reportAcknowledgement(String subject) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementCounter(new NatsObserverContext(NatsObservabilityConstants.CONTEXT_PRODUCER,
                                                 connection.getConnectedUrl(), subject),
                         NatsObservabilityConstants.METRIC_ACK[0],
                         NatsObservabilityConstants.METRIC_ACK[1]);
    }

    /**
     * Reports a consumer consuming a message.
     *
     * @param subject Subject that the consumer receives the message from.
     * @param size    Size of the message in bytes.
     */
    public void reportConsume(String subject, int size) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        reportConsume(new NatsObserverContext(NatsObservabilityConstants.CONTEXT_CONSUMER, connection.getConnectedUrl(),
                                              subject), size);
    }

    /**
     * Reports an error generated by a producer. This method is called when the URL/subject of the current producer is
     * unknown. e.g. when a NATS connection doesn't exist for a producer.
     *
     * @param errorType type of the error.
     */
    public static void reportProducerError(String errorType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        reportError(NatsObservabilityConstants.CONTEXT_PRODUCER, errorType);
    }

    /**
     * Reports an error generated by a producer.
     *
     * @param subject   Subject that the producer is subscribed to.
     * @param errorType type of the error.
     */
    public void reportProducerError(String subject, String errorType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        reportError(subject, NatsObservabilityConstants.CONTEXT_PRODUCER, errorType);
    }

    /**
     * Reports an error generated by a consumer. This method is called when the URL/subject of the current consumer is
     * unknown. e.g. when a NATS connection doesn't exist for a consumer.
     *
     * @param errorType type of the error.
     */
    public static void reportConsumerError(String errorType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        reportError(NatsObservabilityConstants.CONTEXT_CONSUMER, errorType);
    }

    /**
     * Reports an error generated by a consumer.
     *
     * @param subject   Subject that the consumer is subscribed to.
     * @param errorType type of the error.
     */
    public void reportConsumerError(String subject, String errorType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        this.reportError(subject, NatsObservabilityConstants.CONTEXT_CONSUMER, errorType);
    }

    private static void reportPublish(NatsObserverContext observerContext, int size) {
        incrementCounter(observerContext, NatsObservabilityConstants.METRIC_PUBLISHED[0],
                         NatsObservabilityConstants.METRIC_PUBLISHED[1]);
        incrementCounter(observerContext, NatsObservabilityConstants.METRIC_PUBLISHED_SIZE[0],
                         NatsObservabilityConstants.METRIC_PUBLISHED_SIZE[1], size);
    }

    private static void reportConsume(NatsObserverContext observerContext, int size) {
        incrementCounter(observerContext, NatsObservabilityConstants.METRIC_CONSUMED[0],
                         NatsObservabilityConstants.METRIC_CONSUMED[1]);
        incrementCounter(observerContext, NatsObservabilityConstants.METRIC_CONSUMED_SIZE[0],
                         NatsObservabilityConstants.METRIC_CONSUMED_SIZE[1], size);
    }

    public static void reportError(String context, String errorType) {
        NatsObserverContext observerContext = new NatsObserverContext(context);
        observerContext.addTag(NatsObservabilityConstants.TAG_ERROR_TYPE, errorType);
        incrementCounter(observerContext, NatsObservabilityConstants.METRIC_ERRORS[0],
                         NatsObservabilityConstants.METRIC_ERRORS[1]);
    }

    public void reportError(String subject, String context, String errorType) {
        NatsObserverContext observerContext = new NatsObserverContext(context, connection.getConnectedUrl(), subject);
        observerContext.addTag(NatsObservabilityConstants.TAG_ERROR_TYPE, errorType);
        incrementCounter(observerContext, NatsObservabilityConstants.METRIC_ERRORS[0],
                         NatsObservabilityConstants.METRIC_ERRORS[1]);
    }

    public static void reportStremingError(String url, String subject, String context, String errorType) {
        NatsObserverContext observerContext = new NatsObserverContext(context, url, subject);
        observerContext.addTag(NatsObservabilityConstants.TAG_ERROR_TYPE, errorType);
        incrementCounter(observerContext, NatsObservabilityConstants.METRIC_ERRORS[0],
                         NatsObservabilityConstants.METRIC_ERRORS[1]);
    }

    public static void reportConnectionError(String url, String errorType) {
        NatsObserverContext observerContext = new NatsObserverContext();
        observerContext.addTag(NatsObservabilityConstants.TAG_URL, url);
        observerContext.addTag(NatsObservabilityConstants.TAG_ERROR_TYPE, errorType);
        incrementCounter(observerContext, NatsObservabilityConstants.METRIC_ERRORS[0],
                         NatsObservabilityConstants.METRIC_ERRORS[1]);
    }

    private static void incrementCounter(NatsObserverContext observerContext, String name, String desc) {
        incrementCounter(observerContext, name, desc, 1);
    }

    private static void incrementCounter(NatsObserverContext observerContext, String name, String desc, int amount) {
        if (metricRegistry == null) {
            return;
        }
        metricRegistry.counter(new MetricId(
                NatsObservabilityConstants.CONNECTOR_NAME + "_" + name, desc, observerContext.getAllTags()))
                .increment(amount);
    }

    private static void incrementGauge(NatsObserverContext observerContext, String name, String desc) {
        if (metricRegistry == null) {
            return;
        }
        metricRegistry.gauge(new MetricId(
                NatsObservabilityConstants.CONNECTOR_NAME + "_" + name, desc, observerContext.getAllTags()))
                .increment();
    }

    private static void decrementGauge(NatsObserverContext observerContext, String name, String desc) {
        if (metricRegistry == null) {
            return;
        }
        metricRegistry.gauge(new MetricId(
                NatsObservabilityConstants.CONNECTOR_NAME + "_" + name, desc, observerContext.getAllTags()))
                .decrement();
    }
}
