/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats;

/**
 * Represents the constants which will be used for NATS.
 */
public class Constants {
    /**
     * Represents the NATS connection.
     */
    public static final String NATS_CONNECTION = "nats_connection";

    /**
     * Represents the NATS streaming connection.
     */
    public static final String NATS_STREAMING_CONNECTION = "nats_streaming_connection";

    /**
     * Represents connected clients.
     */
    public static final String CONNECTED_CLIENTS = "connected_clients";

    /**
     * Represents dispatcher list.
     */
    public static final String DISPATCHER_LIST = "dispatcher_list";

    /**
     * Represent NATS Connection error listener.
     */
    public static final String SERVICE_LIST = "service_list";

    /**
     * Represent whether connection close already triggered.
     */
    public static final String CLOSING = "closing";
    /**
     * Represents nats package.
     */
    public static final String NATS_PACKAGE = "ballerina/nats";

    /**
     * Represents the message which will be consumed from NATS.
     */
    public static final String NATS_MESSAGE_OBJ_NAME = "Message";

    /**
     * Represents the NATS message.
     */
    public static final String NATS_MSG = "NATSMSG";

    /**
     * Error code for i/o.
     */
    public static final String NATS_ERROR_CODE = "{ballerina/nats}Error";

    /**
     * Represents the NATS error detail record.
     */
    public static final String NATS_ERROR_DETAIL_RECORD = "Detail";

    /**
     * Represents the object which holds the connection.
     */
    public static final String CONNECTION_OBJ = "connection";

    /**
     * Represents the NATS Streaming message.
     */
    public static final String NATS_STREAMING_MSG = "nats_streaming_message";

    public static final String NATS_BASIC_CONSUMER_ANNOTATION = "SubscriptionConfig";

    public static final String NATS_STREAMING_SUBSCRIPTION_ANNOTATION = "StreamingSubscriptionConfig";

    public static final String NATS_STREAMING_MESSAGE_OBJ_NAME = "StreamingMessage";

    public static final String NATS_STREAMING_LISTENER = "StreamingListener";

    public static final String STREAMING_DISPATCHER_LIST = "StreamingDispatcherList";

    public static final String ON_MESSAGE_RESOURCE = "onMessage";

    public static final String ON_ERROR_RESOURCE = "onError";

    public static final String COUNTDOWN_LATCH = "count_down_latch";

    public static final String NATS_CLIENT_SUBSCRIBED = "[ballerina/nats] Client subscribed for ";

    public static final String CONNECTION_CONFIG_SECURE_SOCKET = "secureSocket";
    public static final String CONNECTION_KEYSTORE = "keyStore";
    public static final String CONNECTION_TRUSTORE = "trustStore";
    public static final String CONNECTION_PROTOCOL = "protocol";
    public static final String KEY_STORE_TYPE = "PKCS12";
    public static final String KEY_STORE_PASS = "password";
    public static final String KEY_STORE_PATH = "path";

    public static final String ERROR_SETTING_UP_SECURED_CONNECTION = "Error while setting up secured connection. ";

}
