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
package org.ballerinalang.jdbc.statement;

import org.ballerinalang.jdbc.Constants;
import org.ballerinalang.jdbc.datasource.SQLDatasource;
import org.ballerinalang.jdbc.exceptions.ApplicationException;
import org.ballerinalang.jdbc.exceptions.ErrorGenerator;
import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;

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
public class SelectStatementStream extends AbstractSQLStatement {

    private final ObjectValue client;
    private final SQLDatasource datasource;
    private final String query;
    private final ArrayValue parameters;

    public SelectStatementStream(Strand strand) {
        super(strand);
        this.client = null;
        this.datasource = null;
        this.query = null;
        this.parameters = null;
    }
    public SelectStatementStream(ObjectValue client, SQLDatasource datasource, String query,
                                 ArrayValue parameters, Strand strand) {
        super(strand);
        this.client = client;
        this.datasource = datasource;
        this.query = query;
        this.parameters = parameters;
    }

    @Override
    public Object execute() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        checkAndObserveSQLAction(strand, datasource, query);
        String errorMessagePrefix = "failed to execute select query: ";
        try {
            ArrayValue generatedParams = constructParameters(parameters);
            conn = getDatabaseConnection(strand, client, datasource);
            String processedQuery = createProcessedQueryString(query, generatedParams);
            stmt = getPreparedStatement(conn, datasource, processedQuery);
            ProcessedStatement processedStatement = new ProcessedStatement(conn, stmt, generatedParams,
                    datasource.getDatabaseProductName());
            stmt = processedStatement.prepare();
            rs = stmt.executeQuery();
            List<ColumnDefinition> columnDefinitions = getColumnDefinitions(rs);
            return constructStream(rs, columnDefinitions);
        } catch (SQLException e) {
            cleanupResources(rs, stmt, conn, true);
            handleErrorOnTransaction(this.strand);
            checkAndObserveSQLError(strand, "execute query failed: " + e.getMessage());
            return ErrorGenerator.getSQLDatabaseError(e, errorMessagePrefix);
        } catch (ApplicationException e) {
            cleanupResources(null, stmt, conn, true);
            handleErrorOnTransaction(this.strand);
            checkAndObserveSQLError(strand, "execute query failed: " + e.getMessage());
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
