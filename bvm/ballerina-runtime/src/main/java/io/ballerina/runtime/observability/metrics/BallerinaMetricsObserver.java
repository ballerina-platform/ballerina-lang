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
package io.ballerina.runtime.observability.metrics;

import io.ballerina.runtime.observability.BallerinaObserver;
import io.ballerina.runtime.observability.ObserverContext;

import java.io.PrintStream;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.observability.ObservabilityConstants.PROPERTY_KEY_HTTP_STATUS_CODE;
import static io.ballerina.runtime.observability.ObservabilityConstants.STATUS_CODE_GROUP_SUFFIX;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE_GROUP;

/**
 * Observe the runtime and collect measurements.
 */
public class BallerinaMetricsObserver implements BallerinaObserver {

    private static final String PROPERTY_START_TIME = "_observation_start_time_";
    private static final String PROPERTY_IN_PROGRESS_COUNTER = "_observation_in_progress_counter_";

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
        startObservation(observerContext);
    }

    @Override
    public void startClientObservation(ObserverContext observerContext) {
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
        Set<Tag> tags = observerContext.getAllTags();
        try {
            Gauge inProgressGauge = metricRegistry.gauge(new MetricId("inprogress_requests", "In-progress requests",
                    tags));
            inProgressGauge.increment();
            /*
             * The in progress counter is stored so that the same counter can be decremted when the observation
             * ends. This is needed as the the program may add tags to the context causing the tags to be
             * different at the end compared to the start.
             */
            observerContext.addProperty(PROPERTY_IN_PROGRESS_COUNTER, inProgressGauge);
        } catch (RuntimeException e) {
            handleError("inprogress_requests", tags, e);
        }
    }

    private void stopObservation(ObserverContext observerContext) {
        Set<Tag> tags = new HashSet<>();
        Map<String, Tag> customTags = observerContext.customMetricTags;
        if (customTags != null) {
            tags.addAll(customTags.values());
        }
        tags.addAll(observerContext.getAllTags());

        // Add status_code_group tag
        Integer statusCode = (Integer) observerContext.getProperty(PROPERTY_KEY_HTTP_STATUS_CODE);
        if (statusCode != null && statusCode > 0) {
            tags.add(Tag.of(TAG_KEY_HTTP_STATUS_CODE_GROUP, (statusCode / 100) + STATUS_CODE_GROUP_SUFFIX));
        }

        try {
            Long startTime = (Long) observerContext.getProperty(PROPERTY_START_TIME);
            long duration = System.nanoTime() - startTime;
            ((Gauge) observerContext.getProperty(PROPERTY_IN_PROGRESS_COUNTER)).decrement();
            metricRegistry.gauge(new MetricId("response_time_seconds",
                    "Response time", tags), responseTimeStatisticConfigs).setValue(duration / 1E9);
            metricRegistry.counter(new MetricId("response_time_nanoseconds_total",
                    "Total response response time for all requests", tags)).increment(duration);
            metricRegistry.counter(new MetricId("requests_total",
                    "Total number of requests", tags)).increment();
        } catch (RuntimeException e) {
            handleError("multiple metrics", tags, e);
        }
    }

    private void handleError(String metricName, Set<Tag> tags, RuntimeException e) {
        // Metric Provider may throw exceptions if there is a mismatch in tags.
        consoleError.println("error: error collecting metrics for " + metricName + " with tags " + tags +
                ": " + e.getMessage());
    }
}
