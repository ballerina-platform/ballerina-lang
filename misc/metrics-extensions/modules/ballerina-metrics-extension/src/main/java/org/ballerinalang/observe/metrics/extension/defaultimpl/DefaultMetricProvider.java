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

import io.ballerina.runtime.observability.metrics.Counter;
import io.ballerina.runtime.observability.metrics.Gauge;
import io.ballerina.runtime.observability.metrics.MetricId;
import io.ballerina.runtime.observability.metrics.PolledGauge;
import io.ballerina.runtime.observability.metrics.StatisticConfig;
import io.ballerina.runtime.observability.metrics.spi.MetricProvider;

import java.util.function.ToDoubleFunction;

/**
 * {@link MetricProvider} implementation to provide default Ballerina implementations.
 *
 * @since 0.980.0
 */
public class DefaultMetricProvider implements MetricProvider {

    public DefaultMetricProvider() {
    }

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public void init() {
    }

    @Override
    public Counter newCounter(MetricId metricId) {
        return new DefaultCounter(metricId);
    }

    @Override
    public Gauge newGauge(MetricId metricId, StatisticConfig... statisticConfigs) {
        return new DefaultGauge(metricId, statisticConfigs);
    }

    @Override
    public <T> PolledGauge newPolledGauge(MetricId metricId, T obj, ToDoubleFunction<T> toDoubleFunction) {
        return new DefaultPolledGauge(metricId, obj, toDoubleFunction);
    }

}
