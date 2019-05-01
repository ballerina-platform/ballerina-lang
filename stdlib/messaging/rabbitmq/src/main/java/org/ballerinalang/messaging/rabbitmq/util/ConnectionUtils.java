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
import org.ballerinalang.bre.Context;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * Util class for RabbitMQ Connection handling.
 *
 * @since 0.995.0
 */
public class ConnectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionUtils.class);

    /**
     * Creates a RabbitMQ Connection using the given connection parameters.
     *
     * @param connectionConfig Parameters used to initialize the connection.
     * @return RabbitMQ Connection object.
     */
    public static Connection createConnection(BMap<String, BValue> connectionConfig) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();

            String host = RabbitMQUtils.getStringFromBValue(connectionConfig,
                    RabbitMQConstants.RABBITMQ_CONNECTION_HOST);
            connectionFactory.setHost(host);

            int port = RabbitMQUtils.getIntFromBValue(connectionConfig,
                    RabbitMQConstants.RABBITMQ_CONNECTION_PORT, LOGGER);
            connectionFactory.setPort(port);

            if (connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_USER) != null) {
                connectionFactory.setUsername
                        (RabbitMQUtils.getStringFromBValue(connectionConfig,
                                RabbitMQConstants.RABBITMQ_CONNECTION_USER));
            }
            if (connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_PASS) != null) {
                connectionFactory.setPassword
                        (RabbitMQUtils.getStringFromBValue(connectionConfig,
                                RabbitMQConstants.RABBITMQ_CONNECTION_PASS));
            }
            if (connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_TIMEOUT) != null) {
                connectionFactory.setConnectionTimeout(RabbitMQUtils.getIntFromBValue(connectionConfig,
                        RabbitMQConstants.RABBITMQ_CONNECTION_TIMEOUT, LOGGER));
            }
            if (connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_HANDSHAKE_TIMEOUT) != null) {
                connectionFactory.setHandshakeTimeout(RabbitMQUtils.getIntFromBValue(connectionConfig,
                        RabbitMQConstants.RABBITMQ_CONNECTION_HANDSHAKE_TIMEOUT, LOGGER));
            }
            if (connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_SHUTDOWN_TIMEOUT) != null) {
                connectionFactory.setShutdownTimeout(RabbitMQUtils.getIntFromBValue(connectionConfig,
                        RabbitMQConstants.RABBITMQ_CONNECTION_SHUTDOWN_TIMEOUT, LOGGER));
            }
            if (connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_HEARTBEAT) != null) {
                connectionFactory.setRequestedHeartbeat(RabbitMQUtils.getIntFromBValue(connectionConfig,
                        RabbitMQConstants.RABBITMQ_CONNECTION_HEARTBEAT, LOGGER));
            }
            return connectionFactory.newConnection();
        } catch (IOException | TimeoutException exception) {
            LOGGER.error(RabbitMQConstants.CREATE_CONNECTION_ERROR, exception);
            throw new BallerinaException(RabbitMQConstants.CREATE_CONNECTION_ERROR + exception.getMessage(), exception);
        }
    }

    /**
     * Handles closing the given connection.
     *
     * @param connection RabbitMQ Connection object.
     * @param timeout    Timeout (in milliseconds) for completing all the close-related
     *                   operations, use -1 for infinity.
     * @param context    Context.
     */
    public static void handleCloseConnection(Connection connection, BValue timeout, Context context) {
        boolean validTimeout = timeout instanceof BInteger;
        if (!validTimeout) {
            closeConnection(connection, context);
        } else {
            closeConnection(connection,
                    Math.toIntExact(((BInteger) timeout).intValue()), context);
        }
    }

    /**
     * Closes the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @param context    Context.
     */
    private static void closeConnection(Connection connection, Context context) {
        try {
            connection.close();
        } catch (IOException exception) {
            LOGGER.error(RabbitMQConstants.CLOSE_CONNECTION_ERROR, exception);
            RabbitMQUtils.returnError(RabbitMQConstants.CLOSE_CONNECTION_ERROR + exception.getMessage(),
                    context, exception);
        }
    }

    /**
     * Closes the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @param timeout    Timeout (in milliseconds) for completing all the close-related operations, use -1 for infinity.
     * @param context    Context.
     */
    private static void closeConnection(Connection connection, int timeout, Context context) {
        try {
            connection.close(timeout);
        } catch (IOException exception) {
            LOGGER.error(RabbitMQConstants.CLOSE_CONNECTION_ERROR, exception);
            RabbitMQUtils.returnError(RabbitMQConstants.CLOSE_CONNECTION_ERROR + exception.getMessage(),
                    context, exception);
        }
    }

    /**
     * Checks if close was already called on the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @return True if the connection is already closed and false otherwise.
     */
    public static boolean isClosed(Connection connection) {
        boolean flag = false;
        if (connection == null || !connection.isOpen()) {
            flag = true;
        }
        return flag;
    }

    private ConnectionUtils() {
    }
}
