/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.connectors.data.sql.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.connectors.data.sql.Constants;
import org.ballerinalang.nativeimpl.connectors.data.sql.SQLConnector;
import org.ballerinalang.nativeimpl.connectors.data.sql.SQLDataIterator;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * {@code AbstractSQLAction} is the base class for all SQL Connector Action.
 *
 * @since 0.8.0
 */
public abstract class AbstractSQLAction extends AbstractNativeAction {


    protected void executeQuery(Context context, SQLConnector connector, String query, BArray parameters) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connector.getSQLConnection();
            stmt = getPreparedStatement(conn, connector, query);
            createProcessedStatement(conn, stmt, parameters);
            rs = stmt.executeQuery();
            BDataTable dataTable = new BDataTable(new SQLDataIterator(conn, stmt, rs), new HashMap<>(),
                    getColumnDefinitions(rs));
            context.getControlStack().setReturnValue(0, dataTable);
        } catch (SQLException e) {
            SQLConnectorUtils.cleanupConnection(rs, stmt, conn);
            throw new BallerinaException("execute query failed: " + e.getMessage(), e);
        }
    }

    protected void executeUpdate(Context context, SQLConnector connector, String query, BArray parameters) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connector.getSQLConnection();
            stmt = conn.prepareStatement(query);
            createProcessedStatement(conn, stmt, parameters);
            int count = stmt.executeUpdate();
            BInteger updatedCount = new BInteger(count);
            context.getControlStack().setReturnValue(0, updatedCount);
        } catch (SQLException e) {
            throw new BallerinaException("execute update failed: " + e.getMessage(), e);
        } finally {
            SQLConnectorUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    protected void executeUpdateWithKeys(Context context, SQLConnector connector, String query,
            BArray<BString> keyColumns, BArray parameters) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connector.getSQLConnection();
            int keyColumnCount = 0;
            if (keyColumns != null) {
                keyColumnCount = keyColumns.size();
            }
            if (keyColumnCount > 0) {
                String[] columnArray = new String[keyColumnCount];
                for (int i = 0; i < keyColumnCount; i++) {
                    columnArray[i] = keyColumns.get(i).stringValue();
                }
                stmt = conn.prepareStatement(query, columnArray);
            } else {
                stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            }
            createProcessedStatement(conn, stmt, parameters);
            int count = stmt.executeUpdate();
            BInteger updatedCount = new BInteger(count);
            context.getControlStack().setReturnValue(0, updatedCount);
            rs = stmt.getGeneratedKeys();
            /*The result set contains the auto generated keys. It can have multiple rows if multiple rows have
            updated with the execute operation. There can be multiple auto generated columns in a table.
            TODO: iterate the result set and generate a array of key arrays*/
            if (rs.next()) {
                BArray<BString> generatedKeys = getGeneratedKeys(rs);
                context.getControlStack().setReturnValue(1, generatedKeys);
            }
        } catch (SQLException e) {
            throw new BallerinaException("execute update with generated keys failed: " + e.getMessage(), e);
        } finally {
            SQLConnectorUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    protected void executeProcedure(Context context, SQLConnector connector, String query, BArray parameters) {
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connector.getSQLConnection();
            stmt = getPreparedCall(conn, connector, query, parameters);
            createProcessedStatement(conn, stmt, parameters);
            rs = executeStoredProc(stmt);
            setOutParameters(stmt, parameters);
            if (rs != null) {
                BDataTable datatable = new BDataTable(new SQLDataIterator(conn, stmt, rs), new HashMap<>(),
                        getColumnDefinitions(rs));
                context.getControlStack().setReturnValue(0, datatable);
            } else {
                SQLConnectorUtils.cleanupConnection(null, stmt, conn);
            }
        } catch (SQLException e) {
            SQLConnectorUtils.cleanupConnection(rs, stmt, conn);
            throw new BallerinaException("execute stored procedure failed: " + e.getMessage(), e);
        }
    }

    protected void executeBatchUpdate(Context context, SQLConnector connector, String query, BArray parameters) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connector.getSQLConnection();
            stmt = conn.prepareStatement(query);
            setConnectionAutoCommit(conn, false);
            int paramArrayCount = parameters.size();
            for (int index = 0; index < paramArrayCount; index++) {
                BArray params = (BArray) parameters.get(index);
                createProcessedStatement(conn, stmt, params);
                stmt.addBatch();
            }
            int[] updatedCount = stmt.executeBatch();
            conn.commit();
            BArray<BInteger> arrayValue = new BArray<>(BInteger.class);
            int iSize = updatedCount.length;
            for (int i = 0; i < iSize; ++i) {
                arrayValue.add(i, new BInteger(updatedCount[i]));
            }
            context.getControlStack().setReturnValue(0, arrayValue);
        } catch (SQLException e) {
            throw new BallerinaException("execute update failed: " + e.getMessage(), e);
        } finally {
            setConnectionAutoCommit(conn, true);
            SQLConnectorUtils.cleanupConnection(null, stmt, conn);
        }
    }

    private void setConnectionAutoCommit(Connection conn, boolean status) {
        try {
            if (conn != null) {
                conn.setAutoCommit(status);
            }
        } catch (SQLException e) {
            throw new BallerinaException("set connection commit status failed: " + e.getMessage(), e);
        }
    }

    protected void closeConnections(SQLConnector connector) {
        connector.closeConnectionPool();
    }

    private PreparedStatement getPreparedStatement(Connection conn, SQLConnector connector, String query)
            throws SQLException {
        PreparedStatement stmt;
        boolean mysql = connector.getDatabaseName().contains("mysql");
        /* In MySQL by default, ResultSets are completely retrieved and stored in memory.
           Following properties are set to stream the results back one row at a time.*/
        if (mysql) {
            stmt = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // To fulfill OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE findbugs validation.
            try {
                stmt.setFetchSize(Integer.MIN_VALUE);
            } catch (SQLException e) {
                stmt.close();
            }
        } else {
            stmt = conn.prepareStatement(query);
        }
        return stmt;
    }

    private CallableStatement getPreparedCall(Connection conn, SQLConnector connector, String query, BArray parameters)
            throws SQLException {
        CallableStatement stmt;
        boolean mysql = connector.getDatabaseName().contains("mysql");
        if (mysql) {
            stmt = conn.prepareCall(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            /* Only stream if there aren't any OUT parameters since can't use streaming result sets with callable
               statements that have output parameters */
            if (!hasOutParams(parameters)) {
                stmt.setFetchSize(Integer.MIN_VALUE);
            }
        } else {
            stmt = conn.prepareCall(query);
        }
        return stmt;
    }

    private ArrayList<BDataTable.ColumnDefinition> getColumnDefinitions(ResultSet rs) throws SQLException {
        ArrayList<BDataTable.ColumnDefinition> columnDefs = new ArrayList<>();
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int cols = rsMetaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String colName = rsMetaData.getColumnName(i);
            int colType = rsMetaData.getColumnType(i);
            TypeEnum mappedType = SQLConnectorUtils.getColumnType(colType);
            columnDefs.add(new BDataTable.ColumnDefinition(colName, mappedType));
        }
        return columnDefs;
    }

    private BArray<BString> getGeneratedKeys(ResultSet rs) throws SQLException {
        BArray<BString> generatedKeys = new BArray<>(BString.class);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        int columnType;
        String value;
        BigDecimal bigDecimal;
        for (int i = 1; i <= columnCount; i++) {
            columnType = metaData.getColumnType(i);
            switch (columnType) {
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT:
                value = Integer.toString(rs.getInt(i));
                break;
            case Types.DOUBLE:
                value = Double.toString(rs.getDouble(i));
                break;
            case Types.FLOAT:
                value = Float.toString(rs.getFloat(i));
                break;
            case Types.BOOLEAN:
            case Types.BIT:
                value = Boolean.toString(rs.getBoolean(i));
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                bigDecimal = rs.getBigDecimal(i);
                if (bigDecimal != null) {
                    value = bigDecimal.toPlainString();
                } else {
                    value = null;
                }
                break;
            case Types.BIGINT:
                value = Long.toString(rs.getLong(i));
                break;
            default:
                value = rs.getString(i);
                break;
            }
            generatedKeys.add(i - 1, new BString(value));
        }
        return generatedKeys;
    }

    private void createProcessedStatement(Connection conn, PreparedStatement stmt, BArray params) {
        int paramCount = params.size();
        for (int index = 0; index < paramCount; index++) {
            BStruct paramValue = (BStruct) params.get(index);
            String sqlType = paramValue.getValue(0).stringValue();
            BValue value = paramValue.getValue(1);
            int direction = Integer.parseInt(paramValue.getValue(2).stringValue());
            String structuredSQLType = paramValue.getValue(3).stringValue();
            setParameter(conn, stmt, sqlType, value, direction, index, structuredSQLType);
        }
    }

    private void setParameter(Connection conn, PreparedStatement stmt, String sqlType, BValue value, int direction,
            int index, String structuredSQLType) {
        if (sqlType == null || sqlType.isEmpty()) {
            SQLConnectorUtils.setStringValue(stmt, value, index, direction, Types.VARCHAR);
        } else {
            String sqlDataType = sqlType.toUpperCase(Locale.getDefault());
            switch (sqlDataType) {
            case Constants.SQLDataTypes.INTEGER:
                SQLConnectorUtils.setIntValue(stmt, value, index, direction, Types.INTEGER);
                break;
            case Constants.SQLDataTypes.VARCHAR:
                SQLConnectorUtils.setStringValue(stmt, value, index, direction, Types.VARCHAR);
                break;
            case Constants.SQLDataTypes.DOUBLE:
                SQLConnectorUtils.setDoubleValue(stmt, value, index, direction, Types.DOUBLE);
                break;
            case Constants.SQLDataTypes.NUMERIC:
            case Constants.SQLDataTypes.DECIMAL:
                SQLConnectorUtils.setNumericValue(stmt, value, index, direction, Types.NUMERIC);
                break;
            case Constants.SQLDataTypes.BIT:
            case Constants.SQLDataTypes.BOOLEAN:
                SQLConnectorUtils.setBooleanValue(stmt, value, index, direction, Types.BIT);
                break;
            case Constants.SQLDataTypes.TINYINT:
                SQLConnectorUtils.setTinyIntValue(stmt, value, index, direction, Types.TINYINT);
                break;
            case Constants.SQLDataTypes.SMALLINT:
                SQLConnectorUtils.setSmallIntValue(stmt, value, index, direction, Types.SMALLINT);
                break;
            case Constants.SQLDataTypes.BIGINT:
                SQLConnectorUtils.setBigIntValue(stmt, value, index, direction, Types.BIGINT);
                break;
            case Constants.SQLDataTypes.REAL:
            case Constants.SQLDataTypes.FLOAT:
                SQLConnectorUtils.setRealValue(stmt, value, index, direction, Types.FLOAT);
                break;
            case Constants.SQLDataTypes.DATE:
                SQLConnectorUtils.setDateValue(stmt, value, index, direction, Types.DATE);
                break;
            case Constants.SQLDataTypes.TIMESTAMP:
                SQLConnectorUtils.setTimeStampValue(stmt, value, index, direction, Types.TIMESTAMP);
                break;
            case Constants.SQLDataTypes.TIME:
                SQLConnectorUtils.setTimeValue(stmt, value, index, direction, Types.TIME);
                break;
            case Constants.SQLDataTypes.BINARY:
                SQLConnectorUtils.setBinaryValue(stmt, value, index, direction, Types.BINARY);
                break;
            case Constants.SQLDataTypes.BLOB:
                SQLConnectorUtils.setBlobValue(stmt, value, index, direction, Types.BLOB);
                break;
            case Constants.SQLDataTypes.CLOB:
                SQLConnectorUtils.setClobValue(stmt, value, index, direction, Types.CLOB);
                break;
            case Constants.SQLDataTypes.ARRAY:
                SQLConnectorUtils.setArrayValue(conn, stmt, value, index, direction, Types.ARRAY, structuredSQLType);
                break;
            case Constants.SQLDataTypes.STRUCT:
                SQLConnectorUtils
                        .setUserDefinedValue(conn, stmt, value, index, direction, Types.STRUCT, structuredSQLType);
                break;
            default:
                throw new BallerinaException("unsupported datatype as parameter: " + sqlType + " index:" + index);
            }
        }
    }

    private void setOutParameters(CallableStatement stmt, BArray params) {
        int paramCount = params.size();
        for (int index = 0; index < paramCount; index++) {
            BStruct paramValue = (BStruct) params.get(index);
            String sqlType = paramValue.getValue(0).stringValue();
            int direction = Integer.parseInt(paramValue.getValue(2).stringValue());
            if (direction == Constants.QueryParamDirection.INOUT || direction == Constants.QueryParamDirection.OUT) {
                setOutParameterValue(stmt, sqlType, index, paramValue);
            }
        }
    }

    private void setOutParameterValue(CallableStatement stmt, String sqlType, int index, BStruct paramValue) {
        try {
            String sqlDataType = sqlType.toUpperCase(Locale.getDefault());
            switch (sqlDataType) {
            case Constants.SQLDataTypes.INTEGER: {
                int value = stmt.getInt(index + 1);
                paramValue.setValue(1, new BInteger(value)); //Value is the first position of the struct
            }
            break;
            case Constants.SQLDataTypes.VARCHAR: {
                String value = stmt.getString(index + 1);
                paramValue.setValue(1, new BString(value));
            }
            break;
            case Constants.SQLDataTypes.NUMERIC:
            case Constants.SQLDataTypes.DECIMAL: {
                BigDecimal value = stmt.getBigDecimal(index + 1);
                if (value == null) {
                    paramValue.setValue(1, new BFloat(0));
                } else {
                    paramValue.setValue(1, new BFloat(value.doubleValue()));
                }
            }
            break;
            case Constants.SQLDataTypes.BIT:
            case Constants.SQLDataTypes.BOOLEAN: {
                boolean value = stmt.getBoolean(index + 1);
                paramValue.setValue(1, new BBoolean(value));
            }
            break;
            case Constants.SQLDataTypes.TINYINT: {
                byte value = stmt.getByte(index + 1);
                paramValue.setValue(1, new BInteger(value));
            }
            break;
            case Constants.SQLDataTypes.SMALLINT: {
                short value = stmt.getShort(index + 1);
                paramValue.setValue(1, new BInteger(value));
            }
            break;
            case Constants.SQLDataTypes.BIGINT: {
                long value = stmt.getLong(index + 1);
                paramValue.setValue(1, new BInteger(value));
            }
            break;
            case Constants.SQLDataTypes.REAL:
            case Constants.SQLDataTypes.FLOAT: {
                float value = stmt.getFloat(index + 1);
                paramValue.setValue(1, new BFloat(value));
            }
            break;
            case Constants.SQLDataTypes.DOUBLE: {
                double value = stmt.getDouble(index + 1);
                paramValue.setValue(1, new BFloat(value));
            }
            break;
            case Constants.SQLDataTypes.CLOB: {
                Clob value = stmt.getClob(index + 1);
                paramValue.setValue(1, new BString(SQLConnectorUtils.getString(value)));
            }
            break;
            case Constants.SQLDataTypes.BLOB: {
                Blob value = stmt.getBlob(index + 1);
                paramValue.setValue(1, new BString(SQLConnectorUtils.getString(value)));
            }
            break;
            case Constants.SQLDataTypes.BINARY: {
                byte[] value = stmt.getBytes(index + 1);
                paramValue.setValue(1, new BString(SQLConnectorUtils.getString(value)));
            }
            break;
            case Constants.SQLDataTypes.DATE: {
                Date value = stmt.getDate(index + 1);
                paramValue.setValue(1, new BString(SQLConnectorUtils.getString(value)));
            }
            break;
            case Constants.SQLDataTypes.TIMESTAMP: {
                Timestamp value = stmt.getTimestamp(index + 1);
                paramValue.setValue(1, new BString(SQLConnectorUtils.getString(value)));
            }
            break;
            case Constants.SQLDataTypes.TIME: {
                Time value = stmt.getTime(index + 1);
                paramValue.setValue(1, new BString(SQLConnectorUtils.getString(value)));
            }
            break;
            case Constants.SQLDataTypes.ARRAY: {
                Array value = stmt.getArray(index + 1);
                paramValue.setValue(1, new BString(SQLConnectorUtils.getString(value)));
            }
            break;
            case Constants.SQLDataTypes.STRUCT: {
                Object value = stmt.getObject(index + 1);
                String stringValue = "";
                if (value != null) {
                    if (value instanceof Struct) {
                        stringValue = SQLConnectorUtils.getString((Struct) value);
                    } else {
                        stringValue = value.toString();
                    }
                }
                paramValue.setValue(1, new BString(stringValue));
            }
            break;
            default:
                throw new BallerinaException(
                        "unsupported datatype as out/inout parameter: " + sqlType + " index:" + index);
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in getting out parameter value: " + e.getMessage(), e);
        }
    }

    private boolean hasOutParams(BArray params) {
        int paramCount = params.size();
        for (int index = 0; index < paramCount; index++) {
            BStruct paramValue = (BStruct) params.get(index);
            int direction = Integer.parseInt(paramValue.getValue(2).stringValue());
            if (direction == Constants.QueryParamDirection.OUT || direction == Constants.QueryParamDirection.INOUT) {
                return true;
            }
        }
        return false;
    }

    private ResultSet executeStoredProc(CallableStatement stmt) throws SQLException {
        boolean resultAndNoUpdateCount = stmt.execute();
        ResultSet result = null;
        while (true) {
            if (!resultAndNoUpdateCount) {
                int updateCount = stmt.getUpdateCount();
                if (updateCount == -1) {
                    break;
                }
            } else {
                result = stmt.getResultSet();
                break;
            }
            try {
                resultAndNoUpdateCount = stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT);
            } catch (SQLException e) {
                break;
            }
        }
        return result;
    }
}
