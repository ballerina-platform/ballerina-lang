/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.observe.trace.extension.zipkin;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TABLE_TRACING;

/**
 * This is the constants class that defines all the constants
 * that are used by the {@link ZipkinTracerProvider}.
 */
public class Constants {
    private Constants() {
    }

    static final String TRACER_NAME = "zipkin";

    private static final String ZIPKIN_CONFIG_TABLE = CONFIG_TABLE_TRACING + ".zipkin";
    static final String SAMPLER_TYPE_CONFIG = ZIPKIN_CONFIG_TABLE + ".sampler.type";
    static final String SAMPLER_PARAM_CONFIG = ZIPKIN_CONFIG_TABLE + ".sampler.param";
    static final String REPORTER_HOST_NAME_CONFIG = ZIPKIN_CONFIG_TABLE + ".reporter.hostname";
    static final String REPORTER_PORT_CONFIG = ZIPKIN_CONFIG_TABLE + ".reporter.port";
    static final String REPORTER_FLUSH_INTERVAL_MS_CONFIG = ZIPKIN_CONFIG_TABLE + ".reporter.flush.interval.ms";
    static final String REPORTER_MAX_BUFFER_SPANS_CONFIG = ZIPKIN_CONFIG_TABLE + ".reporter.max.buffer.spans";
    static final String REPORTER_API_CONTEXT_CONFIG = "reporter.api.context";
    static final String REPORTER_COMPRESSION_ENABLED_CONFIG = "reporter.compression.enabled";
    static final String REPORTER_API_VERSION = "reporter.api.version";

    static final String DEFAULT_SAMPLER_TYPE = "const";
    static final int DEFAULT_SAMPLER_PARAM = 1;
    static final String DEFAULT_REPORTER_HOSTNAME = "localhost";
    static final int DEFAULT_REPORTER_PORT = 9411;
    static final int DEFAULT_REPORTER_FLUSH_INTERVAL = 1000;
    static final int DEFAULT_REPORTER_MAX_BUFFER_SPANS = 10000;
    static final String DEFAULT_REPORTER_API_CONTEXT = "/api/v2/spans";
    static final boolean DEFAULT_REPORTER_COMPRESSION_ENABLED = true;
    static final String DEFAULT_REPORTER_API_VERSION = "v2";

    static final String APPLICATION_LAYER_PROTOCOL = "http";
    static final String COMPRESSION_METHOD_ENABLED = "gzip";
    static final String COMPRESSION_METHOD_DISABLED = "none";
}
