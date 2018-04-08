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
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.LaunchListener;
import org.ballerinalang.util.observability.ObservabilityUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;

/**
 * Listen to Launcher events and initialize Metrics.
 */
@JavaSPIService("org.ballerinalang.util.LaunchListener")
public class MetricsLaunchListener implements LaunchListener {

    @Override
    public void beforeRunProgram(boolean service) {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        if (Boolean.valueOf(configRegistry.getConfigOrDefault(CONFIG_METRICS_ENABLED, String.valueOf(Boolean.FALSE)))) {
            ObservabilityUtils.addObserver(new BallerinaMetricsObserver());
        }
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
