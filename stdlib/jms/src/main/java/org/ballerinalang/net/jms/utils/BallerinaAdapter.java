/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.jms.utils;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.net.jms.JmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter class use used to bridge the connector native codes and Ballerina API.
 */
public class BallerinaAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaAdapter.class);

    private BallerinaAdapter() {
    }

    public static void throwBallerinaException(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
        throw getError(message, throwable);
    }

    public static void throwBallerinaException(String message) {
        throw getError(message);
    }

    private static MapValue<String, Object> createErrorRecord() {
        return BallerinaValues.createRecordValue(JmsConstants.PROTOCOL_INTERNAL_PACKAGE_JMS,
                                                 JmsConstants.JMS_ERROR_RECORD);
    }

    public static ErrorValue getError(String errorMessage, Throwable e) {
        LOGGER.error(errorMessage, e);
        return getError(errorMessage);
    }

    public static ErrorValue getError(String errorMessage) {
        MapValue<String, Object> errorRecord = createErrorRecord();
        errorRecord.put(JmsConstants.ERROR_MESSAGE_FIELD, errorMessage);
        return BallerinaErrors.createError(JmsConstants.JMS_ERROR_CODE, errorRecord);
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
            throw getError("The bytesLength cannot be negative");
        }
        try {
            return Math.toIntExact(longVal);
        } catch (ArithmeticException e) {
            logger.warn("The value set for {} needs to be less than {}. The {} value is set to {}", name,
                        Integer.MAX_VALUE, name, Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }
}
