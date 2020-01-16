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
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConnectorException;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Util class for RabbitMQ Channel handling.
 *
 * @since 0.995.0
 */
public class ChannelUtils {
    public static Channel createChannel(Connection connection) {
        try {
            return connection.createChannel();
        } catch (IOException exception) {
            throw RabbitMQUtils.returnErrorValue("Error occurred while initializing the channel: "
                    + exception.getMessage());
        }
    }

    public static Object queueDeclare(Object queueConfig, Channel channel) {
        try {
            if (queueConfig == null) {
                return channel.queueDeclare().getQueue();
            }
            @SuppressWarnings(RabbitMQConstants.UNCHECKED)
            MapValue<String, Object> config = (MapValue<String, Object>) queueConfig;
            String queueName = config.getStringValue(RabbitMQConstants.QUEUE_NAME);
            boolean durable = config.getBooleanValue(RabbitMQConstants.QUEUE_DURABLE);
            boolean exclusive = config.getBooleanValue(RabbitMQConstants.QUEUE_EXCLUSIVE);
            boolean autoDelete = config.getBooleanValue(RabbitMQConstants.QUEUE_AUTO_DELETE);
            Map<String, Object> argumentsMap = null;
            if (config.getMapValue(RabbitMQConstants.QUEUE_ARGUMENTS) != null) {
                argumentsMap = (HashMap<String, Object>) config.getMapValue(RabbitMQConstants.QUEUE_ARGUMENTS);
            }
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, argumentsMap);
        } catch (IOException exception) {
            return RabbitMQUtils.returnErrorValue("Error occurred while declaring the queue: "
                    + exception.getMessage());
        }
        return null;
    }

    public static Object exchangeDeclare(MapValue<String, Object> exchangeConfig, Channel channel) {
        try {
            String exchangeName = exchangeConfig.getStringValue(RabbitMQConstants.EXCHANGE_NAME);
            String exchangeType = exchangeConfig.getStringValue(RabbitMQConstants.EXCHANGE_TYPE);
            boolean durable = exchangeConfig.getBooleanValue(RabbitMQConstants.EXCHANGE_DURABLE);
            boolean autoDelete = exchangeConfig.getBooleanValue(RabbitMQConstants.EXCHANGE_AUTO_DELETE);
            Map<String, Object> argumentsMap = null;
            if (exchangeConfig.getMapValue(RabbitMQConstants.EXCHANGE_ARGUMENTS) != null) {
                argumentsMap =
                        (HashMap<String, Object>) exchangeConfig.getMapValue(RabbitMQConstants.EXCHANGE_ARGUMENTS);
            }
            channel.exchangeDeclare(exchangeName, exchangeType, durable, autoDelete, argumentsMap);
        } catch (RabbitMQConnectorException | IOException exception) {
            return RabbitMQUtils.returnErrorValue("Error occurred while declaring the exchange: "
                    + exception.getMessage());
        }
        return null;
    }

    public static Object queueBind(String queueName, String exchangeName, String bindingKey, Channel channel) {
        try {
            channel.queueBind(queueName, exchangeName, bindingKey, null);
        } catch (IOException exception) {
            return RabbitMQUtils.returnErrorValue("Error occurred while binding the queue: "
                    + exception.getMessage());
        }
        return null;
    }

    public static Object basicPublish(Object messageContent, String routingKey, String exchangeName,
                                      Object properties, Channel channel) {
        String defaultExchangeName = "";
        if (exchangeName != null) {
            defaultExchangeName = exchangeName;
        }
        try {
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            if (properties != null) {
                @SuppressWarnings(RabbitMQConstants.UNCHECKED)
                MapValue<String, Object> basicPropsMap = (MapValue) properties;
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
            channel.basicPublish(defaultExchangeName, routingKey, basicProps,
                    messageContent.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException | RabbitMQConnectorException exception) {
            return RabbitMQUtils.returnErrorValue("Error occurred while publishing the message: "
                    + exception.getMessage());
        }
        return null;
    }

    public static Object queueDelete(String queueName, boolean ifUnused, boolean ifEmpty, Channel channel) {
        try {
            channel.queueDelete(queueName, ifUnused, ifEmpty);
        } catch (IOException | RabbitMQConnectorException exception) {
            return RabbitMQUtils.returnErrorValue("Error occurred while deleting the queue: "
                    + exception.getMessage());
        }
        return null;
    }

    public static Object exchangeDelete(String exchangeName, Channel channel) {
        try {
            channel.exchangeDelete(exchangeName);
        } catch (IOException | BallerinaException exception) {
            return RabbitMQUtils.returnErrorValue("Error occurred while deleting the exchange: "
                    + exception.getMessage());
        }
        return null;
    }

    public static Object queuePurge(String queueName, Channel channel) {
        try {
            channel.queuePurge(queueName);
        } catch (IOException | RabbitMQConnectorException exception) {
            return RabbitMQUtils.returnErrorValue("Error occurred while purging the queue: "
                    + exception.getMessage());
        }
        return null;
    }

    public static Object close(Object closeCode, Object closeMessage, Channel channel) {
        try {
            boolean validCloseCode = closeCode != null && RabbitMQUtils.checkIfInt(closeCode);
            boolean validCloseMessage = closeMessage != null && RabbitMQUtils.checkIfString(closeMessage);
            if (validCloseCode && validCloseMessage) {
                channel.close((int) closeCode, closeMessage.toString());
            } else {
                channel.close();
            }
        } catch (RabbitMQConnectorException | IOException | ArithmeticException | TimeoutException exception) {
            return RabbitMQUtils.returnErrorValue("Error occurred while closing the channel: "
                    + exception.getMessage());
        }
        return null;
    }

    public static Object abort(Object closeCode, Object closeMessage, Channel channel) {
        try {
            boolean validCloseCode = closeCode != null && RabbitMQUtils.checkIfInt(closeCode);
            boolean validCloseMessage = closeMessage != null && RabbitMQUtils.checkIfString(closeMessage);
            if (validCloseCode && validCloseMessage) {
                channel.abort((int) closeCode, closeMessage.toString());
            } else {
                channel.abort();
            }
            return null;
        } catch (RabbitMQConnectorException | IOException | ArithmeticException exception) {
            return RabbitMQUtils.
                    returnErrorValue("Error occurred while aborting the channel: " + exception.getMessage());
        }
    }

    public static Object getConnection(Channel channel) {
        try {
            Connection connection = channel.getConnection();
            ObjectValue connectionObject = BallerinaValues.createObjectValue(RabbitMQConstants.PACKAGE_ID_RABBITMQ,
                    RabbitMQConstants.CONNECTION_OBJECT);
            connectionObject.addNativeData(RabbitMQConstants.CONNECTION_NATIVE_OBJECT, connection);
            return connectionObject;
        } catch (AlreadyClosedException exception) {
            return RabbitMQUtils.returnErrorValue("Error occurred while retrieving the connection: "
                    + exception.getMessage());
        }
    }

    public static Object basicGet(String queueName, boolean ackMode, Channel channel) {
        try {
            GetResponse response = channel.basicGet(queueName, ackMode);
            return createAndPopulateMessageObjectValue(response, channel, ackMode);
        } catch (IOException e) {
            return RabbitMQUtils.returnErrorValue("Error occurred while retrieving the message: " +
                    e.getMessage());
        }
    }

    private static ObjectValue createAndPopulateMessageObjectValue(GetResponse response, Channel channel,
                                                                   boolean autoAck) {
        ObjectValue messageObjectValue = BallerinaValues.createObjectValue(RabbitMQConstants.PACKAGE_ID_RABBITMQ,
                RabbitMQConstants.MESSAGE_OBJECT);
        messageObjectValue.addNativeData(RabbitMQConstants.DELIVERY_TAG, response.getEnvelope().getDeliveryTag());
        messageObjectValue.addNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT, new HandleValue(channel));
        messageObjectValue.addNativeData(RabbitMQConstants.MESSAGE_CONTENT, response.getBody());
        messageObjectValue.addNativeData(RabbitMQConstants.AUTO_ACK_STATUS, autoAck);
        messageObjectValue.addNativeData(RabbitMQConstants.BASIC_PROPERTIES, response.getProps());
        messageObjectValue.addNativeData(RabbitMQConstants.MESSAGE_ACK_STATUS, false);
        return messageObjectValue;
    }

    private ChannelUtils() {
    }
}
