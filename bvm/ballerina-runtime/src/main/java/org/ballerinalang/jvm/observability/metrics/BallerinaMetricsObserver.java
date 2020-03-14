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
package org.ballerinalang.jvm.observability.metrics;

import org.ballerinalang.jvm.observability.BallerinaObserver;
import org.ballerinalang.jvm.observability.ObserverContext;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Set;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_ERROR;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.UNKNOWN_SERVICE;

/**
 * Observe the runtime and collect measurements.
 */
public class BallerinaMetricsObserver implements BallerinaObserver {

    private static final String PROPERTY_START_TIME = "_observation_start_time_";
    private static final String TAG_KEY_SERVICE = "service";
    private static final String TAG_KEY_RESOURCE = "resource";
    private static final String TAG_KEY_ACTION = "action";
    private static final String TAG_KEY_CONNECTOR_NAME = "connector_name";

    private static final PrintStream consoleError = System.err;

    private static final MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

    private static final StatisticConfig[] responseTimeStatisticConfigs = new StatisticConfig[]{
            StatisticConfig.builder()
                    .expiry(Duration.ofMinutes(1))
                    .percentiles(StatisticConfig.DEFAULT.getPercentiles())
                    .build(),
            StatisticConfig.builder()
                    .expiry(Duration.ofMinutes(5))
                    .percentiles(StatisticConfig.DEFAULT.getPercentiles())
                    .build(),
            StatisticConfig.builder()
                    .expiry(Duration.ofMinutes(15))
                    .percentiles(StatisticConfig.DEFAULT.getPercentiles())
                    .build()
    };

    @Override
    public void startServerObservation(ObserverContext observerContext) {
        observerContext.addMainTag(TAG_KEY_SERVICE, observerContext.getServiceName());
        observerContext.addMainTag(TAG_KEY_RESOURCE, observerContext.getResourceName());
        observerContext.addMainTag(TAG_KEY_CONNECTOR_NAME, observerContext.getConnectorName());
        startObservation(observerContext);
    }

    @Override
    public void startClientObservation(ObserverContext observerContext) {
        observerContext.addMainTag(TAG_KEY_ACTION, observerContext.getActionName());
        observerContext.addMainTag(TAG_KEY_CONNECTOR_NAME, observerContext.getConnectorName());
        if (!UNKNOWN_SERVICE.equals(observerContext.getServiceName())) {// If service is present, resource should be too
            observerContext.addMainTag(TAG_KEY_SERVICE, observerContext.getServiceName());
            observerContext.addMainTag(TAG_KEY_RESOURCE, observerContext.getResourceName());
        }
        startObservation(observerContext);
    }

    @Override
    public void stopServerObservation(ObserverContext observerContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        stopObservation(observerContext);
    }

    @Override
    public void stopClientObservation(ObserverContext observerContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        stopObservation(observerContext);
    }

    private void startObservation(ObserverContext observerContext) {
        observerContext.addProperty(PROPERTY_START_TIME, System.nanoTime());
        Set<Tag> mainTags = observerContext.getMainTags();
        try {
            getInprogressGauge(mainTags).increment();
        } catch (RuntimeException e) {
            String connectorName = observerContext.getConnectorName();
            handleError(connectorName, mainTags, e);
        }
    }

    private void stopObservation(ObserverContext observerContext) {
        // Connector name must be a part of the metric name to make sure that every metric is unique with
        // the combination of name and tags.
        Set<Tag> mainTags = observerContext.getMainTags();
        Set<Tag> allTags = observerContext.getAllTags();
        try {
            Long startTime = (Long) observerContext.getProperty(PROPERTY_START_TIME);
            long duration = System.nanoTime() - startTime;
            getInprogressGauge(mainTags).decrement();
            metricRegistry.gauge(new MetricId("response_time_seconds", "Response Time",
                    allTags), responseTimeStatisticConfigs).setValue(duration / 1E9);
            metricRegistry.counter(new MetricId("response_time_nanoseconds",
                    "Response Time Total Count", allTags)).increment(duration);
            metricRegistry.counter(new MetricId("requests_total",
                    "Total number of requests", allTags)).increment();
            Boolean error = (Boolean) observerContext.getProperty(PROPERTY_ERROR);
            if (error != null && error) {
                metricRegistry.counter(new MetricId("failed_requests_total",
                        "Total number of failed requests", allTags)).increment();
            }
        } catch (RuntimeException e) {
            String connectorName = observerContext.getConnectorName();
            handleError(connectorName, allTags, e);
        }
    }

    private Gauge getInprogressGauge(Set<Tag> tags) {
        return metricRegistry.gauge(new MetricId("inprogress_requests",
                "Inprogress Requests", tags), StatisticConfig.DEFAULT);
    }

    private void handleError(String connectorName, Set<Tag> tags, RuntimeException e) {
        // Metric Provider may throw exceptions if there is a mismatch in tags.
        consoleError.println("error: error collecting metrics for " + connectorName + " with tags " + tags +
                ": " + e.getMessage());
    }
}
