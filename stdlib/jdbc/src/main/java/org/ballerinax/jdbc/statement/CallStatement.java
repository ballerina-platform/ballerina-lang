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
package org.ballerinax.jdbc.statement;

import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.TableResourceManager;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.SQLDatasource;
import org.ballerinax.jdbc.datasource.SQLDatasourceUtils;
import org.ballerinax.jdbc.exceptions.ApplicationException;
import org.ballerinax.jdbc.exceptions.ErrorGenerator;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.ballerinax.jdbc.Constants.PARAMETER_RECORD_TYPE_FIELD;
import static org.ballerinax.jdbc.Constants.PARAMETER_VALUE_FIELD;

/**
 * Represents a Call SQL statement.
 *
 * @since 1.0.0
 */
public class CallStatement extends AbstractSQLStatement {

    private final ObjectValue client;
    private final SQLDatasource datasource;
    private final String query;
    private final ArrayValue parameters;
    private final ArrayValue structTypes;

    public CallStatement(ObjectValue client, SQLDatasource datasource, String query, ArrayValue structTypes,
                         ArrayValue parameters, Strand strand) {
        super(strand);
        this.client = client;
        this.datasource = datasource;
        this.query = query;
        this.parameters = parameters;
        this.structTypes = structTypes;
    }

    @Override
    public Object execute() {
        checkAndObserveSQLAction(strand, datasource, query);
        Connection conn = null;
        CallableStatement stmt = null;
        List<ResultSet> resultSets = null;
        boolean isInTransaction = strand.isInTransaction();
        String errorMessagePrefix = "failed to execute stored procedure: ";
        try {
            ArrayValue generatedParams = constructParameters(parameters);
            conn = getDatabaseConnection(strand, client, datasource);
            stmt = getPreparedCall(conn, datasource, query, generatedParams);
            ProcessedStatement processedStatement = new ProcessedStatement(conn, stmt, generatedParams,
                    datasource.getDatabaseProductName());
            stmt = (CallableStatement) processedStatement.prepare();
            boolean refCursorOutParamsPresent = isRefCursorOutParamPresent(generatedParams);
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
            setOutParameters(stmt, parameters, rm);
            if (resultSetsReturned) {
                rm.addAllResultSets(resultSets);
                // If a result set has been returned from the stored procedure it needs to be pushed in to return
                // values
                return constructTablesForResultSets(resultSets, rm, structTypes, datasource.getDatabaseProductName());

            } else if (!refCursorOutParamsPresent) {
                // Even if there aren't any result sets returned from the procedure there could be ref cursors
                // returned as OUT params. If there are present we cannot clean up the connection. If there is no
                // returned result set or ref cursor OUT params we should cleanup the connection.
                cleanupResources(resultSets, stmt, conn, !isInTransaction);
            }
        } catch (SQLException e) {
            cleanupResources(resultSets, stmt, conn, !isInTransaction);
            handleErrorOnTransaction(this.strand);
            checkAndObserveSQLError(strand, "execute stored procedure failed: " + e.getMessage());
            return ErrorGenerator.getSQLDatabaseError(e, errorMessagePrefix);
        } catch (ApplicationException e) {
            cleanupResources(resultSets, stmt, conn, !isInTransaction);
            handleErrorOnTransaction(this.strand);
            checkAndObserveSQLError(strand, "execute stored procedure failed: " + e.getMessage());
            return ErrorGenerator.getSQLApplicationError(e, errorMessagePrefix);
        }
        return null;
    }

    private ArrayValue constructTablesForResultSets(List<ResultSet> resultSets, TableResourceManager rm,
                                                     ArrayValue structTypes, String databaseProductName)
            throws SQLException, ApplicationException {
        int typeFlags = TypeFlags.asMask(TypeFlags.ANYDATA, TypeFlags.PURETYPE);
        BRecordType tableConstraint = new BRecordType("$table$anon$constraint$", null, 0, false, typeFlags);
        tableConstraint.restFieldType = BTypes.typeAnydata;
        ArrayValue bTables = new ArrayValueImpl(new BArrayType(new BTableType(tableConstraint)));
        // TODO: "mysql" equality condition is part of the temporary fix to support returning the result set in the case
        // of stored procedures returning only one result set in MySQL. Refer ballerina-platform/ballerina-lang#8643
        if (databaseProductName.contains(Constants.DatabaseNames.MYSQL)
                && (structTypes != null && structTypes.size() > 1)) {
            throw new ApplicationException(
                    "it is not supported to retrieve result sets from stored procedures since it is returning " +
                            "more than one result set");
        } else if (structTypes == null || resultSets.size() != structTypes.size()) {
            throw new ApplicationException(
                    "mismatching record type count " + (structTypes == null ? 0 : structTypes.size()) + " and "
                            + "returned result set count " + resultSets.size() + " from the stored procedure");
        }
        for (int i = 0; i < resultSets.size(); i++) {
            TypedescValue typedescValue = (TypedescValue) structTypes.get(i);
            BStructureType structureType = (BStructureType) typedescValue.getDescribingType();
            bTables.add(i, constructTable(rm, resultSets.get(i), structureType, databaseProductName));
        }
        return bTables;
    }

    private TableValue constructTable(TableResourceManager rm, ResultSet rs, BStructureType structType,
                                    String databaseProductName) throws SQLException {
        List<ColumnDefinition> columnDefinitions = getColumnDefinitions(rs);
        return constructTable(rm, rs, structType, columnDefinitions, databaseProductName);
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

    private void setOutParameters(CallableStatement stmt, ArrayValue params, TableResourceManager rm)
            throws SQLException, ApplicationException {
        if (params == null) {
            return;
        }
        int paramCount = params.size();
        for (int index = 0; index < paramCount; index++) {
            org.ballerinalang.jvm.types.BType type = TypeChecker.getType(params.get(index));
            if (type.getTag() != TypeTags.RECORD_TYPE_TAG) {
                continue;
            }
            MapValue<String, Object> paramValue = (MapValue<String, Object>) params.get(index);
            if (paramValue != null) {
                String sqlType = StatementProcessUtils.getSQLType(paramValue);
                int direction = StatementProcessUtils.getParameterDirection(paramValue);
                if (direction == Constants.QueryParamDirection.INOUT
                        || direction == Constants.QueryParamDirection.OUT) {
                    setOutParameterValue(stmt, sqlType, index, paramValue, rm);
                }
            } else {
                throw new ApplicationException("OUT value cannot be set for a null parameter at index " + index);
            }
        }
    }

    private void setOutParameterValue(CallableStatement stmt, String sqlType, int index,
            MapValue<String, Object> paramValue, TableResourceManager resourceManager)
            throws SQLException, ApplicationException {
        try {
            String sqlDataType = sqlType.toUpperCase(Locale.getDefault());
            switch (sqlDataType) {
                case Constants.SQLDataTypes.INTEGER: {
                    int value = stmt.getInt(index + 1);
                    //Value is the first position of the struct
                    paramValue.put(PARAMETER_VALUE_FIELD, (long) value);
                }
                break;
                case Constants.SQLDataTypes.NVARCHAR:
                case Constants.SQLDataTypes.NCHAR: {
                    String value = stmt.getNString(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, value);
                }
                break;
                case Constants.SQLDataTypes.CHAR:
                case Constants.SQLDataTypes.LONGNVARCHAR:
                case Constants.SQLDataTypes.VARCHAR: {
                    String value = stmt.getString(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, value);
                }
                break;
                case Constants.SQLDataTypes.NUMERIC:
                case Constants.SQLDataTypes.DECIMAL: {
                    BigDecimal value = stmt.getBigDecimal(index + 1);
                    if (value == null) {
                        paramValue.put(PARAMETER_VALUE_FIELD, 0);
                    } else {
                        paramValue.put(PARAMETER_VALUE_FIELD, new DecimalValue(value));
                    }
                }
                break;
                case Constants.SQLDataTypes.BIT:
                case Constants.SQLDataTypes.BOOLEAN: {
                    boolean value = stmt.getBoolean(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, value);
                }
                break;
                case Constants.SQLDataTypes.TINYINT: {
                    byte value = stmt.getByte(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, value);
                }
                break;
                case Constants.SQLDataTypes.SMALLINT: {
                    short value = stmt.getShort(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, (long) value);
                }
                break;
                case Constants.SQLDataTypes.BIGINT: {
                    long value = stmt.getLong(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, value);
                }
                break;
                case Constants.SQLDataTypes.REAL:
                case Constants.SQLDataTypes.FLOAT: {
                    float value = stmt.getFloat(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, (double) value);
                }
                break;
                case Constants.SQLDataTypes.DOUBLE: {
                    double value = stmt.getDouble(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, value);
                }
                break;
                case Constants.SQLDataTypes.CLOB: {
                    Clob value = stmt.getClob(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, SQLDatasourceUtils.getString(value));
                }
                break;
                case Constants.SQLDataTypes.BLOB: {
                    Blob value = stmt.getBlob(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, SQLDatasourceUtils.getString(value));
                }
                break;
                case Constants.SQLDataTypes.NCLOB: {
                    NClob value = stmt.getNClob(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, SQLDatasourceUtils.getString(value));
                }
                break;
                case Constants.SQLDataTypes.VARBINARY:
                case Constants.SQLDataTypes.BINARY: {
                    byte[] value = stmt.getBytes(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, SQLDatasourceUtils.getString(value));
                }
                break;
                case Constants.SQLDataTypes.DATE: {
                    Date value = stmt.getDate(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, SQLDatasourceUtils.getString(value));
                }
                break;
                case Constants.SQLDataTypes.TIMESTAMP:
                case Constants.SQLDataTypes.DATETIME: {
                    Timestamp value = stmt.getTimestamp(index + 1, utcCalendar);
                    paramValue.put(PARAMETER_VALUE_FIELD, SQLDatasourceUtils.getString(value));
                }
                break;
                case Constants.SQLDataTypes.TIME: {
                    Time value = stmt.getTime(index + 1, utcCalendar);
                    paramValue.put(PARAMETER_VALUE_FIELD, SQLDatasourceUtils.getString(value));
                }
                break;
                case Constants.SQLDataTypes.ARRAY: {
                    Array value = stmt.getArray(index + 1);
                    paramValue.put(PARAMETER_VALUE_FIELD, SQLDatasourceUtils.getString(value));
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
                    paramValue.put(PARAMETER_VALUE_FIELD, stringValue);
                }
                break;
                case Constants.SQLDataTypes.REFCURSOR: {
                    ResultSet rs = (ResultSet) stmt.getObject(index + 1);
                    BStructureType structType = getStructType(paramValue);
                    if (structType != null) {
                        resourceManager.addResultSet(rs);
                        SQLDatasource datasource = retrieveDatasource(client);
                        paramValue.put(PARAMETER_VALUE_FIELD,
                                constructTable(resourceManager, rs, getStructType(paramValue),
                                        datasource.getDatabaseProductName()));
                    } else {
                        throw new ApplicationException(
                                "the struct type for the result set pointed by the ref cursor cannot be null");
                    }
                    break;
                }
                default:
                    throw new ApplicationException("unsupported data type " + sqlType + " specified as OUT/INOUT " +
                            "parameter at index " + index);
            }
        } catch (SQLException e) {
            throw new SQLException("error while getting OUT parameter value. " + e.getMessage(), e.getSQLState(),
                    e.getErrorCode());
        } catch (IOException e) {
            throw new ApplicationException("error while getting OUT parameter value", e.getMessage());
        }
    }

    private SQLDatasource retrieveDatasource(ObjectValue client) {
        return  (SQLDatasource) client.getNativeData(Constants.JDBC_CLIENT);
    }

    private BStructureType getStructType(MapValue<String, Object> parameter) {
        TypedescValue typedescValue = (TypedescValue) parameter.get(PARAMETER_RECORD_TYPE_FIELD);
        BStructureType structType = null;
        if (typedescValue != null) {
            structType = (BStructureType) typedescValue.getDescribingType();
        }
        return structType;
    }

    private CallableStatement getPreparedCall(Connection conn, SQLDatasource datasource, String query,
                                              ArrayValue parameters) throws SQLException {
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

    private boolean hasOutParams(ArrayValue params) {
        int paramCount = params.size();
        for (int index = 0; index < paramCount; index++) {
            MapValue<String, Object> paramValue = (MapValue<String, Object>) params.getRefValue(index);
            int direction = StatementProcessUtils.getParameterDirection(paramValue);
            if (direction == Constants.QueryParamDirection.OUT || direction == Constants.QueryParamDirection.INOUT) {
                return true;
            }
        }
        return false;
    }

    private boolean isRefCursorOutParamPresent(ArrayValue params) {
        boolean refCursorOutParamPresent = false;
        int paramCount = params.size();
        for (int index = 0; index < paramCount; index++) {
            MapValue<String, Object> paramValue = (MapValue<String, Object>) params.getRefValue(index);
            if (paramValue != null) {
                String sqlType = StatementProcessUtils.getSQLType(paramValue);
                int direction = StatementProcessUtils.getParameterDirection(paramValue);
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
            throw ErrorGenerator.getSQLDatabaseError(e, "error while cleaning sql resources: ");
        }
    }
}

