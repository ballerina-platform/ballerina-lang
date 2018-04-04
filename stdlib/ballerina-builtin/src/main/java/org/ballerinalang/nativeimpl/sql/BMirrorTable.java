/*
 * Copyright (c) 2018, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.sql;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.util.TableConstants;
import org.ballerinalang.util.TableUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represent a reflection of a database table. Through a Mirrored table it is possible to add/remove data to/from a
 * database.
 */
public class BMirrorTable extends BTable {
    private SQLDatasource datasource;
    private String tableName;

    public BMirrorTable(DataIterator dataIterator, SQLDatasource datasource, String tableName, BStructType
            constraintType) {
        super(dataIterator, tableName, constraintType);
        this.datasource = datasource;
        this.tableName = tableName;
    }

    public void addData(BStruct data, Context context) {
        Connection conn = null;
        boolean isInTransaction = context.isInTransaction();
        try {
            conn = SQLDatasourceUtils.getDatabaseConnection(context, datasource, isInTransaction);
            String sqlStmt = TableUtils.generateInsertDataStatment(tableName, data);
            PreparedStatement stmt = conn.prepareStatement(sqlStmt);
            TableUtils.prepareAndExecuteStatement(stmt, data);
        } catch (SQLException e) {
            throw new BallerinaException("execute update failed: " + e.getMessage(), e);
        } finally {
            SQLDatasourceUtils.cleanupConnection(null, null, conn, isInTransaction);
        }
    }

    public void removeData(BStruct data, Context context) {
        Connection conn = null;
        boolean isInTransaction = context.isInTransaction();
        try {
            conn = SQLDatasourceUtils.getDatabaseConnection(context, datasource, isInTransaction);
            String sqlStmt = TableUtils.generateDeleteDataStatment(tableName, data);
            PreparedStatement stmt = conn.prepareStatement(sqlStmt);
            TableUtils.prepareAndExecuteStatement(stmt, data);
        } catch (SQLException e) {
            throw new BallerinaException("execute update failed: " + e.getMessage(), e);
        } finally {
            SQLDatasourceUtils.cleanupConnection(null, null, conn, isInTransaction);
        }
    }


    @Override
    public void next() {
        if (((SQLDataIterator) iterator).getResultset() == null) {
            populateIteratorAttributes(tableName);
            this.nextPrefetched = false;
            this.hasNextVal = false;
        }
        if (!nextPrefetched) {
            iterator.next();
        } else {
            nextPrefetched = false;
        }
    }

    @Override
    public boolean hasNext(boolean isInTransaction) {
        if (((SQLDataIterator) iterator).getResultset() == null) {
            populateIteratorAttributes(tableName);
            this.nextPrefetched = false;
            this.hasNextVal = false;
        }
        if (!nextPrefetched) {
            hasNextVal = iterator.next();
            nextPrefetched = true;
        }
        if (!hasNextVal) {
            close(isInTransaction);
        }
        return hasNextVal;
    }

    @Override
    public void close(boolean isInTransaction) {
        if (iterator != null) {
            iterator.close(isInTransaction);
            resetIterator();
        }
    }

    @Override
    public String stringValue() {
       return "";
    }

    private void resetIterator() {
        if (iterator != null) {
            iterator.close(false);
        }
        this.nextPrefetched = false;
        this.hasNextVal = false;
    }

    private void populateIteratorAttributes(String tableName) {
        PreparedStatement preparedStmt = null;
        ResultSet rs = null;
        Connection conn = datasource.getSQLConnection();
        try {
            String query = TableConstants.SQL_SELECT + tableName;
            preparedStmt = conn.prepareStatement(query);
            rs = preparedStmt.executeQuery();
            ((SQLDataIterator) iterator).populateTableResourceManager(conn, preparedStmt);
            ((SQLDataIterator) iterator).setResultSet(rs);
            ((SQLDataIterator) iterator).setColumnDefs(SQLDatasourceUtils.getColumnDefinitions(rs));
        } catch (SQLException e) {
            SQLDatasourceUtils.cleanupConnection(rs, preparedStmt, conn, false);
            throw new BallerinaException("error in creating iterator for table : " + e.getMessage());
        }
    }
}
