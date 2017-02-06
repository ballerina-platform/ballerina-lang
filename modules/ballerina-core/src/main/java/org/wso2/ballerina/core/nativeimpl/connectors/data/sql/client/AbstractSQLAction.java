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

package org.wso2.ballerina.core.nativeimpl.connectors.data.sql.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BDataTable;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.data.sql.SQLConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.data.sql.SQLDataIterator;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * {@code AbstractSQLAction} is the base class for all SQL Connector Action.
 */
public abstract class AbstractSQLAction extends AbstractNativeAction {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSQLAction.class);

    protected void executeQuery(Context context, SQLConnector connector, String query) {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connector.getSQLConnection();
            if (conn == null) {
                logger.error("Error in Connecting to the DB");
                return;
            }

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            BDataTable dataframe = new BDataTable(new SQLDataIterator(conn, stmt, rs), new HashMap<>(),
                    getColumnDefinitions(rs));
            context.getControlStack().setReturnValue(0, dataframe);
        } catch (SQLException e) {
            closeResources(rs, stmt, conn);
            throw new BallerinaException("Error in executing Query." + e.getMessage(), e);
        }
    }

    protected void executeUpdate(Context context, SQLConnector connector, String query) {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connector.getSQLConnection();
            if (conn == null) {
                logger.error("Error in Connecting to the DB");
                return;
            }

            stmt = conn.prepareStatement(query);

            int count = stmt.executeUpdate();
            BInteger updatedCount = new BInteger(count);
            context.getControlStack().setReturnValue(0, updatedCount);
        } catch (SQLException e) {
            throw new BallerinaException("Error in executing Update." + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    protected void executeUpdateWithKeys(Context context, SQLConnector connector, String query,
            BArray<BString> keyColumns) {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connector.getSQLConnection();
            if (conn == null) {
                logger.error("Error in Connecting to the DB");
                return;
            }

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

            int count = stmt.executeUpdate();
            BInteger updatedCount = new BInteger(count);
            context.getControlStack().setReturnValue(0, updatedCount);

            rs = stmt.getGeneratedKeys();

            while (rs.next()) {
                BArray<BString> generatedKeys = getGeneratedKeys(rs);
                context.getControlStack().setReturnValue(1, generatedKeys.get(0)); //TODO:Set Array of Keys
            }
            closeResources(rs, stmt, conn);
        } catch (SQLException e) {
            closeResources(rs, stmt, conn);
            throw new BallerinaException("Error in executing Update." + e.getMessage());
        }
    }

    protected void executeProcedure(Context context, SQLConnector connector, String query) {

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connector.getSQLConnection();
            if (conn == null) {
                logger.error("Error in Connecting to the DB");
                return;
            }

            stmt = conn.prepareCall(query);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                rs = stmt.getResultSet(); //TODO:How to return next result sets
                BDataTable dataframe = new BDataTable(new SQLDataIterator(conn, stmt, rs), new HashMap<>(),
                        getColumnDefinitions(rs));
                context.getControlStack().setReturnValue(0, dataframe);
            } else {
                closeResources(null, stmt, conn);
            }
        } catch (SQLException e) {
            closeResources(rs, stmt, conn);
            throw new BallerinaException("Error in executing Query." + e.getMessage(), e);
        }
    }

    private ArrayList<BDataTable.ColumnDefinition> getColumnDefinitions(ResultSet rs) throws SQLException {
        ArrayList<BDataTable.ColumnDefinition> columnDefs = new ArrayList<BDataTable.ColumnDefinition>();
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int cols = rsMetaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String colName = rsMetaData.getColumnName(i);
            int colType = rsMetaData.getColumnType(i);
            TypeEnum mappedType = getColumnType(colType);
            columnDefs.add(new BDataTable.ColumnDefinition(colName, mappedType));
        }
        return columnDefs;
    }

    private TypeEnum getColumnType(int sqlType) {
        switch (sqlType) {
        case Types.ARRAY:
            return TypeEnum.ARRAY;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
        case Types.CLOB:
        case Types.NCLOB:
            return TypeEnum.STRING;
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return TypeEnum.INT;
        case Types.BIGINT:
            return TypeEnum.LONG;
        case Types.REAL:
            return TypeEnum.FLOAT;
        case Types.BIT:
        case Types.BOOLEAN:
            return TypeEnum.BOOLEAN;
        case Types.NUMERIC:
        case Types.DECIMAL:
        case Types.FLOAT:
        case Types.DOUBLE:
            return TypeEnum.DOUBLE;
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
        case Types.TIME_WITH_TIMEZONE:
        case Types.TIMESTAMP_WITH_TIMEZONE:
        case Types.BLOB:
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return TypeEnum.EMPTY;
        default:
            return TypeEnum.EMPTY;
        }
    }

    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            /* Do nothing here. */
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException ex) {
            /* Do nothing here. */
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            /* Do nothing here. */
        }
    }

    private BArray<BString> getGeneratedKeys(ResultSet rs) throws SQLException {
        BArray<BString> generatredKeys = new BArray<>(BString.class);
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
            generatredKeys.add(i - 1, new BString(value));
        }
        return generatredKeys;
    }
}
