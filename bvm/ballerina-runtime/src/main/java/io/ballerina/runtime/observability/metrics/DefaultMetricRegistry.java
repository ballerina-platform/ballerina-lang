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
package io.ballerina.runtime.observability.metrics;

import java.util.Objects;

/**
 * Hold a default {@link MetricRegistry} instance, which is used by Metric APIs.
 */
public class DefaultMetricRegistry {

    static boolean isNoOp;
    private static MetricRegistry instance;

    /**
     * Get the default {@link MetricRegistry}.
     *
     * @return The default {@link MetricRegistry} instance.
     */
    public static MetricRegistry getInstance() {
        return instance;
    }

    /**
     * Set default {@link MetricRegistry} instance.
     *
     * @param instance A new {@link MetricRegistry} instance.
     */
    public static void setInstance(MetricRegistry instance) {
        if (!isNoOp && DefaultMetricRegistry.instance != null) {
            throw new IllegalStateException("Default Metric Registry has already been set");
        }
        DefaultMetricRegistry.instance = Objects.requireNonNull(instance);
    }

}
