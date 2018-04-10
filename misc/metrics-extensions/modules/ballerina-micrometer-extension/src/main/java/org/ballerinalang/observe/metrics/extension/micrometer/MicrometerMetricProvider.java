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
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.observe.metrics.extension.micrometer.spi.MeterRegistryProvider;
import org.ballerinalang.util.metrics.CallbackGauge;
import org.ballerinalang.util.metrics.Counter;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.MetricId;
import org.ballerinalang.util.metrics.Summary;
import org.ballerinalang.util.metrics.Timer;
import org.ballerinalang.util.metrics.spi.MetricProvider;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.ToDoubleFunction;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TABLE_METRICS;

/**
 * {@link MetricProvider} implementation to provide Micrometer based metrics.
 */
@JavaSPIService("org.ballerinalang.util.metrics.spi.MetricProvider")
public class MicrometerMetricProvider implements MetricProvider {

    private MeterRegistry meterRegistry;
    private static final String METER_REGISTRY_NAME = CONFIG_TABLE_METRICS + ".micrometer_registry";

    public MicrometerMetricProvider() {
    }

    @Override
    public String getName() {
        return "Micrometer";
    }

    @Override
    public void initialize() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        String registryName = configRegistry.getConfiguration(METER_REGISTRY_NAME);
        // Look for MeterRegistryProvider implementations
        Iterator<MeterRegistryProvider> meterRegistryProviders = ServiceLoader.load(MeterRegistryProvider.class)
                .iterator();
        MeterRegistryProvider meterRegistryProvider = null;
        while (meterRegistryProviders.hasNext()) {
            MeterRegistryProvider temp = meterRegistryProviders.next();
            if (registryName != null && registryName.equalsIgnoreCase(temp.getName())) {
                meterRegistryProvider = temp;
                break;
            } else {
                // Use a random provider
                meterRegistryProvider = temp;
            }
        }
        MeterRegistry meterRegistry = null;
        if (meterRegistryProvider != null) {
            meterRegistry = meterRegistryProvider.get();
        }
        if (meterRegistry == null) {
            // This is a CompositeMeterRegistry and it is like a no-op registry when there are no other registries.
            meterRegistry = Metrics.globalRegistry;
        }

        // Register system metrics
        new ClassLoaderMetrics().bindTo(meterRegistry);
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);
        new FileDescriptorMetrics().bindTo(meterRegistry);
        new UptimeMetrics().bindTo(meterRegistry);

        this.meterRegistry = meterRegistry;
    }

    @Override
    public Counter newCounter(MetricId metricId) {
        return new MicrometerCounter(meterRegistry, metricId);
    }

    @Override
    public Gauge newGauge(MetricId metricId) {
        return new MicrometerGauge(meterRegistry, metricId);
    }

    @Override
    public <T> CallbackGauge newCallbackGauge(MetricId metricId, T obj, ToDoubleFunction<T> toDoubleFunction) {
        return new MicrometerCallbackGauge(meterRegistry, metricId, obj, toDoubleFunction);
    }

    @Override
    public Summary newSummary(MetricId metricId) {
        return new MicrometerSummary(meterRegistry, metricId);
    }

    @Override
    public Timer newTimer(MetricId metricId) {
        return new MicrometerTimer(meterRegistry, metricId);
    }
}
