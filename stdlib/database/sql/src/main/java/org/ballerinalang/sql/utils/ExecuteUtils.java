/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.sql.utils;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.datasource.SQLDatasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class holds the utility methods involved with executing the query which does not return rows.
 *
 * @since 1.2.0
 */
public class ExecuteUtils {

    public static Object nativeExecute(ObjectValue client, String sqlQuery) {
        Object dbClient = client.getNativeData(Constants.DATABASE_CLIENT);
        if (dbClient != null) {
            SQLDatasource sqlDatasource = (SQLDatasource) dbClient;
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                connection = sqlDatasource.getSQLConnection();
                statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
                int count = statement.executeUpdate();
                Object lastInsertedId = null;
                if (!isDdlStatement(sqlQuery)) {
                    resultSet = statement.getGeneratedKeys();
                    if (resultSet.next()) {
                        lastInsertedId = getGeneratedKeys(resultSet);
                    }
                }
                Map<String, Object> resultFields = new HashMap<>();
                resultFields.put(Constants.AFFECTED_ROW_COUNT_FIELD, count);
                resultFields.put(Constants.LAST_INSERTED_ID_FIELD, lastInsertedId);
                return BallerinaValues.createRecordValue(Constants.SQL_PACKAGE_ID, Constants.EXCUTE_RESULT_RECORD, resultFields);
            } catch (SQLException e) {
                return ErrorGenerator.getSQLDatabaseError(e,
                        "Error while executing sql query: " + sqlQuery + ". ");
            } finally {
                Utils.closeResources(resultSet, statement, connection);
            }
        } else {
            return ErrorGenerator.getSQLApplicationError(
                    "Client is not properly initialized!");
        }
    }

    private static Object getGeneratedKeys(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        if (columnCount > 0) {
            int sqlType = metaData.getColumnType(1);
            switch (sqlType) {
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.INTEGER:
                case Types.BIGINT:
                case Types.BIT:
                case Types.BOOLEAN:
                    return rs.getLong(1);
                default:
                    return rs.getString(1);
            }
        }
        return null;
    }

    private static boolean isDdlStatement(String query) {
        String upperCaseQuery = query.trim().toUpperCase(Locale.ENGLISH);
        return Arrays.stream(DdlKeyword.values()).anyMatch(ddlKeyword -> upperCaseQuery.startsWith(ddlKeyword.name()));
    }

    private enum DdlKeyword {
        CREATE, ALTER, DROP, TRUNCATE, COMMENT, RENAME
    }
}
