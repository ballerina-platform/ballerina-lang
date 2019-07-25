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

import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.TableResourceManager;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.SQLDatasource;
import org.ballerinax.jdbc.exceptions.ApplicationException;
import org.ballerinax.jdbc.exceptions.ErrorGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    public SelectStatement(ObjectValue client, SQLDatasource datasource, String query, ArrayValue parameters,
                           TypedescValue recordType, Strand strand) {
        super(strand);
        this.client = client;
        this.datasource = datasource;
        this.query = query;
        this.parameters = parameters;
        this.structType = recordType != null ? (BStructureType) recordType.getDescribingType() : null;
    }

    @Override
    public Object execute() {
        //TODO: JBalMigration Commenting out observability
        //TODO: #16033
        // checkAndObserveSQLAction(context, datasource, query);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String errorMessagePrefix = "Failed to execute select query: ";
        try {
            ArrayValue generatedParams = constructParameters(parameters);
            conn = getDatabaseConnection(strand, client, datasource, true);
            String processedQuery = createProcessedQueryString(query, generatedParams);
            stmt = getPreparedStatement(conn, datasource, processedQuery);
            ProcessedStatement processedStatement = new ProcessedStatement(conn, stmt, generatedParams,
                    datasource.getDatabaseProductName());
            stmt = processedStatement.prepare();
            rs = stmt.executeQuery();
            TableResourceManager rm = new TableResourceManager(conn, stmt, true);
            List<ColumnDefinition> columnDefinitions = getColumnDefinitions(rs);
            rm.addResultSet(rs);
            return constructTable(rm, rs, structType, columnDefinitions, datasource.getDatabaseProductName());
        } catch (SQLException e) {
            cleanupResources(rs, stmt, conn, true);
            handleErrorOnTransaction(this.strand);
            //TODO: JBalMigration Commenting out transaction handling and observability
            // checkAndObserveSQLError(context, "execute query failed: " + e.getMessage());
            return ErrorGenerator.getSQLDatabaseError(e, errorMessagePrefix);
        } catch (ApplicationException e) {
            cleanupResources(null, stmt, conn, true);
            //TODO: JBalMigration Commenting out transaction handling and observability
            handleErrorOnTransaction(this.strand);
            // checkAndObserveSQLError(context, "execute query failed: " + e.getMessage());
            return ErrorGenerator.getSQLApplicationError(e, errorMessagePrefix);
        }
    }

    private PreparedStatement getPreparedStatement(Connection conn, SQLDatasource datasource, String query)
            throws SQLException {
        PreparedStatement stmt;
        boolean mysql = datasource.getDatabaseProductName().contains(Constants.DatabaseNames.MYSQL);
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
}
