/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.observability.tracer;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.api.BString;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TABLE_TRACING;

/**
 * {@code TraceConstants} define tracer constants.
 *
 * @since 0.964.1
 */
public final class TraceConstants {

    private TraceConstants() {
    }
    public static final String KEY_SPAN = "_span_";

    public static final String TAG_KEY_SPAN_KIND = "span.kind";

    public static final String TAG_KEY_STR_ERROR_MESSAGE = "error.message";
    public static final String TAG_STR_TRUE = "true";

    public static final String TAG_SPAN_KIND_SERVER = "server";
    public static final String TAG_SPAN_KIND_CLIENT = "client";

    public static final BString SPAN_CONTEXT_MAP_KEY_TRACE_ID = StringUtils.fromString("traceId");
    public static final BString SPAN_CONTEXT_MAP_KEY_SPAN_ID = StringUtils.fromString("spanId");

    public static final String JAEGER = "jaeger";
    public static final String TRACER_NAME_CONFIG = CONFIG_TABLE_TRACING + ".name";
}
