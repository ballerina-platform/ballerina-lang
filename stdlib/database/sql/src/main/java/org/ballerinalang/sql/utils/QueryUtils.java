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
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.datasource.SQLDatasource;
import org.ballerinalang.sql.exception.ApplicationError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class provides the util implementation which executes sql queries.
 *
 * @since 1.2.0
 */
public class QueryUtils {

    public static StreamValue nativeQuery(ObjectValue client, String sqlQuery, Object recordType) {
        Object dbClient = client.getNativeData(Constants.DATABASE_CLIENT);
        if (dbClient != null) {
            SQLDatasource sqlDatasource = (SQLDatasource) dbClient;
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                connection = sqlDatasource.getSQLConnection();
                preparedStatement = connection.prepareStatement(sqlQuery);
                resultSet = preparedStatement.executeQuery();
                List<ColumnDefinition> columnDefinitions;
                BStructureType streamConstraint;
                if (recordType == null) {
                    columnDefinitions = getColumnDefinitions(resultSet, null);
                    BRecordType defaultRecord = getDefaultStreamConstraint();
                    Map<String, BField> fieldMap = new HashMap<>();
                    for (ColumnDefinition column : columnDefinitions) {
                        int flags = Flags.PUBLIC;
                        if (column.isNullable()) {
                            flags += Flags.OPTIONAL;
                        } else {
                            flags += Flags.REQUIRED;
                        }
                        fieldMap.put(column.getSqlName(), new BField(column.getBallerinaType(), column.getSqlName(),
                                flags));
                    }
                    defaultRecord.setFields(fieldMap);
                    streamConstraint = defaultRecord;
                } else {
                    streamConstraint = (BStructureType) ((TypedescValue) recordType).getDescribingType();
                    columnDefinitions = getColumnDefinitions(resultSet, streamConstraint);
                }
                return new StreamValue(new BStreamType(streamConstraint), createRecordIterator(resultSet,
                        preparedStatement, connection, columnDefinitions, streamConstraint));
            } catch (SQLException e) {
                closeResources(resultSet, preparedStatement, connection);
                ErrorValue errorValue = ErrorGenerator.getSQLDatabaseError(e,
                        "Error while executing sql query: " + sqlQuery + ". ");
                return new StreamValue(getDefaultStreamConstraint(), createRecordIterator(errorValue));
            } catch (ApplicationError applicationError) {
                closeResources(resultSet, preparedStatement, connection);
                ErrorValue errorValue = ErrorGenerator.getSQLApplicationError(applicationError.getMessage());
                return new StreamValue(getDefaultStreamConstraint(), createRecordIterator(errorValue));
            }
        } else {
            ErrorValue errorValue = ErrorGenerator.getSQLApplicationError(
                    "Client is not properly initialized!");
            return new StreamValue(getDefaultStreamConstraint(), createRecordIterator(errorValue));
        }
    }

    private static BRecordType getDefaultStreamConstraint() {
        BRecordType defaultRecord = new BRecordType("$stream$anon$constraint$",
                new BPackage("ballerina", "lang.annotations", "0.0.0"), 0, false,
                TypeFlags.asMask(TypeFlags.ANYDATA, TypeFlags.PURETYPE));
        defaultRecord.restFieldType = BTypes.typeAnydata;
        return defaultRecord;
    }

    private static void closeResources(ResultSet resultSet, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private static List<ColumnDefinition> getColumnDefinitions(ResultSet resultSet, BStructureType streamConstraint)
            throws SQLException, ApplicationError {
        List<ColumnDefinition> columnDefs = new ArrayList<>();
        Set<String> columnNames = new HashSet<>();
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        int cols = rsMetaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String colName = rsMetaData.getColumnLabel(i);
            if (columnNames.contains(colName)) {
                String tableName = rsMetaData.getTableName(i).toUpperCase(Locale.getDefault());
                colName = tableName + "." + colName;
            }
            int sqlType = rsMetaData.getColumnType(i);
            String sqlTypeName = rsMetaData.getColumnTypeName(i);
            boolean isNullable = true;
            if (rsMetaData.isNullable(i) == ResultSetMetaData.columnNoNulls) {
                isNullable = false;
            }
            columnDefs.add(generateColumnDefinition(colName, sqlType, sqlTypeName, streamConstraint, isNullable));
            columnNames.add(colName);
        }
        return columnDefs;
    }

    private static ColumnDefinition generateColumnDefinition(String columnName, int sqlType, String sqlTypeName,
                                                             BStructureType streamConstraint, boolean isNullable)
            throws ApplicationError {
        String ballerinaFieldName = null;
        BType ballerinaType = null;
        if (streamConstraint != null) {
            for (Map.Entry<String, BField> field : streamConstraint.getFields().entrySet()) {
                if (field.getKey().equalsIgnoreCase(columnName)) {
                    ballerinaFieldName = field.getKey();
                    ballerinaType = field.getValue().type;
                    if (RecordConverterUtils.isValidFieldConstraint(sqlType, ballerinaType)) {
                        throw new ApplicationError(ballerinaType.getName() + " cannot be mapped to sql type: "
                                + sqlTypeName + " .");
                    }
                    break;
                }
            }
            if (ballerinaFieldName == null || ballerinaType == null) {
                throw new ApplicationError("No mapping field for the column name : " + columnName
                        + " found in the provided record type : " + streamConstraint.getName() + " .");
            }
        } else {
            ballerinaType = getDefaultBallerinaType(sqlType);
            ballerinaFieldName = columnName;
        }
        return new ColumnDefinition(columnName, ballerinaFieldName, sqlType, ballerinaType, isNullable);

    }

    private static ObjectValue createRecordIterator(ResultSet resultSet,
                                                    PreparedStatement preparedStatement,
                                                    Connection connection, List<ColumnDefinition> columnDefinitions,
                                                    BStructureType streamConstraint) {
        ObjectValue resultIterator = BallerinaValues.createObjectValue(Constants.SQL_PACKAGE_ID,
                Constants.RESULT_ITERATOR_OBJECT, new Object[1]);
        resultIterator.addNativeData(Constants.RESULT_SET_NATIVE_DATA_FIELD, resultSet);
        resultIterator.addNativeData(Constants.STATEMENT_NATIVE_DATA_FIELD, preparedStatement);
        resultIterator.addNativeData(Constants.CONNECTION_NATIVE_DATA_FIELD, connection);
        resultIterator.addNativeData(Constants.COLUMN_DEFINITIONS_DATA_FIELD, columnDefinitions);
        resultIterator.addNativeData(Constants.RECORD_TYPE_DATA_FIELD, streamConstraint);
        return resultIterator;
    }

    private static ObjectValue createRecordIterator(ErrorValue errorValue) {
        return BallerinaValues.createObjectValue(Constants.SQL_PACKAGE_ID, Constants.RESULT_ITERATOR_OBJECT,
                errorValue);
    }

    private static BType getDefaultBallerinaType(int sqlType) {
        switch (sqlType) {
            case Types.ARRAY:
                return new BArrayType(BTypes.typeAny);
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.TIME_WITH_TIMEZONE:
            case Types.ROWID:
                return BTypes.typeString;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return BTypes.typeInt;
            case Types.BIT:
            case Types.BOOLEAN:
                return BTypes.typeBoolean;
            case Types.NUMERIC:
            case Types.DECIMAL:
                return BTypes.typeDecimal;
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return BTypes.typeFloat;
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return new BArrayType(BTypes.typeByte);
            case Types.STRUCT:
                return getDefaultStreamConstraint();
            default:
                return BTypes.typeAny;
        }
    }
}
