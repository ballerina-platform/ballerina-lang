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
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.SQLDatasource;
import org.ballerinax.jdbc.SQLDatasourceUtils;
import org.ballerinax.jdbc.exceptions.ApplicationException;
import org.ballerinax.jdbc.exceptions.DatabaseException;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
        //TODO: JBalMigration Commenting out transaction handling and observability
        //TODO: #16033
        // checkAndObserveSQLAction(context, datasource, query);
        Connection conn = null;
        PreparedStatement stmt = null;
        int[] updatedCount;
        int paramArrayCount = 0;
        if (parameters != null) {
            paramArrayCount = parameters.size();
        }

        boolean isInTransaction = strand.isInTransaction();
        String errorMessagePrefix = "execute batch update failed";
        try {
            conn = getDatabaseConnection(strand, client, datasource, false);
            stmt = conn.prepareStatement(query);
            conn.setAutoCommit(false);
            if (paramArrayCount == 0) {
                stmt.addBatch();
            }
            for (int index = 0; index < paramArrayCount; index++) {
                ArrayValue params = (ArrayValue) parameters.getValue(index);
                ArrayValue generatedParams = constructParameters(params);
                createProcessedStatement(conn, stmt, generatedParams, datasource.getDatabaseProductName());
                stmt.addBatch();
            }
            updatedCount = stmt.executeBatch();
            if (!isInTransaction) {
                conn.commit();
            }
            return createFrozenBatchUpdateResultRecord(createUpdatedCountArray(updatedCount, paramArrayCount), null);
        } catch (BatchUpdateException e) {
            // Depending on the driver, at this point, driver may or may not have executed the remaining commands in
            // the batch which come after the command that failed.
            // We could have rolled back the connection to keep a consistent behavior in Ballerina regardless of
            // the driver. But, in Ballerina, we've decided to honor whatever the behavior of the driver and
            // decide it based on the user input of `rollbackAllInFailure` property, because a Ballerina developer
            // might have a requirement to ignore a few failed commands in the batch and let the rest of the commands
            // run if driver allows it.
            updatedCount = e.getUpdateCounts();
            if (conn != null) {
                if (!isInTransaction && rollbackAllInFailure) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        errorMessagePrefix += ", failed to rollback any changes happened in-between";
                    }
                }
            }
            handleErrorOnTransaction(this.strand);
            return createFrozenBatchUpdateResultRecord(createUpdatedCountArray(updatedCount, paramArrayCount),
                    SQLDatasourceUtils.getSQLDatabaseError(e, errorMessagePrefix + ": "));
        } catch (SQLException e) {
             handleErrorOnTransaction(this.strand);
             //checkAndObserveSQLError(context, e.getMessage());
            return createFrozenBatchUpdateResultRecord(createUpdatedCountArray(null, paramArrayCount),
                    SQLDatasourceUtils.getSQLDatabaseError(e, errorMessagePrefix + ": "));
        } catch (DatabaseException e) {
            handleErrorOnTransaction(this.strand);
            // checkAndObserveSQLError(context, e.getMessage());
            return createFrozenBatchUpdateResultRecord(createUpdatedCountArray(null, paramArrayCount),
                    SQLDatasourceUtils.getSQLDatabaseError(e, errorMessagePrefix + ": "));
        } catch (ApplicationException e) {
            handleErrorOnTransaction(this.strand);
            // checkAndObserveSQLError(context, e.getMessage());
            return createFrozenBatchUpdateResultRecord(createUpdatedCountArray(null, paramArrayCount),
                    SQLDatasourceUtils.getSQLApplicationError(e, errorMessagePrefix + ": "));
        } finally {
            cleanupResources(stmt, conn, !isInTransaction);
        }
    }

    private ArrayValue createUpdatedCountArray(int[] updatedCounts, int paramArrayCount) {
        // After a command in a batch update fails to execute properly and a BatchUpdateException is thrown, the
        // driver may or may not continue to process the remaining commands in the batch. If the driver does not
        // continue processing after a failure, the array returned by the method will have -3 (EXECUTE_FAILED) for
        // those updates.
        long[] returnedCount = new long[paramArrayCount];
        Arrays.fill(returnedCount, Statement.EXECUTE_FAILED);
        ArrayValue countArray = new ArrayValue(returnedCount);
        if (updatedCounts != null) {
            int iSize = updatedCounts.length;
            for (int i = 0; i < iSize; ++i) {
                countArray.add(i, updatedCounts[i]);
            }
        }
        return countArray;
    }

    private MapValue<String, Object> createFrozenBatchUpdateResultRecord(ArrayValue countArray, ErrorValue retError) {
        MapValue<String, Object> batchUpdateResultRecord = BallerinaValues
                .createRecordValue(Constants.JDBC_PACKAGE_PATH, Constants.JDBC_BATCH_UPDATE_RESULT);
        MapValue<String, Object> populatedUpdateResultRecord = BallerinaValues
                .createRecord(batchUpdateResultRecord, countArray, retError);
        populatedUpdateResultRecord.attemptFreeze(new Status(State.FROZEN));
        return populatedUpdateResultRecord;
    }
}
