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

import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.metrics.BallerinaMetricsObserver;
import io.ballerina.runtime.observability.metrics.DefaultMetricRegistry;
import io.ballerina.runtime.observability.metrics.MetricRegistry;
import io.ballerina.runtime.observability.metrics.spi.MetricProvider;
import io.ballerina.runtime.observability.metrics.spi.MetricReporterFactory;
import io.ballerina.runtime.observability.tracer.BallerinaTracingObserver;
import io.ballerina.runtime.observability.tracer.TracersStore;
import io.ballerina.runtime.observability.tracer.spi.TracerProvider;
import io.ballerina.runtime.observability.tracer.spi.TracerProviderFactory;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.observe.noop.NoOpMetricProvider;
import org.ballerinalang.observe.noop.NoOpMetricReporterFactory;
import org.ballerinalang.observe.noop.NoOpTracerProviderFactory;

import java.io.PrintStream;
import java.util.ServiceLoader;

import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_OBSERVABILITY_PROVIDER;
import static org.ballerinalang.observe.Constants.DEFAULT_METRIC_PROVIDER_NAME;
import static org.ballerinalang.observe.Constants.DEFAULT_METRIC_REPORTER_NAME;
import static org.ballerinalang.observe.Constants.DEFAULT_TRACER_NAME;
import static org.ballerinalang.observe.Constants.METRIC_PROVIDER_NAME;
import static org.ballerinalang.observe.Constants.METRIC_PROVIDER_NATIVE_DATA_KEY;
import static org.ballerinalang.observe.Constants.METRIC_REPORTER_NAME;
import static org.ballerinalang.observe.Constants.TRACER_NAME;
import static org.ballerinalang.observe.Constants.TRACER_PROVIDER_NATIVE_DATA_KEY;

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

    public static BObject getMetricReporter() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();

        // Loading the proper Metrics Reporter
        String inheritedMetricsReporterName = configRegistry.getConfigOrDefault(CONFIG_OBSERVABILITY_PROVIDER,
                DEFAULT_METRIC_REPORTER_NAME);
        String reporterName = configRegistry.getConfigOrDefault(METRIC_REPORTER_NAME, inheritedMetricsReporterName);
        MetricReporterFactory selectedReporterFactory = null;
        for (MetricReporterFactory reporterFactory : ServiceLoader.load(MetricReporterFactory.class)) {
            if (reporterName != null && reporterName.equalsIgnoreCase(reporterFactory.getName())) {
                selectedReporterFactory = reporterFactory;
                break;
            }
        }
        if (selectedReporterFactory == null) {
            errStream.println("error: metrics reporter " + reporterName + " not found");
            selectedReporterFactory = new NoOpMetricReporterFactory();
        }
        BObject reporterBObject = selectedReporterFactory.getReporterBObject();

        // Loading the proper Metrics Provider
        String providerName = configRegistry.getConfigOrDefault(METRIC_PROVIDER_NAME, DEFAULT_METRIC_PROVIDER_NAME);
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
        reporterBObject.addNativeData(METRIC_PROVIDER_NATIVE_DATA_KEY, selectedProvider);

        return reporterBObject;
    }

    public static BObject getTracerProvider() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        String inheritedTracingProviderName = configRegistry.getConfigOrDefault(CONFIG_OBSERVABILITY_PROVIDER,
                DEFAULT_TRACER_NAME);
        String tracerName = configRegistry.getConfigOrDefault(TRACER_NAME, inheritedTracingProviderName);

        TracerProviderFactory selectedProviderFactory = null;
        for (TracerProviderFactory providerFactory : ServiceLoader.load(TracerProviderFactory.class)) {
            if (tracerName.equalsIgnoreCase(providerFactory.getName())) {
                selectedProviderFactory = providerFactory;
            }
        }
        if (selectedProviderFactory == null) {
            errStream.println("error: tracer " + tracerName + " not found");
            selectedProviderFactory = new NoOpTracerProviderFactory();
        }
        BObject providerBObject = selectedProviderFactory.getProviderBObject();
        providerBObject.addNativeData(TRACER_PROVIDER_NATIVE_DATA_KEY, selectedProviderFactory.getProvider());
        return providerBObject;
    }

    public static Object enableMetrics(BObject metricProviderBObject) {
        MetricProvider metricProvider =
                (MetricProvider) metricProviderBObject.getNativeData(METRIC_PROVIDER_NATIVE_DATA_KEY);
        metricProvider.init();
        DefaultMetricRegistry.setInstance(new MetricRegistry(metricProvider));

        ObserveUtils.addObserver(new BallerinaMetricsObserver());
        return null;
    }

    public static Object enableTracing(BObject tracerProviderBObject) {
        TracerProvider tracerProvider =
                (TracerProvider) tracerProviderBObject.getNativeData(TRACER_PROVIDER_NATIVE_DATA_KEY);
        TracersStore.getInstance().setTracerGenerator(tracerProvider);

        ObserveUtils.addObserver(new BallerinaTracingObserver());
        return null;
    }

    public static void printError(BString message) {
        errStream.println("error: " + message.getValue());
    }
}
