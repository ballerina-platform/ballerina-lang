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

import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.datasource.SQLDatasource;

import java.util.Properties;
import java.util.UUID;

/**
 * This class implements the utils methods for the clients to be used.
 *
 * @since 1.2.0
 */
public class ClientUtils {

    private ClientUtils() {
    }

    public static Object createClient(ObjectValue client, SQLDatasource.SQLDatasourceParams sqlDatasourceParams) {
        try {
            SQLDatasource sqlDatasource = SQLDatasource.retrieveDatasource(sqlDatasourceParams);
            client.addNativeData(Constants.DATABASE_CLIENT, sqlDatasource);
            client.addNativeData(Constants.SQL_CONNECTOR_TRANSACTION_ID, UUID.randomUUID().toString());
            return null;
        } catch (ErrorValue errorValue) {
            return errorValue;
        }
    }

    public static Object createSqlClient(ObjectValue client, MapValue<BString, Object> sqlDatasourceParams,
                                         MapValue<BString, Object> globalConnectionPool) {
        return createClient(client, createSQLDatasourceParams(sqlDatasourceParams, globalConnectionPool));
    }

    public static Object close(ObjectValue client) {
        Object datasourceObj = client.getNativeData(Constants.DATABASE_CLIENT);
        // When an exception is thrown during database endpoint init (eg: driver not present) stop operation
        // of the endpoint is automatically called. But at this point, datasource is null therefore to handle that
        // situation following null check is needed.
        if (datasourceObj != null) {
            ((SQLDatasource) datasourceObj).decrementClientCounterAndAttemptPoolShutdown();
        }
        return null;
    }

    private static SQLDatasource.SQLDatasourceParams createSQLDatasourceParams
            (MapValue<BString, Object> sqlDatasourceParams, MapValue<BString, Object> globalConnectionPool) {
        MapValue<BString, Object> connPoolProps = (MapValue<BString, Object>) sqlDatasourceParams
                .getMapValue(Constants.SQLParamsFields.CONNECTION_POOL_OPTIONS);
        Properties poolProperties = null;
        if (connPoolProps != null) {
            poolProperties = new Properties();
            for (BString key : connPoolProps.getKeys()) {
                poolProperties.setProperty(key.getValue(), connPoolProps.getStringValue(key).getValue());
            }
        }
        BString userVal = sqlDatasourceParams.getStringValue(Constants.SQLParamsFields.USER);
        String user = userVal == null ? null : userVal.getValue();
        BString passwordVal = sqlDatasourceParams.getStringValue(Constants.SQLParamsFields.PASSWORD);
        String password = passwordVal == null ? null : passwordVal.getValue();
        BString dataSourceNamVal = sqlDatasourceParams.getStringValue(Constants.SQLParamsFields.DATASOURCE_NAME);
        String datasourceName = dataSourceNamVal == null ? null : dataSourceNamVal.getValue();
        return new SQLDatasource.SQLDatasourceParams()
                .setUrl(sqlDatasourceParams.getStringValue(Constants.SQLParamsFields.URL).getValue())
                .setUser(user)
                .setPassword(password)
                .setDatasourceName(datasourceName)
                .setOptions(sqlDatasourceParams.getMapValue(Constants.SQLParamsFields.OPTIONS))
                .setConnectionPool(sqlDatasourceParams.getMapValue(Constants.SQLParamsFields.CONNECTION_POOL),
                                   globalConnectionPool)
                .setPoolProperties(poolProperties);
    }

}
