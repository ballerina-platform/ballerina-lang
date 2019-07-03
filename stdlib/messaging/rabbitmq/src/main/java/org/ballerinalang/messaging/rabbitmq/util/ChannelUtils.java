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
import com.rabbitmq.client.Connection;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
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
     * Creates a RabbitMQ AMQ Channel.
     *
     * @param connection RabbitMQ Connection object.
     * @return RabbitMQ Channel object.
     */
    public static Channel createChannel(Connection connection) {
        try {
            return connection.createChannel();
        } catch (IOException exception) {
            String errorMessage = "An error occurred while creating the channel ";
            throw new BallerinaException(errorMessage + exception.getMessage(), exception);
        }
    }

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
                abort(channel, (int) closeCode, closeMessage.toString());
            } else {
                abort(channel);
            }
        } catch (IOException | ArithmeticException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.ABORT_CHANNEL_ERROR + exception.getMessage(),
                    exception);
        }
    }

    /**
     * Aborts the channel.
     *
     * @param channel RabbitMQ Channel object.
     * @throws IOException If an I/O problem is encountered.
     */
    public static void abort(Channel channel) throws IOException {
        channel.abort();
    }

    /**
     * Aborts the channel.
     *
     * @param channel      RabbitMQ Channel object.
     * @param closeCode    The close code (See under "Reply Codes" in the AMQP specification).
     * @param closeMessage A message indicating the reason for closing the connection.
     * @throws IOException If an I/O problem is encountered.
     */
    public static void abort(Channel channel, int closeCode, String closeMessage) throws IOException {
        channel.abort(closeCode, closeMessage);
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
                close(channel, (int) closeCode, closeMessage.toString());
            } else {
                close(channel);
            }
        } catch (IOException | ArithmeticException | TimeoutException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CLOSE_CHANNEL_ERROR + exception.getMessage(),
                    exception);
        }
    }

    /**
     * Closes the channel.
     *
     * @param channel RabbitMQ Channel object.
     * @throws IOException      If an I/O problem is encountered.
     * @throws TimeoutException Thrown the operation times out.
     */
    public static void close(Channel channel) throws IOException, TimeoutException {
        channel.close();
    }

    /**
     * Closes the channel.
     *
     * @param channel      RabbitMQ Channel object.
     * @param closeCode    The close code (See under "Reply Codes" in the AMQP specification).
     * @param closeMessage A message indicating the reason for closing the connection.
     * @throws IOException      If an I/O problem is encountered.
     * @throws TimeoutException Thrown the operation times out.
     */
    public static void close(Channel channel, int closeCode, String closeMessage)
            throws IOException, TimeoutException {
        channel.close(closeCode, closeMessage);

    }

    /**
     * Declares a queue with an auto-generated queue name.
     *
     * @param channel RabbitMQ Channel object.
     * @return An auto-generated queue name.
     */
    public static String queueDeclare(Channel channel) {
        try {
            return channel.queueDeclare().getQueue();
        } catch (IOException exception) {
            String errorMessage = "An error occurred while auto-declaring the queue ";
            throw new BallerinaException(errorMessage + exception.getMessage(), exception);
        }
    }

    /**
     * Declare a queue.
     *
     * @param channel    RabbitMQ Channel object.
     * @param queueName  The name of the queue.
     * @param durable    True if we are declaring a durable queue (the queue will survive a server restart).
     * @param exclusive  True if we are declaring an exclusive queue (restricted to this connection).
     * @param autoDelete True if we are declaring an autodelete queue (server will delete it when no longer in use).
     */
    public static void queueDeclare(Channel channel, String queueName, boolean durable, boolean exclusive,
                                    boolean autoDelete) {
        try {
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
        } catch (IOException exception) {
            String errorMessage = "An error occurred while declaring the queue: ";
            throw new RabbitMQConnectorException(errorMessage + exception.getMessage(), exception);
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
     * Binds a queue to an exchange.
     *
     * @param channel      RabbitMQ Channel object.
     * @param queueName    Name of the queue.
     * @param exchangeName Name of the exchange.
     * @param bindingKey   The binding key used for the binding.
     */
    public static void queueBind(Channel channel, String queueName, String exchangeName, String bindingKey) {
        try {
            channel.queueBind(queueName, exchangeName, bindingKey);
        } catch (Exception e) {
            String errorMessage = "An error occurred while binding the queue to an exchange ";
            throw new RabbitMQConnectorException(errorMessage + e.getMessage(), e);
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
