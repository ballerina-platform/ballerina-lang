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

package org.wso2.ballerina.core.model.values;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * This iterator mainly wrap Java ResultSet.
 */
public class RDBMSResultSetIterator implements DataIterator {

    private static final Logger logger = LoggerFactory.getLogger(RDBMSResultSetIterator.class);

    private Connection conn;
    private Statement stmt;
    private ResultSet rs;
    private ParamValue[] records;
    private int columnCount;

    public RDBMSResultSetIterator(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
        this.conn = conn;
        this.stmt = stmt;
        this.rs = rs;
        init(rs);
    }

    private void init(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        columnCount = metaData.getColumnCount();
        records = new ParamValue[columnCount];
        for (int i = 0; i < columnCount; i++) {
            records[i].setColumnTypes(metaData.getColumnType(i));
            records[i].setColumnNames(metaData.getColumnTypeName(i));
        }
    }

    @Override
    public Object getColumnValue(int columnIndex) {
        if (records.length == 0) {
            this.next();
        }
        return records[columnIndex].getValue();
    }

    @Override
    public Object getColumnValue(String columnName) {
        if (records.length == 0) {
            this.next();
        }
        int index = 0;
        for (int i = 0; i < records.length; i++) {
            if (records[i].getColumnNames().equals(columnName)) {
                index = i;
                break;
            }
        }
        return records[index].getValue();
    }

    @Override
    public void close() throws IOException {
        cleanupConnection(rs, stmt, conn);
    }

    @Override
    public boolean hasNext() {
        try {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Object next() {
        for (int i = 0; i < columnCount; i++) {
            try {
                Object obj = null;
                switch (records[i].getColumnTypes()) {
                /* handle string types */
                case Types.VARCHAR:
                /* fall through */
                case Types.LONGVARCHAR:
                /* fall through */
                case Types.CHAR:
                /* fall through */
                case Types.CLOB:
                /* fall through */
                case Types.NCHAR:
                /* fall through */
                case Types.NCLOB:
                /* fall through */
                case Types.NVARCHAR:
                /* fall through */
                case Types.LONGNVARCHAR:
                    obj = rs.getString(i);
                    break;
                /* handle numbers */
                case Types.INTEGER:
                /* fall through */
                case Types.TINYINT:
                /* fall through */
                case Types.SMALLINT:
                    obj = rs.getInt(i);
                    break;
                case Types.DOUBLE:
                    obj = rs.getDouble(i);
                    break;
                case Types.FLOAT:
                    obj = rs.getFloat(i);
                    break;
                case Types.BOOLEAN:
                /* fall through */
                case Types.BIT:
                    obj = rs.getBoolean(i);
                    break;
                case Types.DECIMAL:
                    obj = rs.getBigDecimal(i);
                    break;
                /* handle data/time values */
                case Types.TIME:
                /* handle time data type */
                    obj = rs.getTime(i);
                    break;
                case Types.DATE:
                /* handle date data type */
                    obj = rs.getDate(i);
                    break;
                case Types.TIMESTAMP:
                    obj = rs.getTimestamp(i);
                    break;
                /* handle binary types */
                case Types.BLOB:
                    obj = rs.getBlob(i);
                    break;
                case Types.BINARY:
                /* fall through */
                case Types.LONGVARBINARY:
                /* fall through */
                case Types.VARBINARY:
                    obj = rs.getBinaryStream(i);
                    break;
                /* handling User Defined Types */
                case Types.STRUCT:
                    obj = rs.getObject(i);
                    break;
                case Types.ARRAY:
                    obj = rs.getArray(i);
                    break;
                case Types.NUMERIC:
                    obj = rs.getBigDecimal(i);
                    break;
                case Types.BIGINT:
                    obj = rs.getLong(i);
                    break;
                }
                records[i].setValue(obj);
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return records;
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
        this.records = null;
    }
}
