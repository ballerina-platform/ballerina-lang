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
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.database.sql.Constants;
import org.ballerinalang.database.sql.SQLDatasourceUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.Map;

/**
 * Returns the MySQL Client connector.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "mysql",
        functionName = "createClient",
        args = {@Argument(name = "config", type = TypeKind.STRUCT, structType = "ClientEndpointConfiguration")},
        isPublic = true
)
public class CreateClient extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct configBStruct = (BStruct) context.getRefArgument(0);
        Struct clientEndpointConfig = BLangConnectorSPIUtil.toStruct(configBStruct);
        Map<String, Value> dbOptions = clientEndpointConfig.getMapField(Constants.EndpointConfig.DB_OPTIONS);
        String urlOptions = "";
        if (dbOptions != null) {
            urlOptions = SQLDatasourceUtils.createJDBCDbOptions(Constants.JDBCUrlSeparators.MYSQL_PROPERTY_BEGIN_SYMBOL,
                    Constants.JDBCUrlSeparators.MYSQL_SEPARATOR, dbOptions);
        }
        BStruct sqlClient = SQLDatasourceUtils
                .createServerBasedDBClient(context, Constants.DBTypes.MYSQL, clientEndpointConfig, urlOptions);
        context.setReturnValues(sqlClient);
    }
}
