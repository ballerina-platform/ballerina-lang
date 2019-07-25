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

package org.ballerinalang.messaging.kafka.api;

import org.ballerinalang.messaging.kafka.exceptions.KafkaConnectorException;

/**
 * Inlet of Kafka inbound messages.
 */
public interface KafkaServerConnector {

    /**
     * Start the server connector which actually starts consuming for Kafka records from a remote broker.
     *
     * @throws KafkaConnectorException if error occurred while starting the Kafka server connector
     */
    void start() throws KafkaConnectorException;

    /**
     * Stop the server connector which actually closes consumer connection with remote broker.
     *
     * @return true if stopped successfully, false otherwise
     * @throws KafkaConnectorException if error occurred while stopping the Kafka server connector
     */
    boolean stop() throws KafkaConnectorException;

}
