/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util;

import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * {@code TableProvider} creates In Memory database for tables.
 *
 * @since 0.963.0
 */
public class TableProvider {

    private static TableProvider tableProvider = null;
    private Connection connection = null;
    private int tableID;

    private TableProvider() {
        initDatabase();
    }

    public static TableProvider getInstance() {
        if (tableProvider == null) {
            synchronized (TableProvider.class) {
                if (tableProvider == null) {
                    tableProvider = new TableProvider();
                }
            }
        }
        return tableProvider;
    }

    private synchronized int getTableID() {
        return this.tableID++;
    }

    private void initDatabase() {
        try {
            Class.forName(TableConstants.DRIVER_CLASS_NAME);
            String jdbcURL = TableConstants.DB_JDBC_URL;
            connection = DriverManager.getConnection(jdbcURL, TableConstants.DB_USER_NAME, TableConstants.DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new BallerinaException("error in initializing database for table types : " + e.getMessage());
        }
    }

    public String createTable(BType constrainedType) {
        String tableName = TableConstants.TABLE_PREFIX + ((BTableType) constrainedType).getConstrainedType().getName()
                .toUpperCase() + "_" + getTableID();
        String sqlStmt = generateCreateTableStatment(tableName, constrainedType);
        executeStatement(sqlStmt);
        return tableName;
    }

    public String insertData(String tableName, BStruct constrainedType) {
        String sqlStmt = generateInsertDataStatment(tableName, constrainedType);
        executeStatement(sqlStmt);
        return tableName;
    }

    public void dropTable(String tableName) {
        String sqlStmt = TableConstants.SQL_DROP + tableName;
        executeStatement(sqlStmt);
    }

    public TableIterator createIterator(String tableName, BStructType type) {
        TableIterator itr;
        Statement st = null;
        try {
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(TableConstants.SQL_SELECT + tableName);
            itr = new TableIterator(rs, type);
        } catch (SQLException e) {
            releaseResources(st);
            throw new BallerinaException("error in creating iterator for table : " + e.getMessage());
        }
        return itr;
    }

    private String generateCreateTableStatment(String tableName, BType constrainedType) {
        StringBuilder sb = new StringBuilder();
        sb.append(TableConstants.SQL_CREATE).append(tableName).append(" (");
        BStructType.StructField[] structFields = ((BStructType) ((BTableType) constrainedType).getConstrainedType())
                .getStructFields();
        String seperator = "";
        for (BStructType.StructField sf : structFields) {
            int type = sf.getFieldType().getTag();
            String name = sf.getFieldName();
            sb.append(seperator).append(name).append(" ");
            switch (type) {
            case TypeTags.INT_TAG:
                sb.append(TableConstants.SQL_TYPE_BIGINT);
                break;
            case TypeTags.STRING_TAG:
                sb.append(TableConstants.SQL_TYPE_VARCHAR);
                break;
            case TypeTags.FLOAT_TAG:
                sb.append(TableConstants.SQL_TYPE_DOUBLE);
                break;
            case TypeTags.BOOLEAN_TAG:
                sb.append(TableConstants.SQL_TYPE_BOOLEAN);
                break;
            case TypeTags.JSON_TAG:
            case TypeTags.XML_TAG:
                sb.append(TableConstants.SQL_TYPE_LONG_VARCHAR);
                break;
            }
            seperator = ",";
        }
        sb.append(")");
        return sb.toString();
    }

    private String generateInsertDataStatment(String tableName, BStruct constrainedType) {
        StringBuilder sbSql = new StringBuilder();
        StringBuilder sbValues = new StringBuilder();
        sbSql.append(TableConstants.SQL_INSERT_INTO).append(tableName).append(" (");
        BStructType.StructField[] structFields = constrainedType.getType().getStructFields();
        String sep = "";
        int intFieldIndex = 0;
        int floatFieldIndex = 0;
        int stringFieldIndex = 0;
        int booleanFieldIndex = 0;
        int refFieldIndex = 0;
        for (BStructType.StructField sf : structFields) {
            String name = sf.getFieldName();
            sbSql.append(sep).append(name).append(" ");
            int type = sf.getFieldType().getTag();
            switch (type) {
            case TypeTags.INT_TAG:
                sbValues.append(sep).append(constrainedType.getIntField(intFieldIndex));
                ++intFieldIndex;
                break;
            case TypeTags.STRING_TAG:
                sbValues.append(sep).append("'").append(constrainedType.getStringField(stringFieldIndex)).append("'");
                ++stringFieldIndex;
                break;
            case TypeTags.FLOAT_TAG:
                sbValues.append(sep).append(constrainedType.getFloatField(floatFieldIndex));
                ++floatFieldIndex;
                break;
            case TypeTags.BOOLEAN_TAG:
                sbValues.append(sep).append(constrainedType.getBooleanField(booleanFieldIndex));
                ++booleanFieldIndex;
                break;
            case TypeTags.XML_TAG:
            case TypeTags.JSON_TAG:
                String sValue = constrainedType.getRefField(refFieldIndex).toString();
                sbValues.append(sep).append("'").append(sValue).append("'");
                ++refFieldIndex;
                break;
            }
            sep = ",";
        }
        sbSql.append(") values (").append(sbValues).append(")");
        return sbSql.toString();
    }

    private void executeStatement(String queryStatement) {
        Statement st = null;
        try {
            st = connection.createStatement();
            st.executeUpdate(queryStatement);
        } catch (SQLException e) {
            throw new BallerinaException(
                    "error in executing statement : " + queryStatement + " error:" + e.getMessage());
        } finally {
            releaseResources(st);
        }
    }

    private void releaseResources(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in releasing table resources : " + e.getMessage());
        }
    }
}
