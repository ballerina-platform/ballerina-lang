/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.sql.utils;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.exception.ApplicationError;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.sql.Constants.AFFECTED_ROW_COUNT_FIELD;
import static org.ballerinalang.sql.Constants.EXECUTION_RESULT_FIELD;
import static org.ballerinalang.sql.Constants.EXECUTION_RESULT_RECORD;
import static org.ballerinalang.sql.Constants.LAST_INSERTED_ID_FIELD;
import static org.ballerinalang.sql.Constants.QUERY_RESULT_FIELD;
import static org.ballerinalang.sql.Constants.RESULT_SET_COUNT_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.Constants.RESULT_SET_TOTAL_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.Constants.SQL_PACKAGE_ID;
import static org.ballerinalang.sql.Constants.STATEMENT_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.Constants.TYPE_DESCRIPTIONS_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.utils.Utils.cleanUpConnection;
import static org.ballerinalang.sql.utils.Utils.createRecordIterator;
import static org.ballerinalang.sql.utils.Utils.getColumnDefinitions;
import static org.ballerinalang.sql.utils.Utils.getDefaultStreamConstraint;
import static org.ballerinalang.sql.utils.Utils.getGeneratedKeys;

/**
 * This class provides functionality for the `ProcedureCallResult` to iterate through the sql result sets.
 */
public class ProcedureCallResultUtils {

    public static Object getNextQueryResult(ObjectValue procedureCallResult) {
        CallableStatement statement = (CallableStatement) procedureCallResult
                .getNativeData(STATEMENT_NATIVE_DATA_FIELD);
        ResultSet resultSet;
        try {
            boolean moreResults = statement.getMoreResults();
            if (moreResults) {
                List<ColumnDefinition> columnDefinitions;
                BStructureType streamConstraint;
                resultSet = statement.getResultSet();
                int totalRecordDescriptions = (int) procedureCallResult
                        .getNativeData(RESULT_SET_TOTAL_NATIVE_DATA_FIELD);
                if (totalRecordDescriptions == 0) {
                    columnDefinitions = getColumnDefinitions(resultSet, null);
                    BRecordType defaultRecord = getDefaultStreamConstraint();
                    Map<String, BField> fieldMap = new HashMap<>();
                    for (ColumnDefinition column : columnDefinitions) {
                        int flags = Flags.PUBLIC;
                        if (column.isNullable()) {
                            flags += Flags.OPTIONAL;
                        } else {
                            flags += Flags.REQUIRED;
                        }
                        fieldMap.put(column.getColumnName(), new BField(column.getBallerinaType(),
                                column.getColumnName(), flags));
                    }
                    defaultRecord.setFields(fieldMap);
                    streamConstraint = defaultRecord;
                } else {
                    Object[] recordDescriptions = (Object[]) procedureCallResult
                            .getNativeData(TYPE_DESCRIPTIONS_NATIVE_DATA_FIELD);
                    int recordDescription = (int) procedureCallResult.getNativeData(RESULT_SET_COUNT_NATIVE_DATA_FIELD);
                    if (recordDescription <= totalRecordDescriptions) {
                        streamConstraint = (BStructureType)
                                ((TypedescValue) recordDescriptions[recordDescription]).getDescribingType();
                        columnDefinitions = getColumnDefinitions(resultSet, streamConstraint);
                        procedureCallResult.addNativeData(RESULT_SET_COUNT_NATIVE_DATA_FIELD, recordDescription + 1);
                    } else {
                        throw new ApplicationError("The record description array count does not match with the " +
                                "returned result sets count.");
                    }
                }
                StreamValue streamValue = new StreamValue(new BStreamType(streamConstraint),
                        createRecordIterator(resultSet, null, null, columnDefinitions, streamConstraint));
                procedureCallResult.set(QUERY_RESULT_FIELD, streamValue);
                procedureCallResult.set(EXECUTION_RESULT_FIELD, null);
            } else {
                Object lastInsertedId = null;
                int count = statement.getUpdateCount();
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    lastInsertedId = getGeneratedKeys(resultSet);
                }
                Map<String, Object> resultFields = new HashMap<>();
                resultFields.put(AFFECTED_ROW_COUNT_FIELD, count);
                resultFields.put(LAST_INSERTED_ID_FIELD, lastInsertedId);
                MapValue<BString, Object> executionResult = BallerinaValues.createRecordValue(
                        SQL_PACKAGE_ID, EXECUTION_RESULT_RECORD, resultFields);
                procedureCallResult.set(EXECUTION_RESULT_FIELD, executionResult);
                procedureCallResult.set(QUERY_RESULT_FIELD, null);
            }
            return moreResults;
        } catch (SQLException e) {
            return ErrorGenerator.getSQLDatabaseError(e, "Error when accessing the next query result.");
        } catch (ApplicationError e) {
            return ErrorGenerator.getSQLApplicationError("Error when accessing the next query result. "
                    + e.getMessage());
        } catch (Throwable throwable) {
            return ErrorGenerator.getSQLApplicationError("Error when accessing the next SQL result. "
                    + throwable.getMessage());
        }
    }

    public static Object closeCallResult(ObjectValue procedureCallResult) {
        Statement statement = (Statement) procedureCallResult.getNativeData(Constants.STATEMENT_NATIVE_DATA_FIELD);
        Connection connection = (Connection) procedureCallResult.getNativeData(Constants.CONNECTION_NATIVE_DATA_FIELD);
        return cleanUpConnection(procedureCallResult, null, statement, connection);
    }
}
