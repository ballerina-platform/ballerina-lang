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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.SQLDatasource;
import org.ballerinax.jdbc.exceptions.ApplicationException;
import org.ballerinax.jdbc.exceptions.ErrorGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Represents an Update SQL statement.
 *
 * @since 1.0.0
 */
public class UpdateStatement extends AbstractSQLStatement {

    private final ObjectValue client;
    private final SQLDatasource datasource;
    private final String query;
    private final ArrayValue parameters;

    public UpdateStatement(ObjectValue client, SQLDatasource datasource, String query, ArrayValue parameters,
                           Strand strand) {
        super(strand);
        this.client = client;
        this.datasource = datasource;
        this.query = query;
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
        boolean isInTransaction = strand.isInTransaction();
        String errorMessagePrefix = "Failed to execute update query: ";
        try {
            ArrayValue generatedParams = constructParameters(parameters);
            conn = getDatabaseConnection(strand, client, datasource, false);
            String processedQuery = createProcessedQueryString(query, generatedParams);
            stmt = conn.prepareStatement(processedQuery, Statement.RETURN_GENERATED_KEYS);
            ProcessedStatement processedStatement = new ProcessedStatement(conn, stmt, generatedParams,
                    datasource.getDatabaseProductName());
            stmt = processedStatement.prepare();
            int count = stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            //This result set contains the auto generated keys.
            MapValue<String, Object> generatedKeys;
            if (rs.next()) {
                generatedKeys = getGeneratedKeys(rs);
            } else {
                generatedKeys = new MapValueImpl<>();
            }
            return createFrozenUpdateResultRecord(count, generatedKeys);
        } catch (SQLException e) {
            handleErrorOnTransaction(this.strand);
            return ErrorGenerator.getSQLDatabaseError(e, errorMessagePrefix);
           // checkAndObserveSQLError(context, "execute update failed: " + e.getMessage());
        } catch (ApplicationException e) {
            handleErrorOnTransaction(this.strand);
            return ErrorGenerator.getSQLApplicationError(e, errorMessagePrefix);
           // checkAndObserveSQLError(context, "execute update failed: " + e.getMessage());
        } finally {
            cleanupResources(rs, stmt, conn, !isInTransaction);
        }
    }

    private MapValue<String, Object> getGeneratedKeys(ResultSet rs) throws SQLException {
        MapValue<String, Object> generatedKeys = new MapValueImpl<>(new BMapType(BTypes.typeAnydata));
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Object value;
        String columnName;
        for (int i = 1; i <= columnCount; i++) {
            columnName = metaData.getColumnLabel(i);
            value = extractValueFromResultSet(metaData, rs, i);
            generatedKeys.put(columnName, value);
        }
        return generatedKeys;
    }

    private MapValue<String, Object> createFrozenUpdateResultRecord(int count, MapValue<String, Object> generatedKeys) {
        MapValue<String, Object> updateResultRecord = BallerinaValues
                .createRecordValue(Constants.JDBC_PACKAGE_PATH, Constants.JDBC_UPDATE_RESULT);
        MapValue<String, Object> populatedUpdateResultRecord = BallerinaValues
                .createRecord(updateResultRecord, count, generatedKeys);
        populatedUpdateResultRecord.attemptFreeze(new Status(State.FROZEN));
        return populatedUpdateResultRecord;
    }
}
