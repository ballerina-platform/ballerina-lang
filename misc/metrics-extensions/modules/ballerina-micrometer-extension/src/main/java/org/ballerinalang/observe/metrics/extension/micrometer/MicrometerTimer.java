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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.ballerinalang.util.metrics.AbstractMetric;
import org.ballerinalang.util.metrics.MetricId;
import org.ballerinalang.util.metrics.Timer;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An implementation of {@link Timer} using Micrometer.
 */
public class MicrometerTimer extends AbstractMetric implements Timer {

    private final io.micrometer.core.instrument.Timer timer;

    public MicrometerTimer(MeterRegistry meterRegistry, MetricId id) {
        super(id);
        timer = io.micrometer.core.instrument.Timer.builder(id.getName())
                .description(id.getDescription())
                .tags(id.getTags().stream().map(tag -> Tag.of(tag.getKey(), tag.getValue()))
                        .collect(Collectors.toList()))
                .publishPercentiles(0.5, 0.75, 0.98, 0.99, 0.999)
                .register(meterRegistry);
    }

    @Override
    public void record(long amount, TimeUnit unit) {
        timer.record(amount, unit);
    }

    @Override
    public long count() {
        return timer.count();
    }

    @Override
    public double mean(TimeUnit unit) {
        return timer.mean(unit);
    }

    @Override
    public double max(TimeUnit unit) {
        return timer.max(unit);
    }

    @Override
    public SortedMap<Double, Double> percentileValues(TimeUnit unit) {
        SortedMap<Double, Double> result = new TreeMap<>();
        HistogramSnapshot snapshot = timer.takeSnapshot();
        for (ValueAtPercentile valueAtPercentile : snapshot.percentileValues()) {
            result.put(valueAtPercentile.percentile(), valueAtPercentile.value(unit));
        }
        return Collections.unmodifiableSortedMap(result);
    }
}
