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
import org.ballerinalang.util.metrics.AbstractMetric;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.MetricId;

import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

/**
 * An implementation of {@link Gauge} using Micrometer.
 */
public class MicrometerGauge extends AbstractMetric implements Gauge {

    private final DoubleAdder value = new DoubleAdder();

    public MicrometerGauge(MeterRegistry meterRegistry, MetricId id) {
        super(id);
        io.micrometer.core.instrument.Gauge.builder(id.getName(), value, DoubleAdder::sum)
                .description(id.getDescription())
                .tags(id.getTags().stream().map(tag -> Tag.of(tag.getKey(), tag.getValue()))
                        .collect(Collectors.toList())).register(meterRegistry);
    }

    @Override
    public void increment(double amount) {
        value.add(amount);
    }

    @Override
    public void decrement(double amount) {
        value.add(-amount);
    }

    @Override
    public void set(double value) {
        synchronized (this) {
            this.value.reset();
            this.value.add(value);
        }
    }

    @Override
    public double get() {
        synchronized (this) {
            return value.sum();
        }
    }
}
