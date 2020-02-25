/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.sql.exceptions;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.sql.Constants;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating JDBC Client errors.
 *
 * @since 1.0.0
 */
public class ErrorGenerator {
    private ErrorGenerator() {

    }

    public static ErrorValue getSQLDatabaseError(SQLException exception) {
        return getSQLDatabaseError(exception, "");
    }

    public static ErrorValue getSQLDatabaseError(SQLException exception, String messagePrefix) {
        String sqlErrorMessage =
                exception.getMessage() != null ? exception.getMessage() : Constants.DATABASE_ERROR_MESSAGE;
        int vendorCode = exception.getErrorCode();
        String sqlState = exception.getSQLState();
        String errorMessage = messagePrefix + sqlErrorMessage + ".";
        return getSQLDatabaseError(errorMessage, vendorCode, sqlState);
    }

    public static ErrorValue getSQLApplicationError(ApplicationException exception, String messagePrefix) {
        String message =
                exception.getMessage() != null ? exception.getMessage() : Constants.APPLICATION_ERROR_MESSAGE;
        String detailedErrorMessage = messagePrefix + message + ".";
        if (exception.getDetailedErrorMessage() != null) {
            detailedErrorMessage += " " + exception.getDetailedErrorMessage() + ".";
        }
        return getSQLApplicationError(detailedErrorMessage);
    }

    public static ErrorValue getSQLApplicationError(ApplicationException exception) {
        return getSQLApplicationError(exception, "");
    }

    public static ErrorValue getSQLApplicationError(String detailedErrorMessage) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("message", detailedErrorMessage);
        MapValue<String, Object> sqlClientErrorDetailRecord = BallerinaValues.
                createRecordValue(Constants.SQL_PACKAGE_ID, Constants.APPLICATION_ERROR_DATA_RECORD_NAME, valueMap);
        return BallerinaErrors.createError(Constants.APPLICATION_ERROR_CODE, sqlClientErrorDetailRecord);
    }

    private static ErrorValue getSQLDatabaseError(String message, int vendorCode, String sqlState) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("message", message);
        valueMap.put("errorCode", vendorCode);
        valueMap.put("sqlState", sqlState);
        MapValue<String, Object> sqlClientErrorDetailRecord = BallerinaValues.
                createRecordValue(Constants.SQL_PACKAGE_ID, Constants.DATABASE_ERROR_DATA_RECORD_NAME, valueMap);
        return BallerinaErrors.createError(Constants.DATABASE_ERROR_CODE, sqlClientErrorDetailRecord);
    }
}
