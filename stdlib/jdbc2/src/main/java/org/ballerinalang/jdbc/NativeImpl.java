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
package org.ballerinalang.jdbc;

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.sql.datasource.SQLDatasource;
import org.ballerinalang.sql.exceptions.ErrorGenerator;
import org.ballerinalang.sql.utils.ClientUtils;

/**
 * This class will include the native method implementation for the JDBC client.
 *
 * @since 1.2.0
 */
public class NativeImpl {

    public static Object close(ObjectValue client) {
        return ClientUtils.close(client);
    }

    public static Object createClient(ObjectValue client, MapValue<String, Object> clientConfig,
                                      MapValue<String, Object> globalPool) {
        String url = clientConfig.getStringValue(Constants.ClientConfiguration.URL);
        if (!isJdbcUrlValid(url)) {
            return ErrorGenerator.getSQLApplicationError("invalid JDBC URL: " + url);
        }
        String user = clientConfig.getStringValue(Constants.ClientConfiguration.USER);
        String password = clientConfig.getStringValue(Constants.ClientConfiguration.PASSWORD);
        MapValue<String, Object> options = (MapValue<String, Object>) clientConfig
                .getMapValue(Constants.ClientConfiguration.OPTIONS);
        MapValue<String, Object> properties = null;
        String datasourceName = null;
        if (options != null) {
            properties = (MapValue<String, Object>) options.
                    getMapValue(Constants.ClientConfiguration.PROPERTIES);
            datasourceName = options.getStringValue(Constants.ClientConfiguration.DATASOURCE_NAME);
        }
        MapValue<String, Object> connectionPool = (MapValue<String, Object>) clientConfig
                .getMapValue(Constants.ClientConfiguration.CONNECTION_POOL_OPTIONS);
        if (connectionPool == null) {
            connectionPool = globalPool;
        }
        SQLDatasource.SQLDatasourceParams sqlDatasourceParams = new SQLDatasource.SQLDatasourceParams().
                setUrl(url).setUser(user).setPassword(password).setDatasourceName(datasourceName).
                setOptions(properties).setConnectionPool(connectionPool);
        return ClientUtils.createClient(client, sqlDatasourceParams);
    }

    // Unable to perform a complete validation since URL differs based on the database.
    private static boolean isJdbcUrlValid(String jdbcUrl) {
        return !jdbcUrl.isEmpty() && jdbcUrl.trim().startsWith("jdbc:");
    }
}
