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

package org.ballerinalang.messaging.rabbitmq.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.*;

/**
 * Util class for RabbitMQ Connection handling.
 */
public class ConnectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionUtils.class);

    public static Connection createConnection(BMap<String, BValue> connectionConfig) {
        String host = RabbitMQUtils.getStringFromBValue(connectionConfig, RABBITMQ_CONNECTION_HOST);
        int port = RabbitMQUtils.getIntFromBValue(connectionConfig, RABBITMQ_CONNECTION_PORT, LOGGER);
        String user = null;
        String pass = null;
        if (connectionConfig.get(RABBITMQ_CONNECTION_USER) != null) {
            user = RabbitMQUtils.getStringFromBValue(connectionConfig, RABBITMQ_CONNECTION_USER);
        }
        if (connectionConfig.get(RABBITMQ_CONNECTION_PASS) != null) {
            pass = RabbitMQUtils.getStringFromBValue(connectionConfig, RABBITMQ_CONNECTION_PASS);
        }
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            if (user != null && pass != null) {
                connectionFactory.setUsername(user);
                connectionFactory.setPassword(pass);
            }
            return connectionFactory.newConnection();
        } catch (IOException | TimeoutException exception) {
            String errorMessage = "Error while connecting to the broker";
            LOGGER.error(errorMessage, exception);
            throw new BallerinaException(errorMessage + " " + exception.getMessage(), exception);
        }
    }

    public static void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (IOException exception) {
            String errorMessage = "Error while closing the connection";
            LOGGER.error(errorMessage, exception);
            throw new BallerinaException(errorMessage + " " + exception.getMessage(), exception);
        }
    }

    private ConnectionUtils() {
    }
}
