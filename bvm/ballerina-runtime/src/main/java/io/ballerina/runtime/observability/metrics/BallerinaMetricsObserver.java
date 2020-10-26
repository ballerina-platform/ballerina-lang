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
import java.util.Set;

/**
 * Observe the runtime and collect measurements.
 */
public class BallerinaMetricsObserver implements BallerinaObserver {

    private static final String PROPERTY_START_TIME = "_observation_start_time_";

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
        Set<Tag> mainTags = observerContext.getMainTags();
        try {
            getInprogressGauge(mainTags).increment();
        } catch (RuntimeException e) {
            handleError("inprogress_requests", mainTags, e);
        }
    }

    private void stopObservation(ObserverContext observerContext) {
        Set<Tag> mainTags = observerContext.getMainTags();
        Set<Tag> allTags = observerContext.getAllTags();
        try {
            Long startTime = (Long) observerContext.getProperty(PROPERTY_START_TIME);
            long duration = System.nanoTime() - startTime;
            getInprogressGauge(mainTags).decrement();
            metricRegistry.gauge(new MetricId("response_time_seconds", "Response time",
                    allTags), responseTimeStatisticConfigs).setValue(duration / 1E9);
            metricRegistry.counter(new MetricId("response_time_nanoseconds_total",
                    "Total response response time for all requests", allTags)).increment(duration);
            metricRegistry.counter(new MetricId("requests_total",
                    "Total number of requests", allTags)).increment();
        } catch (RuntimeException e) {
            handleError("multiple metrics", allTags, e);
        }
    }

    private Gauge getInprogressGauge(Set<Tag> tags) {
        return metricRegistry.gauge(new MetricId("inprogress_requests", "In-progress requests", tags));
    }

    private void handleError(String metricName, Set<Tag> tags, RuntimeException e) {
        // Metric Provider may throw exceptions if there is a mismatch in tags.
        consoleError.println("error: error collecting metrics for " + metricName + " with tags " + tags +
                ": " + e.getMessage());
    }
}
