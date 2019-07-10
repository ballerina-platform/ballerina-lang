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
import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.TableResourceManager;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
/**
 * Represents a Select SQL statement.
 *
 * @since 1.0.0
 */
public class SelectStatement extends AbstractSQLStatement {

    private final ObjectValue client;
    private final SQLDatasource datasource;
    private final String query;
    private final ArrayValue parameters;
    private final BStructureType structType;
    private final boolean loadSQLTableToMemory;

    public SelectStatement(ObjectValue client, SQLDatasource datasource, String query, ArrayValue parameters,
                           TypedescValue recordType, boolean loadSQLTableToMemory) {
        this.client = client;
        this.datasource = datasource;
        this.query = query;
        this.parameters = parameters;
        this.structType = recordType != null ? (BStructureType) recordType.getDescribingType() : null;
        this.loadSQLTableToMemory = loadSQLTableToMemory;
    }

    @Override
    public Object execute() {
        //TODO: JBalMigration Commenting out observability
        //TODO: #16033
        // checkAndObserveSQLAction(context, datasource, query);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String errorMessagePrefix = "execute query failed: ";
        try {
            ArrayValue generatedParams = constructParameters(parameters);
            conn = getDatabaseConnection(client, datasource, true);
            String processedQuery = createProcessedQueryString(query, generatedParams);
            stmt = getPreparedStatement(conn, datasource, processedQuery, loadSQLTableToMemory);
            createProcessedStatement(conn, stmt, generatedParams, datasource.getDatabaseProductName());
            rs = stmt.executeQuery();
            TableResourceManager rm = new TableResourceManager(conn, stmt, true);
            List<ColumnDefinition> columnDefinitions = getColumnDefinitions(rs);
            if (loadSQLTableToMemory) {
                CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
                cachedRowSet.populate(rs);
                rs = cachedRowSet;
                rm.gracefullyReleaseResources();
            } else {
                rm.addResultSet(rs);
            }
            return constructTable(rm, rs, structType, columnDefinitions, datasource.getDatabaseProductName());
        } catch (SQLException e) {
            cleanupResources(rs, stmt, conn, true);
            //TODO: JBalMigration Commenting out transaction handling and observability
            //handleErrorOnTransaction(context);
            // checkAndObserveSQLError(context, "execute query failed: " + e.getMessage());
            return SQLDatasourceUtils.getSQLDatabaseError(e, errorMessagePrefix);
        } catch (DatabaseException e) {
            cleanupResources(null, stmt, conn, true);
            //TODO: JBalMigration Commenting out transaction handling and observability
            //handleErrorOnTransaction(context);
            // checkAndObserveSQLError(context, "execute query failed: " + e.getMessage());
            return SQLDatasourceUtils.getSQLDatabaseError(e, errorMessagePrefix);
        } catch (ApplicationException e) {
            cleanupResources(null, stmt, conn, true);
            //TODO: JBalMigration Commenting out transaction handling and observability
            //handleErrorOnTransaction(context);
            // checkAndObserveSQLError(context, "execute query failed: " + e.getMessage());
            return SQLDatasourceUtils.getSQLApplicationError(e, errorMessagePrefix);
        }
    }

    private PreparedStatement getPreparedStatement(Connection conn, SQLDatasource datasource, String query,
                                                     boolean loadToMemory) throws SQLException {
        PreparedStatement stmt;
        boolean mysql = datasource.getDatabaseProductName().contains(Constants.DatabaseNames.MYSQL);
        /* In MySQL by default, ResultSets are completely retrieved and stored in memory.
           Following properties are set to stream the results back one row at a time.*/
        if (mysql && !loadToMemory) {
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
}
