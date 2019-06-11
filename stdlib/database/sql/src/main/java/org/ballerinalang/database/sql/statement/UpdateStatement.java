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
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.database.sql.Constants;
import org.ballerinalang.database.sql.SQLDatasource;
import org.ballerinalang.database.sql.SQLDatasourceUtils;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

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
    private final Context context;
    private final SQLDatasource datasource;
    private final String query;
    private final BValueArray keyColumns;
    private final BValueArray parameters;
    private final CallableUnitCallback callback;

    public UpdateStatement(Context context, SQLDatasource datasource, String query,
                           BValueArray keyColumns, BValueArray parameters, CallableUnitCallback callback) {
        this.context = context;
        this.datasource = datasource;
        this.query = query;
        this.keyColumns = keyColumns;
        this.parameters = parameters;
        this.callback = callback;
    }

    @Override
    public void execute() {
        checkAndObserveSQLAction(context, datasource, query);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isInTransaction = context.isInTransaction();
        try {
            BValueArray generatedParams = constructParameters(context, parameters);
            conn = SQLDatasourceUtils.getDatabaseConnection(context, datasource, false);
            String processedQuery = createProcessedQueryString(query, generatedParams);
            int keyColumnCount = 0;
            if (keyColumns != null) {
                keyColumnCount = (int) keyColumns.size();
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
            BMap generatedKeys;
            if (rs.next()) {
                generatedKeys = getGeneratedKeys(rs);
            } else {
                generatedKeys = new BMap();
            }
            context.setReturnValues(createResultRecord(context, count, generatedKeys));
            callback.notifySuccess();
        } catch (Throwable e) {
            context.setReturnValues(SQLDatasourceUtils.getSQLConnectorError(context, e, "execute update failed: "));
            callback.notifySuccess();
            SQLDatasourceUtils.handleErrorOnTransaction(context);
            checkAndObserveSQLError(context, "execute update failed: " + e.getMessage());
        } finally {
            SQLDatasourceUtils.cleanupResources(rs, stmt, conn, !isInTransaction);
        }
    }

    private BMap getGeneratedKeys(ResultSet rs) throws SQLException {
        BMap<String, BValue> generatedKeys = new BMap<>(BTypes.typeAnydata);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        int columnType;
        BValue value;
        String columnName;
        BigDecimal bigDecimal;
        for (int i = 1; i <= columnCount; i++) {
            columnType = metaData.getColumnType(i);
            columnName = metaData.getColumnLabel(i);
            switch (columnType) {
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.SMALLINT:
                    value = new BInteger(rs.getInt(i));
                    break;
                case Types.DOUBLE:
                    value = new BFloat(rs.getDouble(i));
                    break;
                case Types.FLOAT:
                    value = new BFloat(rs.getFloat(i));
                    break;
                case Types.BOOLEAN:
                case Types.BIT:
                    value = new BBoolean(rs.getBoolean(i));
                    break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                    bigDecimal = rs.getBigDecimal(i);
                    if (bigDecimal != null) {
                        value = new BDecimal(bigDecimal);
                    } else {
                        value = null;
                    }
                    break;
                case Types.BIGINT:
                    value = new BInteger(rs.getLong(i));
                    break;
                default:
                    value = new BString(rs.getString(i));
                    break;
            }
            generatedKeys.put(columnName, value);
        }
        return generatedKeys;
    }

    private static BMap<String, BValue> createResultRecord(Context context, int count, BMap keyValues) {
        PackageInfo sqlPackageInfo = context.getProgramFile().getPackageInfo(Constants.SQL_PACKAGE_PATH);
        StructureTypeInfo resultRecordInfo = sqlPackageInfo.getStructInfo(Constants.SQL_UPDATE_RESULT);
        BMap<String, BValue> resultRecord = BLangVMStructs.createBStruct(resultRecordInfo, count, keyValues);
        resultRecord.attemptFreeze(new BVM.FreezeStatus(BVM.FreezeStatus.State.FROZEN));
        return resultRecord;
    }
}
