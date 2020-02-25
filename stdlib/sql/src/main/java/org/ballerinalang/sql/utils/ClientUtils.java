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

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.datasource.SQLDatasource;

import java.util.UUID;

/**
 * This class implements the utils methods for the clients to be used.
 */
public class ClientUtils {

    private ClientUtils() {
    }

    public static void createClient(ObjectValue client, SQLDatasource.SQLDatasourceParams sqlDatasourceParams) {
        SQLDatasource sqlDatasource = SQLDatasource.retrieveDatasource(sqlDatasourceParams);
        client.addNativeData(Constants.DATABASE_CLIENT, sqlDatasource);
        client.addNativeData(Constants.CONNECTOR_ID_KEY, UUID.randomUUID().toString());
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
}
