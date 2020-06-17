/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.sql.utils;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.values.AbstractObjectValue;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StringValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.datasource.SQLDatasource;
import org.ballerinalang.sql.datasource.SQLDatasourceUtils;
import org.ballerinalang.sql.exception.ApplicationError;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class holds the utility methods involved with executing the query which does not return rows.
 *
 * @since 1.2.0
 */
public class ExecuteUtils {

    public static Object nativeExecute(ObjectValue client, Object paramSQLString) {
        Object dbClient = client.getNativeData(Constants.DATABASE_CLIENT);
        Strand strand = Scheduler.getStrand();
        if (dbClient != null) {
            SQLDatasource sqlDatasource = (SQLDatasource) dbClient;
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            String sqlQuery = null;
            try {
                if (paramSQLString instanceof StringValue) {
                    sqlQuery = ((StringValue) paramSQLString).getValue();
                } else {
                    sqlQuery = Utils.getSqlQuery((AbstractObjectValue) paramSQLString);
                }
                connection = SQLDatasourceUtils.getConnection(strand, client, sqlDatasource);
                statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
                if (paramSQLString instanceof AbstractObjectValue) {
                    Utils.setParams(connection, statement, (AbstractObjectValue) paramSQLString);
                }
                int count = statement.executeUpdate();
                Object lastInsertedId = null;
                if (!isDdlStatement(sqlQuery)) {
                    resultSet = statement.getGeneratedKeys();
                    if (resultSet.next()) {
                        lastInsertedId = getGeneratedKeys(resultSet);
                    }
                }
                Map<String, Object> resultFields = new HashMap<>();
                resultFields.put(Constants.AFFECTED_ROW_COUNT_FIELD, count);
                resultFields.put(Constants.LAST_INSERTED_ID_FIELD, lastInsertedId);
                return BallerinaValues.createRecordValue(Constants.SQL_PACKAGE_ID,
                        Constants.EXECUTION_RESULT_RECORD, resultFields);
            } catch (SQLException e) {
                return ErrorGenerator.getSQLDatabaseError(e,
                        "Error while executing sql query: " + sqlQuery + ". ");
            } catch (ApplicationError | IOException e) {
                return ErrorGenerator.getSQLApplicationError("Error while executing sql query: "
                        + sqlQuery + ". " + e.getMessage());
            } finally {
                Utils.closeResources(strand, resultSet, statement, connection);
            }
        } else {
            return ErrorGenerator.getSQLApplicationError(
                    "Client is not properly initialized!");
        }
    }

    public static Object nativeBatchExecute(ObjectValue client, ArrayValue paramSQLStrings) {
        Object dbClient = client.getNativeData(Constants.DATABASE_CLIENT);
        if (dbClient != null) {
            SQLDatasource sqlDatasource = (SQLDatasource) dbClient;
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            Strand strand = Scheduler.getStrand();
            String sqlQuery = null;
            List<AbstractObjectValue> parameters = new ArrayList<>();
            List<MapValue<BString, Object>> executionResults = new ArrayList<>();
            try {
                Object[] paramSQLObjects = paramSQLStrings.getValues();
                AbstractObjectValue parameterizedQuery = (AbstractObjectValue) paramSQLObjects[0];
                sqlQuery = Utils.getSqlQuery(parameterizedQuery);
                parameters.add(parameterizedQuery);
                for (int i = 1; i < paramSQLStrings.size(); i++) {
                    parameterizedQuery = (AbstractObjectValue) paramSQLObjects[i];
                    String paramSQLQuery = Utils.getSqlQuery(parameterizedQuery);

                    if (sqlQuery.equals(paramSQLQuery)) {
                        parameters.add(parameterizedQuery);
                    } else {
                        return ErrorGenerator.getSQLApplicationError("Batch Execute cannot contain different SQL " +
                                "commands. These has to be executed in different function calls");
                    }
                }
                connection = SQLDatasourceUtils.getConnection(strand, client, sqlDatasource);
                statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
                for (AbstractObjectValue param : parameters) {
                    Utils.setParams(connection, statement, param);
                    statement.addBatch();
                }
                int[] counts = statement.executeBatch();

                if (!isDdlStatement(sqlQuery)) {
                    resultSet = statement.getGeneratedKeys();
                }
                for (int count : counts) {
                    Map<String, Object> resultField = new HashMap<>();
                    resultField.put(Constants.AFFECTED_ROW_COUNT_FIELD, count);
                    Object lastInsertedId = null;
                    if (resultSet != null && resultSet.next()) {
                        lastInsertedId = getGeneratedKeys(resultSet);
                    }
                    resultField.put(Constants.LAST_INSERTED_ID_FIELD, lastInsertedId);
                    executionResults.add(BallerinaValues.createRecordValue(Constants.SQL_PACKAGE_ID,
                            Constants.EXECUTION_RESULT_RECORD, resultField));
                }
                return BValueCreator.createArrayValue(executionResults.toArray(), new BArrayType(
                        new BRecordType(Constants.EXECUTION_RESULT_RECORD, Constants.SQL_PACKAGE_ID, 0, false, 0)));
            } catch (BatchUpdateException e) {
                int[] updateCounts = e.getUpdateCounts();
                for (int count : updateCounts) {
                    Map<String, Object> resultField = new HashMap<>();
                    resultField.put(Constants.AFFECTED_ROW_COUNT_FIELD, count);
                    resultField.put(Constants.LAST_INSERTED_ID_FIELD, null);
                    executionResults.add(BallerinaValues.createRecordValue(Constants.SQL_PACKAGE_ID,
                            Constants.EXECUTION_RESULT_RECORD, resultField));
                }
                return ErrorGenerator.getSQLBatchExecuteError(e, executionResults,
                        "Error while executing batch command starting with: '" + sqlQuery + "'.");
            } catch (SQLException e) {
                return ErrorGenerator.getSQLDatabaseError(e, "Error while executing sql batch " +
                        "command starting with : " + sqlQuery + ". ");
            } catch (ApplicationError | IOException e) {
                return ErrorGenerator.getSQLApplicationError("Error while executing sql query: "
                        + e.getMessage());
            } finally {
                Utils.closeResources(strand, resultSet, statement, connection);
            }
        } else {
            return ErrorGenerator.getSQLApplicationError(
                    "Client is not properly initialized!");
        }
    }

    private static Object getGeneratedKeys(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        if (columnCount > 0) {
            int sqlType = metaData.getColumnType(1);
            switch (sqlType) {
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.INTEGER:
                case Types.BIGINT:
                case Types.BIT:
                case Types.BOOLEAN:
                    return rs.getLong(1);
                default:
                    return rs.getString(1);
            }
        }
        return null;
    }

    private static boolean isDdlStatement(String query) {
        String upperCaseQuery = query.trim().toUpperCase(Locale.ENGLISH);
        return Arrays.stream(DdlKeyword.values()).anyMatch(ddlKeyword -> upperCaseQuery.startsWith(ddlKeyword.name()));
    }

    private enum DdlKeyword {
        CREATE, ALTER, DROP, TRUNCATE, COMMENT, RENAME
    }
}
