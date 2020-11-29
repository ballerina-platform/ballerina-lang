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

import io.ballerina.runtime.api.Module;

import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_TABLE_METRICS;
import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_TABLE_TRACING;

/**
 * Constants related to Ballerina Observe Module.
 */
public class Constants {
    public static final Module OBSERVE_INTERNAL_MODULE = new Module("ballerinai", "observe", "0.8.0");

    public static final String METRIC_PROVIDER_NATIVE_DATA_KEY = "_metric_provider_";
    public static final String TRACER_PROVIDER_NATIVE_DATA_KEY = "_tracer_provider_";

    public static final String METRIC_PROVIDER_NAME = CONFIG_TABLE_METRICS + ".provider";
    public static final String DEFAULT_METRIC_PROVIDER_NAME = "default";

    public static final String METRIC_REPORTER_NAME = CONFIG_TABLE_METRICS + ".reporter";
    public static final String DEFAULT_METRIC_REPORTER_NAME = "prometheus";

    public static final String TRACER_NAME = CONFIG_TABLE_TRACING + ".name";
    public static final String DEFAULT_TRACER_NAME = "jaeger";
}
