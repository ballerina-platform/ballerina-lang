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
package io.ballerina.runtime.observability.metrics.noop;

import io.ballerina.runtime.observability.metrics.Counter;
import io.ballerina.runtime.observability.metrics.Gauge;
import io.ballerina.runtime.observability.metrics.MetricId;
import io.ballerina.runtime.observability.metrics.PolledGauge;
import io.ballerina.runtime.observability.metrics.StatisticConfig;
import io.ballerina.runtime.observability.metrics.spi.MetricProvider;

import java.util.function.ToDoubleFunction;

/**
 * Provide No-Op implementations of metrics.
 */
public class NoOpMetricProvider implements MetricProvider {
    public static final String NAME = "noop";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init() {
        // Do nothing
    }

    @Override
    public Counter newCounter(MetricId metricId) {
        return new NoOpCounter(metricId);
    }

    @Override
    public Gauge newGauge(MetricId metricId, StatisticConfig... statisticConfigs) {
        return new NoOpGauge(metricId);
    }

    @Override
    public <T> PolledGauge newPolledGauge(MetricId metricId, T obj, ToDoubleFunction<T> toDoubleFunction) {
        return new NoOpPolledGauge(metricId);
    }
}
