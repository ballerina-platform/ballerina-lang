/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.jdbc.nativeimpl;

import org.ballerinalang.jdbc.Constants;
import org.ballerinalang.jdbc.datasource.SQLDatasource;
import org.ballerinalang.jdbc.datasource.SQLDatasourceUtils;
import org.ballerinalang.jdbc.exceptions.ErrorGenerator;
import org.ballerinalang.jdbc.statement.SQLStatement;
import org.ballerinalang.jdbc.statement.SelectStatementStream;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will include the native methods for the JDBC client.
 */
public class Utils {

    public static Object close(ObjectValue client) {
        SQLDatasource datasource = (SQLDatasource) client.getNativeData(Constants.JDBC_CLIENT);
        // When an exception is thrown during database endpoint init (eg: driver not present) stop operation
        // of the endpoint is automatically called. But at this point, datasource is null therefore to handle that
        // situation following null check is needed.
        if (datasource != null && !datasource.isGlobalDatasource()) {
            datasource.decrementClientCounterAndAttemptPoolShutdown();
        }
        return null;
    }

    public static void createClient(ObjectValue client, MapValue<String, Object> clientConfig,
                                    MapValue<String, Object> globalPool) {
        String url = clientConfig.getStringValue(Constants.ClientConfiguration.URL);
        if (!isJdbcUrlValid(url)) {
            throw ErrorGenerator.getSQLApplicationError("invalid JDBC URL: " + url);
        }
        String username = clientConfig.getStringValue(Constants.ClientConfiguration.USER);
        String password = clientConfig.getStringValue(Constants.ClientConfiguration.PASSWORD);
        MapValue<String, Object> options = (MapValue<String, Object>) clientConfig
                .getMapValue(Constants.ClientConfiguration.OPTIONS);
        MapValue<String, Object> connectionPool = (MapValue<String, Object>) clientConfig
                .getMapValue(Constants.ClientConfiguration.CONNECTION_POOL_OPTIONS);
        String driver = clientConfig.getStringValue(Constants.ClientConfiguration.DRIVER);
        boolean useGlobalPool = connectionPool == null;
        if (useGlobalPool) {
            connectionPool = globalPool;
        }
        SQLDatasource.SQLDatasourceParams sqlDatasourceParams = new SQLDatasource.SQLDatasourceParams().
                setJdbcUrl(url).setUsername(username).setPassword(password).
                setGlobalDatasource(useGlobalPool).setOptions(options).
                setConnectionPool(connectionPool).setDriver(driver);

        SQLDatasource sqlDatasource = SQLDatasource.retrieveDatasource(sqlDatasourceParams, connectionPool);
        client.addNativeData(Constants.JDBC_CLIENT, sqlDatasource);
        client.addNativeData(Constants.CONNECTOR_ID_KEY, UUID.randomUUID().toString());
    }

    // Unable to perform a complete validation since URL differs based on the database.
    private static boolean isJdbcUrlValid(String jdbcUrl) {
        return !jdbcUrl.isEmpty() && jdbcUrl.trim().startsWith("jdbc:");
    }

    public static void initGlobalPoolContainer(ObjectValue globalPoolConfigContainer,
                                               MapValue<String, Object> poolConfig) {
        SQLDatasourceUtils.putDatasourceContainer(poolConfig, new ConcurrentHashMap<>());
    }

    public static StreamValue nativeQuery(ObjectValue client, String query,
                                          Object parameters, Object recordType) {
        SQLStatement selectStatement = new SelectStatementStream(Scheduler.getStrand());
        Object result = selectStatement.execute();
        if (result instanceof StreamValue) {
            return (StreamValue) result;
        }
        return null;
    }
}
