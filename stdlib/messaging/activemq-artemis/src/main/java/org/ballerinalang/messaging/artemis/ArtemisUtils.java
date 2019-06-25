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

import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.reader.BytesMessageUtil;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;

import java.util.Arrays;

/**
 * Utility class for Artemis.
 */
public class ArtemisUtils {

    /**
     * Util function to throw a {@link BallerinaException}.
     *
     * @param message   the error message
     * @param exception the exception to be propagated
     * @param logger    the logger to log errors
     */
    public static void throwException(String message, Exception exception, Logger logger) {
        logger.error(message, exception);
        throw new ArtemisConnectorException(message, exception);
    }

    /**
     * Get error struct.
     *
     * @param errMsg Error message
     * @return Error struct
     */
    public static ErrorValue getError(String errMsg) {
        MapValue<String, Object> artemisErrorRecord = createArtemisErrorRecord();
        artemisErrorRecord.put(ArtemisConstants.ARTEMIS_ERROR_MESSAGE, errMsg);
        return BallerinaErrors.createError(ArtemisConstants.ARTEMIS_ERROR_CODE, artemisErrorRecord);
    }

    private static MapValue<String, Object> createArtemisErrorRecord() {
        return BallerinaValues.createRecordValue(ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS,
                                                 ArtemisConstants.ARTEMIS_ERROR_RECORD);
    }

    /**
     * Get error struct from throwable.
     *
     * @param exception Throwable representing the error.
     * @return Error struct
     */
    public static ErrorValue getError(Exception exception) {
        if (exception.getMessage() == null) {
            return getError("Artemis connector error");
        } else {
            return getError(exception.getMessage());
        }
    }

    /**
     * Gets an int from the {@link MapValue} config.
     *
     * @param config the config
     * @param key    the key that has an integer value
     * @param logger the logger to log errors
     * @return the relevant int value from the config
     */
    public static int getIntFromConfig(MapValue config, String key, Logger logger) {
        return getIntFromLong(config.getIntValue(key), key, logger);
    }

    /**
     * Gets an integer from a long value. Handles errors appropriately.
     *
     * @param longVal the long value.
     * @param name    the name of the long value: useful for logging the error.
     * @param logger  the logger to log errors
     * @return the int value from the given long value
     */
    public static int getIntFromLong(long longVal, String name, Logger logger) {
        if (longVal <= 0) {
            return -1;
        }
        try {
            return Math.toIntExact(longVal);
        } catch (ArithmeticException e) {
            logger.warn("The value set for {} needs to be less than {}. The {} value is set to {}", name,
                        Integer.MAX_VALUE, name, Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Get the relevant BValure for an Object.
     *
     * @param obj the Object
     * @return the relevant BValue for the object or error
     */
    public static Object getValidObj(Object obj) {
        if (obj instanceof String || obj instanceof Integer || obj instanceof Long || obj instanceof Short ||
                obj instanceof Float || obj instanceof Double || obj instanceof Boolean || obj instanceof Byte) {
            return obj;
        } else if (obj instanceof SimpleString) {
            return obj.toString();
        } else if (obj instanceof byte[]) {
            return new ArrayValue((byte[]) obj);
        } else {
            return ArtemisUtils.getError("Unsupported type");
        }
    }

    /**
     * Gets the {@link RoutingType} from the String type.
     *
     * @param routingType the string routing type
     * @return the relevant {@link RoutingType}
     */
    public static RoutingType getRoutingTypeFromString(String routingType) {
        return ArtemisConstants.MULTICAST.equals(routingType) ? RoutingType.MULTICAST : RoutingType.ANYCAST;
    }

    /**
     * Get the natively stored {@link ClientSession} from the BMap.
     *
     * @param obj the Ballerina object as a BMap
     * @return the natively stored {@link ClientSession}
     */
    public static ClientSession getClientSessionFromBMap(ObjectValue obj) {
        ObjectValue sessionObj = obj.getObjectValue(ArtemisConstants.SESSION);
        return (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);
    }

    /**
     * Close the session if it has been created implicitly identified by the anonymousSession field in the Ballerina
     * object.
     *
     * @param obj the Ballerina object as a BMap
     * @throws ActiveMQException on session closure failure
     */
    public static void closeIfAnonymousSession(ObjectValue obj) throws ActiveMQException {
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        ObjectValue sessionObj = obj.getObjectValue(ArtemisConstants.SESSION);
        boolean anonymousSession = sessionObj.getBooleanValue("anonymousSession");
        if (anonymousSession) {
            ClientSession session = ArtemisUtils.getClientSessionFromBMap(obj);
            if (!session.isClosed()) {
                session.close();
            }
        }
    }

    /**
     * Get only the required bytes data from the BValueArray. This utility function is required because the byte array
     * can have 100 elements if it is unbounded in the Ballerina code.
     *
     * @param bytesArray The {@link ArrayValue} with the bytes array
     * @return the bytes from the BValue object
     */
    public static byte[] getBytesData(ArrayValue bytesArray) {
        return Arrays.copyOf(bytesArray.getBytes(), bytesArray.size());
    }

    public static ClientConsumer getClientConsumer(ObjectValue bObj, ClientSession session,
                                                   String consumerFilter, String queueName,
                                                   SimpleString addressName, boolean autoCreated, String routingType,
                                                   boolean temporary, String queueFilter, boolean durable,
                                                   int maxConsumers, boolean purgeOnNoConsumers, boolean exclusive,
                                                   boolean lastValue, Logger logger) throws ActiveMQException {
        if (autoCreated) {
            SimpleString simpleQueueName = new SimpleString(queueName);
            SimpleString simpleQueueFilter = queueFilter != null ? new SimpleString(queueFilter) : null;
            ClientSession.QueueQuery queueQuery = session.queueQuery(simpleQueueName);
            if (!queueQuery.isExists()) {
                if (!temporary) {
                    session.createQueue(addressName, getRoutingTypeFromString(routingType),
                                        simpleQueueName, simpleQueueFilter, durable, true, maxConsumers,
                                        purgeOnNoConsumers, exclusive, lastValue);
                } else {
                    session.createTemporaryQueue(addressName, getRoutingTypeFromString(routingType),
                                                 simpleQueueName, simpleQueueFilter, maxConsumers,
                                                 purgeOnNoConsumers, exclusive, lastValue);
                }
            } else {
                logger.warn(
                        "Queue with the name {} already exists with routingType: {}, durable: {}, temporary: {}, " +
                                "filter: {}, purgeOnNoConsumers: {}, exclusive: {}, lastValue: {}",
                        queueName, queueQuery.getRoutingType(), queueQuery.isDurable(), queueQuery.isTemporary(),
                        queueQuery.getFilterString(), queueQuery.isPurgeOnNoConsumers(), queueQuery.isExclusive(),
                        queueQuery.isLastValue());
            }
        }


        ClientConsumer consumer = session.createConsumer(queueName, consumerFilter, false);
        bObj.addNativeData(ArtemisConstants.ARTEMIS_CONSUMER, consumer);
        return consumer;
    }

    public static boolean isAnonymousSession(ObjectValue sessionObj) {
        return sessionObj.getBooleanValue("anonymousSession");
    }

    public static ArrayValue getArrayValue(ClientMessage message) {
        ActiveMQBuffer msgBuffer = message.getBodyBuffer();
        byte[] bytes = new byte[msgBuffer.readableBytes()];
        BytesMessageUtil.bytesReadBytes(msgBuffer, bytes);
        return new ArrayValue(bytes);
    }

    public static void populateMessageObj(ClientMessage clientMessage, Object transactionContext,
                                          ObjectValue messageObj) {
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        MapValue<String, Object> messageConfigObj = (MapValue<String, Object>) messageObj.get(
                ArtemisConstants.MESSAGE_CONFIG);
        populateMessageConfigObj(clientMessage, messageConfigObj);

        messageObj.addNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT, transactionContext);
        messageObj.addNativeData(ArtemisConstants.ARTEMIS_MESSAGE, clientMessage);
    }

    private static void populateMessageConfigObj(ClientMessage clientMessage,
                                                 MapValue<String, Object> messageConfigObj) {
        messageConfigObj.put(ArtemisConstants.EXPIRATION, clientMessage.getExpiration());
        messageConfigObj.put(ArtemisConstants.TIME_STAMP, clientMessage.getTimestamp());
        messageConfigObj.put(ArtemisConstants.PRIORITY, clientMessage.getPriority());
        messageConfigObj.put(ArtemisConstants.DURABLE, clientMessage.isDurable());
        setRoutingTypeToConfig(messageConfigObj, clientMessage);
        if (clientMessage.getGroupID() != null) {
            messageConfigObj.put(ArtemisConstants.GROUP_ID, clientMessage.getGroupID().toString());
        }
        messageConfigObj.put(ArtemisConstants.GROUP_SEQUENCE, clientMessage.getGroupSequence());
        if (clientMessage.getCorrelationID() != null) {
            messageConfigObj.put(ArtemisConstants.CORRELATION_ID, clientMessage.getCorrelationID().toString());
        }
        if (clientMessage.getReplyTo() != null) {
            messageConfigObj.put(ArtemisConstants.REPLY_TO, clientMessage.getReplyTo().toString());
        }
    }

    private static void setRoutingTypeToConfig(MapValue<String, Object> msgConfigObj, ClientMessage message) {
        byte routingType = message.getType();
        if (routingType == RoutingType.MULTICAST.getType()) {
            msgConfigObj.put(ArtemisConstants.ROUTING_TYPE, ArtemisConstants.MULTICAST);
        } else if (routingType == RoutingType.ANYCAST.getType()) {
            msgConfigObj.put(ArtemisConstants.ROUTING_TYPE, ArtemisConstants.ANYCAST);
        }
    }

    public static String getStringFromObjOrNull(Object value) {
        return value != null ? (String) value : null;
    }

    public static String getAddressName(MapValue queueConfig, String queueName) {
        Object addressName = queueConfig.get(ArtemisConstants.ADDRESS_NAME);
        return addressName != null ? (String) addressName : queueName;
    }

    private ArtemisUtils() {

    }
}
