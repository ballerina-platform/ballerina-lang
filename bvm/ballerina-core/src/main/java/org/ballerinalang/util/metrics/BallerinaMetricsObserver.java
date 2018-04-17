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

import org.ballerinalang.bre.bvm.ObservableContext;
import org.ballerinalang.util.observability.BallerinaObserver;
import org.ballerinalang.util.observability.ObserverContext;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.util.observability.ObservabilityConstants.PROPERTY_ERROR;

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
    public void startServerObservation(ObserverContext observerContext, ObservableContext observableContext) {
        String[] mainTags = {TAG_KEY_SERVICE, observerContext.getServiceName(), TAG_KEY_RESOURCE,
                observerContext.getResourceName()};
        startObservation(observerContext, mainTags);
    }

    @Override
    public void startClientObservation(ObserverContext observerContext, ObservableContext observableContext) {
        String[] mainTags = {TAG_KEY_ACTION, observerContext.getActionName()};
        startObservation(observerContext, mainTags);
    }

    @Override
    public void stopServerObservation(ObserverContext observerContext, ObservableContext observableContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        String[] mainTags = {TAG_KEY_SERVICE, observerContext.getServiceName(), TAG_KEY_RESOURCE,
                observerContext.getResourceName()};
        stopObservation(observerContext, mainTags);
    }

    @Override
    public void stopClientObservation(ObserverContext observerContext, ObservableContext observableContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        String[] mainTags = {TAG_KEY_ACTION, observerContext.getActionName()};
        stopObservation(observerContext, mainTags);
    }

    private void startObservation(ObserverContext observerContext, String[] mainTags) {
        observerContext.addProperty(PROPERTY_START_TIME, System.nanoTime());
        try {
            String connectorName = observerContext.getConnectorName();
            getInprogressGauge(connectorName, mainTags).increment();
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    private void stopObservation(ObserverContext observerContext, String[] mainTags) {
        try {
            Long startTime = (Long) observerContext.getProperty(PROPERTY_START_TIME);
            long duration = System.nanoTime() - startTime;
            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size() + mainTags.length);
            Tags.tags(allTags, observerContext.getTags());
            Tags.tags(allTags, mainTags);
            // Connector name must be a part of the metric name to make sure that every metric is unique with
            // the combination of name and tags.
            String connectorName = observerContext.getConnectorName();
            getInprogressGauge(connectorName, mainTags).decrement();
            metricRegistry.timer(new MetricId(connectorName + "_response_time", "Response Time",
                    allTags)).record(duration, TimeUnit.NANOSECONDS);
            metricRegistry.counter(new MetricId(connectorName + "_requests_total",
                    "Total number of requests", allTags)).increment();
            Boolean error = (Boolean) observerContext.getProperty(PROPERTY_ERROR);
            if (error != null && error) {
                metricRegistry.counter(new MetricId(connectorName + "_failed_requests_total",
                        "Total number of failed requests", allTags)).increment();
            }
        } catch (RuntimeException e) {
            handleError(e);
        }
    }

    private Gauge getInprogressGauge(String connectorName, String[] tags) {
        return Gauge.builder(connectorName + "_inprogress_requests").description("Inprogress Requests")
                .tags(tags).register();
    }

    private void handleError(RuntimeException e) {
        // Metric Provider may throw exceptions if there is a mismatch in tags.
        consoleError.println("ballerina: error collecting metrics: " + e.getMessage());
    }
}
