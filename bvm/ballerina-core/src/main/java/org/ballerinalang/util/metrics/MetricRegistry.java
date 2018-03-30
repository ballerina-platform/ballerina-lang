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


import org.ballerinalang.util.metrics.noop.NoOpMetricProvider;
import org.ballerinalang.util.metrics.spi.MetricProvider;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Registry for keeping metrics by name.
 */
public class MetricRegistry {

    private final MetricProvider metricProvider;
    private final ConcurrentMap<MetricId, Metric> metrics;

    /**
     * Lazy initialization for Default {@link MetricRegistry}.
     */
    private static class LazyHolder {
        static final MetricRegistry REGISTRY = new MetricRegistry();
    }

    public static MetricRegistry getDefaultRegistry() {
        return LazyHolder.REGISTRY;
    }

    public MetricRegistry() {
        this(() -> {
            // Look for MetricProvider implementations
            Iterator<MetricProvider> metricProviders = ServiceLoader.load(MetricProvider.class).iterator();
            MetricProvider metricProvider = null;
            while (metricProviders.hasNext()) {
                MetricProvider temp = metricProviders.next();
                if (!NoOpMetricProvider.class.isInstance(temp)) {
                    metricProvider = temp;
                }
            }
            if (metricProvider == null) {
                metricProvider = new NoOpMetricProvider();
            }
            return metricProvider;
        });
    }

    public MetricRegistry(Supplier<MetricProvider> metricProviderSupplier) {
        this(metricProviderSupplier.get());
    }

    public MetricRegistry(MetricProvider metricProvider) {
        this.metrics = new ConcurrentHashMap<>();
        this.metricProvider = metricProvider;
    }

    public Counter counter(MetricId id) {
        return getOrCreate(id, Counter.class, () -> metricProvider.newCounter(id));
    }

    public Gauge gauge(MetricId id) {
        return getOrCreate(id, Gauge.class, () -> metricProvider.newGauge(id));
    }

    public <T> CallbackGauge callbackGauge(MetricId id, T obj, ToDoubleFunction<T> valueFunction) {
        return getOrCreate(id, CallbackGauge.class, () -> metricProvider.newCallbackGauge(id, obj, valueFunction));
    }

    public Summary summary(MetricId id) {
        return getOrCreate(id, Summary.class, () -> metricProvider.newSummary(id));
    }

    public Timer timer(MetricId id) {
        return getOrCreate(id, Timer.class, () -> metricProvider.newTimer(id));
    }

    private <M extends Metric> M getOrCreate(MetricId id, Class<M> metricClass, Supplier<M> metricSupplier) {
        final Metric metric = metrics.get(id);
        if (metric != null) {
            if (metricClass.isInstance(metric)) {
                return (M) metric;
            }
        }
        synchronized (metrics) {
            Metric newMetric = metricSupplier.get();
            final Metric existing = metrics.putIfAbsent(id, newMetric);
            if (existing != null) {
                if (metricClass.isInstance(metric)) {
                    return (M) existing;
                } else {
                    throw new IllegalArgumentException(id + " is already used for a different type of metric");
                }
            }
            return (M) newMetric;
        }
    }

    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     */
    public void remove(String name) {
        List<MetricId> ids = metrics.keySet().stream().filter(id -> id.getName().equals(name)).collect(Collectors.toList());
        ids.forEach(id -> metrics.remove(id));
    }
}
