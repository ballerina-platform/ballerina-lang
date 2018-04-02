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
package org.ballerinalang.util.observability;

import org.ballerinalang.config.ConfigRegistry;

/**
 * Read observability generic configurations.
 */
public class ObservabilityConfig {

    private static final ObservabilityConfig INSTANCE = new ObservabilityConfig();

    private final ConfigRegistry configRegistry = ConfigRegistry.getInstance();

    private final boolean metricsEnabled;
    private final boolean tracingEnabled;

    private ObservabilityConfig() {
        metricsEnabled = getEnabledFlag(ObservabilityConstants.CONFIG_METRICS_ENABLED);
        tracingEnabled = getEnabledFlag(ObservabilityConstants.CONFIG_TRACING_ENABLED);
    }

    private boolean getEnabledFlag(String key) {
        return Boolean.valueOf(configRegistry.getConfigOrDefault(key, String.valueOf(Boolean.FALSE)));
    }

    public static ObservabilityConfig getInstance() {
        return INSTANCE;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public boolean isTracingEnabled() {
        return tracingEnabled;
    }
}
