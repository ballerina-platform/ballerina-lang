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

package org.ballerinalang.nativeimpl.sql.endpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.sql.Constants;
import org.ballerinalang.nativeimpl.sql.SQLDatasource;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Returns the SQL Client connector.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "sql",
        functionName = "createSQLClient",
        args = {@Argument(name = "config", type = TypeKind.STRUCT, structType = "ClientEndpointConfiguration")},
        isPublic = true
)
public class CreateSQLClient extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct configBStruct = (BStruct) context.getRefArgument(0);
        Struct clientEndpointConfig = BLangConnectorSPIUtil.toStruct(configBStruct);

        //Extract parameters from the endpoint config
        String database = clientEndpointConfig.getRefField(Constants.EndpointConfig.DATABASE).getStringValue();
        String host = clientEndpointConfig.getStringField(Constants.EndpointConfig.HOST);
        int port = (int) clientEndpointConfig.getIntField(Constants.EndpointConfig.PORT);
        String name = clientEndpointConfig.getStringField(Constants.EndpointConfig.NAME);
        String username = clientEndpointConfig.getStringField(Constants.EndpointConfig.USERNAME);
        String password = clientEndpointConfig.getStringField(Constants.EndpointConfig.PASSWORD);
        Struct options = clientEndpointConfig.getStructField(Constants.EndpointConfig.OPTIONS);

        SQLDatasource datasource = new SQLDatasource();
        datasource.init(options, database, host, port, username, password, name);

        BStruct sqlClient = BLangConnectorSPIUtil.createBStruct(context.getProgramFile(), Constants.SQL_PACKAGE_PATH,
                Constants.SQL_CLIENT);
        sqlClient.addNativeData(Constants.SQL_CLIENT, datasource);
        context.setReturnValues(sqlClient);
    }
}
