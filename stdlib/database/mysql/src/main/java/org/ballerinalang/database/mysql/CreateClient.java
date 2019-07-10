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
package org.ballerinalang.database.mysql;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.database.sql.Constants;
import org.ballerinalang.database.sql.SQLDatasourceUtils;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.UUID;

/**
 * Returns the MySQL Client connector.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "mysql",
        functionName = "createClient",
        args = {@Argument(name = "config", type = TypeKind.RECORD, structType = "ClientEndpointConfiguration")},
        isPublic = true
)
public class CreateClient extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        /*BMap<String, BValue> clientEndpointConfig = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BRefType> globalPoolOptions = (BMap<String, BRefType>) context.getRefArgument(1);
        BMap<String, BRefType> dbOptions = (BMap<String, BRefType>) clientEndpointConfig
                .get(Constants.EndpointConfig.DB_OPTIONS);
        String urlOptions = "";
        if (dbOptions != null) {
            urlOptions =
                    SQLDatasourceUtils.createJDBCDbOptions(Constants.JDBCUrlSeparators.MYSQL_PROPERTY_BEGIN_SYMBOL,
                            Constants.JDBCUrlSeparators.MYSQL_SEPARATOR, dbOptions);
        }
        BMap<String, BValue> sqlClient = SQLDatasourceUtils
                .createServerBasedDBClient(context, Constants.DBTypes.MYSQL, clientEndpointConfig, urlOptions,
                        globalPoolOptions);
        sqlClient.addNativeData(Constants.CONNECTOR_ID_KEY, UUID.randomUUID().toString());
        context.setReturnValues(sqlClient);*/
    }

    public static ObjectValue createClient(Strand strand, MapValue<String, Object> config,
                                           MapValue<String, Object> globalPoolOptions) {
        MapValue<String, Object> dbOptions = (MapValue<String, Object>) config
                .getMapValue(Constants.EndpointConfig.DB_OPTIONS);
        String urlOptions = "";
        if (dbOptions != null) {
            urlOptions =
                    SQLDatasourceUtils.createJDBCDbOptions(Constants.JDBCUrlSeparators.MYSQL_PROPERTY_BEGIN_SYMBOL,
                            Constants.JDBCUrlSeparators.MYSQL_SEPARATOR, dbOptions);
        }
        ObjectValue sqlClient = SQLDatasourceUtils
                .createServerBasedDBClient(Constants.DBTypes.MYSQL, config, urlOptions, globalPoolOptions);
        sqlClient.addNativeData(Constants.CONNECTOR_ID_KEY, UUID.randomUUID().toString());
        return sqlClient;
    }
}
