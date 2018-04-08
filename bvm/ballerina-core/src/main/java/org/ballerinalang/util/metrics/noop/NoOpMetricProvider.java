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
package org.ballerinalang.util.metrics.noop;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.util.metrics.CallbackGauge;
import org.ballerinalang.util.metrics.Counter;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.MetricId;
import org.ballerinalang.util.metrics.Summary;
import org.ballerinalang.util.metrics.Timer;
import org.ballerinalang.util.metrics.spi.MetricProvider;

import java.util.function.ToDoubleFunction;

/**
 * Provide No-Op implementations of metrics.
 */
@JavaSPIService("org.ballerinalang.util.metrics.spi.MetricProvider")
public class NoOpMetricProvider implements MetricProvider {

    @Override
    public String getName() {
        return "NoOp";
    }

    @Override
    public Counter newCounter(MetricId metricId) {
        return new NoOpCounter(metricId);
    }

    @Override
    public Gauge newGauge(MetricId metricId) {
        return new NoOpGauge(metricId);
    }

    @Override
    public <T> CallbackGauge newCallbackGauge(MetricId metricId, T obj, ToDoubleFunction<T> toDoubleFunction) {
        return new NoOpCallbackGauge(metricId);
    }

    @Override
    public Summary newSummary(MetricId metricId) {
        return null;
    }

    @Override
    public Timer newTimer(MetricId metricId) {
        return null;
    }

}
