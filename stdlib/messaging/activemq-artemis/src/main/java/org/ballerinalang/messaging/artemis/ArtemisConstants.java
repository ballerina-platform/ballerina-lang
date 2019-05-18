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
 *
 */

package org.ballerinalang.messaging.artemis;

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;

/**
 * Constants related to Artemis connector.
 *
 * @since 0.995
 */
public class ArtemisConstants {

    public static final String BALLERINA = BALLERINA_BUILTIN_PKG_PREFIX;
    public static final String ARTEMIS = "artemis";
    public static final String PROTOCOL_PACKAGE_ARTEMIS = BALLERINA_PACKAGE_PREFIX + ARTEMIS;

    // Error related constants
    static final String ARTEMIS_ERROR_CODE = "{ballerina/artemis}ArtemisError";
    static final String ARTEMIS_ERROR_RECORD = "ArtemisError";
    static final String ARTEMIS_ERROR_MESSAGE = "message";

    // Native objects
    public static final String ARTEMIS_CONNECTION_POOL = "artemis-connection-pool";
    public static final String ARTEMIS_SESSION_FACTORY = "artemis-session-factory";
    public static final String ARTEMIS_SESSION = "artemis-session";
    public static final String ARTEMIS_MESSAGE = "artemis-message";
    public static final String ARTEMIS_PRODUCER = "artemis-producer";
    public static final String ARTEMIS_CONSUMER = "artemis-consumer";
    public static final String ARTEMIS_TRANSACTION_CONTEXT = "artemis-transaction-context";
    public static final String ARTEMIS_AUTO_ACK = "autoAck";

    // The object types
    public static final String MESSAGE_OBJ = "Message";
    public static final String CONNECTION_OBJ = "Connection";
    public static final String SESSION_OBJ = "Session";
    public static final String PRODUCER_OBJ = "Producer";
    public static final String LISTENER_OBJ = "Listener";
    public static final String CONSUMER_OBJ = "Consumer";
    static final String MESSAGE_OBJ_FULL_NAME = PROTOCOL_PACKAGE_ARTEMIS + ":" + MESSAGE_OBJ;

    // Warning suppression
    public static final String UNCHECKED = "unchecked";

    public static final String COUNTDOWN_LATCH = "countdown-latch";

    // Field names for Connection
    public static final String TIME_TO_LIVE = "timeToLive";
    public static final String CALL_TIMEOUT = "callTimeout";
    public static final String CONSUMER_WINDOW_SIZE = "consumerWindowSize";
    public static final String CONSUMER_MAX_RATE = "consumerMaxRate";
    public static final String PRODUCER_WINDOW_SIZE = "producerWindowSize";
    public static final String PRODUCER_MAX_RATE = "producerMaxRate";
    public static final String RETRY_INTERVAL = "retryInterval";
    public static final String RETRY_INTERVAL_MULTIPLIER = "retryIntervalMultiplier";
    public static final String MAX_RETRY_INTERVAL = "maxRetryInterval";
    public static final String RECONNECT_ATTEMPTS = "reconnectAttempts";
    public static final String INITIAL_CONNECT_ATTEMPTS = "initialConnectAttempts";


    // Field names for Consumer
    public static final String FILTER = "filter";
    public static final String AUTO_ACK = "autoAck";
    public static final String BROWSE_ONLY = "browseOnly";
    public static final String QUEUE_CONFIG = "queueConfig";
    public static final String QUEUE_NAME = "queueName";
    public static final String TEMPORARY = "temporary";
    public static final String MAX_CONSUMERS = "maxConsumers";
    public static final String PURGE_ON_NO_CONSUMERS = "purgeOnNoConsumers";
    public static final String EXCLUSIVE = "exclusive";
    public static final String LAST_VALUE = "lastValue";
    static final String SERVICE_CONFIG = "ServiceConfig";
    public static final String ADDRESS_NAME = "addressName";

    // Field names for Message
    public static final String MESSAGE_TYPE = "messageType";
    public static final String EXPIRATION = "expiration";
    public static final String TIME_STAMP = "timeStamp";
    public static final String PRIORITY = "priority";

    // Field names for Producer
    public static final String RATE = "rate";
    public static final String SESSION = "session";

    // Common field names
    public static final String DURABLE = "durable";
    public static final String ROUTING_TYPE = "routingType";
    public static final String AUTO_CREATED = "autoCreated";
    static final String MULTICAST = "MULTICAST";

    // Field name for Session
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String AUTO_COMMIT_SENDS = "autoCommitSends";
    public static final String AUTO_COMMIT_ACKS = "autoCommitAcks";

    private ArtemisConstants() {

    }
}
