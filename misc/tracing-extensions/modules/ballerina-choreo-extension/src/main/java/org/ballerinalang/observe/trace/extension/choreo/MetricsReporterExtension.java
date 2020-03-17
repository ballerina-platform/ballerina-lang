/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.observe.trace.extension.choreo;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.observability.metrics.Counter;
import org.ballerinalang.jvm.observability.metrics.DefaultMetricRegistry;
import org.ballerinalang.jvm.observability.metrics.Gauge;
import org.ballerinalang.jvm.observability.metrics.Metric;
import org.ballerinalang.jvm.observability.metrics.PercentileValue;
import org.ballerinalang.jvm.observability.metrics.PolledGauge;
import org.ballerinalang.jvm.observability.metrics.Snapshot;
import org.ballerinalang.jvm.observability.metrics.Tag;
import org.ballerinalang.jvm.observability.metrics.spi.MetricReporter;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClient;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClientHolder;
import org.ballerinalang.observe.trace.extension.choreo.logging.LogFactory;
import org.ballerinalang.observe.trace.extension.choreo.logging.Logger;
import org.ballerinalang.observe.trace.extension.choreo.model.ChoreoMetric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static org.ballerinalang.observe.trace.extension.choreo.Constants.EXTENSION_NAME;

/**
 * Ballerina MetricReporter extension for Choreo cloud.
 */
public class MetricsReporterExtension implements MetricReporter, AutoCloseable {
    private static final Logger LOGGER = LogFactory.getLogger();
    private static final int PUBLISH_INTERVAL = 10 * 1000;
    private static final String UP_METRIC_NAME = "up";
    private Task task;

    @Override
    public void init() {
        ChoreoClient choreoClient = ChoreoClientHolder.getChoreoClient(this);
        if (Objects.isNull(choreoClient)) {
            throw BValueCreator.createErrorValue(StringUtils.fromString("Choreo client is not initialized"), null);
        }

        task = new Task(choreoClient);

        Timer time = new Timer("com.wso2.choreo.MetricsReporter-FlushTimer", true);
        time.schedule(task, 0, PUBLISH_INTERVAL);
        LOGGER.info("started publishing metrics to Choreo");
    }

    @Override
    public String getName() {
        return EXTENSION_NAME;
    }

    @Override
    public void close() throws Exception {
        LOGGER.info("sending metrics to Choreo");
        task.report();
    }

    /**
     * Worker which handles periodically publishing metrics to Choreo.
     */
    private static class Task extends TimerTask {
        private ChoreoClient choreoClient;
        private long lastCounterResetTimestamp;

        private Task(ChoreoClient choreoClient) {
            this.choreoClient = choreoClient;
            this.lastCounterResetTimestamp = System.currentTimeMillis();
        }

        @Override
        public void run() {
            this.report();
        }

        public void report() {
            List<ChoreoMetric> choreoMetrics = new ArrayList<>();
            long currentTimestamp = System.currentTimeMillis();
            Metric[] metrics = DefaultMetricRegistry.getInstance().getAllMetrics();
            for (Metric metric : metrics) {
                Map<String, String> tags = new HashMap<>(metric.getId().getTags().size() + 1);
                for (Tag tag : metric.getId().getTags()) {
                    tags.put(tag.getKey(), tag.getValue());
                }
                String metricName = metric.getId().getName();
                if (metric instanceof Counter) {
                    Counter counter = (Counter) metric;
                    // TODO: Request an atomic get and reset method to avoid loosing metrics
                    long value = counter.getValue();
                    counter.reset();
                    tags.put("timeWindow", String.valueOf(currentTimestamp - lastCounterResetTimestamp));
                    ChoreoMetric counterMetric = new ChoreoMetric(currentTimestamp, metricName, value, tags);
                    choreoMetrics.add(counterMetric);
                } else if (metric instanceof Gauge) {
                    for (Snapshot snapshot : ((Gauge) metric).getSnapshots()) {
                        Map<String, String> timeWindowTags = new HashMap<>(tags.size());
                        timeWindowTags.putAll(tags);
                        timeWindowTags.put("timeWindow", String.valueOf(snapshot.getTimeWindow().toMillis()));

                        ChoreoMetric meanMetric = new ChoreoMetric(currentTimestamp, metricName + "_mean",
                                snapshot.getMean(), timeWindowTags);
                        choreoMetrics.add(meanMetric);

                        ChoreoMetric maxMetric = new ChoreoMetric(currentTimestamp, metricName + "_max",
                                snapshot.getMean(), timeWindowTags);
                        choreoMetrics.add(maxMetric);

                        ChoreoMetric minMetric = new ChoreoMetric(currentTimestamp, metricName + "_min",
                                snapshot.getMin(), timeWindowTags);
                        choreoMetrics.add(minMetric);

                        ChoreoMetric stdDevMetric = new ChoreoMetric(currentTimestamp, metricName + "_stdDev",
                                snapshot.getStdDev(), timeWindowTags);
                        choreoMetrics.add(stdDevMetric);

                        for (PercentileValue percentileValue : snapshot.getPercentileValues()) {
                            Map<String, String> percentileTags = new HashMap<>(timeWindowTags.size() + 1);
                            percentileTags.putAll(timeWindowTags);
                            percentileTags.put("percentile", String.valueOf(percentileValue.getPercentile()));

                            ChoreoMetric percentileMetric = new ChoreoMetric(currentTimestamp, metricName,
                                    percentileValue.getValue(), percentileTags);
                            choreoMetrics.add(percentileMetric);
                        }
                    }
                } else if (metric instanceof PolledGauge) {
                    ChoreoMetric polledGaugeMetric = new ChoreoMetric(currentTimestamp, metricName,
                            ((PolledGauge) metric).getValue(), tags);
                    choreoMetrics.add(polledGaugeMetric);
                }
            }
            // Adding up metric to keep track of service uptime
            ChoreoMetric polledGaugeMetric = new ChoreoMetric(currentTimestamp, UP_METRIC_NAME,
                    1.0, Collections.emptyMap());
            choreoMetrics.add(polledGaugeMetric);

            try {
                choreoClient.publishMetrics(choreoMetrics.toArray(new ChoreoMetric[0]));
            } catch (Throwable t) {
                LOGGER.error("failed to publish metrics to Choreo due to " + t.getMessage());
            }
            lastCounterResetTimestamp = currentTimestamp;
        }
    }
}
