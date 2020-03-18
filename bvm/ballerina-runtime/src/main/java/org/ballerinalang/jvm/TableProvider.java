/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.TableIterator;
import org.ballerinalang.jvm.values.api.BString;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

/**
 * {@code TableProvider} creates In Memory database for tables.
 *
 * @since 0.995.0
 */
public class TableProvider {

    private static final String UNASSIGNABLE_UNIONTYPE_EXCEPTION =
            "Corresponding Union type in the record is not an assignable nillable type";
    private static TableProvider tableProvider = null;
    private int tableID;
    private int indexID;

    private TableProvider() {
        tableID = 0;
        indexID = 0;
    }

    public static TableProvider getInstance() {
        if (tableProvider != null) {
            return tableProvider;
        }
        synchronized (TableProvider.class) {
            if (tableProvider == null) {
                tableProvider = new TableProvider();
            }
        }
        return tableProvider;
    }

    private synchronized int getTableID() {
        return this.tableID++;
    }

    private synchronized int getIndexID() {
        return this.indexID++;
    }

    public String createTable(BType constrainedType, ArrayValue primaryKeys) {
        String tableName = TableConstants.TABLE_PREFIX + constrainedType.getName()
                .toUpperCase() + "_" + getTableID();
        String sqlStmt = generateCreateTableStatement(tableName, constrainedType, primaryKeys);
        executeStatement(sqlStmt);
        return tableName;
    }

    public String createTable(String fromTableName, String joinTableName,  String query, BStructureType tableType,
                              ArrayValue params) {
        String newTableName = TableConstants.TABLE_PREFIX + tableType.getName().toUpperCase()
                              + "_" + getTableID();
        String sqlStmt = query.replaceFirst(TableConstants.TABLE_NAME_REGEX, fromTableName);
        if (joinTableName != null && !joinTableName.isEmpty()) {
            sqlStmt = sqlStmt.replaceFirst(TableConstants.TABLE_NAME_REGEX, joinTableName);
        }
        sqlStmt = generateCreateTableStatement(sqlStmt, newTableName);
        prepareAndExecuteStatement(sqlStmt, params);
        return newTableName;
    }

    public String createTable(String fromTableName, String query, BStructureType tableType,
                              ArrayValue params) {
        return createTable(fromTableName, null, query, tableType, params);
    }

    public void insertData(String tableName, MapValueImpl<String, Object> constrainedType) {
        String sqlStmt = TableUtils.generateInsertDataStatement(tableName, constrainedType);
        prepareAndExecuteStatement(sqlStmt, constrainedType);
    }

    public void insertData_bstring(String tableName, MapValueImpl<BString, Object> constrainedType) {
        String sqlStmt = TableUtils.generateInsertDataStatement(tableName, constrainedType);
        prepareAndExecuteStatement_bstring(sqlStmt, constrainedType);
    }

    public void deleteData(String tableName, MapValueImpl<String, Object> constrainedType) {
        String sqlStmt = TableUtils.generateDeleteDataStatment(tableName, constrainedType);
        prepareAndExecuteStatement(sqlStmt, constrainedType);
    }

    public void dropTable(String tableName) {
        String sqlStmt = TableConstants.SQL_DROP + tableName;
        executeStatement(sqlStmt);
    }

    public TableIterator createIterator(String tableName, BStructureType type) {
        TableIterator itr;
        Statement stmt = null;
        Connection conn = this.getConnection();
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(TableConstants.SQL_SELECT + tableName);
            TableResourceManager rm = new TableResourceManager(conn, stmt, true);
            rm.addResultSet(rs);
            itr = new TableIterator(rm, rs, type);
        } catch (SQLException e) {
            releaseResources(conn, stmt);
            throw TableUtils.createTableOperationError("error in creating iterator for table : " + e.getMessage());
        }
        return itr;
    }

    private Connection getConnection() {
        Connection conn;
        try {
            conn = DriverManager
                    .getConnection(TableConstants.DB_JDBC_URL, TableConstants.DB_USER_NAME, TableConstants.DB_PASSWORD);
        } catch (SQLException e) {
            throw TableUtils.createTableOperationError("error in getting connection for table db : " + e.getMessage());
        }
        return conn;
    }

    private String generateCreateTableStatement(String tableName, BType constrainedType, ArrayValue primaryKeys) {
        StringBuilder sb = new StringBuilder();
        sb.append(TableConstants.SQL_CREATE).append(tableName).append(" (");
        Collection<BField> structFields = ((BStructureType) constrainedType).getFields().values();
        String seperator = "";
        for (BField sf : structFields) {
            int type = sf.getFieldType().getTag();
            String name = sf.getFieldName();
            sb.append(seperator).append(name).append(" ");
            switch (type) {
                case TypeTags.INT_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.BOOLEAN_TAG:
                case TypeTags.JSON_TAG:
                case TypeTags.XML_TAG:
                case TypeTags.ARRAY_TAG:
                    generateCreateTableStatement(type, sf, sb);
                    break;
                case TypeTags.UNION_TAG:
                    List<BType> members = ((BUnionType) sf.getFieldType()).getMemberTypes();
                    if (members.size() != 2) {
                        throw TableUtils.createTableOperationError(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
                    }
                    if (members.get(0).getTag() == TypeTags.NULL_TAG) {
                        generateCreateTableStatement(members.get(1).getTag(), sf, sb);
                    } else if (members.get(1).getTag() == TypeTags.NULL_TAG) {
                        generateCreateTableStatement(members.get(0).getTag(), sf, sb);
                    } else {
                        throw TableUtils.createTableOperationError(UNASSIGNABLE_UNIONTYPE_EXCEPTION);
                    }
                    break;
                default:
                    throw TableUtils
                            .createTableOperationError("Unsupported column type for table : " + sf.getFieldType());
            }
            seperator = ",";
        }
        //Add primary key information
        if (primaryKeys != null) {
            seperator = "";
            int primaryKeyCount = primaryKeys.size();
            if (primaryKeyCount > 0) {
                sb.append(TableConstants.PRIMARY_KEY);
                for (int i = 0; i < primaryKeyCount; i++) {
                    sb.append(seperator).append(primaryKeys.getString(i));
                    seperator = ",";
                }
                sb.append(")");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private void generateCreateTableStatement(int type, BField sf, StringBuilder sb) {
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
            case TypeTags.DECIMAL_TAG:
                sb.append(TableConstants.SQL_TYPE_DECIMAL);
                break;
            case TypeTags.BOOLEAN_TAG:
                sb.append(TableConstants.SQL_TYPE_BOOLEAN);
                break;
            case TypeTags.JSON_TAG:
            case TypeTags.XML_TAG:
                sb.append(TableConstants.SQL_TYPE_CLOB);
                break;
            case TypeTags.ARRAY_TAG:
                BType elementType = ((BArrayType) sf.getFieldType()).getElementType();
                if (elementType.getTag() == TypeTags.BYTE_TAG) {
                    sb.append(TableConstants.SQL_TYPE_BLOB);
                } else {
                    sb.append(TableConstants.SQL_TYPE_ARRAY);
                }
                break;
            default:
                throw TableUtils
                        .createTableOperationError("Unsupported nillable field for table : " + sf.getFieldType());
        }
    }

    private String generateCreateTableStatement(String query, String newTableName) {
        StringBuilder sb = new StringBuilder();
        sb.append(TableConstants.SQL_CREATE).append(newTableName).append(" ").append(TableConstants.SQL_AS);
        sb.append(query);
        return sb.toString();
    }

    private void executeStatement(String queryStatement) {
        Statement stmt = null;
        Connection conn = this.getConnection();
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(queryStatement);
        } catch (SQLException e) {
            throw TableUtils.createTableOperationError(
                    "error in executing statement : " + queryStatement + " error:" + e.getMessage());
        } finally {
            releaseResources(conn, stmt);
        }
    }

    private void prepareAndExecuteStatement(String queryStatement, ArrayValue params) {
        PreparedStatement stmt = null;
        Connection conn = this.getConnection();
        try {
            stmt = conn.prepareStatement(queryStatement);
            for (int index = 1; index <= params.size(); index++) {
                Object param = params.getRefValue(index - 1);
                BType paramType = TypeChecker.getType(param);
                switch (paramType.getTag()) {
                    case TypeTags.INT_TAG:
                        stmt.setLong(index, (Long) params.getRefValue(index - 1));
                        break;
                    case TypeTags.STRING_TAG:
                        stmt.setString(index, (String) params.getRefValue(index - 1));
                        break;
                    case TypeTags.FLOAT_TAG:
                        stmt.setDouble(index, (Double) params.getRefValue(index - 1));
                        break;
                    case TypeTags.DECIMAL_TAG:
                        stmt.setBigDecimal(index, ((DecimalValue) params.getRefValue(index - 1)).value());
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        stmt.setBoolean(index, (Boolean) params.getRefValue(index - 1));
                        break;
                    case TypeTags.XML_TAG:
                    case TypeTags.JSON_TAG:
                        stmt.setString(index, params.getString(index - 1));
                        break;
                    case TypeTags.ARRAY_TAG:
                        BType elementType = ((BArrayType) paramType).getElementType();
                        if (elementType.getTag() == TypeTags.BYTE_TAG) {
                            byte[] blobData = params.getBytes();
                            stmt.setBlob(index, new ByteArrayInputStream(blobData), blobData.length);
                        } else {
                            Object[] arrayData = TableUtils.getArrayData((ArrayValue) param);
                            stmt.setObject(index, arrayData);
                        }
                        break;
                }
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw TableUtils.createTableOperationError(
                    "error in executing statement : " + queryStatement + " error:" + e.getMessage());
        } finally {
            releaseResources(conn, stmt);
        }
    }

    private void prepareAndExecuteStatement(String queryStatement, MapValueImpl<String, Object> constrainedType) {
        PreparedStatement stmt = null;
        Connection conn = this.getConnection();
        try {
            stmt = conn.prepareStatement(queryStatement);
            TableUtils.prepareAndExecuteStatement(stmt, constrainedType);
        } catch (SQLException e) {
            throw TableUtils.createTableOperationError(
                    "error in executing statement : " + queryStatement + " error:" + e.getMessage());
        } finally {
            releaseResources(conn, stmt);
        }
    }
    private void prepareAndExecuteStatement_bstring(String queryStatement,
                                                     MapValueImpl<BString, Object> constrainedType) {
        PreparedStatement stmt = null;
        Connection conn = this.getConnection();
        try {
            stmt = conn.prepareStatement(queryStatement);
            TableUtils.prepareAndExecuteStatement_bstring(stmt, constrainedType);
        } catch (SQLException e) {
            throw TableUtils.createTableOperationError(
                    "error in executing statement : " + queryStatement + " error:" + e.getMessage());
        } finally {
            releaseResources(conn, stmt);
        }
    }

    private void releaseResources(Connection conn, Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            throw TableUtils
                    .createTableOperationError("error in releasing table statement resource : " + e.getMessage());
        }
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            throw TableUtils
                    .createTableOperationError("error in releasing table connection resource : " + e.getMessage());
        }
    }
}
