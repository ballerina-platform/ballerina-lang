/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.statistics;

/**
 * Latency Metrics parameters
 */
public class MetricsConstants {

    public static final String TYPE_SERVER_REQUEST = "TYPE_SERVER_REQUEST";

    public static final String TYPE_CLIENT_REQUEST = "TYPE_CLIENT_REQUEST";

    public static final String TYPE_CLIENT_RESPONSE = "TYPE_CLIENT_RESPONSE";

    public static final String TYPE_SERVER_RESPONSE = "TYPE_SERVER_RESPONSE";

    public static final String TYPE_SERVER_CONNECTION = "TYPE_SERVER_CONNECTION";

    public static final String TYPE_CLIENT_CONNECTION = "TYPE_CLIENT_CONNECTION";

    public static final String SERVER_REQUEST_METRICS_HOLDER = "SERVER_REQUEST_METRICS_HOLDER";

    public static final String SERVER_RESPONSE_METRICS_HOLDER = "SERVER_RESPONSE_METRICS_HOLDER";

    public static final String CLIENT_RESPONSE_METRICS_HOLDER = "CLIENT_RESPONSE_METRICS_HOLDER";

    public static final String CLIENT_REQUEST_METRICS_HOLDER = "CLIENT_REQUEST_METRICS_HOLDER";

    public static final String REQUEST_LIFE_TIMER = "request.life.timer";

    public static final String REQUEST_BODY_TIMER = "request.body.timer";

    public static final String REQUEST_HEADER_TIMER = "request.header.timer";

    public static final String RESPONSE_LIFE_TIMER = "response.life.timer";

    public static final String RESPONSE_HEADER_TIMER = "response.header.timer";

    public static final String RESPONSE_BODY_TIMER = "response.body.read.timer";

    public static final String CONNECTION_TIMER = "connection.timer";
}
