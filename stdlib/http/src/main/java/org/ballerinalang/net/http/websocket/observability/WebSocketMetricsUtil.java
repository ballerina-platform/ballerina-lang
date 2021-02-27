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

package org.ballerinalang.net.http.websocket.observability;

import io.ballerina.runtime.observability.ObservabilityConstants;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.metrics.DefaultMetricRegistry;
import io.ballerina.runtime.observability.metrics.MetricId;
import io.ballerina.runtime.observability.metrics.MetricRegistry;
import io.ballerina.runtime.observability.metrics.Tag;

import java.util.Set;

/**
 * Providing metrics functionality to WebSockets.
 *
 * @since 1.1.0
 */

class WebSocketMetricsUtil {

    private static final MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

    static void reportRequestMetrics(WebSocketObserverContext observerContext) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementCounterMetric(observerContext, WebSocketObservabilityConstants.METRIC_REQUESTS[0],
                               WebSocketObservabilityConstants.METRIC_REQUESTS[1]);
    }

    static void reportConnectionMetrics(WebSocketObserverContext observerContext) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        incrementGaugeMetric(observerContext, WebSocketObservabilityConstants.METRIC_CONNECTIONS[0],
                               WebSocketObservabilityConstants.METRIC_CONNECTIONS[1]);
    }

    static void reportSendMetrics(WebSocketObserverContext observerContext, String type) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        //Define type of message (text, binary, control, close)
        observerContext.addTag(WebSocketObservabilityConstants.TAG_MESSAGE_TYPE, type);
        //Increment message sent metric
        incrementCounterMetric(observerContext, WebSocketObservabilityConstants.METRIC_MESSAGES_SENT[0],
                               WebSocketObservabilityConstants.METRIC_MESSAGES_SENT[1]);
    }

    static void reportReceivedMetrics(WebSocketObserverContext observerContext, String type) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        //Define type of message (text, binary, control, close)
        observerContext.addMainTag(WebSocketObservabilityConstants.TAG_MESSAGE_TYPE, type);
        //Increment messages received metric
        incrementCounterMetric(observerContext, WebSocketObservabilityConstants.METRIC_MESSAGES_RECEIVED[0],
                               WebSocketObservabilityConstants.METRIC_MESSAGES_RECEIVED[1]);
    }

    static void reportCloseMetrics(WebSocketObserverContext observerContext) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        decrementGaugeMetric(observerContext, WebSocketObservabilityConstants.METRIC_CONNECTIONS[0],
                             WebSocketObservabilityConstants.METRIC_CONNECTIONS[1]);
    }

    static void reportResourceInvocationMetrics(WebSocketObserverContext observerContext, String resource) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        observerContext.addMainTag(WebSocketObservabilityConstants.TAG_RESOURCE, resource);
        incrementCounterMetric(observerContext, WebSocketObservabilityConstants.METRIC_RESOURCES_INVOKED[0],
                               WebSocketObservabilityConstants.METRIC_RESOURCES_INVOKED[1]);
    }

    static void reportErrorMetrics(WebSocketObserverContext observerContext, String errorType, String messageType) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        observerContext.addTag(WebSocketObservabilityConstants.TAG_ERROR_TYPE, errorType);
        //If the error is related to sending/receiving a message, set the type of message
        if (messageType != null) {
            observerContext.addTag(WebSocketObservabilityConstants.TAG_MESSAGE_TYPE, messageType);
        }
        //Increment errors metric
        incrementCounterMetric(observerContext, WebSocketObservabilityConstants.METRIC_ERRORS[0],
                               WebSocketObservabilityConstants.METRIC_ERRORS[1]);
    }

    static void reportErrorMetrics(String errorType, String url, String clientOrServer) {
        if (!ObserveUtils.isMetricsEnabled()) {
            return;
        }
        WebSocketObserverContext observerContext = new WebSocketObserverContext();
        observerContext.addTag(WebSocketObservabilityConstants.TAG_ERROR_TYPE, errorType);
        observerContext.addTag(WebSocketObservabilityConstants.TAG_SERVICE, url);
        observerContext.addTag(WebSocketObservabilityConstants.TAG_CONTEXT, clientOrServer);
        //Increment errors metric
        incrementCounterMetric(observerContext, WebSocketObservabilityConstants.METRIC_ERRORS[0],
                               WebSocketObservabilityConstants.METRIC_ERRORS[1]);
    }

    private static void incrementCounterMetric(WebSocketObserverContext observerContext, String name, String desc) {
        Set<Tag> tags = observerContext.getAllTags();
        metricRegistry.counter(new MetricId(ObservabilityConstants.SERVER_CONNECTOR_WEBSOCKET + "_" +
                                                    name, desc, tags)).increment();
    }

    private static void incrementGaugeMetric(WebSocketObserverContext observerContext, String name, String desc) {
        Set<Tag> tags = observerContext.getAllTags();
        metricRegistry.gauge(new MetricId(ObservabilityConstants.SERVER_CONNECTOR_WEBSOCKET + "_" +
                                                  name, desc, tags)).increment();
    }

    private static void decrementGaugeMetric(WebSocketObserverContext observerContext, String name, String desc) {
        Set<Tag> tags = observerContext.getAllTags();
        metricRegistry.gauge(new MetricId(ObservabilityConstants.SERVER_CONNECTOR_WEBSOCKET + "_" +
                                                  name, desc, tags)).decrement();
    }

    private WebSocketMetricsUtil() {
    }

}
