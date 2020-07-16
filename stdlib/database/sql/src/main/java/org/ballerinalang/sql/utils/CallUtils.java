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
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.AbstractObjectValue;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.StringValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.sql.datasource.SQLDatasource;
import org.ballerinalang.sql.datasource.SQLDatasourceUtils;
import org.ballerinalang.sql.exception.ApplicationError;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.sql.Constants.AFFECTED_ROW_COUNT_FIELD;
import static org.ballerinalang.sql.Constants.CONNECTION_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.Constants.DATABASE_CLIENT;
import static org.ballerinalang.sql.Constants.EXECUTION_RESULT_FIELD;
import static org.ballerinalang.sql.Constants.EXECUTION_RESULT_RECORD;
import static org.ballerinalang.sql.Constants.LAST_INSERTED_ID_FIELD;
import static org.ballerinalang.sql.Constants.PROCEDURE_CALL_RESULT;
import static org.ballerinalang.sql.Constants.QUERY_RESULT_FIELD;
import static org.ballerinalang.sql.Constants.RESULT_SET_COUNT_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.Constants.RESULT_SET_TOTAL_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.Constants.SQL_PACKAGE_ID;
import static org.ballerinalang.sql.Constants.STATEMENT_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.Constants.TYPE_DESCRIPTIONS_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.utils.Utils.getColumnDefinitions;
import static org.ballerinalang.sql.utils.Utils.getDefaultStreamConstraint;
import static org.ballerinalang.sql.utils.Utils.getGeneratedKeys;

/**
 * This class holds the utility methods involved with executing the call statements.
 */
public class CallUtils {
    public static Object nativeCall(ObjectValue client, AbstractObjectValue paramSQLString, ArrayValue recordTypes) {
        Object dbClient = client.getNativeData(DATABASE_CLIENT);
        Strand strand = Scheduler.getStrand();
        if (dbClient != null) {
            SQLDatasource sqlDatasource = (SQLDatasource) dbClient;
            Connection connection;
            CallableStatement statement;
            ResultSet resultSet;
            String sqlQuery = null;
            try {
                if (paramSQLString instanceof StringValue) {
                    sqlQuery = ((StringValue) paramSQLString).getValue();
                } else {
                    sqlQuery = Utils.getSqlQuery(paramSQLString);
                }
                connection = SQLDatasourceUtils.getConnection(strand, client, sqlDatasource);
                statement = connection.prepareCall(sqlQuery);
                Utils.setParams(connection, statement, paramSQLString);
                boolean resultType = statement.execute();

                ObjectValue procedureCallResult = BallerinaValues.createObjectValue(SQL_PACKAGE_ID,
                        PROCEDURE_CALL_RESULT, strand);
                Object[] recordDescriptions = recordTypes.getValues();
                int resultSetCount = 0;
                if (resultType) {
                    List<ColumnDefinition> columnDefinitions;
                    BStructureType streamConstraint;
                    resultSet = statement.getResultSet();
                    if (recordTypes.size() == 0) {
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
                        streamConstraint = (BStructureType) ((TypedescValue) recordDescriptions[0]).getDescribingType();
                        columnDefinitions = getColumnDefinitions(resultSet, streamConstraint);
                        resultSetCount++;
                    }
                    StreamValue streamValue = new StreamValue(new BStreamType(streamConstraint),
                            Utils.createRecordIterator(resultSet, null, null, columnDefinitions, streamConstraint));
                    procedureCallResult.set(QUERY_RESULT_FIELD, streamValue);
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
                }
                procedureCallResult.addNativeData(STATEMENT_NATIVE_DATA_FIELD, statement);
                procedureCallResult.addNativeData(CONNECTION_NATIVE_DATA_FIELD, connection);
                procedureCallResult.addNativeData(TYPE_DESCRIPTIONS_NATIVE_DATA_FIELD, recordDescriptions);
                procedureCallResult.addNativeData(RESULT_SET_TOTAL_NATIVE_DATA_FIELD, recordTypes.size());
                procedureCallResult.addNativeData(RESULT_SET_COUNT_NATIVE_DATA_FIELD, resultSetCount);
                return procedureCallResult;
            } catch (SQLException e) {
                return ErrorGenerator.getSQLDatabaseError(e, "Error while executing sql query: " + sqlQuery + ". ");
            } catch (ApplicationError | IOException e) {
                return ErrorGenerator.getSQLApplicationError("Error while executing sql query: "
                        + sqlQuery + ". " + e.getMessage());
            }
        } else {
            return ErrorGenerator.getSQLApplicationError("Client is not properly initialized!");
        }
    }
}
