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

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.PoolKey;
import org.ballerinax.jdbc.datasource.PoolOptionsWrapper;
import org.ballerinax.jdbc.datasource.SQLDatasource;
import org.ballerinax.jdbc.datasource.SQLDatasourceUtils;
import org.ballerinax.jdbc.exceptions.ErrorGenerator;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * External function implementations of the JDBC client.
 *
 * @since 1.1.0
 */
public class ExternFunctions {

    private ExternFunctions() {}

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

    public static void createClient(ObjectValue client, MapValue<String, Object> config,
                                    MapValue<String, Object> globalPoolOptions) {
        String url = config.getStringValue(Constants.EndpointConfig.URL);

        if (!isJdbcUrlValid(url)) {
            throw ErrorGenerator.getSQLApplicationError("invalid JDBC URL: " + url);
        }

        String username = config.getStringValue(Constants.EndpointConfig.USERNAME);
        String password = config.getStringValue(Constants.EndpointConfig.PASSWORD);
        @SuppressWarnings("unchecked")
        MapValue<String, Object> dbOptions = (MapValue<String, Object>) config
                .getMapValue(Constants.EndpointConfig.DB_OPTIONS);
        @SuppressWarnings("unchecked")
        MapValue<String, Object> poolOptions = (MapValue<String, Object>) config
                .getMapValue(Constants.EndpointConfig.POOL_OPTIONS);
        boolean userProvidedPoolOptionsNotPresent = poolOptions == null;
        if (userProvidedPoolOptionsNotPresent) {
            poolOptions = globalPoolOptions;
        }
        PoolOptionsWrapper poolOptionsWrapper = new PoolOptionsWrapper(poolOptions, new PoolKey(url, dbOptions));
        String dbType = url.split(":")[1].toUpperCase(Locale.getDefault());

        SQLDatasource.SQLDatasourceParamsBuilder builder = new SQLDatasource.SQLDatasourceParamsBuilder(dbType);
        SQLDatasource.SQLDatasourceParams sqlDatasourceParams = builder.withPoolOptions(poolOptionsWrapper)
                .withJdbcUrl(url).withUsername(username).withPassword(password).withDbOptionsMap(dbOptions)
                .withIsGlobalDatasource(userProvidedPoolOptionsNotPresent).build();

        SQLDatasource sqlDatasource = sqlDatasourceParams.getPoolOptionsWrapper()
                .retrieveDatasource(sqlDatasourceParams);
        client.addNativeData(Constants.JDBC_CLIENT, sqlDatasource);
        client.addNativeData(Constants.CONNECTOR_ID_KEY, UUID.randomUUID().toString());
    }

    // Unable to perform a complete validation since URL differs based on the database.
    private static boolean isJdbcUrlValid(String jdbcUrl) {
        boolean isJdbcUrlEmpty = jdbcUrl.isEmpty();
        String[] splitComponents = jdbcUrl.split(":");
        return !isJdbcUrlEmpty && splitComponents.length > 2 && "jdbc"
                .equals(splitComponents[0].toLowerCase(Locale.getDefault()));
    }

    public static void initGlobalPoolContainer(ObjectValue globalPoolConfigContainer,
                                               MapValue<String, Object> poolConfig) {
        SQLDatasourceUtils.addDatasourceContainer(poolConfig, new ConcurrentHashMap<>());
    }
}
