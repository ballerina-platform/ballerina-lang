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

import org.ballerinalang.database.sql.Constants;
import org.ballerinalang.database.sql.SQLDatasource;
import org.ballerinalang.database.sql.SQLDatasourceUtils;
import org.ballerinalang.database.sql.exceptions.ApplicationException;
import org.ballerinalang.database.sql.exceptions.DatabaseException;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Represents an Update SQL statement.
 *
 * @since 1.0.0
 */
public class UpdateStatement extends AbstractSQLStatement {
    private final ObjectValue client;
    private final SQLDatasource datasource;
    private final String query;
    private final ArrayValue keyColumns;
    private final ArrayValue parameters;

    public UpdateStatement(ObjectValue client, SQLDatasource datasource, String query,
                           ArrayValue keyColumns, ArrayValue parameters) {
        this.client = client;
        this.datasource = datasource;
        this.query = query;
        this.keyColumns = keyColumns;
        this.parameters = parameters;
    }

    @Override
    public Object execute() {
        //TODO: JBalMigration Commenting out transaction handling and observability
        //TODO: #16033
        //checkAndObserveSQLAction(context, datasource, query)
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isInTransaction = false;
        String errorMessagePrefix = "execute update failed: ";
        try {
            ArrayValue generatedParams = constructParameters(parameters);
            conn = getDatabaseConnection(client, datasource, false);
            String processedQuery = createProcessedQueryString(query, generatedParams);
            int keyColumnCount = 0;
            if (keyColumns != null) {
                keyColumnCount = keyColumns.size();
            }
            if (keyColumnCount > 0) {
                String[] columnArray = new String[keyColumnCount];
                for (int i = 0; i < keyColumnCount; i++) {
                    columnArray[i] = keyColumns.getString(i);
                }
                stmt = conn.prepareStatement(processedQuery, columnArray);
            } else {
                stmt = conn.prepareStatement(processedQuery, Statement.RETURN_GENERATED_KEYS);
            }
            createProcessedStatement(conn, stmt, generatedParams, datasource.getDatabaseProductName());
            int count = stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            /*The result set contains the auto generated keys. There can be multiple auto generated columns
            in a table.*/
            MapValue<String, Object> generatedKeys;
            if (rs.next()) {
                generatedKeys = getGeneratedKeys(rs);
            } else {
                generatedKeys = new MapValueImpl<>();
            }
            return createFrozenUpdateResultRecord(count, generatedKeys);
        } catch (SQLException e) {
            return SQLDatasourceUtils.getSQLDatabaseError(e, errorMessagePrefix);
            //handleErrorOnTransaction(context);
           // checkAndObserveSQLError(context, "execute update failed: " + e.getMessage());
        }  catch (DatabaseException e) {
            return SQLDatasourceUtils.getSQLDatabaseError(e, errorMessagePrefix);
            //handleErrorOnTransaction(context);
            // checkAndObserveSQLError(context, "execute update failed: " + e.getMessage());
        }  catch (ApplicationException e) {
            return SQLDatasourceUtils.getSQLApplicationError(e, errorMessagePrefix);
            //handleErrorOnTransaction(context);
           // checkAndObserveSQLError(context, "execute update failed: " + e.getMessage());
        } finally {
            cleanupResources(rs, stmt, conn, !isInTransaction);
        }
    }

    private MapValue<String, Object> getGeneratedKeys(ResultSet rs) throws SQLException {
        MapValue<String, Object> generatedKeys = new MapValueImpl<>(BTypes.typeAnydata);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        int columnType;
        Object value;
        String columnName;
        BigDecimal bigDecimal;
        for (int i = 1; i <= columnCount; i++) {
            columnType = metaData.getColumnType(i);
            columnName = metaData.getColumnLabel(i);
            switch (columnType) {
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.SMALLINT:
                    value = rs.getInt(i);
                    break;
                case Types.DOUBLE:
                    value = rs.getDouble(i);
                    break;
                case Types.FLOAT:
                    value = rs.getFloat(i);
                    break;
                case Types.BOOLEAN:
                case Types.BIT:
                    value = rs.getBoolean(i);
                    break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                    bigDecimal = rs.getBigDecimal(i);
                    value = bigDecimal;
                    break;
                case Types.BIGINT:
                    value = rs.getLong(i);
                    break;
                default:
                    value = rs.getString(i);
                    break;
            }
            generatedKeys.put(columnName, value);
        }
        return generatedKeys;
    }

    private MapValue<String, Object> createFrozenUpdateResultRecord(int count, MapValue<String, Object> generatedKeys) {
        MapValue<String, Object> updateResultRecord = BallerinaValues
                .createRecordValue(Constants.SQL_PACKAGE_PATH, Constants.SQL_UPDATE_RESULT);
        MapValue<String, Object> populatedUpdateResultRecord = BallerinaValues
                .createRecord(updateResultRecord, count, generatedKeys);
        populatedUpdateResultRecord.attemptFreeze(new Status(State.FROZEN));
        return populatedUpdateResultRecord;
    }
}
