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
package io.ballerina.runtime.observability.metrics;

import io.ballerina.runtime.observability.metrics.spi.MetricProvider;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Registry for keeping metrics by name.
 */
public class MetricRegistry {

    // Metric Provider implementation, which provides actual implementations
    private final MetricProvider metricProvider;
    // Metrics Map by ID
    private final ConcurrentMap<MetricId, Metric> metrics;

    public MetricRegistry(MetricProvider metricProvider) {
        this.metricProvider = metricProvider;
        this.metrics = new ConcurrentHashMap<>();
    }

    /**
     * Use {@link Counter#builder(String)}.
     *
     * @param id The {@link MetricId}.
     * @return A existing or a new {@link Counter} metric.
     */
    public Counter counter(MetricId id) {
        return getOrCreate(id, Counter.class, () -> metricProvider.newCounter(id));
    }

    /**
     * Registers the counter metrics instance.
     *
     * @param counter The {@link Counter} instance.
     * @return A existing or a newly registered {@link Counter} metric.
     */
    public Counter register(Counter counter) {
        return register(counter, Counter.class);
    }

    /**
     * Unregister the counter metrics instance.
     *
     * @param counter The {@link Counter} instance.
     */
    public void unregister(Counter counter) {
        unregister(counter, Counter.class);
    }

    /**
     * Use {@link Gauge#builder(String)}.
     *
     * @param id               The {@link MetricId}.
     * @param statisticConfigs {@link StatisticConfig statistic configurations} to summarize gauge values.
     * @return A existing or a new {@link Gauge} metric.
     */
    public Gauge gauge(MetricId id, StatisticConfig... statisticConfigs) {
        return getOrCreate(id, Gauge.class, () -> metricProvider.newGauge(id, statisticConfigs));
    }

    /**
     * Registers the gauge metrics instance.
     *
     * @param gauge The {@link Gauge} instance.
     * @return A existing or a newly registered {@link Gauge} metric.
     */
    public Gauge register(Gauge gauge) {
        return register(gauge, Gauge.class);
    }

    /**
     * Unregister the gauge metrics instance.
     *
     * @param gauge The {@link Gauge} instance.
     */
    public void unregister(Gauge gauge) {
        unregister(gauge, Gauge.class);
    }

    /**
     * Use {@link PolledGauge#builder(String, Object, ToDoubleFunction)}.
     *
     * @param id            The {@link MetricId}.
     * @param obj           State object used to compute a value.
     * @param valueFunction Function that produces an instantaneous gauge value from the state object.
     * @param <T>           The type of the state object from which the gauge value is extracted.
     * @return A existing or a new {@link PolledGauge} metric.
     */
    public <T> PolledGauge polledGauge(MetricId id, T obj, ToDoubleFunction<T> valueFunction) {
        return getOrCreate(id, PolledGauge.class, () -> metricProvider.newPolledGauge(id, obj, valueFunction));
    }

    /**
     * Registers the polled gauge metrics instance.
     *
     * @param gauge The {@link PolledGauge} instance.
     * @return A existing or a newly registered {@link PolledGauge} metric.
     */
    public PolledGauge register(PolledGauge gauge) {
        return register(gauge, PolledGauge.class);
    }

    /**
     * Unregisters the polled gauge metrics instance.
     *
     * @param gauge The {@link PolledGauge} instance.
     */
    public void unregister(PolledGauge gauge) {
        unregister(gauge, PolledGauge.class);
    }

    private <M extends Metric> M getOrCreate(MetricId id, Class<M> metricClass, Supplier<M> metricSupplier) {
        Metric metric = readMetric(id, metricClass);
        if (metric == null) {
            Metric newMetric = metricSupplier.get();
            return writeMetricIfNotExists(newMetric, metricClass);
        } else {
            return (M) metric;
        }
    }

    private <M extends Metric> M readMetric(MetricId metricId, Class<M> metricClass) {
        Metric existingMetrics = lookup(metricId);
        if (existingMetrics != null) {
            if (metricClass.isInstance(existingMetrics)) {
                return (M) existingMetrics;
            } else {
                throw new IllegalArgumentException(metricId + " is already used for a different type " +
                        "of metric: " + metricClass.getSimpleName());
            }
        }
        return null;
    }

    private <M extends Metric> M writeMetricIfNotExists(Metric metric, Class<M> metricClass) {
        final Metric existing = metrics.putIfAbsent(metric.getId(), metric);
        if (existing != null) {
            if (metricClass.isInstance(existing)) {
                return (M) existing;
            } else {
                throw new IllegalArgumentException(metric.getId() + " is already used for a different type of metric: "
                        + metricClass.getSimpleName());
            }
        }
        return (M) metric;
    }

    private <M extends Metric> M register(Metric registerMetric, Class<M> metricClass) {
        Metric metric = readMetric(registerMetric.getId(), metricClass);
        if (metric == null) {
            return writeMetricIfNotExists(registerMetric, metricClass);
        } else {
            return (M) metric;
        }
    }

    private void unregister(Metric registerMetric, Class metricClass) {
        Metric metric = readMetric(registerMetric.getId(), metricClass);
        if (metric != null) {
            metrics.remove(registerMetric.getId());
        }
    }

    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     */
    public void remove(String name) {
        List<MetricId> ids = metrics.keySet().stream()
                .filter(id -> id.getName().equals(name)).collect(Collectors.toList());
        ids.forEach(metrics::remove);
    }

    public MetricProvider getMetricProvider() {
        return metricProvider;
    }

    public Metric[] getAllMetrics() {
        return this.metrics.values().toArray(new Metric[this.metrics.values().size()]);
    }

    public Metric lookup(MetricId metricId) {
        return metrics.get(metricId);
    }
}
