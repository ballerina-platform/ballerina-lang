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

package org.ballerinalang.messaging.kafka.exceptions;

/**
 * Represents exception that happens in Kafka Connector level.
 */
public class KafkaConnectorException extends Exception {

    /**
     * Creates a Kafka Connector Exception.
     *
     * @param message Relevant exception message.
     * @param e       Exception object, that has the details of the relevant exception.
     */
    public KafkaConnectorException(String message, Throwable e) {
        super(message, e);
    }

    /**
     * Creates a Kafka Connector Exception.
     *
     * @param message Relevant exception message.
     */
    public KafkaConnectorException(String message) {
        super(message);
    }
}
