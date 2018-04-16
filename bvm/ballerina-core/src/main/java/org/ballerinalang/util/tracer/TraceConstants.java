/*
 * Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.tracer;

/**
 * {@code TraceConstants} define tracer constants.
 *
 * @since 0.964.1
 */
public class TraceConstants {

    private TraceConstants() {
    }

    static final String DEFAULT_CONNECTOR_NAME = "BallerinaConnector";
    static final String DEFAULT_ACTION_NAME = "BallerinaAction";
    public static final String TRACE_PREFIX = "trace___";
    public static final String TRACE_HEADER = "x-b7a-trace";
    public static final String USER_TRACE_HEADER = "x-b7a-utrace-";
    public static final String KEY_SPAN = "_span_";

    public static final String TAG_KEY_SPAN_KIND = "span.kind";

    public static final String TAG_KEY_STR_ERROR = "error";
    public static final String TAG_STR_TRUE = "true";

    public static final String TAG_SPAN_KIND_SERVER = "server";
    public static final String TAG_SPAN_KIND_CLIENT = "client";

    public static final String LOG_KEY_MESSAGE = "message";
    public static final String LOG_KEY_ERROR_KIND = "error.kind";
    public static final String LOG_KEY_EVENT_TYPE = "event";
    public static final String LOG_ERROR_KIND_EXCEPTION = "Exception";
    public static final String LOG_EVENT_TYPE_ERROR = "error";

    public static final String DEFAULT_USER_API_GROUP = "default-group";

    public static final String JAEGER = "jaeger";
    public static final String ENABLED_CONFIG = "enabled";
    public static final String TRACER_NAME_CONFIG = "name";

}
