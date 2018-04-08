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
import org.ballerinalang.util.metrics.MetricId;
import org.ballerinalang.util.metrics.Summary;

import java.util.stream.Collectors;

/**
 * An implementation of {@link Summary} using Micrometer.
 */
public class MicrometerSummary extends AbstractMetric implements Summary {

    private final io.micrometer.core.instrument.DistributionSummary summary;

    public MicrometerSummary(MeterRegistry meterRegistry, MetricId id) {
        super(id);
        summary = io.micrometer.core.instrument.DistributionSummary.builder(id.getName())
                .description(id.getDescription())
                .tags(id.getTags().stream().map(tag -> Tag.of(tag.getKey(), tag.getValue()))
                        .collect(Collectors.toList()))
                .publishPercentiles(0.5, 0.75, 0.98, 0.99, 0.999)
                .register(meterRegistry);
    }

    @Override
    public void record(double amount) {
        summary.record(amount);
    }

    @Override
    public long count() {
        return summary.count();
    }

    @Override
    public double mean() {
        return summary.mean();
    }

    @Override
    public double max() {
        return summary.max();
    }

    @Override
    public double percentile(double percentile) {
        return summary.percentile(percentile);
    }
}
