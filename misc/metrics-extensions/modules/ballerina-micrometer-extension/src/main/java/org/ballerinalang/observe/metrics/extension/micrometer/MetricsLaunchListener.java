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
import org.ballerinalang.observe.metrics.extension.micrometer.spi.MeterRegistryProvider;
import org.ballerinalang.util.LaunchListener;

import java.util.ServiceLoader;

/**
 * Listen to Launcher events and initialize Metrics.
 */
@JavaSPIService("org.ballerinalang.util.LaunchListener")
public class MetricsLaunchListener implements LaunchListener {

    @Override
    public void beforeRunProgram(boolean service) {
        // Initialize Metrics
        ServiceLoader<MeterRegistryProvider> providers = ServiceLoader.load(MeterRegistryProvider.class);
        providers.forEach(provider -> {
            MeterRegistry registry = provider.get();
            // Provider implementation may return null
            if (registry != null) {
                Metrics.addRegistry(registry);
            }
        });

        new ClassLoaderMetrics().bindTo(Metrics.globalRegistry);
        new JvmMemoryMetrics().bindTo(Metrics.globalRegistry);
        new JvmGcMetrics().bindTo(Metrics.globalRegistry);
        new ProcessorMetrics().bindTo(Metrics.globalRegistry);
        new JvmThreadMetrics().bindTo(Metrics.globalRegistry);
        new FileDescriptorMetrics().bindTo(Metrics.globalRegistry);
        new UptimeMetrics().bindTo(Metrics.globalRegistry);
    }

    @Override
    public void afterRunProgram(boolean service) {
    }
}
