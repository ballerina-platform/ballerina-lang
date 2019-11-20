/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinax.jdbc.functions;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.PoolKey;
import org.ballerinax.jdbc.datasource.PoolOptionsWrapper;
import org.ballerinax.jdbc.datasource.SQLDatasource;
import org.ballerinax.jdbc.exceptions.ErrorGenerator;

import java.util.Locale;
import java.util.UUID;

/**
 * Returns the JDBC Client.
 *
 * @since 0.970
 */

@BallerinaFunction(orgName = "ballerinax",
                   packageName = "java.jdbc",
                   functionName = "createClient",
                   isPublic = true)
public class CreateClient {

    public static void createClient(Strand strand, ObjectValue client, MapValue<String, Object> config,
            MapValue<String, Object> globalPoolOptions) {
        String url = config.getStringValue(Constants.EndpointConfig.URL);

        if (!isJdbcUrlValid(url)) {
            throw ErrorGenerator.getSQLApplicationError("invalid JDBC URL: " + url);
        }

        String username = config.getStringValue(Constants.EndpointConfig.USERNAME);
        String password = config.getStringValue(Constants.EndpointConfig.PASSWORD);
        MapValue<String, Object> dbOptions = (MapValue<String, Object>) config
                .getMapValue(Constants.EndpointConfig.DB_OPTIONS);
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

    private CreateClient() {

    }
}
