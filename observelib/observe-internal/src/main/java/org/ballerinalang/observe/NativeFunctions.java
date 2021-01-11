/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.observe;

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.metrics.BallerinaMetricsObserver;
import io.ballerina.runtime.observability.metrics.DefaultMetricRegistry;
import io.ballerina.runtime.observability.metrics.MetricRegistry;
import io.ballerina.runtime.observability.metrics.spi.MetricProvider;
import io.ballerina.runtime.observability.tracer.BallerinaTracingObserver;
import io.ballerina.runtime.observability.tracer.TracersStore;
import io.ballerina.runtime.observability.tracer.spi.TracerProvider;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.observe.noop.NoOpMetricProvider;
import org.ballerinalang.observe.noop.NoOpTracerProvider;

import java.io.PrintStream;
import java.util.ServiceLoader;

import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_OBSERVABILITY_METRICS_PROVIDER;
import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_OBSERVABILITY_TRACING_PROVIDER;
import static org.ballerinalang.observe.Constants.DEFAULT_METRIC_PROVIDER_NAME;
import static org.ballerinalang.observe.Constants.DEFAULT_TRACING_PROVIDER_NAME;

/**
 * Java inter-op functions called by the ballerina observability internal module.
 */
public class NativeFunctions {
    private static final PrintStream errStream = System.err;

    public static boolean isMetricsEnabled() {
        return ObserveUtils.isMetricsEnabled();
    }

    public static boolean isTracingEnabled() {
        return ObserveUtils.isTracingEnabled();
    }

    public static BError enableMetrics() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();

        // Loading the proper Metrics Provider
        String providerName = configRegistry.getConfigOrDefault(CONFIG_OBSERVABILITY_METRICS_PROVIDER,
                DEFAULT_METRIC_PROVIDER_NAME);
        MetricProvider selectedProvider = null;
        for (MetricProvider providerFactory : ServiceLoader.load(MetricProvider.class)) {
            if (providerName.equalsIgnoreCase(providerFactory.getName())) {
                selectedProvider = providerFactory;
                break;
            }
        }
        if (selectedProvider == null) {
            errStream.println("error: metrics provider " + providerName + " not found");
            selectedProvider = new NoOpMetricProvider();
        }

        try {
            selectedProvider.init();
            DefaultMetricRegistry.setInstance(new MetricRegistry(selectedProvider));
            ObserveUtils.addObserver(new BallerinaMetricsObserver());
            return null;
        } catch (BError e) {
            return e;
        }
    }

    public static BError enableTracing() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();

        // Loading the proper tracing Provider
        String tracerName = configRegistry.getConfigOrDefault(CONFIG_OBSERVABILITY_TRACING_PROVIDER,
                DEFAULT_TRACING_PROVIDER_NAME);
        TracerProvider selectedProvider = null;
        for (TracerProvider providerFactory : ServiceLoader.load(TracerProvider.class)) {
            if (tracerName.equalsIgnoreCase(providerFactory.getName())) {
                selectedProvider = providerFactory;
            }
        }
        if (selectedProvider == null) {
            errStream.println("error: tracer provider " + tracerName + " not found");
            selectedProvider = new NoOpTracerProvider();
        }

        try {
            selectedProvider.init();
            TracersStore.getInstance().setTracerGenerator(selectedProvider);
            ObserveUtils.addObserver(new BallerinaTracingObserver());
            return null;
        } catch (BError e) {
            return e;
        }
    }

    public static void printError(BString message) {
        errStream.println("error: " + message.getValue());
    }
}
