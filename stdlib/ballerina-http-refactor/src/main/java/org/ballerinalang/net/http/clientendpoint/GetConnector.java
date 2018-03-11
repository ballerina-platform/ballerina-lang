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

package org.ballerinalang.net.http.clientendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;

import static org.ballerinalang.net.http.HttpConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.http.HttpConstants.HTTP_PACKAGE_PATH;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getConnector",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ClientEndpoint",
                             structPackage = "ballerina.net.http"),
        returnType = {@ReturnType(type = TypeKind.CONNECTOR)},
        isPublic = true
)
public class GetConnector extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct clientEndPoint  = (BStruct)getRefArgument(context, 0);
        BStruct clientEndpointConfig = (BStruct)clientEndPoint.getRefField(0);
        BConnector clientConnector = BLangConnectorSPIUtil.createBConnector(context.getProgramFile(), HTTP_PACKAGE_PATH,
                CLIENT_CONNECTOR, clientEndpointConfig.getStringField(0));
        clientConnector.setNativeData(HttpConstants.CLIENT_CONNECTOR, clientEndPoint.getNativeData
                (HttpConstants.CLIENT_CONNECTOR));
        return new BValue[]{clientConnector};
    }
}
