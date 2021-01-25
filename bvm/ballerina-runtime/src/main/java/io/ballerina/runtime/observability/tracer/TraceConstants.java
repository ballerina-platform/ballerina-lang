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
package io.ballerina.runtime.observability.tracer;

/**
 * {@code TraceConstants} define tracer constants.
 *
 * @since 0.964.1
 */
public class TraceConstants {

    private TraceConstants() {
    }

    public static final String TAG_KEY_SPAN_KIND = "span.kind";

    public static final String TAG_KEY_STR_ERROR_MESSAGE = "error.message";
    public static final String TAG_KEY_HTTP_STATUS_CODE = "http.status_code";
    public static final String TAG_STR_TRUE = "true";

    public static final String TAG_SPAN_KIND_SERVER = "server";
    public static final String TAG_SPAN_KIND_CLIENT = "client";
}
