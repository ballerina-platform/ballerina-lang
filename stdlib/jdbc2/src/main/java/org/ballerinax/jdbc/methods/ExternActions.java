/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinax.jdbc.methods;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.SQLDatasource;
import org.ballerinax.jdbc.statement.BatchUpdateStatement;
import org.ballerinax.jdbc.statement.CallStatement;
import org.ballerinax.jdbc.statement.SQLStatement;
import org.ballerinax.jdbc.statement.SelectStatement;
import org.ballerinax.jdbc.statement.SelectStatementStream;
import org.ballerinax.jdbc.statement.UpdateStatement;

/**
 * External remote method implementations of the JDBC client.
 *
 * @since 1.1.0
 */
public class ExternActions {

    private ExternActions() {
    }

    public static MapValue<String, Object> nativeBatchUpdate(ObjectValue client, String sqlQuery,
                                                             boolean rollbackAllInFailure, ArrayValue... parameters) {
        SQLDatasource datasource = (SQLDatasource) client.getNativeData(Constants.JDBC_CLIENT);
        SQLStatement batchUpdateStatement = new BatchUpdateStatement(client, datasource, sqlQuery,
                rollbackAllInFailure, Scheduler.getStrand(), parameters);
        return (MapValue<String, Object>) batchUpdateStatement.execute();
    }

    public static Object nativeCall(ObjectValue client, String sqlQuery, Object recordType,
                                    ArrayValue parameters) {
        SQLDatasource datasource = (SQLDatasource) client.getNativeData(Constants.JDBC_CLIENT);
        SQLStatement callStatement = new CallStatement(client, datasource, sqlQuery, (ArrayValue) recordType,
                parameters, Scheduler.getStrand());
        return callStatement.execute();
    }

    public static Object nativeSelect(ObjectValue client, String query, Object recordType,
                                      ArrayValue parameters) {
        SQLDatasource sqlDatasource = (SQLDatasource) client.getNativeData(Constants.JDBC_CLIENT);
        SQLStatement selectStatement = new SelectStatement(client, sqlDatasource, query, parameters,
                (TypedescValue) recordType, Scheduler.getStrand());
        return selectStatement.execute();
    }

    public static Object nativeSelectStream(ObjectValue client, String query,
                                            ArrayValue parameters) {
        SQLDatasource sqlDatasource = (SQLDatasource) client.getNativeData(Constants.JDBC_CLIENT);
        SQLStatement selectStatement = new SelectStatementStream(client, sqlDatasource, query, parameters,
                Scheduler.getStrand());
        return selectStatement.execute();
    }

    public static Object nativeUpdate(ObjectValue client, String query, ArrayValue parameters) {
        SQLDatasource sqlDatasource = (SQLDatasource) client.getNativeData(Constants.JDBC_CLIENT);
        SQLStatement updateStatement = new UpdateStatement(client, sqlDatasource, query, parameters,
                Scheduler.getStrand());
        return updateStatement.execute();
    }
}
