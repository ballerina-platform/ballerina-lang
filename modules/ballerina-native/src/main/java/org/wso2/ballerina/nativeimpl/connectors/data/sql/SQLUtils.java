/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.nativeimpl.connectors.data.sql;

import org.wso2.ballerina.core.model.types.TypeEnum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * This contains all the utils methods related to SQL operations.
 *
 * @since 0.8.0
 */
public class SQLUtils {

    /**
     * This method will return equal ballerina data type for SQL type.
     *
     * @param sqlType SQL type in column
     * @return TypeEnum that represent respective ballerina type.
     */
    public static TypeEnum getColumnType(int sqlType) {
        switch (sqlType) {
        case Types.ARRAY:
            return TypeEnum.ARRAY;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
        case Types.CLOB:
        case Types.NCLOB:
            return TypeEnum.STRING;
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return TypeEnum.INT;
        case Types.BIGINT:
            return TypeEnum.LONG;
        case Types.REAL:
            return TypeEnum.FLOAT;
        case Types.BIT:
        case Types.BOOLEAN:
            return TypeEnum.BOOLEAN;
        case Types.NUMERIC:
        case Types.DECIMAL:
        case Types.FLOAT:
        case Types.DOUBLE:
            return TypeEnum.DOUBLE;
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
        case Types.TIME_WITH_TIMEZONE:
        case Types.TIMESTAMP_WITH_TIMEZONE:
        case Types.BLOB:
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return TypeEnum.EMPTY;
        default:
            return TypeEnum.EMPTY;
        }
    }

    /**
     * This will close Database connection, statement and the resultset.
     *
     * @param rs   SQL resultset
     * @param stmt SQL statement
     * @param conn SQL connection
     */
    public static void cleanupConnection(ResultSet rs, Statement stmt, Connection conn) {
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
    }
}
