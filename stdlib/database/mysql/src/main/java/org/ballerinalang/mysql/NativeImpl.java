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
package org.ballerinalang.mysql;

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.sql.datasource.SQLDatasource;
import org.ballerinalang.sql.utils.ClientUtils;

/**
 * Native implementation for the mysql client methods.
 *
 * @since 1.2.0
 */
public class NativeImpl {

    public static Object createClient(ObjectValue client, MapValue<String, Object> clientConfig,
                                      MapValue<String, Object> globalPool) {
        String url = "jdbc:mysql://" + clientConfig.getStringValue(Constants.ClientConfiguration.HOST);
        Long portValue = clientConfig.getIntValue(Constants.ClientConfiguration.PORT);
        if (portValue > 0) {
            url += ":" + portValue.intValue();
        }
        String user = clientConfig.getStringValue(Constants.ClientConfiguration.USER);
        String password = clientConfig.getStringValue(Constants.ClientConfiguration.PASSWORD);
        String database = clientConfig.getStringValue(Constants.ClientConfiguration.DATABASE);
        if (database != null && !database.isEmpty()) {
            url += "/" + database;
        }
        MapValue options = clientConfig.getMapValue(Constants.ClientConfiguration.OPTIONS);
        MapValue properties = Utils.generateOptionsMap(options);

        MapValue connectionPool = clientConfig.getMapValue(Constants.ClientConfiguration.CONNECTION_POOL_OPTIONS);
        if (connectionPool == null) {
            connectionPool = globalPool;
        }

        String datasourceName = Constants.MYSQL_DATASOURCE_NAME;
        if (options != null && options.getBooleanValue(Constants.Options.USE_XA_DATASOURCE)) {
            datasourceName = Constants.MYSQL_XA_DATASOURCE_NAME;
        }

        SQLDatasource.SQLDatasourceParams sqlDatasourceParams = new SQLDatasource.SQLDatasourceParams().
                setUrl(url).setUser(user).setPassword(password).setDatasourceName(datasourceName).
                setOptions(properties).setConnectionPool(connectionPool);
        return ClientUtils.createClient(client, sqlDatasourceParams);
    }

    public static Object close(ObjectValue client) {
        return ClientUtils.close(client);
    }
}
