/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.metrics;

import org.ballerinalang.util.observability.BallerinaObserver;
import org.ballerinalang.util.observability.ObserverContext;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.util.observability.ObservabilityConstants.PROPERTY_ERROR;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE;

/**
 * Observe the runtime and collect measurements.
 */
public class BallerinaMetricsObserver implements BallerinaObserver {

    private static final String PROPERTY_START_TIME = "_observation_start_time_";
    private static final String TAG_KEY_SERVICE = "service";
    private static final String TAG_KEY_RESOURCE = "resource";
    private static final String TAG_KEY_ACTION = "action";

    private static final PrintStream consoleError = System.err;

    private static final MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

    @Override
    public void startServerObservation(ObserverContext observerContext) {
        String[] mainTags = {TAG_KEY_SERVICE, observerContext.getServiceName(), TAG_KEY_RESOURCE,
                observerContext.getResourceName()};
        startObservation(observerContext, mainTags);
    }

    @Override
    public void startClientObservation(ObserverContext observerContext) {
        String[] mainTags = {TAG_KEY_ACTION, observerContext.getActionName()};
        startObservation(observerContext, mainTags);
    }

    @Override
    public void stopServerObservation(ObserverContext observerContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        String[] mainTags = {TAG_KEY_SERVICE, observerContext.getServiceName(), TAG_KEY_RESOURCE,
                observerContext.getResourceName()};
        stopObservation(observerContext, mainTags);
    }

    @Override
    public void stopClientObservation(ObserverContext observerContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        String[] mainTags = {TAG_KEY_ACTION, observerContext.getActionName()};
        stopObservation(observerContext, mainTags);
    }

    private void startObservation(ObserverContext observerContext, String[] mainTags) {
        observerContext.addProperty(PROPERTY_START_TIME, System.nanoTime());
        String connectorName = observerContext.getConnectorName();
        Set<Tag> mainTagSet = new HashSet<>(mainTags.length);
        try {
            // Tags are validated (both key and value should not be null)
            Tags.tags(mainTagSet, mainTags);
            getInprogressGauge(connectorName, mainTagSet).increment();
        } catch (RuntimeException e) {
            handleError(connectorName, mainTagSet, e);
        }
    }

    private void stopObservation(ObserverContext observerContext, String[] mainTags) {
        // Connector name must be a part of the metric name to make sure that every metric is unique with
        // the combination of name and tags.
        String connectorName = observerContext.getConnectorName();
        Map<String, String> tags = observerContext.getTags();
        Set<Tag> allTags = new HashSet<>(tags.size() + mainTags.length);
        try {
            // Tags are validated (both key and value should not be null)
            Tags.tags(allTags, observerContext.getTags());
            Tags.tags(allTags, mainTags);
            Set<Tag> mainTagSet = new HashSet<>(mainTags.length);
            Tags.tags(mainTagSet, mainTags);
            Long startTime = (Long) observerContext.getProperty(PROPERTY_START_TIME);
            long duration = System.nanoTime() - startTime;
            getInprogressGauge(connectorName, mainTagSet).decrement();
            metricRegistry.timer(new MetricId(connectorName + "_response_time", "Response Time",
                    allTags)).record(duration, TimeUnit.NANOSECONDS);
            metricRegistry.counter(new MetricId(connectorName + "_requests_total",
                    "Total number of requests", allTags)).increment();
            // Check HTTP status code
            String statusCode = tags.get(TAG_KEY_HTTP_STATUS_CODE);
            if (statusCode != null) {
                int httpStatusCode = Integer.parseInt(statusCode);
                if (httpStatusCode > 0) {
                    incrementHttpStatusCodeCounters(httpStatusCode, connectorName, mainTagSet);
                }
            }
            Boolean error = (Boolean) observerContext.getProperty(PROPERTY_ERROR);
            if (error != null && error) {
                metricRegistry.counter(new MetricId(connectorName + "_failed_requests_total",
                        "Total number of failed requests", allTags)).increment();
            }
        } catch (RuntimeException e) {
            handleError(connectorName, allTags, e);
        }
    }

    private Gauge getInprogressGauge(String connectorName, Set<Tag> tags) {
        return metricRegistry.gauge(new MetricId(connectorName + "_inprogress_requests",
                "Inprogress Requests", tags));
    }

    private void incrementHttpStatusCodeCounters(int statusCode, String connectorName, Set<Tag> tags) {
        if (statusCode >= 100 && statusCode < 200) {
            metricRegistry.counter(new MetricId(connectorName + "_1XX_requests_total",
                    "Total number of requests that resulted in HTTP 1xx informational responses", tags))
                    .increment();
        } else if (statusCode < 300) {
            metricRegistry.counter(new MetricId(connectorName + "_2XX_requests_total",
                    "Total number of requests that resulted in HTTP 2xx successful responses", tags))
                    .increment();
        } else if (statusCode < 400) {
            metricRegistry.counter(new MetricId(connectorName + "_3XX_requests_total",
                    "Total number of requests that resulted in HTTP 3xx redirections", tags)).increment();
        } else if (statusCode < 500) {
            metricRegistry.counter(new MetricId(connectorName + "_4XX_requests_total",
                    "Total number of requests that resulted in HTTP 4xx client errors", tags)).increment();
        } else if (statusCode < 600) {
            metricRegistry.counter(new MetricId(connectorName + "_5XX_requests_total",
                    "Total number of requests that resulted in HTTP 5xx server errors", tags)).increment();
        }
    }

    private void handleError(String connectorName, Set<Tag> tags, RuntimeException e) {
        // Metric Provider may throw exceptions if there is a mismatch in tags.
        consoleError.println("ballerina: error collecting metrics for " + connectorName + " with tags " + tags +
                ": " + e.getMessage());
    }
}
