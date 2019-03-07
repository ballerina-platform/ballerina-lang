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

package org.ballerinalang.nats.nativeimpl;

/**
 * Represents the constants which will be used for NATS_URL_PREFIX.
 */
public class Constants {
    /**
     * Represents the nats connection.
     */
    public static final String NATS_CONNECTION = "nats_connection";

    /**
     * Represents nats package.
     */
    public static final String NATS_PACKAGE = "ballerina/nats";

    /**
     * Represents the message which will be consumed from NATS_URL_PREFIX.
     */
    public static final String NATS_MESSAGE_OBJ_NAME = "Message";

    /**
     * Represents the NATS_URL_PREFIX message.
     */
    public static final String NATS_MSG = "NATSMSG";

    /**
     * Represents the message content native data.
     */
    public static final String MSG_CONTENT_NAME = "content";

    /**
     * Represents the service configuration.
     */
    public static final String NATS_SERVICE_CONFIG = "ServiceConfig";

    /**
     * IO error struct name.
     */
    static final String IO_ERROR = "IOError";

    /**
     * Specifies the error code for i/o.
     */
    public static final String NATS_ERROR_CODE = "{ballerina/nats}NATSError";

    /**
     * Represents the struct which holds the connection.
     */
    public static final String CONNECTION_STRUCT = "connection";
}
