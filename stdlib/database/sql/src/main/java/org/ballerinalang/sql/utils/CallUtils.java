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
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.AbstractObjectValue;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.StringValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.datasource.SQLDatasource;
import org.ballerinalang.sql.datasource.SQLDatasourceUtils;
import org.ballerinalang.sql.exception.ApplicationError;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.sql.Constants.AFFECTED_ROW_COUNT_FIELD;
import static org.ballerinalang.sql.Constants.CONNECTION_NATIVE_DATA_FIELD;
import static org.ballerinalang.sql.Constants.DATABASE_CLIENT;
import static org.ballerinalang.sql.Constants.EXECUTION_RESULT_FIELD;
import static org.ballerinalang.sql.Constants.EXECUTION_RESULT_RECORD;
import static org.ballerinalang.sql.Constants.LAST_INSERTED_ID_FIELD;
import static org.ballerinalang.sql.Constants.PROCEDURE_CALL_META_DATA;
import static org.ballerinalang.sql.Constants.PROCEDURE_CALL_PARAM_CACHE;
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
import static org.ballerinalang.sql.utils.Utils.setSQLValueParam;

/**
 * This class holds the utility methods involved with executing the call statements.
 */
public class CallUtils {
    private static final Calendar calendar = Calendar.getInstance(
            TimeZone.getTimeZone(Constants.TIMEZONE_UTC.getValue()));

    public static Object nativeCall(ObjectValue client, Object paramSQLString, ArrayValue recordTypes) {
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
                    sqlQuery = Utils.getSqlQuery((AbstractObjectValue) paramSQLString);
                }
                connection = SQLDatasourceUtils.getConnection(strand, client, sqlDatasource);
                statement = connection.prepareCall(sqlQuery);

                Object cacheObject = client.getNativeData(PROCEDURE_CALL_PARAM_CACHE);
                Object metaDataObject = client.getNativeData(PROCEDURE_CALL_META_DATA);

                HashMap<Integer, Integer> cache;
                if (cacheObject != null) {
                    cache = (HashMap<Integer, Integer>) cacheObject;
                } else {
                    cache = new HashMap<>();
                }
                if (paramSQLString instanceof AbstractObjectValue) {
                    HashMap<Integer, Integer> metaData;
                    if (metaDataObject != null) {
                        metaData = (HashMap<Integer, Integer>) metaDataObject;
                    } else {
                        metaData = new HashMap<>();
                    }

                    setCallParameters(connection, statement, sqlQuery, (AbstractObjectValue) paramSQLString,
                            cache, metaData);
                    if (!cache.isEmpty()) {
                        client.addNativeData(PROCEDURE_CALL_PARAM_CACHE, cache);
                    }
                    if (!metaData.isEmpty()) {
                        client.addNativeData(PROCEDURE_CALL_META_DATA, metaData);
                    }
                }

                boolean resultType = statement.execute();

                if (paramSQLString instanceof AbstractObjectValue) {
                    populateOutParameters(statement, (AbstractObjectValue) paramSQLString, cache);
                }

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
                return ErrorGenerator.getSQLDatabaseError(e, "Error while executing SQL query: " + sqlQuery + ". ");
            } catch (ApplicationError | IOException e) {
                return ErrorGenerator.getSQLApplicationError("Error while executing SQL query: "
                        + sqlQuery + ". " + e.getMessage());
            }
        } else {
            return ErrorGenerator.getSQLApplicationError("Client is not properly initialized!");
        }
    }

    static void setCallParameters(Connection connection, CallableStatement statement, String sqlQuery,
                                  AbstractObjectValue paramString, HashMap<Integer, Integer> cache,
                                  HashMap<Integer, Integer> metaData)
            throws SQLException, ApplicationError, IOException {
        ArrayValue arrayValue = paramString.getArrayValue(Constants.ParameterizedQueryFields.INSERTIONS);
        for (int i = 0; i < arrayValue.size(); i++) {
            Object object = arrayValue.get(i);
            int index = i + 1;
            if (object instanceof ObjectValue) {
                ObjectValue objectValue = (ObjectValue) object;
                if ((objectValue.getType().getTag() != TypeTags.OBJECT_TYPE_TAG)) {
                    throw new ApplicationError("Unsupported type:" +
                            objectValue.getType().getQualifiedName() + " in column index: " + index);
                }

                String objectType = objectValue.getType().getName();
                Integer sqlType;
                switch (objectType) {
                    case Constants.ParameterObject.INOUT_PARAMETER:
                        Object innerObject = objectValue.get(Constants.ParameterObject.IN_VALUE_FIELD);
                        sqlType = !cache.isEmpty() ? cache.get(index) : null;
                        if (sqlType == null) {
                            sqlType = setSQLValueParam(connection, statement, innerObject, index, true);
                            cache.put(index, sqlType);
                        } else {
                            setSQLValueParam(connection, statement, innerObject, index, false);
                        }
                        statement.registerOutParameter(index, sqlType);
                        break;
                    case Constants.ParameterObject.OUT_PARAMETER:
                        sqlType = !cache.isEmpty() ? cache.get(index) : null;
                        if (sqlType == null) {
                            metaData.putAll(getOutParameterTypes(connection, sqlQuery, arrayValue.size()));
                            sqlType = metaData.get(index);
                            cache.put(index, sqlType);
                        }
                        statement.registerOutParameter(index, sqlType);
                        break;
                    default:
                        setSQLValueParam(connection, statement, object, index, false);
                }
            } else {
                setSQLValueParam(connection, statement, object, index, false);
            }
        }
    }

    static void populateOutParameters(CallableStatement statement, AbstractObjectValue paramSQLString,
                                      HashMap<Integer, Integer> cache) throws SQLException, ApplicationError {
        if (cache.size() == 0) {
            return;
        }
        ArrayValue arrayValue = paramSQLString.getArrayValue(Constants.ParameterizedQueryFields.INSERTIONS);

        for (Map.Entry<Integer, Integer> entry : cache.entrySet()) {
            int paramIndex = entry.getKey();
            int sqlType = entry.getValue();

            ObjectValue parameter = (ObjectValue) arrayValue.get(paramIndex - 1);
            parameter.addNativeData(Constants.ParameterObject.SQL_TYPE_NATIVE_DATA, sqlType);

            switch (sqlType) {
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR:
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getString(paramIndex));
                    break;
                case Types.BLOB:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getBlob(paramIndex));
                    break;
                case Types.CLOB:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getClob(paramIndex));
                    break;
                case Types.NCLOB:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getNClob(paramIndex));
                    break;
                case Types.DATE:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getDate(paramIndex, calendar));
                    break;
                case Types.TIME:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getTime(paramIndex, calendar));
                    break;
                case Types.TIME_WITH_TIMEZONE:
                    try {
                        Time time = statement.getTime(paramIndex, calendar);
                        parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA, time);
                    } catch (SQLException ex) {
                        //Some database drivers do not support getTime operation,
                        // therefore falling back to getObject method.
                        OffsetTime offsetTime = statement.getObject(paramIndex, OffsetTime.class);
                        parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                                Time.valueOf(offsetTime.toLocalTime()));
                    }
                    break;
                case Types.TIMESTAMP:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getTimestamp(paramIndex, calendar));
                    break;
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    try {
                        parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                                statement.getTimestamp(paramIndex, calendar));
                    } catch (SQLException ex) {
                        //Some database drivers do not support getTimestamp operation,
                        // therefore falling back to getObject method.
                        OffsetDateTime offsetDateTime = statement.getObject(paramIndex, OffsetDateTime.class);
                        parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                                Timestamp.valueOf(offsetDateTime.toLocalDateTime()));
                    }
                    break;
                case Types.ARRAY:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getArray(paramIndex));
                    break;
                case Types.ROWID:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getRowId(paramIndex));
                    break;
                case Types.TINYINT:
                case Types.SMALLINT:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA, statement.getInt(paramIndex));
                    break;
                case Types.INTEGER:
                case Types.BIGINT:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA, statement.getLong(paramIndex));
                    break;
                case Types.REAL:
                case Types.FLOAT:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getFloat(paramIndex));
                    break;
                case Types.DOUBLE:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getDouble(paramIndex));
                    break;
                case Types.NUMERIC:
                case Types.DECIMAL:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getBigDecimal(paramIndex));
                    break;
                case Types.BIT:
                case Types.BOOLEAN:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getBoolean(paramIndex));
                    break;
                case Types.REF:
                case Types.STRUCT:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getObject(paramIndex));
                    break;
                case Types.SQLXML:
                    parameter.addNativeData(Constants.ParameterObject.VALUE_NATIVE_DATA,
                            statement.getSQLXML(paramIndex));
                    break;
                default:
                    throw new ApplicationError("Unsupported SQL type '" + sqlType + "' when reading Procedure call " +
                            "Out parameter of index '" + paramIndex + "'.");
            }
        }
    }

    static HashMap<Integer, Integer> getOutParameterTypes(Connection connection, String sqlQuery, int size)
            throws ApplicationError {
        HashMap<Integer, Integer> colTypes = new HashMap<>();
        Pattern pattern = Pattern.compile("[^(]*\\s([^(]*).*");
        Matcher m = pattern.matcher(sqlQuery);
        if (m.matches() && m.groupCount() == 1) {
            String procedureCallName = m.group(1);
            try {
                ResultSet procCols = connection.getMetaData().getProcedureColumns(connection.getCatalog(),
                        connection.getSchema(), procedureCallName, null);
                int count = 1;
                while (procCols.next()) {
                    colTypes.put(count, procCols.getInt("DATA_TYPE"));
                    count++;
                }
                if (colTypes.size() != size) {
                    throw new ApplicationError("Unable to parse SQL call query to get metadata for OutParameter, " +
                            "as column count does not match for procedure '" + procCols.getString("PROCEDURE_NAME") +
                            "'.");
                }
            } catch (SQLException e) {
                throw new ApplicationError("Unable to parse SQL call query to get metadata for OutParameters of " +
                        "procedure, '" + procedureCallName + "', " + e.getMessage());
            }
        } else {
            throw new ApplicationError("Unable to parse SQL call query to process type of the Parameters. Ensure " +
                    "that the query follows standard format for procedure calls.");
        }
        return colTypes;
    }
}
