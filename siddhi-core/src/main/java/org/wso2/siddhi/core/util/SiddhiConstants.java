/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.util;

public final class SiddhiConstants {

    public static final String NAMESPACE_EVENT_TABLE = "eventtable";
    public static final String NAMESPACE_INPUT_TRANSPORT = "inputtransport";
    public static final String NAMESPACE_INPUT_MAPPER = "inputmapper";

    public static final String NAMESPACE_OUTPUT_TRANSPORT = "outputtransport";
    public static final String NAMESPACE_OUTPUT_MAPPER = "outputmapper";

    public static final String EXTENSION_MULTI_ENDPOINT_TRANSPORT = "multi-endpoint";
    public static final String EXTENSION_PARTITIONED_TRANSPORT = "partitioned";

    public static final int BEFORE_WINDOW_DATA_INDEX = 0;
    public static final int ON_AFTER_WINDOW_DATA_INDEX = 1;
    public static final int OUTPUT_DATA_INDEX = 2;
    public static final int STATE_OUTPUT_DATA_INDEX = 3;

    public static final int STREAM_EVENT_CHAIN_INDEX = 0;
    public static final int STREAM_EVENT_INDEX = 1;
    public static final int STREAM_ATTRIBUTE_TYPE_INDEX = 2;
    public static final int STREAM_ATTRIBUTE_INDEX = 3;

    public static final String ANNOTATION_NAME = "Name";
    public static final String ANNOTATION_PLAYBACK = "Playback";
    public static final String ANNOTATION_ENFORCE_ORDER = "EnforceOrder";
    public static final String ANNOTATION_ASYNC = "Async";
    public static final String ANNOTATION_STATISTICS = "Statistics";
    public static final String ANNOTATION_INDEX_BY = "IndexBy";
    public static final String ANNOTATION_INDEX = "Index";
    public static final String ANNOTATION_PRIMARY_KEY = "PrimaryKey";
    public static final String ANNOTATION_FROM = "from";
    public static final String ANNOTATION_SOURCE = "source";
    public static final String ANNOTATION_SINK = "sink";
    public static final String ANNOTATION_MAP = "map";
    public static final String ANNOTATION_DISTRIBUTION = "distribution";
    public static final String ANNOTATION_ENDPOINT = "endpoint";
    public static final String ANNOTATION_ATTRIBUTES = "attributes";
    public static final String ANNOTATION_PAYLOAD = "payload";
    public static final String ANNOTATION_BUFFER_SIZE = "BufferSize";
    public static final String ANNOTATION_IDLE_TIME = "IdleTime";
    public static final String ANNOTATION_INCREMENT = "Increment";
    public static final String ANNOTATION_ELEMENT_TYPE = "type";

    public static final String TRUE = "true";
    public static final String TRIGGER_START = "start";
    public static final int DEFAULT_EVENT_BUFFER_SIZE = 1024;
    public static final int HAVING_STATE = -2;
    public static final int UNKNOWN_STATE = -1;
    public static final int CURRENT = -1;
    public static final int LAST = -2;
    public static final int ANY = -1;

    public static final String METRIC_INFIX_EXECUTION_PLANS = "ExecutionPlans";
    public static final String METRIC_INFIX_STREAMS = "Streams";
    public static final String METRIC_INFIX_SIDDHI = "Siddhi";
    public static final String METRIC_INFIX_QUERIES = "Queries";
    public static final String METRIC_INFIX_WINDOWS = "Windows";
    public static final String METRIC_DELIMITER = ".";
    public static final String METRIC_AGGREGATE_ANNOTATION = "[+]";
    public static final String EXTENSION_SEPARATOR = ":";
}
