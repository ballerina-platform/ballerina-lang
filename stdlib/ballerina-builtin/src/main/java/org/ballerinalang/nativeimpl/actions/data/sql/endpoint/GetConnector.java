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

package org.ballerinalang.nativeimpl.actions.data.sql.endpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.data.sql.Constants;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Returns the SQL Client connector.
 *
 * @since 0.965
 */

@BallerinaFunction(packageName = "ballerina.data.sql",
                   functionName = "getConnector",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "Client",
                                        structPackage = "ballerina.data.sql"),
                   returnType = { @ReturnType(type = TypeKind.CONNECTOR) },
                   isPublic = true)
public class GetConnector extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct clientEndPoint = (BStruct) context.getRefArgument(0);
        BStruct clientEndpointConfig = (BStruct) clientEndPoint.getRefField(0);
        BConnector clientConnector = BLangConnectorSPIUtil
                .createBConnector(context.getProgramFile(), Constants.SQL_PACKAGE_PATH, Constants.CLIENT_CONNECTOR,
                        clientEndpointConfig.getStringField(0));
        clientConnector
                .setNativeData(Constants.CLIENT_CONNECTOR, clientEndPoint.getNativeData(Constants.CLIENT_CONNECTOR));
        context.setReturnValues(clientConnector);
    }
}
