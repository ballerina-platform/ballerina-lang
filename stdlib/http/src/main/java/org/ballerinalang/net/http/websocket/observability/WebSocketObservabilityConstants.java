/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.observability;

/**
 * Providing observability functionality to WebSockets.
 *
 * @since 1.1.0
 */
public class WebSocketObservabilityConstants {

    public static final String TAG_CONNECTION_ID = "connectionID";
    public static final String TAG_CONTEXT = "client_or_server";
    public static final String TAG_SERVICE = "service";
    public static final String TAG_MESSAGE_TYPE = "type";
    public static final String TAG_ERROR_TYPE = "error_type";
    public static final String TAG_RESOURCE = "resource";

    static final String[] METRIC_REQUESTS = { "requests", "Number of WebSocket connection requests"};
    static final String[] METRIC_CONNECTIONS = {"connections", "Number of currenctly active connections"};
    static final String[] METRIC_MESSAGES_RECEIVED = {"messages_received", "Number of messages received"};
    static final String[] METRIC_MESSAGES_SENT = {"messages_sent", "Number of messages sent"};
    static final String[] METRIC_ERRORS = {"errors", "Number of errors"};
    static final String[] METRIC_RESOURCES_INVOKED = {"resources_invoked", "Number of resources invoked"};

    static final String CONTEXT_CLIENT = "client";
    public static final String CONTEXT_SERVER = "server";

    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_BINARY = "binary";
    public static final String MESSAGE_TYPE_PING = "ping";
    public static final String MESSAGE_TYPE_PONG = "pong";
    public static final String MESSAGE_TYPE_CLOSE = "close";

    public static final String ERROR_TYPE_CONNECTION = "connection";
    public static final String ERROR_TYPE_CLOSE = "close";
    public static final String ERROR_TYPE_MESSAGE_SENT = "message_sent";
    public static final String ERROR_TYPE_MESSAGE_RECEIVED = "message_received";
    public static final String ERROR_TYPE_READY = "ready";
    public static final String ERROR_TYPE_RESOURCE_INVOCATION = "resource_invocation";

    static final String UNKNOWN = "unknown";

    private WebSocketObservabilityConstants(){
    }
}
