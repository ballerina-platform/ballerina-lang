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

package org.ballerinalang.messaging.rabbitmq;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.*;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.RABBITMQ_ERROR_CODE;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.RABBITMQ_ERROR_RECORD;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.STRUCT_PACKAGE_RABBITMQ;


/**
 * Util class used to bridge the RabbitMQ connector's native code and the Ballerina API.
 */
public class RabbitMQUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQUtils.class);

    public static Struct getReceiverObject(Context context) {
        return BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
    }

    public static <T> T getNativeObject(Struct struct, String objectId,
                                        Class<T> objectClass, Context context) {
        Object messageNativeData = struct.getNativeData(objectId);
        return verifyNativeObject(context, struct.getName(), objectClass, messageNativeData);
    }

    public static <T> T getNativeObject(BMap<String, BValue> struct, String objectId,
                                        Class<T> objectClass, Context context) {
        Object messageNativeData = struct.getNativeData(objectId);
        return verifyNativeObject(context, struct.getType().getName(), objectClass, messageNativeData);
    }

    private static <T> T verifyNativeObject(Context context, String structName, Class<T> objectClass, Object
            nativeData) {
        if (!objectClass.isInstance(nativeData)) {
            throw new BallerinaException(structName + " is not properly initialized.", context);
        }
        return objectClass.cast(nativeData);
    }

    public static void throwRabbitMQException(String message, Context context, Throwable throwable) {
        LOGGER.error(message, throwable);
        throw new BallerinaException(message + " " + throwable.getMessage(), throwable, context);
    }

    private static BMap<String, BValue> createErrorRecord(Context context, String errorMessage, Exception e) {
        BMap<String, BValue> errorStruct =
                BLangConnectorSPIUtil.createBStruct(context, STRUCT_PACKAGE_RABBITMQ, RABBITMQ_ERROR_RECORD);
        errorStruct.put(BLangVMErrors.ERROR_MESSAGE_FIELD, new BString(errorMessage + " " + e.getMessage()));
        return errorStruct;
    }

    public static void returnError(String errorMessage, Context context, Exception e) {
        LOGGER.error(errorMessage, e);
        BMap<String, BValue> errorRecord = createErrorRecord(context, errorMessage, e);
        context.setReturnValues(BLangVMErrors.
                createError(context, true, BTypes.typeError, RABBITMQ_ERROR_CODE, errorRecord));
    }

    public static int getIntFromBValue(BMap<String, BValue> config, String key, Logger logger) {
        long timeout = ((BInteger) config.get(key)).intValue();
        if (timeout <= 0) {
            return -1;
        }
        try {
            return Math.toIntExact(timeout);
        } catch (ArithmeticException e) {
            logger.warn("The value set for {} needs to be less than {}. The {} value is set to {}",
                    key, Integer.MAX_VALUE, key, Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }

    public static String getStringFromBValue(BMap<String, BValue> config, String key) {
        return config.get(key).toString();
    }

    public static boolean getBooleanFromBValue(BMap<String, BValue> config, String key) {
        return ((BBoolean) config.get(key)).booleanValue();
    }

    private RabbitMQUtils() {
    }
}
