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
package org.ballerinalang.util.metrics;

import org.ballerinalang.util.metrics.spi.MetricProvider;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.StampedLock;
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
    // Lock used to read and write to metrics maps
    private final StampedLock stampedLock;

    public MetricRegistry(MetricProvider metricProvider) {
        this.metricProvider = metricProvider;
        this.metrics = new ConcurrentHashMap<>();
        this.stampedLock = new StampedLock();
    }

    /**
     * Use {@link Counter#builder(String)}.
     *
     * @param id The {@link MetricId}.
     * @return A existing or a new {@link Counter} metric.
     */
    // This is method is only allowed to be used within package.
    Counter counter(MetricId id) {
        return getOrCreate(id, Counter.class, () -> metricProvider.newCounter(id));
    }

    /**
     * Use {@link Gauge#builder(String)}.
     *
     * @param id The {@link MetricId}.
     * @return A existing or a new {@link Gauge} metric.
     */
    Gauge gauge(MetricId id) {
        return getOrCreate(id, Gauge.class, () -> metricProvider.newGauge(id));
    }

    /**
     * Use {@link CallbackGauge#builder(String, Object, ToDoubleFunction)}.
     *
     * @param id            The {@link MetricId}.
     * @param obj           State object used to compute a value.
     * @param valueFunction Function that produces an instantaneous gauge value from the state object.
     * @param <T>           The type of the state object from which the gauge value is extracted.
     * @return A existing or a new {@link CallbackGauge} metric.
     */
    <T> CallbackGauge callbackGauge(MetricId id, T obj, ToDoubleFunction<T> valueFunction) {
        return getOrCreate(id, CallbackGauge.class, () -> metricProvider.newCallbackGauge(id, obj, valueFunction));
    }

    /**
     * Use {@link Summary#builder(String)}.
     *
     * @param id The {@link MetricId}.
     * @return A existing or a new {@link Summary} metric.
     */
    Summary summary(MetricId id) {
        return getOrCreate(id, Summary.class, () -> metricProvider.newSummary(id));
    }

    /**
     * Use {@link Timer#builder(String)}.
     *
     * @param id The {@link MetricId}.
     * @return A existing or a new {@link Timer} metric.
     */
    Timer timer(MetricId id) {
        return getOrCreate(id, Timer.class, () -> metricProvider.newTimer(id));
    }

    private <M extends Metric> M getOrCreate(MetricId id, Class<M> metricClass, Supplier<M> metricSupplier) {
        long stamp = stampedLock.tryOptimisticRead();
        Metric metric = metrics.get(id);
        if (!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                metric = metrics.get(id);
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        if (metric != null) {
            if (metricClass.isInstance(metric)) {
                return (M) metric;
            }
        }
        stamp = stampedLock.writeLock();
        try {
            Metric newMetric = metricSupplier.get();
            final Metric existing = metrics.putIfAbsent(id, newMetric);
            if (existing != null) {
                if (metricClass.isInstance(existing)) {
                    return (M) existing;
                } else {
                    throw new IllegalArgumentException(id + " is already used for a different type of metric: "
                            + metricClass.getSimpleName());
                }
            }
            return (M) newMetric;
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     */
    public void remove(String name) {
        long stamp = stampedLock.writeLock();
        try {
            List<MetricId> ids = metrics.keySet().stream()
                    .filter(id -> id.getName().equals(name)).collect(Collectors.toList());
            ids.forEach(id -> metrics.remove(id));
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }
}
