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

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.LaunchListener;
import org.ballerinalang.util.metrics.noop.NoOpMetricProvider;
import org.ballerinalang.util.metrics.spi.MetricProvider;
import org.ballerinalang.util.observability.ObservabilityUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.ServiceLoader;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TABLE_METRICS;

/**
 * Listen to Launcher events and initialize Metrics.
 */
@JavaSPIService("org.ballerinalang.util.LaunchListener")
public class MetricsLaunchListener implements LaunchListener {

    private static final String METRIC_PROVIDER_NAME = CONFIG_TABLE_METRICS + ".provider";

    @Override
    public void beforeRunProgram(boolean service) {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        if (!configRegistry.getAsBoolean(CONFIG_METRICS_ENABLED)) {
            // Create default MetricRegistry with NoOpMetricProvider
            DefaultMetricRegistry.setInstance(new MetricRegistry(new NoOpMetricProvider()));
            return;
        }
        String providerName = configRegistry.getAsString(METRIC_PROVIDER_NAME);
        // Look for MetricProvider implementations
        Iterator<MetricProvider> metricProviders = ServiceLoader.load(MetricProvider.class).iterator();
        MetricProvider metricProvider = null;
        while (metricProviders.hasNext()) {
            MetricProvider temp = metricProviders.next();
            if (providerName != null && providerName.equalsIgnoreCase(temp.getName())) {
                metricProvider = temp;
                break;
            } else {
                if (!NoOpMetricProvider.class.isInstance(temp)) {
                    metricProvider = temp;
                    break;
                }
            }
        }
        if (metricProvider == null) {
            metricProvider = new NoOpMetricProvider();
        }

        // Initialize Metric Provider
        metricProvider.initialize();
        // Create default MetricRegistry
        DefaultMetricRegistry.setInstance(new MetricRegistry(metricProvider));
        // Register Ballerina specific metrics
        registerBallerinaMetrics();

        // Add Metrics Observer
        ObservabilityUtils.addObserver(new BallerinaMetricsObserver());
    }

    private void registerBallerinaMetrics() {
        final BLangScheduler.SchedulerStats schedulerStats = BLangScheduler.getStats();
        final String prefix = "ballerina_scheduler_";
        CallbackGauge.builder(prefix + "ready_worker_count", schedulerStats,
                BLangScheduler.SchedulerStats::getReadyWorkerCount).register();
        CallbackGauge.builder(prefix + "running_worker_count", schedulerStats,
                BLangScheduler.SchedulerStats::getRunningWorkerCount).register();
        CallbackGauge.builder(prefix + "excepted_worker_count", schedulerStats,
                BLangScheduler.SchedulerStats::getExceptedWorkerCount).register();
        CallbackGauge.builder(prefix + "paused_worker_count", schedulerStats,
                BLangScheduler.SchedulerStats::getPausedWorkerCount).register();
        CallbackGauge.builder(prefix + "waiting_for_response_worker_count", schedulerStats,
                BLangScheduler.SchedulerStats::getWaitingForResponseWorkerCount).register();
        CallbackGauge.builder(prefix + "waiting_for_lock_worker_count", schedulerStats,
                BLangScheduler.SchedulerStats::getWaitingForLockWorkerCount).register();
    }

    @Override
    public void afterRunProgram(boolean service) {
        if (service) {
            Gauge gauge = Gauge.builder("startup_time_milliseconds")
                    .description("Startup time in milliseconds").register();
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            gauge.set(System.currentTimeMillis() - runtimeMXBean.getStartTime());
        }
    }
}
