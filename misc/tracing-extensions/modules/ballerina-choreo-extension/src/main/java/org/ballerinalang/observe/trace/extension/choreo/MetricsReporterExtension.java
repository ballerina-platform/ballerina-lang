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

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.observability.metrics.Counter;
import io.ballerina.runtime.observability.metrics.DefaultMetricRegistry;
import io.ballerina.runtime.observability.metrics.Gauge;
import io.ballerina.runtime.observability.metrics.Metric;
import io.ballerina.runtime.observability.metrics.PercentileValue;
import io.ballerina.runtime.observability.metrics.PolledGauge;
import io.ballerina.runtime.observability.metrics.Snapshot;
import io.ballerina.runtime.observability.metrics.Tag;
import io.ballerina.runtime.observability.metrics.spi.MetricReporter;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClient;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClientHolder;
import org.ballerinalang.observe.trace.extension.choreo.client.error.ChoreoClientException;
import org.ballerinalang.observe.trace.extension.choreo.logging.LogFactory;
import org.ballerinalang.observe.trace.extension.choreo.logging.Logger;
import org.ballerinalang.observe.trace.extension.choreo.model.ChoreoMetric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.observe.trace.extension.choreo.Constants.CHOREO_EXTENSION_NAME;

/**
 * Ballerina MetricReporter extension for Choreo cloud.
 *
 * @since 2.0.0
 */
public class MetricsReporterExtension implements MetricReporter, AutoCloseable {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final int PUBLISH_INTERVAL_SECS = 10;
    private static final String UP_METRIC_NAME = "up";
    private static final String TIME_WINDOW_TAG_KEY = "timeWindow";
    private static final String PERCENTILE_TAG_KEY = "percentile";
    private static final String METRIC_MEAN_POSTFIX = "_mean";
    private static final String METRIC_MAX_POSTFIX = "_max";
    private static final String METRIC_MIN_POSTFIX = "_min";
    private static final String METRIC_STD_DEV_POSTFIX = "_stdDev";
    private static final String METRIC_PERCENTILE_POSTFIX = "_percentile";

    private ScheduledExecutorService executorService;
    private Task task;

    @Override
    public void init() {
        ChoreoClient choreoClient;
        try {
            choreoClient = ChoreoClientHolder.getChoreoClient(this);
        } catch (ChoreoClientException e) {
            throw ErrorCreator.createError(
                    StringUtils.fromString("Could not initialize the client. Please check Ballerina configurations."),
                    StringUtils.fromString(e.getMessage()));
        }

        if (Objects.isNull(choreoClient)) {
            throw ErrorCreator.createError(StringUtils.fromString("Choreo client is not initialized"));
        }

        executorService = new ScheduledThreadPoolExecutor(1);
        task = new Task(choreoClient);
        executorService.scheduleWithFixedDelay(task, 0, PUBLISH_INTERVAL_SECS, TimeUnit.SECONDS);
        LOGGER.info("started publishing metrics to Choreo");
    }

    @Override
    public String getName() {
        return CHOREO_EXTENSION_NAME;
    }

    @Override
    public void close() {
        LOGGER.info("sending metrics to Choreo");
        executorService.execute(task);
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("failed to wait for publishing metrics to complete due to " + e.getMessage());
        }
    }

    /**
     * Worker which handles periodically publishing metrics to Choreo.
     */
    private static class Task extends TimerTask {
        private final ChoreoClient choreoClient;
        private long lastCounterResetTimestamp;

        private Task(ChoreoClient choreoClient) {
            this.choreoClient = choreoClient;
            this.lastCounterResetTimestamp = System.currentTimeMillis();
        }

        @Override
        public void run() {
            List<ChoreoMetric> choreoMetrics = new ArrayList<>();
            long currentTimestamp = System.currentTimeMillis();
            Metric[] metrics = DefaultMetricRegistry.getInstance().getAllMetrics();
            for (Metric metric : metrics) {
                String metricName = metric.getId().getName();
                if (metric instanceof Counter) {
                    Map<String, String> tags = generateTagsMap(metric, 1);
                    tags.put(TIME_WINDOW_TAG_KEY, String.valueOf(currentTimestamp - lastCounterResetTimestamp));
                    ChoreoMetric counterMetric = new ChoreoMetric(currentTimestamp, metricName,
                            ((Counter) metric).getValueThenReset(), tags);
                    choreoMetrics.add(counterMetric);
                } else if (metric instanceof Gauge) {
                    Gauge gauge = (Gauge) metric;
                    Map<String, String> tags = generateTagsMap(metric, 0);
                    ChoreoMetric gaugeMetric = new ChoreoMetric(currentTimestamp, metricName, gauge.getValue(), tags);
                    choreoMetrics.add(gaugeMetric);
                    for (Snapshot snapshot : gauge.getSnapshots()) {
                        Map<String, String> snapshotTags = new HashMap<>(tags.size() + 1);
                        snapshotTags.put(TIME_WINDOW_TAG_KEY, String.valueOf(snapshot.getTimeWindow().toMillis()));

                        ChoreoMetric meanMetric = new ChoreoMetric(currentTimestamp, metricName
                                + METRIC_MEAN_POSTFIX, snapshot.getMean(), snapshotTags);
                        choreoMetrics.add(meanMetric);

                        ChoreoMetric maxMetric = new ChoreoMetric(currentTimestamp, metricName
                                + METRIC_MAX_POSTFIX, snapshot.getMax(), snapshotTags);
                        choreoMetrics.add(maxMetric);

                        ChoreoMetric minMetric = new ChoreoMetric(currentTimestamp, metricName
                                + METRIC_MIN_POSTFIX, snapshot.getMin(), snapshotTags);
                        choreoMetrics.add(minMetric);

                        ChoreoMetric stdDevMetric = new ChoreoMetric(currentTimestamp, metricName
                                + METRIC_STD_DEV_POSTFIX, snapshot.getStdDev(), snapshotTags);
                        choreoMetrics.add(stdDevMetric);

                        for (PercentileValue percentileValue : snapshot.getPercentileValues()) {
                            Map<String, String> percentileTags = new HashMap<>(snapshotTags.size() + 1);
                            percentileTags.putAll(snapshotTags);
                            percentileTags.put(PERCENTILE_TAG_KEY, String.valueOf(percentileValue.getPercentile()));

                            ChoreoMetric percentileMetric = new ChoreoMetric(currentTimestamp, metricName
                                    + METRIC_PERCENTILE_POSTFIX, percentileValue.getValue(), percentileTags);
                            choreoMetrics.add(percentileMetric);
                        }
                    }
                } else if (metric instanceof PolledGauge) {
                    Map<String, String> tags = generateTagsMap(metric, 0);
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

        /**
         * Generate tags map form metric.
         *
         * @param metric The metric to generate the tags map from
         * @return The map of tags
         */
        private Map<String, String> generateTagsMap(Metric metric, int requiredAdditionalCapacity) {
            Map<String, String> tags = new HashMap<>(metric.getId().getTags().size()
                    + requiredAdditionalCapacity);
            for (Tag tag : metric.getId().getTags()) {
                tags.put(tag.getKey(), tag.getValue());
            }
            return tags;
        }
    }
}
