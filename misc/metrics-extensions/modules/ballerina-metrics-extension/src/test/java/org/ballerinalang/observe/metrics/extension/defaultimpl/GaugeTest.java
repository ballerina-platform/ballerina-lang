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
import io.ballerina.runtime.observability.metrics.PolledGauge;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Tests for {@link Gauge}.
 *
 * @since 0.980.0
 */
public class GaugeTest {

    private MetricRegistry metricRegistry;

    @BeforeClass
    public void init() {
        DefaultMetricProvider metricProvider = new DefaultMetricProvider();
        metricRegistry = new MetricRegistry(metricProvider);
    }

    @Test
    public void testGauge() {
        Gauge gauge = Gauge.builder("test_gauge").description("Test Gauge").register(metricRegistry);
        gauge.increment(100D);
        gauge.decrement(50D);
        Assert.assertEquals(gauge.getValue(), 50D);
    }

    @Test
    public void testPolledGauge() {
        List<Integer> list = new ArrayList<>();
        IntStream.range(0, 100).forEach(list::add);
        PolledGauge gauge = PolledGauge.builder("test_polled_gauge", list, List::size).description("")
                .register(metricRegistry);
        Assert.assertEquals(gauge.getValue(), 100D);
    }

    @Test
    public void testGaugeSet() {
        Gauge gauge = Gauge.builder("test_gauge_count_sum").description("Test Gauge")
                .register(metricRegistry);
        gauge.setValue(2);
        Assert.assertEquals(1, gauge.getCount());
        Assert.assertEquals(2.0, gauge.getSum());
        gauge.setValue(2);
        Assert.assertEquals(2, gauge.getCount());
        Assert.assertEquals(4.0, gauge.getSum());
    }

}
