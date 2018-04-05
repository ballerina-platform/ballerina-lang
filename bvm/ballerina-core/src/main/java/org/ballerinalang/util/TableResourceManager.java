/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code {@link TableResourceManager }} is a container for a {@code {@link Connection}}, a {@code {@link Statement}}
 * and a {@code {@link Set}} of {@code {@link ResultSet}} associated with the {@code {@link Connection}}.
 *
 * @since 0.965.0
 */
public class TableResourceManager {
    private Connection connection;
    private Statement statement;
    private Set<ResultSet> resultSets;

    public TableResourceManager(Connection conn, Statement stmt) {
        this.connection = conn;
        this.statement = stmt;
        this.resultSets = new HashSet<>(0);
    }

    /**
     * Close the connection and the statement.
     *
     * @throws SQLException if an issue occur while closing the connection or statement
     */
    public void releaseResources() throws SQLException {
        if (statement != null && !statement.isClosed()) {
            statement.close();
            statement = null;
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }

    /**
     * Add a {@code {@link ResultSet}} to the Set of result sets. This {@code {@link ResultSet}} should be a one
     * associated with the connection. If an unrelated {@code {@link ResultSet}} is added it may result in undesirable
     * consequences as when calling {@link #gracefullyReleaseResources(boolean)} releasing resources is based on
     * whether all the result sets are closed.
     *
     * @param rs the result set to be added
     */
    public void addResultSet(ResultSet rs) {
        this.resultSets.add(rs);
    }

    /**
     * Add a list of {@code {@link ResultSet}} to the Set of result sets. If an unrelated {@code {@link ResultSet}}
     * is added it may result in undesirable consequences as when calling {@link #gracefullyReleaseResources(boolean)}
     * releasing resources is based on whether all the result sets are closed.
     *
     * @param resultSets the list of result sets to be added
     */
    public void addAllResultSets(List<ResultSet> resultSets) {
        this.resultSets.addAll(resultSets);
    }

    /**
     * Close the statement only if all ResultSets are closed, and close the connection only if all ResultSets are
     * closed and a transaction is not in progress.
     *
     * @param isInTransaction Whether a transaction is in progress
     * @throws SQLException if an issue occur while closing the connection or statement
     */
    public void gracefullyReleaseResources(boolean isInTransaction) throws SQLException {
        boolean allResultSetsClosed = true;
        for (ResultSet rs : resultSets) {
            if (rs != null && !rs.isClosed()) {
                allResultSetsClosed = false;
                break;
            }
        }
        if (allResultSetsClosed) {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            if (!isInTransaction && connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
}
