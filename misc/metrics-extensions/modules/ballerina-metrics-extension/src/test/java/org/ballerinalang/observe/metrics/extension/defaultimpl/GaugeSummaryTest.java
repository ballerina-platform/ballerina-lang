/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observe.metrics.extension.defaultimpl;

import io.ballerina.runtime.observability.metrics.Gauge;
import io.ballerina.runtime.observability.metrics.MetricRegistry;
import io.ballerina.runtime.observability.metrics.PercentileValue;
import io.ballerina.runtime.observability.metrics.Snapshot;
import io.ballerina.runtime.observability.metrics.StatisticConfig;
import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.DoubleRecorder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for summarization in {@link Gauge}.
 *
 * @since 0.980.0
 */
public class GaugeSummaryTest {

    private MetricRegistry metricRegistry;

    @BeforeClass
    public void init() {
        DefaultMetricProvider metricProvider = new DefaultMetricProvider();
        metricRegistry = new MetricRegistry(metricProvider);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStatisticConfigNegativePercentilePrecision() {
        StatisticConfig.builder().percentilePrecision(-1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStatisticConfigTooBigPercentilePrecision() {
        StatisticConfig.builder().percentilePrecision(6);
    }

    @Test
    public void testStatisticConfigPercentilePrecision() {
        for (int digits = 0; digits < 6; digits++) {
            StatisticConfig.builder().percentilePrecision(digits);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStatisticConfigNegativePercentiles() {
        StatisticConfig.builder().percentiles(0.1, -0.2, 0.3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStatisticConfigTooBigPercentiles() {
        StatisticConfig.builder().percentiles(0.1, 0.2, 1.1);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testStatisticConfigNullPercentiles() {
        StatisticConfig.builder().percentiles((double[]) null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStatisticConfigEmptyPercentiles() {
        StatisticConfig.builder().percentiles();
    }

    @Test
    public void testGaugeSnapshot() {
        double[] percentiles = new double[]{0.0, 0.5, 0.75, 0.9, 0.98, 0.99, 0.999, 1.0};
        StatisticConfig statisticConfig = StatisticConfig.builder().percentiles(percentiles).percentilePrecision(2)
                .build();
        Gauge gauge = Gauge.builder("test_gauge_percentiles").description("Test Gauge")
                .summarize(statisticConfig)
                .register(metricRegistry);
        int nSamples = 1_000_000; // simulate one million samples
        DoubleRecorder recorder = new DoubleRecorder(statisticConfig.getPercentilePrecision());
        for (int i = 1; i <= nSamples; i++) {
            // In this test, we observe the numbers from 1 to nSamples,
            gauge.setValue(i);
            recorder.recordValue(i);
        }
        DoubleHistogram histogram = recorder.getIntervalHistogram();
        Snapshot snapshot = gauge.getSnapshots()[0];
        Assert.assertTrue(histogram.valuesAreEquivalent(snapshot.getMin(), histogram.getMinValue()));
        Assert.assertTrue(histogram.valuesAreEquivalent(snapshot.getMax(), histogram.getMaxValue()));
        Assert.assertTrue(histogram.valuesAreEquivalent(snapshot.getStdDev(), histogram.getStdDeviation()));
        Assert.assertTrue(histogram.valuesAreEquivalent(snapshot.getMean(), histogram.getMean()));
        Assert.assertEquals(snapshot.getMin(), histogram.getMinValue());
        Assert.assertEquals(snapshot.getMean(), histogram.getMean());
        Assert.assertEquals(snapshot.getStdDev(), histogram.getStdDeviation());
        Assert.assertEquals(snapshot.getMax(), histogram.getMaxValue());
        PercentileValue[] percentileValues = snapshot.getPercentileValues();
        for (int i = 0; i < percentiles.length; i++) {
            Assert.assertEquals(percentileValues[i].getPercentile(), percentiles[i]);
            Assert.assertEquals(percentileValues[i].getValue(), histogram.getValueAtPercentile(percentiles[i] * 100));
        }
    }
}
