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
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.AbstractObjectValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.StringValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.datasource.SQLDatasource;
import org.ballerinalang.sql.datasource.SQLDatasourceUtils;
import org.ballerinalang.sql.exception.ApplicationError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.sql.utils.Utils.closeResources;
import static org.ballerinalang.sql.utils.Utils.getColumnDefinitions;
import static org.ballerinalang.sql.utils.Utils.getDefaultStreamConstraint;
import static org.ballerinalang.sql.utils.Utils.getSqlQuery;
import static org.ballerinalang.sql.utils.Utils.setParams;

/**
 * This class provides the util implementation which executes sql queries.
 *
 * @since 1.2.0
 */
public class QueryUtils {

    public static StreamValue nativeQuery(ObjectValue client, Object paramSQLString,
                                          Object recordType) {
        Object dbClient = client.getNativeData(Constants.DATABASE_CLIENT);
        Strand strand = Scheduler.getStrand();
        if (dbClient != null) {
            SQLDatasource sqlDatasource = (SQLDatasource) dbClient;
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            String sqlQuery = null;
            try {
                if (paramSQLString instanceof StringValue) {
                    sqlQuery = ((StringValue) paramSQLString).getValue();
                } else {
                    sqlQuery = getSqlQuery((AbstractObjectValue) paramSQLString);
                }
                connection = SQLDatasourceUtils.getConnection(strand, client, sqlDatasource);
                statement = connection.prepareStatement(sqlQuery);
                if (paramSQLString instanceof AbstractObjectValue) {
                    setParams(connection, statement, (AbstractObjectValue) paramSQLString);
                }
                resultSet = statement.executeQuery();
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
                        fieldMap.put(column.getColumnName(), new BField(column.getBallerinaType(),
                                column.getColumnName(), flags));
                    }
                    defaultRecord.setFields(fieldMap);
                    streamConstraint = defaultRecord;
                } else {
                    streamConstraint = (BStructureType) ((TypedescValue) recordType).getDescribingType();
                    columnDefinitions = getColumnDefinitions(resultSet, streamConstraint);
                }
                return new StreamValue(new BStreamType(streamConstraint), Utils.createRecordIterator(resultSet,
                        statement, connection, columnDefinitions, streamConstraint));
            } catch (SQLException e) {
                closeResources(strand, resultSet, statement, connection);
                ErrorValue errorValue = ErrorGenerator.getSQLDatabaseError(e,
                        "Error while executing SQL query: " + sqlQuery + ". ");
                return new StreamValue(new BStreamType(getDefaultStreamConstraint()), createRecordIterator(errorValue));
            } catch (ApplicationError applicationError) {
                closeResources(strand, resultSet, statement, connection);
                ErrorValue errorValue = ErrorGenerator.getSQLApplicationError(applicationError.getMessage());
                return getErrorStream(recordType, errorValue);
            } catch (Throwable e) {
                closeResources(strand, resultSet, statement, connection);
                String message = e.getMessage();
                if (message == null) {
                    message = e.getClass().getName();
                }
                ErrorValue errorValue = ErrorGenerator.getSQLApplicationError(
                        "Error while executing SQL query: " + sqlQuery + ". " + message);
                return getErrorStream(recordType, errorValue);
            }
        } else {
            ErrorValue errorValue = ErrorGenerator.getSQLApplicationError("Client is not properly initialized!");
            return getErrorStream(recordType, errorValue);
        }
    }

    private static StreamValue getErrorStream(Object recordType, ErrorValue errorValue) {
        if (recordType == null) {
            return new StreamValue(new BStreamType(getDefaultStreamConstraint()), createRecordIterator(errorValue));
        } else {
            return new StreamValue(new BStreamType(((TypedescValue) recordType).getDescribingType()),
                    createRecordIterator(errorValue));
        }
    }

    private static ObjectValue createRecordIterator(ErrorValue errorValue) {
        return BallerinaValues.createObjectValue(Constants.SQL_PACKAGE_ID, Constants.RESULT_ITERATOR_OBJECT,
                errorValue);
    }

}
