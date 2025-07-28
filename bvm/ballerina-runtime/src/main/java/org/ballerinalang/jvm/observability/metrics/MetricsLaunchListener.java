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
package org.ballerinalang.jvm.observability.metrics;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.launch.LaunchListener;
import org.ballerinalang.jvm.observability.InvalidConfigurationException;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.metrics.noop.NoOpMetricProvider;
import org.ballerinalang.jvm.observability.metrics.noop.NoOpMetricReporter;
import org.ballerinalang.jvm.observability.metrics.spi.MetricProvider;
import org.ballerinalang.jvm.observability.metrics.spi.MetricReporter;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.ServiceLoader;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TABLE_METRICS;

/**
 * Listen to Launcher events and initialize Metrics.
 */
public class MetricsLaunchListener implements LaunchListener {

    private static final PrintStream consoleError = System.err;
    private static final String METRIC_PROVIDER_NAME = CONFIG_TABLE_METRICS + ".provider";
    private static final String METRIC_REPORTER_NAME = CONFIG_TABLE_METRICS + ".reporter";
    private static final String DEFAULT_METRIC_PROVIDER_NAME = "default";
    private static final String DEFAULT_METRIC_REPORTER_NAME = "prometheus";

    @Override
    public void beforeRunProgram(boolean service) {
        if (DefaultMetricRegistry.getInstance() == null || DefaultMetricRegistry.isNoOp) {
            ConfigRegistry configRegistry = ConfigRegistry.getInstance();
            if (!configRegistry.isInitialized()) {
                // If config registry is not initialized, skip setting metric registry as well.
                return;
            }
            if (!configRegistry.getAsBoolean(CONFIG_METRICS_ENABLED)) {
                // Create default MetricRegistry with NoOpMetricProvider
                DefaultMetricRegistry.setInstance(new MetricRegistry(new NoOpMetricProvider()));
                DefaultMetricRegistry.isNoOp = true;
                return;
            }

            //load metric provider configured
            MetricProvider metricProvider = loadMetricProvider(configRegistry);
            // Initialize Metric Provider
            metricProvider.init();
            // Create default MetricRegistry
            DefaultMetricRegistry.setInstance(new MetricRegistry(metricProvider));
            DefaultMetricRegistry.isNoOp = false;
            // Register Ballerina specific metrics
            registerBallerinaMetrics();
            //load metric reporter configured
            MetricReporter reporter = loadMetricReporter(configRegistry);
            //initialize metric reporter
            try {
                reporter.init();
            } catch (InvalidConfigurationException e) {
                consoleError.println("Invalid configuration error when initializing metrics reporter. "
                        + e.getMessage());
            }

            // Add Metrics Observer
            ObserveUtils.addObserver(new BallerinaMetricsObserver());
        }
    }

    private MetricProvider loadMetricProvider(ConfigRegistry configRegistry) {
        String providerName = configRegistry.getConfigOrDefault(METRIC_PROVIDER_NAME, DEFAULT_METRIC_PROVIDER_NAME);
        // Look for MetricProvider implementations
        Iterator<MetricProvider> metricProviders = ServiceLoader.load(MetricProvider.class).iterator();
        while (metricProviders.hasNext()) {
            MetricProvider temp = metricProviders.next();
            if (providerName != null && providerName.equalsIgnoreCase(temp.getName())) {
                return temp;
            }
        }
        return new NoOpMetricProvider();
    }

    private MetricReporter loadMetricReporter(ConfigRegistry configRegistry) {
        String reporterName = configRegistry.getConfigOrDefault(METRIC_REPORTER_NAME, DEFAULT_METRIC_REPORTER_NAME);
        // Look for MetricProvider implementations
        Iterator<MetricReporter> metricReporters = ServiceLoader.load(MetricReporter.class).iterator();
        while (metricReporters.hasNext()) {
            MetricReporter temp = metricReporters.next();
            if (reporterName != null && reporterName.equalsIgnoreCase(temp.getName())) {
                return temp;
            }
        }
        return new NoOpMetricReporter();
    }

    private void registerBallerinaMetrics() {
//        final BLangScheduler.SchedulerStats schedulerStats = BLangScheduler.getStats();
//        final String prefix = "ballerina_scheduler_";
//        PolledGauge.builder(prefix + "ready_worker_count", schedulerStats,
//                BLangScheduler.SchedulerStats::getReadyWorkerCount).register();
//        PolledGauge.builder(prefix + "running_worker_count", schedulerStats,
//                BLangScheduler.SchedulerStats::getRunningWorkerCount).register();
//        PolledGauge.builder(prefix + "excepted_worker_count", schedulerStats,
//                BLangScheduler.SchedulerStats::getExceptedWorkerCount).register();
//        PolledGauge.builder(prefix + "paused_worker_count", schedulerStats,
//                BLangScheduler.SchedulerStats::getPausedWorkerCount).register();
//        PolledGauge.builder(prefix + "waiting_for_response_worker_count", schedulerStats,
//                BLangScheduler.SchedulerStats::getWaitingForResponseWorkerCount).register();
//        PolledGauge.builder(prefix + "waiting_for_lock_worker_count", schedulerStats,
//                BLangScheduler.SchedulerStats::getWaitingForLockWorkerCount).register();
    }

    @Override
    public void afterRunProgram(boolean service) {
        if (service) {
            Gauge gauge = Gauge.builder("startup_time_milliseconds")
                    .description("Startup time in milliseconds").register();
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            gauge.setValue(System.currentTimeMillis() - runtimeMXBean.getStartTime());
        }
    }
}
