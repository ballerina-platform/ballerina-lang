/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.sql.utils;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.sql.Constants;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating SQL Client errors.
 *
 * @since 1.2.0
 */
public class ErrorGenerator {
    private ErrorGenerator() {
    }

    public static ErrorValue getSQLDatabaseError(SQLException exception, String messagePrefix) {
        String sqlErrorMessage =
                exception.getMessage() != null ? exception.getMessage() : Constants.DATABASE_ERROR_MESSAGE;
        int vendorCode = exception.getErrorCode();
        String sqlState = exception.getSQLState();
        String errorMessage = messagePrefix + sqlErrorMessage + ".";
        return getSQLDatabaseError(errorMessage, vendorCode, sqlState);
    }

    public static ErrorValue getSQLApplicationError(String detailedErrorMessage) {
        return BallerinaErrors.createDistinctError(Constants.APPLICATION_ERROR, Constants.SQL_PACKAGE_ID,
                detailedErrorMessage);
    }

    private static ErrorValue getSQLDatabaseError(String message, int vendorCode, String sqlState) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(Constants.ErrorRecordFields.ERROR_CODE, vendorCode);
        valueMap.put(Constants.ErrorRecordFields.SQL_STATE, sqlState);
        MapValue<BString, Object> sqlClientErrorDetailRecord = BallerinaValues.
                createRecordValue(Constants.SQL_PACKAGE_ID, Constants.DATABASE_ERROR_DATA, valueMap);
        return BallerinaErrors.createDistinctError(Constants.DATABASE_ERROR, Constants.SQL_PACKAGE_ID,
                message, sqlClientErrorDetailRecord);
    }
}
