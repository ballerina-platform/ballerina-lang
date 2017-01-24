/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.core.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.DataIterator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This iterator mainly wrap Java ResultSet.
 */
public class RDBMSResultSetIterator implements DataIterator {

    private static final Logger logger = LoggerFactory.getLogger(RDBMSResultSetIterator.class);

    private Connection conn;
    private Statement stmt;
    private ResultSet rs;

    public RDBMSResultSetIterator(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
        this.conn = conn;
        this.stmt = stmt;
        this.rs = rs;
    }

    @Override
    public void close() throws IOException {
        cleanupConnection(rs, stmt, conn);
    }

    @Override
    public boolean next() {
        try {
            return rs.next();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public String getString(int index) {
        try {
            return rs.getString(index);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public long getLong(int index) {
        try {
            return rs.getLong(index);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    public int getInt(int index) {
        try {
            return rs.getInt(index);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    public float getFloat(int index) {
        try {
            return rs.getFloat(index);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    public double getDouble(int index) {
        try {
            return rs.getDouble(index);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    public boolean getBoolean(int index) {
        try {
            return rs.getBoolean(index);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }


    @Override
    protected void finalize() throws Throwable {
        cleanupConnection(rs, stmt, conn);
        super.finalize();
    }

    private void cleanupConnection(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        this.rs = null;
        this.stmt = null;
        this.conn = null;
    }
}
