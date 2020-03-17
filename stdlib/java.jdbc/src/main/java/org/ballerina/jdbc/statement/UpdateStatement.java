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
package org.ballerina.jdbc.statement;

import org.ballerina.jdbc.Constants;
import org.ballerina.jdbc.datasource.SQLDatasource;
import org.ballerina.jdbc.exceptions.ApplicationException;
import org.ballerina.jdbc.exceptions.ErrorGenerator;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

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
    private boolean returnGeneratedKeys;

    public UpdateStatement(ObjectValue client, SQLDatasource datasource, String query, boolean returnGeneratedKeys,
                           ArrayValue parameters, Strand strand) {
        super(strand);
        this.client = client;
        this.datasource = datasource;
        this.query = query;
        this.parameters = parameters;
        this.returnGeneratedKeys = returnGeneratedKeys;
    }

    @Override
    public Object execute() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        checkAndObserveSQLAction(strand, datasource, query);
        boolean isInTransaction = strand.isInTransaction();
        String errorMessagePrefix = "failed to execute update query: ";
        MapValue<String, Object> generatedKeys = new MapValueImpl<>();
        try {
            ArrayValue generatedParams = constructParameters(parameters);
            conn = getDatabaseConnection(strand, client, datasource);
            String processedQuery = createProcessedQueryString(query, generatedParams);

            if (returnGeneratedKeys) {
                stmt = conn.prepareStatement(processedQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            } else {
                stmt = conn.prepareStatement(processedQuery);
            }

            ProcessedStatement processedStatement = new ProcessedStatement(conn, stmt, generatedParams,
                    datasource.getDatabaseProductName());
            stmt = processedStatement.prepare();
            int count = stmt.executeUpdate();
            if (returnGeneratedKeys) {
                rs = stmt.getGeneratedKeys();
                //This result set contains the auto generated keys.
                if (rs.next()) {
                    generatedKeys = getGeneratedKeys(rs);
                }
            }
            return createFrozenUpdateResultRecord(count, generatedKeys);
        } catch (SQLException e) {
            handleErrorOnTransaction(this.strand);
            checkAndObserveSQLError(strand, "execute update failed: " + e.getMessage());
            return ErrorGenerator.getSQLDatabaseError(e, errorMessagePrefix);
        } catch (ApplicationException e) {
            handleErrorOnTransaction(this.strand);
            checkAndObserveSQLError(strand, "execute update failed: " + e.getMessage());
            return ErrorGenerator.getSQLApplicationError(e, errorMessagePrefix);
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
                .createRecordValue(Constants.JDBC_PACKAGE_ID, Constants.JDBC_UPDATE_RESULT);
        MapValue<String, Object> populatedUpdateResultRecord = BallerinaValues
                .createRecord(updateResultRecord, count, generatedKeys);
        populatedUpdateResultRecord.attemptFreeze(new Status(State.FROZEN));
        return populatedUpdateResultRecord;
    }

}
