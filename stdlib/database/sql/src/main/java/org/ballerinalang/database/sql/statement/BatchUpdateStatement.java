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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.database.sql.SQLDatasource;
import org.ballerinalang.database.sql.SQLDatasourceUtils;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.exceptions.BallerinaException;

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

    private final Context context;
    private final SQLDatasource datasource;
    private final String query;
    private final BValueArray parameters;
    private final CallableUnitCallback callback;

    public BatchUpdateStatement(Context context, SQLDatasource datasource, String query,
                                BValueArray parameters, CallableUnitCallback callback) {
        this.context = context;
        this.datasource = datasource;
        this.query = query;
        this.parameters = parameters;
        this.callback = callback;
    }

    @Override
    public void execute() {
        checkAndObserveSQLAction(context, datasource, query);
        Connection conn = null;
        PreparedStatement stmt = null;
        int[] updatedCount;
        int paramArrayCount = 0;
        boolean isInTransaction = context.isInTransaction();
        try {
            conn = SQLDatasourceUtils.getDatabaseConnection(context, datasource, false);
            stmt = conn.prepareStatement(query);
            conn.setAutoCommit(false);
            if (parameters != null) {
                paramArrayCount = (int) parameters.size();
                if (paramArrayCount == 0) {
                    stmt.addBatch();
                }
                for (int index = 0; index < paramArrayCount; index++) {
                    BValueArray params = (BValueArray) parameters.getRefValue(index);
                    BValueArray generatedParams = constructParameters(context, params);
                    createProcessedStatement(conn, stmt, generatedParams, datasource.getDatabaseProductName());
                    stmt.addBatch();
                }
            } else {
                stmt.addBatch();
            }
            updatedCount = stmt.executeBatch();
            if (!isInTransaction) {
                conn.commit();
            }
            processAndSetBatchUpdateResult(updatedCount, paramArrayCount);
            callback.notifySuccess();
        } catch (BatchUpdateException e) {
            // Depending on the driver, at this point, driver may or may not have executed the remaining commands in
            // the batch which come after the command that failed.
            // We could have rolled back the connection to keep a consistent behavior in Ballerina regardless of
            // the driver. But, in Ballerina, we've decided to honor whatever the behavior of the driver and
            // avoid rolling back the connection, because a Ballerina developer might have a requirement to
            // ignore a few failed commands in the batch and let the rest of the commands run if driver allows it.
            updatedCount = e.getUpdateCounts();
            processAndSetBatchUpdateResult(updatedCount, paramArrayCount);
            callback.notifySuccess();
        } catch (Throwable e) {
            String errorMessage = "execute batch update failed";
            if (conn != null) {
                if (!isInTransaction) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        errorMessage += ", failed to rollback any changes happened in-between";
                    }
                }
            }
            context.setReturnValues(
                    SQLDatasourceUtils.getSQLConnectorError(context, e, errorMessage + ": "));
            callback.notifySuccess();
            SQLDatasourceUtils.handleErrorOnTransaction(context);
            checkAndObserveSQLError(context, e.getMessage());
        } finally {
            SQLDatasourceUtils.cleanupResources(stmt, conn, !isInTransaction);
        }
    }

    private void processAndSetBatchUpdateResult(int[] updatedCounts, int paramArrayCount) {
        // After a command in a batch update fails to execute properly and a BatchUpdateException is thrown, the
        // driver may or may not continue to process the remaining commands in the batch. If the driver does not
        // continue processing after a failure, the array returned by the method will have -3 (EXECUTE_FAILED) for
        // those updates.
        long[] returnedCount = new long[paramArrayCount];
        Arrays.fill(returnedCount, Statement.EXECUTE_FAILED);
        BValueArray countArray = new BValueArray(returnedCount);
        if (updatedCounts != null) {
            int iSize = updatedCounts.length;
            for (int i = 0; i < iSize; ++i) {
                countArray.add(i, updatedCounts[i]);
            }
        }
        context.setReturnValues(countArray);
    }
}
