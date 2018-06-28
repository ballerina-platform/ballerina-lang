/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.database.sql;

import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.transactions.BallerinaTransactionContext;

import java.sql.Connection;
import java.sql.SQLException;
import javax.transaction.xa.XAResource;

/**
 * {@code SQLTransactionContext} transaction context for SQL transactions.
 *
 * @since 0.87
 */
public class SQLTransactionContext implements BallerinaTransactionContext {
    private Connection conn;
    private XAResource xaResource;

    public SQLTransactionContext(Connection conn, XAResource resource) {
        this.conn = conn;
        this.xaResource = resource;
    }

    public SQLTransactionContext(Connection conn) {
        this.conn = conn;
    }

    public Connection getConnection() {
        return this.conn;
    }

    @Override
    public void commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new BallerinaException("transaction commit failed:" + e.getMessage());
        }
    }

    @Override
    public void rollback() {
        try {
            if (!conn.isClosed()) {
                conn.rollback();
            }
        } catch (SQLException e) {
            throw new BallerinaException("transaction rollback failed:" + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new BallerinaException("connection close failed:" + e.getMessage());
        }
    }

    @Override
    public void done() {
        /* ignore */
    }

    @Override
    public XAResource getXAResource() {
        return this.xaResource;
    }
}
