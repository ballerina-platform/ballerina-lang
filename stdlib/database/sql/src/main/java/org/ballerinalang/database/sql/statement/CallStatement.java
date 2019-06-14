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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.database.sql.statement;

import org.ballerinalang.bre.Context;
import org.ballerinalang.database.sql.Constants;
import org.ballerinalang.database.sql.SQLDatasource;
import org.ballerinalang.database.sql.SQLDatasourceUtils;
import org.ballerinalang.model.ColumnDefinition;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.TableResourceManager;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.ballerinalang.database.sql.Constants.PARAMETER_RECORD_TYPE_FIELD;
import static org.ballerinalang.database.sql.Constants.PARAMETER_VALUE_FIELD;

/**
 * Represents a Call SQL statement.
 *
 * @since 1.0.0
 */
public class CallStatement extends AbstractSQLStatement {

    private final Context context;
    private final SQLDatasource datasource;
    private final String query;
    private final BValueArray parameters;
    private final BValueArray structTypes;

    public CallStatement(Context context, SQLDatasource datasource, String query, BValueArray parameters,
                         BValueArray structTypes) {

        this.context = context;
        this.datasource = datasource;
        this.query = query;
        this.parameters = parameters;
        this.structTypes = structTypes;
    }

    @Override
    public void execute() {
        checkAndObserveSQLAction(context, datasource, query);
        Connection conn = null;
        CallableStatement stmt = null;
        List<ResultSet> resultSets = null;
        boolean isInTransaction = context.isInTransaction();
        try {
            BValueArray generatedParams = constructParameters(context, parameters);
            conn = getDatabaseConnection(context, datasource, false);
            stmt = getPreparedCall(conn, datasource, query, generatedParams);
            createProcessedStatement(conn, stmt, generatedParams, datasource.getDatabaseProductName());
            boolean refCursorOutParamsPresent = generatedParams != null && isRefCursorOutParamPresent(generatedParams);
            boolean resultSetsReturned = false;
            TableResourceManager rm = null;
            boolean requiredToReturnTables = structTypes != null && structTypes.size() > 0;
            if (requiredToReturnTables) {
                resultSets = executeStoredProc(stmt, datasource.getDatabaseProductName());
                resultSetsReturned = !resultSets.isEmpty();
            } else {
                stmt.execute();
            }
            if (resultSetsReturned || refCursorOutParamsPresent) {
                rm = new TableResourceManager(conn, stmt, !isInTransaction);
            }
            setOutParameters(context, stmt, parameters, rm);
            if (resultSetsReturned) {
                rm.addAllResultSets(resultSets);
                // If a result set has been returned from the stored procedure it needs to be pushed in to return
                // values
                context.setReturnValues(constructTablesForResultSets(resultSets, rm, context, structTypes,
                        datasource.getDatabaseProductName()));
            } else if (!refCursorOutParamsPresent) {
                // Even if there aren't any result sets returned from the procedure there could be ref cursors
                // returned as OUT params. If there are present we cannot clean up the connection. If there is no
                // returned result set or ref cursor OUT params we should cleanup the connection.
                cleanupResources(resultSets, stmt, conn, !isInTransaction);
                context.setReturnValues();
            }
        } catch (Throwable e) {
            cleanupResources(resultSets, stmt, conn, !isInTransaction);
            context.setReturnValues(
                    SQLDatasourceUtils.getSQLConnectorError(context, e, "execute stored procedure failed: "));
            handleErrorOnTransaction(context);
            checkAndObserveSQLError(context, "execute stored procedure failed: " + e.getMessage());
        }
    }

    private BValueArray constructTablesForResultSets(List<ResultSet> resultSets, TableResourceManager rm,
                                                     Context context, BValueArray structTypes,
                                                     String databaseProductName)
            throws SQLException {
        BType returnedTableType =
                ((BUnionType) context.getCallableUnitInfo().getRetParamTypes()[0]).getMemberTypes().get(0);
        BValueArray bTables = new BValueArray(returnedTableType);
        // TODO: "mysql" equality condition is part of the temporary fix to support returning the result set in the case
        // of stored procedures returning only one result set in MySQL. Refer ballerina-platform/ballerina-lang#8643
        if (databaseProductName.contains(Constants.DatabaseNames.MYSQL)
                && (structTypes != null && structTypes.size() > 1)) {
            throw new BallerinaException(
                    "Retrieving result sets from stored procedures returning more than one result set, "
                            + "is not supported");
        } else if (structTypes == null || resultSets.size() != structTypes.size()) {
            throw new BallerinaException(
                    "Mismatching record type count: " + (structTypes == null ? 0 : structTypes.size()) + " and "
                            + "returned result set count: " + resultSets.size() + " from the stored procedure");
        }
        for (int i = 0; i < resultSets.size(); i++) {
            bTables.add(i, constructTable(rm, context, resultSets.get(i),
                    (BStructureType) structTypes.getRefValue(i).value(), databaseProductName));
        }
        return bTables;
    }

    private BTable constructTable(TableResourceManager rm, Context context, ResultSet rs, BStructureType structType,
                                    String databaseProductName) throws SQLException {
        List<ColumnDefinition> columnDefinitions = getColumnDefinitions(rs);
        return constructTable(rm, context, rs, structType, columnDefinitions, databaseProductName);
    }

    private List<ResultSet> executeStoredProc(CallableStatement stmt, String databaseProductName) throws SQLException {
        boolean resultAndNoUpdateCount = stmt.execute();
        List<ResultSet> resultSets = new ArrayList<>();
        ResultSet result;
        while (true) {
            if (!resultAndNoUpdateCount) {
                // Current result is an update count(not a ResultSet) or there is no result at all
                int updateCount = stmt.getUpdateCount();
                if (updateCount == -1) {
                    // There is no result at all
                    break;
                }
            } else {
                // Current result is a ResultSet
                result = stmt.getResultSet();
                resultSets.add(result);
                if (databaseProductName.contains(Constants.DatabaseNames.MYSQL)) {
                    // TODO: "mysql" equality condition is part of the temporary fix to support returning the result
                    // set in the case of stored procedures returning only one result set in MySQL. Refer
                    // ballerina-platform/ballerina-lang#8643
                    break;
                }

            }
            // This point reaches if current result was an update count. So it is needed to capture any remaining
            // results
            try {
                resultAndNoUpdateCount = stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT);
            } catch (SQLException e) {
                break;
            }
        }
        return resultSets;
    }

    private void setOutParameters(Context context, CallableStatement stmt, BValueArray params,
                                  TableResourceManager rm) {
        if (params == null) {
            return;
        }
        int paramCount = (int) params.size();
        for (int index = 0; index < paramCount; index++) {
            if (params.getRefValue(index).getType().getTag() != TypeTags.OBJECT_TYPE_TAG
                    && params.getRefValue(index).getType().getTag() != TypeTags.RECORD_TYPE_TAG) {
                continue;
            }
            BMap<String, BValue> paramValue = (BMap<String, BValue>) params.getRefValue(index);
            if (paramValue != null) {
                String sqlType = getSQLType(paramValue);
                int direction = getParameterDirection(paramValue);
                if (direction == Constants.QueryParamDirection.INOUT
                        || direction == Constants.QueryParamDirection.OUT) {
                    setOutParameterValue(context, stmt, sqlType, index, paramValue, rm);
                }
            } else {
                throw new BallerinaException("out value cannot set for null parameter with index: " + index);
            }
        }
    }

    private void setOutParameterValue(Context context, CallableStatement stmt, String sqlType, int index,
                                      BMap<String, BValue> paramValue, TableResourceManager resourceManager) {
        try {
            String sqlDataType = sqlType.toUpperCase(Locale.getDefault());
            switch (sqlDataType) {
                case Constants.SQLDataTypes.INTEGER: {
                    int value = stmt.getInt(index + 1);
                    //Value is the first position of the struct
                    paramValue.put(PARAMETER_VALUE_FIELD, new BInteger(value));
                }
                break;
                case Constants.SQLDataTypes.VARCHAR: {
                    String value = stmt.getString(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BString(value));
                }
                break;
                case Constants.SQLDataTypes.NUMERIC:
                case Constants.SQLDataTypes.DECIMAL: {
                    BigDecimal value = stmt.getBigDecimal(index + 1);
                    if (value == null) {
                        paramValue.put(PARAMETER_VALUE_FIELD, new BFloat(0));
                    } else {
                        paramValue.put(PARAMETER_VALUE_FIELD, new BFloat(value.doubleValue()));
                    }
                }
                break;
                case Constants.SQLDataTypes.BIT:
                case Constants.SQLDataTypes.BOOLEAN: {
                    boolean value = stmt.getBoolean(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BBoolean(value));
                }
                break;
                case Constants.SQLDataTypes.TINYINT: {
                    byte value = stmt.getByte(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BInteger(value));
                }
                break;
                case Constants.SQLDataTypes.SMALLINT: {
                    short value = stmt.getShort(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BInteger(value));
                }
                break;
                case Constants.SQLDataTypes.BIGINT: {
                    long value = stmt.getLong(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BInteger(value));
                }
                break;
                case Constants.SQLDataTypes.REAL:
                case Constants.SQLDataTypes.FLOAT: {
                    float value = stmt.getFloat(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BFloat(value));
                }
                break;
                case Constants.SQLDataTypes.DOUBLE: {
                    double value = stmt.getDouble(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BFloat(value));
                }
                break;
                case Constants.SQLDataTypes.CLOB: {
                    Clob value = stmt.getClob(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BString(SQLDatasourceUtils.getString(value)));
                }
                break;
                case Constants.SQLDataTypes.BLOB: {
                    Blob value = stmt.getBlob(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BString(SQLDatasourceUtils.getString(value)));
                }
                break;
                case Constants.SQLDataTypes.BINARY: {
                    byte[] value = stmt.getBytes(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BString(SQLDatasourceUtils.getString(value)));
                }
                break;
                case Constants.SQLDataTypes.DATE: {
                    Date value = stmt.getDate(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BString(SQLDatasourceUtils.getString(value)));
                }
                break;
                case Constants.SQLDataTypes.TIMESTAMP:
                case Constants.SQLDataTypes.DATETIME: {
                    Timestamp value = stmt.getTimestamp(index + 1, utcCalendar);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BString(SQLDatasourceUtils.getString(value)));
                }
                break;
                case Constants.SQLDataTypes.TIME: {
                    Time value = stmt.getTime(index + 1, utcCalendar);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BString(SQLDatasourceUtils.getString(value)));
                }
                break;
                case Constants.SQLDataTypes.ARRAY: {
                    Array value = stmt.getArray(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, new BString(SQLDatasourceUtils.getString(value)));
                }
                break;
                case Constants.SQLDataTypes.STRUCT: {
                    Object value = stmt.getObject(index + 1);
                    String stringValue = "";
                    if (value != null) {
                        if (value instanceof Struct) {
                            stringValue = SQLDatasourceUtils.getString((Struct) value);
                        } else {
                            stringValue = value.toString();
                        }
                    }
                    paramValue.put(PARAMETER_VALUE_FIELD, new BString(stringValue));
                }
                break;
                case Constants.SQLDataTypes.REFCURSOR: {
                    ResultSet rs = (ResultSet) stmt.getObject(index + 1);
                    BStructureType structType = getStructType(paramValue);
                    if (structType != null) {
                        resourceManager.addResultSet(rs);
                        SQLDatasource datasource = retrieveDatasource(context);
                        paramValue.put(PARAMETER_VALUE_FIELD,
                                constructTable(resourceManager, context, rs, getStructType(paramValue),
                                        datasource.getDatabaseProductName()));
                    } else {
                        throw new BallerinaException(
                                "The Struct Type for the result set pointed by the Ref Cursor cannot be null");
                    }
                    break;
                }
                default:
                    throw new BallerinaException(
                            "unsupported datatype as out/inout parameter: " + sqlType + " index:" + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in getting out parameter value: " + e.getMessage(), e);
        }
    }

    private SQLDatasource retrieveDatasource(Context context) {
        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        return (SQLDatasource) bConnector.getNativeData(Constants.SQL_CLIENT);
    }

    private BStructureType getStructType(BMap<String, BValue> parameter) {
        BTypeDescValue type = (BTypeDescValue) parameter.get(PARAMETER_RECORD_TYPE_FIELD);
        BStructureType structType = null;
        if (type != null) {
            structType = (BStructureType) type.value();
        }
        return structType;
    }

    private CallableStatement getPreparedCall(Connection conn, SQLDatasource datasource, String query,
                                              BValueArray parameters) throws SQLException {
        CallableStatement stmt;
        boolean mysql = datasource.getDatabaseProductName().contains(Constants.DatabaseNames.MYSQL);
        if (mysql) {
            stmt = conn.prepareCall(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            /* Only stream if there aren't any OUT parameters since can't use streaming result sets with callable
               statements that have output parameters */
            if (parameters != null && !hasOutParams(parameters)) {
                stmt.setFetchSize(Integer.MIN_VALUE);
            }
        } else {
            stmt = conn.prepareCall(query);
        }
        return stmt;
    }

    private boolean hasOutParams(BValueArray params) {
        int paramCount = (int) params.size();
        for (int index = 0; index < paramCount; index++) {
            BMap<String, BValue> paramValue = (BMap<String, BValue>) params.getRefValue(index);
            int direction = getParameterDirection(paramValue);
            if (direction == Constants.QueryParamDirection.OUT || direction == Constants.QueryParamDirection.INOUT) {
                return true;
            }
        }
        return false;
    }

    private boolean isRefCursorOutParamPresent(BValueArray params) {
        boolean refCursorOutParamPresent = false;
        int paramCount = (int) params.size();
        for (int index = 0; index < paramCount; index++) {
            BMap<String, BValue> paramValue = (BMap<String, BValue>) params.getRefValue(index);
            if (paramValue != null) {
                String sqlType = getSQLType(paramValue);
                int direction = getParameterDirection(paramValue);
                if (direction == Constants.QueryParamDirection.OUT && Constants.SQLDataTypes.REFCURSOR
                        .equals(sqlType)) {
                    refCursorOutParamPresent = true;
                    break;
                }
            }
        }
        return refCursorOutParamPresent;
    }

    /**
     * This will close database connection, statement and result sets.
     *
     * @param resultSets SQL result sets
     * @param stmt SQL statement
     * @param conn SQL connection
     * @param connectionClosable Whether the connection is closable or not. If the connection is not closable this
     * method will not release the connection. Therefore to avoid connection leaks it should have been taken care
     * of externally.
     */
    private void cleanupResources(List<ResultSet> resultSets, Statement stmt, Connection conn,
            boolean connectionClosable) {
        try {
            if (resultSets != null) {
                for (ResultSet rs : resultSets) {
                    if (rs != null && !rs.isClosed()) {
                        rs.close();
                    }
                }
            }
            cleanupResources(stmt, conn, connectionClosable);
        } catch (SQLException e) {
            throw new BallerinaException("error in cleaning sql resources: " + e.getMessage(), e);
        }
    }
}

