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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
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
     * @param context   the Ballerina context
     * @param exception the exception to be propagated
     * @param logger    the logger to log errors
     */
    public static void throwException(String message, Context context, Exception exception, Logger logger) {
        logger.error(message, exception);
        throw new ArtemisConnectorException(message, exception, context);
    }

    /**
     * Get error struct.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error message
     * @return Error struct
     */
    public static BError getError(Context context, String errMsg) {
        BMap<String, BValue> artemisErrorRecord = createArtemisErrorRecord(context);
        artemisErrorRecord.put(ArtemisConstants.ARTEMIS_ERROR_MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, ArtemisConstants.ARTEMIS_ERROR_CODE,
                                         artemisErrorRecord);
    }

    private static BMap<String, BValue> createArtemisErrorRecord(Context context) {
        return BLangConnectorSPIUtil.createBStruct(context, ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS,
                                                   ArtemisConstants.ARTEMIS_ERROR_RECORD);
    }

    /**
     * Get error struct from throwable.
     *
     * @param context   Represent ballerina context
     * @param exception Throwable representing the error.
     * @return Error struct
     */
    public static BError getError(Context context, Exception exception) {
        if (exception.getMessage() == null) {
            return getError(context, "Artemis connector error");
        } else {
            return getError(context, exception.getMessage());
        }
    }

    /**
     * Gets an int from the {@link BMap} config.
     *
     * @param config the BMap config
     * @param key    the key that has an integer value
     * @param logger the logger to log errors
     * @return the relevant int value from the config
     */
    public static int getIntFromConfig(BMap<String, BValue> config, String key, Logger logger) {
        return getIntFromLong(((BInteger) config.get(key)).intValue(), key, logger);
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
     * @param obj     the Object
     * @param context the Ballerina context to to be used in case of errors
     * @return the relevant BValue for the object or error
     */
    public static BValue getBValueFromObj(Object obj, Context context) {
        if (obj instanceof String) {
            return new BString((String) obj);
        } else if (obj instanceof SimpleString) {
            return new BString(((SimpleString) obj).toString());
        } else if (obj instanceof Integer) {
            return new BInteger((int) obj);
        } else if (obj instanceof Long) {
            return new BInteger((long) obj);
        } else if (obj instanceof Short) {
            return new BInteger((short) obj);
        } else if (obj instanceof Float) {
            return new BFloat((float) obj);
        } else if (obj instanceof Double) {
            return new BFloat((double) obj);
        } else if (obj instanceof Boolean) {
            return new BBoolean((boolean) obj);
        } else if (obj instanceof Byte) {
            return new BByte((byte) obj);
        } else if (obj instanceof byte[]) {
            return new BValueArray((byte[]) obj);
        } else {
            return ArtemisUtils.getError(context, "Unsupported type");
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
    public static ClientSession getClientSessionFromBMap(BMap<String, BValue> obj) {
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> sessionObj = (BMap<String, BValue>) obj.get(ArtemisConstants.SESSION);
        return (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);
    }

    /**
     * Close the session if it has been created implicitly identified by the anonymousSession field in the Ballerina
     * object.
     *
     * @param obj the Ballerina object as a BMap
     * @throws ActiveMQException on session closure failure
     */
    public static void closeIfAnonymousSession(BMap<String, BValue> obj) throws ActiveMQException {
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> sessionObj = (BMap<String, BValue>) obj.get(ArtemisConstants.SESSION);
        boolean anonymousSession = ((BBoolean) sessionObj.get("anonymousSession")).booleanValue();
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
     * @param bytesArray The {@link BValue} with the bytes array
     * @return the bytes from the BValue object
     */
    public static byte[] getBytesData(BValueArray bytesArray) {
        return Arrays.copyOf(bytesArray.getBytes(), (int) bytesArray.size());
    }

    public static ClientConsumer getClientConsumer(BMap<String, BValue> bObj, ClientSession session,
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

    public static boolean isAnonymousSession(BMap<String, BValue> sessionObj) {
        return ((BBoolean) sessionObj.get("anonymousSession")).booleanValue();
    }

    public static BValue getBArrayValue(ClientMessage message) {
        ActiveMQBuffer msgBuffer = message.getBodyBuffer();
        byte[] bytes = new byte[msgBuffer.readableBytes()];
        BytesMessageUtil.bytesReadBytes(msgBuffer, bytes);
        return new BValueArray(bytes);
    }

    public static void populateMessageObj(ClientMessage clientMessage, Object transactionContext,
                                          BMap<String, BValue> messageObj) {
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> messageConfigObj = (BMap<String, BValue>) messageObj.get(ArtemisConstants.MESSAGE_CONFIG);
        populateMessageConfigObj(clientMessage, messageConfigObj);

        messageObj.addNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT, transactionContext);
        messageObj.addNativeData(ArtemisConstants.ARTEMIS_MESSAGE, clientMessage);
    }

    private static void populateMessageConfigObj(ClientMessage clientMessage, BMap<String, BValue> messageConfigObj) {
        messageConfigObj.put(ArtemisConstants.EXPIRATION, new BInteger(clientMessage.getExpiration()));
        messageConfigObj.put(ArtemisConstants.TIME_STAMP, new BInteger(clientMessage.getTimestamp()));
        messageConfigObj.put(ArtemisConstants.PRIORITY, new BByte(clientMessage.getPriority()));
        messageConfigObj.put(ArtemisConstants.DURABLE, new BBoolean(clientMessage.isDurable()));
        setRoutingTypeToConfig(messageConfigObj, clientMessage);
        if (clientMessage.getGroupID() != null) {
            messageConfigObj.put(ArtemisConstants.GROUP_ID, new BString(clientMessage.getGroupID().toString()));
        }
        messageConfigObj.put(ArtemisConstants.GROUP_SEQUENCE, new BInteger(clientMessage.getGroupSequence()));
        if (clientMessage.getCorrelationID() != null) {
            messageConfigObj.put(ArtemisConstants.CORRELATION_ID,
                                 new BString(clientMessage.getCorrelationID().toString()));
        }
        if (clientMessage.getReplyTo() != null) {
            messageConfigObj.put(ArtemisConstants.REPLY_TO, new BString(clientMessage.getReplyTo().toString()));
        }
    }

    public static void setRoutingTypeToConfig(BMap<String, BValue> msgConfigObj, ClientMessage message) {
        byte routingType = message.getType();
        if (routingType == RoutingType.MULTICAST.getType()) {
            msgConfigObj.put(ArtemisConstants.ROUTING_TYPE, new BString(ArtemisConstants.MULTICAST));
        } else if (routingType == RoutingType.ANYCAST.getType()) {
            msgConfigObj.put(ArtemisConstants.ROUTING_TYPE, new BString(ArtemisConstants.ANYCAST));
        }
    }

    private ArtemisUtils() {

    }
}
