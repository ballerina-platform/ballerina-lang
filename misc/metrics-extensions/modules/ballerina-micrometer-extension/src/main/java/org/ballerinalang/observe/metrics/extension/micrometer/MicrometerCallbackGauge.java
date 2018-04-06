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
import org.ballerinalang.util.metrics.CallbackGauge;
import org.ballerinalang.util.metrics.MetricId;

import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * An implementation of {@link CallbackGauge} using Micrometer.
 */
public class MicrometerCallbackGauge extends AbstractMetric implements CallbackGauge {

    private final io.micrometer.core.instrument.Gauge gauge;

    public <T> MicrometerCallbackGauge(MeterRegistry meterRegistry, MetricId id, T obj,
                                       ToDoubleFunction<T> toDoubleFunction) {
        super(id);
        gauge = io.micrometer.core.instrument.Gauge.builder(id.getName(), obj, toDoubleFunction)
                .description(id.getDescription())
                .tags(id.getTags().stream().map(tag -> Tag.of(tag.getKey(), tag.getValue()))
                        .collect(Collectors.toList())).register(meterRegistry);
    }


    @Override
    public double get() {
        return gauge.value();
    }
}
