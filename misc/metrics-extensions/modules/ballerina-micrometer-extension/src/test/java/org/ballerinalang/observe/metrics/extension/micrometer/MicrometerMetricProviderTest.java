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
package org.ballerinalang.observe.metrics.extension.micrometer;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.ballerinalang.util.metrics.CallbackGauge;
import org.ballerinalang.util.metrics.Counter;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.MetricRegistry;
import org.ballerinalang.util.metrics.Summary;
import org.ballerinalang.util.metrics.Timer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Tests for {@link MetricRegistry}.
 */
public class MicrometerMetricProviderTest {

    private MetricRegistry metricRegistry;

    @BeforeClass
    public void init() {
        metricRegistry = new MetricRegistry(new MicrometerMetricProvider(
                new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)));
    }

    @Test
    public void testCounter() {
        Counter counter = Counter.builder("test_counter").description("Test Counter").register(metricRegistry);
        counter.increment(100D);
        Assert.assertEquals(counter.count(), 100D);
    }

    @Test
    public void testCounterTags() {
        Counter counter = Counter.builder("test_tags_counter").description("Test Counter Tags")
                .tags("key", "value").register(metricRegistry);
        counter.increment(100D);
        Assert.assertEquals(counter.count(), 100D);
    }

    @Test
    public void testCounterMultipleTags() {
        Counter counter = Counter.builder("test_multiple_tags_counter").description("Test Counter Multiple Tags")
                .tags("key1", "value1", "key2", "value2").register(metricRegistry);
        counter.increment(100D);
        counter = Counter.builder("test_multiple_tags_counter").description("Test Counter Multiple Tags")
                .tags("key2", "value2", "key1", "value1").register(metricRegistry);
        counter.increment(100D);
        Assert.assertEquals(counter.count(), 200D);
    }

    @Test
    public void testCounterDifferentMultipleTags() {
        Counter counter = Counter.builder("test_different_multiple_tags_counter")
                .tags("key1", "value1", "key2", "value2").register(metricRegistry);
        counter.increment(100D);
        Assert.assertEquals(counter.count(), 100D);
        try {
            Counter.builder("test_different_multiple_tags_counter")
                    .tags("key2", "value2", "key3", "value3").register(metricRegistry);
            Assert.fail("Different tags are are not allowed for metrics with same name");
        } catch (IllegalArgumentException e) {
            // Test successful
        }
    }

    @Test
    public void testCounterTagsFailures() {
        try {
            Counter.builder("test_tag_fail_counter").description("Test Counter Tags Failures")
                    .tags("key").register(metricRegistry);
            Assert.fail("Different number of key values are not allowed");
        } catch (IllegalArgumentException e) {
            // Test successful
        }
    }

    @Test
    public void testGauge() {
        Gauge gauge = Gauge.builder("test_gauge").description("Test Gauge").register(metricRegistry);
        gauge.increment(100D);
        gauge.decrement(50D);
        Assert.assertEquals(gauge.get(), 50D);
    }

    @Test
    public void testCallbackGauge() {
        List<Integer> list = new ArrayList();
        IntStream.range(0, 100).forEach(list::add);
        CallbackGauge gauge = CallbackGauge.builder("test_callback_gauge", list, List::size).description("")
                .register(metricRegistry);
        Assert.assertEquals(gauge.get(), 100D);
    }

    @Test
    public void testSummary() {
        Summary summary = Summary.builder("test_summary").description("Test Summary").register(metricRegistry);
        summary.record(100);
        Assert.assertEquals(summary.count(), 1);
    }

    @Test
    public void testTimer() {
        Timer timer = Timer.builder("test_timer").description("Test Timer").tag("method", "record")
                .register(metricRegistry);
        timer.record(100, TimeUnit.NANOSECONDS);
        Assert.assertEquals(timer.count(), 1);
    }

}
