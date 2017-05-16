/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.table.rdbms;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Logger;
import org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;

public class RDBMSTableTestUtils {

    public static final String TABLE_NAME = "StockTable";
    private static final Logger log = Logger.getLogger(RDBMSTableTestUtils.class);
    private static final String CONNECTION_URL_MYSQL = "jdbc:mysql://localhost:3306/dasdb";
    private static final String CONNECTION_URL_H2 = "jdbc:h2:./target/dasdb";
    private static final String CONNECTION_URL_ORACLE = "jdbc:oracle:thin:@192.168.122.2:1521/dasdb";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    public static String url = CONNECTION_URL_H2;
    private static DataSource dataSource;

    private RDBMSTableTestUtils() {

    }

    public static DataSource getDataSource() {
        return getDataSource(TestType.H2);
    }

    public static DataSource getDataSource(TestType type) {
        if (dataSource == null) {
            dataSource = initDataSource(type);
        }
        return dataSource;
    }

    private static DataSource initDataSource(TestType type) {
        Properties connectionProperties = new Properties();
        switch (type) {
            case MySQL:
                url = CONNECTION_URL_MYSQL;
                break;
            case H2:
                url = CONNECTION_URL_H2;
                break;
            case ORACLE:
                url = CONNECTION_URL_ORACLE;
                break;
        }
        connectionProperties.setProperty("jdbcUrl", url);
        connectionProperties.setProperty("dataSource.user", USERNAME);
        connectionProperties.setProperty("dataSource.password", PASSWORD);
        HikariConfig config = new HikariConfig(connectionProperties);
        return new HikariDataSource(config);
    }

    public static void clearDatabaseTable(String tableName) throws SQLException {
        PreparedStatement stmt = null;
        Connection con = null;
        try {
            con = getDataSource().getConnection();
            con.setAutoCommit(false);
            stmt = con.prepareStatement("DROP TABLE " + tableName);
            stmt.execute();
            con.commit();
        } catch (SQLException e) {
            log.debug("Clearing DB table failed due to " + e.getMessage(), e);
        } finally {
            RDBMSTableUtils.cleanupConnection(null, stmt, con);
        }
    }

    public static long getRowsInTable(String tableName) throws SQLException {
        PreparedStatement stmt = null;
        Connection con = null;
        try {
            con = getDataSource().getConnection();
            stmt = con.prepareStatement("SELECT count(*) FROM " + tableName + "");
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            log.error("Getting rows in DB table failed due to " + e.getMessage(), e);
            throw e;
        } finally {
            RDBMSTableUtils.cleanupConnection(null, stmt, con);
        }
    }

    public enum TestType {
        MySQL, H2, ORACLE, MSSQL, DB2, POSTGRES
    }
}
