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
package org.ballerinax.jdbc;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.UUID;

/**
 * Returns the JDBC Client connector.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerinax", packageName = "jdbc",
        functionName = "createClient",
        args = {@Argument(name = "config", type = TypeKind.RECORD, structType = "ClientEndpointConfig"),
        @Argument(name = "globalPoolOptions", type = TypeKind.RECORD, structType = "PoolOptions")},
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
        if (!dbOptions.isEmpty()) {
            urlOptions = SQLDatasourceUtils.createJDBCDbOptions(Constants.JDBCUrlSeparators.H2_PROPERTY_BEGIN_SYMBOL,
                    Constants.JDBCUrlSeparators.H2_SEPARATOR, dbOptions);
        }
        BMap<String, BValue> sqlClient = SQLDatasourceUtils
                .createMultiModeDBClient(context, Constants.DBTypes.H2, clientEndpointConfig, urlOptions,
                        globalPoolOptions);
        sqlClient.addNativeData(Constants.CONNECTOR_ID_KEY, UUID.randomUUID().toString());
        context.setReturnValues(sqlClient);*/
    }

    public static ObjectValue createClient(Strand strand, Object config, MapValue<String, Object> globalPoolOptions) {
        ObjectValue jdbcClient = SQLDatasourceUtils
                .createSQLDBClient((MapValue<String, Object>) config, globalPoolOptions);
        jdbcClient.addNativeData(Constants.CONNECTOR_ID_KEY, UUID.randomUUID().toString());
        return jdbcClient;
    }
}
