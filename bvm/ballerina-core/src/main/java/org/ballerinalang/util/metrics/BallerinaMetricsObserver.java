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
        startObservation(observerContext);
    }

    @Override
    public void startClientObservation(ObserverContext observerContext, ObservableContext observableContext) {
        startObservation(observerContext);
    }

    private void startObservation(ObserverContext observerContext) {
        observerContext.addProperty(PROPERTY_START_TIME, System.nanoTime());
    }

    @Override
    public void stopServerObservation(ObserverContext observerContext, ObservableContext observableContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        String[] additionalTags = {TAG_KEY_SERVICE, observerContext.getServiceName(), TAG_KEY_RESOURCE,
                observerContext.getResourceName()};
        stopObservation(observerContext, additionalTags);
    }

    @Override
    public void stopClientObservation(ObserverContext observerContext, ObservableContext observableContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        String[] additionalTags = {TAG_KEY_ACTION, observerContext.getActionName()};
        stopObservation(observerContext, additionalTags);
    }

    private void stopObservation(ObserverContext observerContext, String[] additionalTags) {
        try {
            Long startTime = (Long) observerContext.getProperty(PROPERTY_START_TIME);
            long duration = System.nanoTime() - startTime;
            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size() + additionalTags.length);
            Tags.tags(allTags, observerContext.getTags());
            Tags.tags(allTags, additionalTags);
            // Connector name must be a part of the metric name to make sure that every metric is unique with
            // the combination of name and tags.
            String namePrefix = observerContext.getConnectorName();
            Timer responseTimer = metricRegistry.timer(new MetricId(namePrefix + "_response_time",
                    "Response Time", allTags));
            responseTimer.record(duration, TimeUnit.NANOSECONDS);
            Counter requestsCounter = metricRegistry.counter(new MetricId(namePrefix + "_requests_total",
                    "Total number of requests", allTags));
            requestsCounter.increment();
        } catch (RuntimeException e) {
            // Metric Provider may throw exceptions if there is a mismatch in tags.
            consoleError.println("ballerina: error collecting metrics: " + e.getMessage());
        }
    }
}
