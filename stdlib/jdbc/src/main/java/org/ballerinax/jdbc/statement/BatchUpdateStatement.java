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
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.SQLDatasource;
import org.ballerinax.jdbc.exceptions.ApplicationException;
import org.ballerinax.jdbc.exceptions.ErrorGenerator;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 * Represents a Batch Update SQL statement.
 *
 * @since 1.0.0
 */
public class BatchUpdateStatement extends AbstractSQLStatement {

    private final ObjectValue client;
    private final SQLDatasource datasource;
    private final String query;
    private final ArrayValue parameters;
    private final boolean rollbackAllInFailure;

    public BatchUpdateStatement(ObjectValue client, SQLDatasource datasource, String query,
                                ArrayValue parameters, boolean rollbackAllInFailure, Strand strand) {
        super(strand);
        this.client = client;
        this.datasource = datasource;
        this.query = query;
        this.parameters = parameters;
        this.rollbackAllInFailure = rollbackAllInFailure;
    }

    @Override
    public MapValue<String, Object> execute() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs;
        MapValue<String, ArrayValue> generatedKeys = new MapValueImpl<>();
        int[] updatedCount;
        int paramArrayCount = 0;
        checkAndObserveSQLAction(strand, datasource, query);
        if (parameters != null) {
            paramArrayCount = parameters.size();
        }

        boolean isInTransaction = strand.isInTransaction();
        String errorMessagePrefix = "failed to execute batch update";
        try {
            conn = getDatabaseConnection(strand, client, datasource);
            boolean generatedKeyReturningSupported = isGeneratedKeyReturningSupported();
            if (generatedKeyReturningSupported) {
                stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            } else {
                stmt = conn.prepareStatement(query);
            }
            conn.setAutoCommit(false);
            if (paramArrayCount == 0) {
                stmt.addBatch();
            }
            for (int index = 0; index < paramArrayCount; index++) {
                ArrayValue params = (ArrayValue) parameters.get(index);
                ArrayValue generatedParams = constructParameters(params);
                ProcessedStatement processedStatement = new ProcessedStatement(conn, stmt, generatedParams,
                        datasource.getDatabaseProductName());
                stmt = processedStatement.prepare();
                stmt.addBatch();
            }
            updatedCount = stmt.executeBatch();
            if (generatedKeyReturningSupported) {
                rs = stmt.getGeneratedKeys();
                //This result set contains the auto generated keys.
                generatedKeys = getGeneratedKeysFromBatch(rs);
            }
            if (!isInTransaction) {
                conn.commit();
            }
            return createFrozenBatchUpdateResultRecord(createUpdatedCountArray(updatedCount, paramArrayCount),
                    generatedKeys, null);
        } catch (BatchUpdateException e) {
            // Depending on the driver, at this point, driver may or may not have executed the remaining commands in
            // the batch which come after the command that failed.
            // We could have rolled back the connection to keep a consistent behavior in Ballerina regardless of
            // the driver. But, in Ballerina, we've decided to honor whatever the behavior of the driver and
            // decide it based on the user input of `rollbackAllInFailure` property, because a Ballerina developer
            // might have a requirement to ignore a few failed commands in the batch and let the rest of the commands
            // run if driver allows it.
            updatedCount = e.getUpdateCounts();
            checkAndObserveSQLError(strand, e.getMessage());
            if (!isInTransaction && rollbackAllInFailure) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    errorMessagePrefix += " and failed to rollback the intermediate changes";
                }
            }
            handleErrorOnTransaction(this.strand);
            return createFrozenBatchUpdateResultRecord(createUpdatedCountArray(updatedCount, paramArrayCount),
                    generatedKeys, ErrorGenerator.getSQLDatabaseError(e, errorMessagePrefix + ": "));
        } catch (SQLException e) {
            handleErrorOnTransaction(this.strand);
            checkAndObserveSQLError(strand, e.getMessage());
            return createFrozenBatchUpdateResultRecord(createUpdatedCountArray(null, paramArrayCount),
                    generatedKeys, ErrorGenerator.getSQLDatabaseError(e, errorMessagePrefix + ": "));
        } catch (ApplicationException e) {
            handleErrorOnTransaction(this.strand);
            checkAndObserveSQLError(strand, e.getMessage());
            return createFrozenBatchUpdateResultRecord(createUpdatedCountArray(null, paramArrayCount),
                    generatedKeys, ErrorGenerator.getSQLApplicationError(e, errorMessagePrefix + ": "));
        } finally {
            cleanupResources(stmt, conn, !isInTransaction);
        }
    }

    // It has been identified that Oracle and MS SQL Server does not support returning generated keys along with
    // batch update. And such effort would result in an exception causing batch update failure.
    // If such other databases are identified they can be included here.
    // The name of the database is being checked because there is no way to identify through the API.
    private boolean isGeneratedKeyReturningSupported() {
        return !Constants.DatabaseNames.ORACLE.equals(datasource.getDatabaseProductName())
                && !Constants.DatabaseNames.MSSQL_SERVER.equals(datasource.getDatabaseProductName());
    }

    private ArrayValue createUpdatedCountArray(int[] updatedCounts, int paramArrayCount) {
        // After a command in a batch update fails to execute properly and a BatchUpdateException is thrown, the
        // driver may or may not continue to process the remaining commands in the batch. If the driver does not
        // continue processing after a failure, the array returned by the method will have -3 (EXECUTE_FAILED) for
        // those updates.
        long[] returnedCount = new long[paramArrayCount];
        Arrays.fill(returnedCount, Statement.EXECUTE_FAILED);
        ArrayValue countArray = new ArrayValueImpl(returnedCount);
        if (updatedCounts != null) {
            int iSize = updatedCounts.length;
            for (int i = 0; i < iSize; ++i) {
                countArray.add(i, updatedCounts[i]);
            }
        }
        return countArray;
    }

    private MapValue<String, Object> createFrozenBatchUpdateResultRecord(ArrayValue countArray,
            MapValue<String, ArrayValue> generatedKeys, ErrorValue retError) {
        MapValue<String, Object> batchUpdateResultRecord = BallerinaValues
                .createRecordValue(Constants.JDBC_PACKAGE_ID, Constants.JDBC_BATCH_UPDATE_RESULT);
        MapValue<String, Object> populatedUpdateResultRecord = BallerinaValues
                .createRecord(batchUpdateResultRecord, countArray, generatedKeys, retError);
        populatedUpdateResultRecord.attemptFreeze(new Status(State.FROZEN));
        return populatedUpdateResultRecord;
    }

    private MapValue<String, ArrayValue> getGeneratedKeysFromBatch(ResultSet rs) throws SQLException {
        MapValue<String, ArrayValue> generatedKeys = new MapValueImpl<>(new BArrayType(BTypes.typeAnydata));
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Object value;
        String columnName;
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                columnName = metaData.getColumnLabel(i);
                value = extractValueFromResultSet(metaData, rs, i);
                addToMap(generatedKeys, columnName, value);
            }
        }
        return generatedKeys;
    }

    private void addToMap(MapValue<String, ArrayValue> map, String columnName, Object value) {
        ArrayValue list = map.get(columnName);
        if (list == null) {
            list = new ArrayValueImpl(new BArrayType(BTypes.typeAnydata));
            map.put(columnName, list);
        }
        list.append(value);
    }
}
