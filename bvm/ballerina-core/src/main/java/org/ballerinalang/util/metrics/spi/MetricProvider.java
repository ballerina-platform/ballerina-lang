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
package org.ballerinalang.util.metrics.spi;

import org.ballerinalang.util.metrics.CallbackGauge;
import org.ballerinalang.util.metrics.Counter;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.MetricId;
import org.ballerinalang.util.metrics.MetricRegistry;
import org.ballerinalang.util.metrics.Summary;
import org.ballerinalang.util.metrics.Timer;

import java.util.function.ToDoubleFunction;

/**
 * This represents the Java SPI interface for a metric provider to be used with {@link MetricRegistry}.
 */
public interface MetricProvider {

    /**
     * Returns a unique name of the Metric Provider. This will be used when loading the metric provider implementation
     * in {@link MetricRegistry}
     *
     * @return the Metric Provider name.
     */
    String getName();

    Counter newCounter(MetricId metricId);

    Gauge newGauge(MetricId metricId);

    <T> CallbackGauge newCallbackGauge(MetricId metricId, T obj, ToDoubleFunction<T> toDoubleFunction);

    Summary newSummary(MetricId metricId);

    Timer newTimer(MetricId metricId);

}
