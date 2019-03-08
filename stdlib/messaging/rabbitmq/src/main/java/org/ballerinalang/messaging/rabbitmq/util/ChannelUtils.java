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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.Map;

import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.*;

/**
 * Util class for RabbitMQ Connection handling.
 */
public class ChannelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelUtils.class);

    public static Channel createChannel(Connection connection) {
        try {
            return connection.createChannel();
        } catch (Exception e) {
            String errorMessage = "Error occurred while creating the channel";
            LOGGER.error(errorMessage, e);
            throw new BallerinaException(errorMessage, e);
        }
    }

    public static String queueDeclare(Channel channel) {
        try {
            return channel.queueDeclare().getQueue();
        } catch (IOException e) {
            String errorMessage = "Error while auto declaring the queue";
            LOGGER.error(errorMessage, e);
            throw new BallerinaException(errorMessage + " " + e.getMessage(), e);
        }
    }

    public static void queueDeclare(Channel channel, BMap<String, BValue> queueConfig) {
        String queueName = RabbitMQUtils.getStringFromBValue(queueConfig, ALIAS_QUEUE_NAME);
        boolean durable = RabbitMQUtils.getBooleanFromBValue(queueConfig, ALIAS_QUEUE_DURABLE);
        boolean exclusive = RabbitMQUtils.getBooleanFromBValue(queueConfig, ALIAS_QUEUE_EXCLUSIVE);
        boolean autoDelete = RabbitMQUtils.getBooleanFromBValue(queueConfig, ALIAS_QUEUE_AUTODELETE);
        Map<String, Object> arguments = null;
        try {
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
        } catch (IOException e) {
            String errorMessage = "Error while declaring the queue";
            LOGGER.error(errorMessage, e);
            throw new BallerinaException(errorMessage + " " + e.getMessage(), e);
        }
    }

    public static void exchangeDeclare(Channel channel, BMap<String, BValue> exchangeConfig) {
        String exchangeName = RabbitMQUtils.getStringFromBValue(exchangeConfig, ALIAS_EXCHANGE_NAME);
        String exchangeType = RabbitMQUtils.getStringFromBValue(exchangeConfig, ALIAS_EXCHANGE_TYPE);
        boolean durable = RabbitMQUtils.getBooleanFromBValue(exchangeConfig, ALIAS_EXCHANGE_DURABLE);
        try {
            channel.exchangeDeclare(exchangeName, exchangeType, durable);
        } catch (IOException e) {
            String errorMessage = "Error while declaring the exchange";
            LOGGER.error(errorMessage, e);
            throw new BallerinaException(errorMessage + " " + e.getMessage(), e);
        }
    }

    public static void queueBind(Channel channel, String queueName, String exchangeName, String routingKey) {
        try {
            channel.queueBind(queueName, exchangeName, routingKey);
        } catch (Exception e) {
            String errorMessage = "Error while binding the queue";
            LOGGER.error(errorMessage, e);
            throw new BallerinaException(errorMessage + " " + e.getMessage(), e);
        }
    }

    public static void basicPublish(Channel channel, String rutingKey, String message, String exchange) {
        try {
            channel.basicPublish(exchange, rutingKey, null, message.getBytes("UTF-8"));
        } catch (Exception e) {
            String errorMessage = "Error while publishing message to the queue";
            LOGGER.error(errorMessage, e);
            throw new BallerinaException(errorMessage + " " + e.getMessage(), e);
        }

    }

    // TODO: Synchronous message receiving
//    public static void basicConsume(Channel channel) {
//
//    }
}
