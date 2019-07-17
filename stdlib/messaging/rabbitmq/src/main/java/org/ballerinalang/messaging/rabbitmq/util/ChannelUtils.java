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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConnectorException;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Util class for RabbitMQ Channel handling.
 *
 * @since 0.995.0
 */
public class ChannelUtils {

    /**
     * Closes the channel.
     *
     * @param channel      RabbitMQ Channel object.
     * @param closeMessage The close code (See under "Reply Codes" in the AMQP specification).
     * @param closeCode    A message indicating the reason for closing the channel.
     */
    public static void handleAbortChannel(Channel channel, Object closeCode, Object closeMessage) {
        boolean validCloseCode = closeCode != null && RabbitMQUtils.checkIfInt(closeCode);
        boolean validCloseMessage = closeMessage != null && RabbitMQUtils.checkIfString(closeMessage);
        try {
            if (validCloseCode && validCloseMessage) {
                channel.abort((int) closeCode, closeMessage.toString());
            } else {
                channel.abort();
            }
        } catch (IOException | ArithmeticException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.ABORT_CHANNEL_ERROR + exception.getMessage(),
                    exception);
        }
    }

    /**
     * Closes the channel.
     *
     * @param channel      RabbitMQ Channel object.
     * @param closeCode    The close code (See under "Reply Codes" in the AMQP specification).
     * @param closeMessage A message indicating the reason for closing the connection.
     */
    public static void handleCloseChannel(Channel channel, Object closeCode, Object closeMessage) {
        boolean validCloseCode = closeCode != null && RabbitMQUtils.checkIfInt(closeCode);
        boolean validCloseMessage = closeMessage != null && RabbitMQUtils.checkIfString(closeMessage);
        try {
            if (validCloseCode && validCloseMessage) {
                channel.close((int) closeCode, closeMessage.toString());
            } else {
                channel.close();
            }
        } catch (IOException | ArithmeticException | TimeoutException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CLOSE_CHANNEL_ERROR + exception.getMessage(),
                    exception);
        }
    }

    /**
     * Declares an exchange.
     *
     * @param channel        RabbitMQ Channel object.
     * @param exchangeConfig Parameters related to declaring an exchange.
     */
    public static void exchangeDeclare(Channel channel, MapValue<String, Object> exchangeConfig) {
        String exchangeName = exchangeConfig.getStringValue(RabbitMQConstants.ALIAS_EXCHANGE_NAME);
        String exchangeType = exchangeConfig.getStringValue(RabbitMQConstants.ALIAS_EXCHANGE_TYPE);
        boolean durable = exchangeConfig.getBooleanValue(RabbitMQConstants.ALIAS_EXCHANGE_DURABLE);
        boolean autoDelete = exchangeConfig.getBooleanValue(RabbitMQConstants.ALIAS_EXCHANGE_AUTODELETE);
        try {
            channel.exchangeDeclare(exchangeName, exchangeType, durable, autoDelete, null);
        } catch (IOException exception) {
            String errorMessage = "An error occurred while declaring the exchange: ";
            throw new RabbitMQConnectorException(errorMessage + exception.getMessage(), exception);
        }
    }

    /**
     * Publishes messages to an exchange.
     * Actively declares an non-exclusive, autodelete, non-durable queue if the queue doesn't exist.
     *
     * @param channel         RabbitMQ Channel object.
     * @param routingKey      The routing key of the queue.
     * @param message         The message body.
     * @param exchange        The name of the exchange.
     * @param basicProperties Properties of the message.
     */
    public static void basicPublish(Channel channel, String routingKey, byte[] message, String exchange,
                                    Object basicProperties) {
        try {
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            if (basicProperties != null) {
                @SuppressWarnings(RabbitMQConstants.UNCHECKED)
                MapValue<String, Object> basicPropsMap = (MapValue) basicProperties;
                String replyTo = basicPropsMap.getStringValue(RabbitMQConstants.ALIAS_REPLY_TO);
                String contentType = basicPropsMap.getStringValue(RabbitMQConstants.ALIAS_CONTENT_TYPE);
                String contentEncoding = basicPropsMap.getStringValue(RabbitMQConstants.ALIAS_CONTENT_ENCODING);
                String correlationId = basicPropsMap.getStringValue(RabbitMQConstants.ALIAS_CORRELATION_ID);
                if (replyTo != null) {
                    builder.replyTo(replyTo);
                }
                if (contentType != null) {
                    builder.contentType(contentType);
                }
                if (contentEncoding != null) {
                    builder.contentEncoding(contentEncoding);
                }
                if (correlationId != null) {
                    builder.correlationId(correlationId);
                }
            }
            AMQP.BasicProperties basicProps = builder.build();
            channel.basicPublish(exchange, routingKey, basicProps, message);
        } catch (Exception e) {
            String errorMessage = "An error occurred while publishing the message to the queue ";
            throw new RabbitMQConnectorException(errorMessage + e.getMessage(), e);
        }
    }

    /**
     * Deletes a queue.
     *
     * @param channel   RabbitMQ Channel object.
     * @param queueName Name of the queue.
     * @param ifUnused  True if the queue should be deleted only if not in use.
     * @param ifEmpty   True if the queue should be deleted only if empty.
     */
    public static void queueDelete(Channel channel, String queueName, Object ifUnused, Object ifEmpty) {
        boolean isValidValues = ifUnused != null && RabbitMQUtils.checkIfBoolean(ifUnused)
                && ifEmpty != null && RabbitMQUtils.checkIfBoolean(ifEmpty);
        try {
            if (isValidValues) {
                channel.queueDelete(queueName, Boolean.valueOf(ifUnused.toString()),
                        Boolean.valueOf(ifEmpty.toString()));
            } else {
                channel.queueDelete(queueName);
            }
        } catch (Exception exception) {
            String errorMessage = "An error occurred while deleting the queue ";
            throw new RabbitMQConnectorException(errorMessage + exception.getMessage(), exception);
        }
    }

    /**
     * Validates whether the message has been acknowledged.
     *
     * @param messageObject Message object.
     * @return True if the message was acknowledged already, and false otherwise.
     */
    public static boolean validateMultipleAcknowledgements(ObjectValue messageObject) {
        return (Boolean) messageObject.getNativeData(RabbitMQConstants.MESSAGE_ACK_STATUS);
    }

    /**
     * Validates the acknowledgement mode of the message.
     *
     * @param messageObject Message object.
     * @return True if the ack-mode is client acknowledgement mode, and false otherwise.
     */
    public static boolean validateAckMode(ObjectValue messageObject) {
        return !(Boolean) messageObject.getNativeData(RabbitMQConstants.AUTO_ACK_STATUS);
    }

    private ChannelUtils() {
    }
}
