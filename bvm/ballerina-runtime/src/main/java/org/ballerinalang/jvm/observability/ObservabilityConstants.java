/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.observability;

/**
 * All constants related to observability.
 * <p>
 * Tags in this class are used for both Metrics and Tracing. Some tags follow the standard names defined by
 * Open Tracing Specification.
 * </p>
 */
public class ObservabilityConstants {

    private ObservabilityConstants() {
    }

    public static final String SERVICE_NAME = "service_name";
    public static final String KEY_TRACE_CONTEXT = "_trace_context_";
    public static final String KEY_OBSERVER_CONTEXT = "__observer_context__";
    public static final String UNKNOWN_SERVICE = "Unknown Service";
    public static final String UNKNOWN_CONNECTOR = "Unknown";

    public static final String SERVER_CONNECTOR_HTTP = "http";
    public static final String SERVER_CONNECTOR_WEBSOCKET = "ws";

    public static final String TAG_KEY_HTTP_METHOD = "http.method";
    public static final String TAG_KEY_HTTP_URL = "http.url";
    public static final String TAG_KEY_HTTP_STATUS_CODE_GROUP = "http.status_code_group";
    public static final String TAG_KEY_PROTOCOL = "protocol";

    public static final String TAG_KEY_PEER_ADDRESS = "peer.address";

    public static final String PROPERTY_HTTP_HOST = "host";
    public static final String PROPERTY_HTTP_PORT = "port";

    public static final String TAG_KEY_DB_INSTANCE = "db.instance";
    public static final String TAG_KEY_DB_STATEMENT = "db.statement";
    public static final String TAG_KEY_DB_TYPE = "db.type";

    public static final String TAG_DB_TYPE_SQL = "sql";

    public static final String PROPERTY_TRACE_PROPERTIES = "trace_properties";
    public static final String PROPERTY_ERROR = "error";
    public static final String PROPERTY_ERROR_MESSAGE = "error_message";
    public static final String PROPERTY_BSTRUCT_ERROR = "bstruct_error";

    public static final String STATUS_CODE_GROUP_SUFFIX = "xx";
    public static final String INTERNAL_SERVER_ERROR_STATUS_CODE_GROUP = "5xx";

    // TOML Configs
    public static final String CONFIG_TABLE_OBSERVABILITY = "b7a.observability";
    public static final String CONFIG_TABLE_METRICS = CONFIG_TABLE_OBSERVABILITY + ".metrics";
    public static final String CONFIG_TABLE_TRACING = CONFIG_TABLE_OBSERVABILITY + ".tracing";

    // Observability Configs
    public static final String CONFIG_OBSERVABILITY_ENABLED = CONFIG_TABLE_OBSERVABILITY + ".enabled";
    public static final String CONFIG_OBSERVABILITY_PROVIDER = CONFIG_TABLE_OBSERVABILITY + ".provider";

    // Metrics Configs
    public static final String CONFIG_METRICS_ENABLED = CONFIG_TABLE_METRICS + ".enabled";

    // Tracing Configs
    public static final String CONFIG_TRACING_ENABLED = CONFIG_TABLE_TRACING + ".enabled";
}
