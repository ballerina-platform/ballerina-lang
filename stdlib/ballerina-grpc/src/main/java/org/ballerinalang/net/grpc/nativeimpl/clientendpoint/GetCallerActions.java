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
package org.ballerinalang.net.grpc.nativeimpl.clientendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.net.grpc.MessageConstants.CLIENT_CONNECTION;
import static org.ballerinalang.net.grpc.MessageConstants.CLIENT_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;

/**
 * Get the client connection instance binds to the client endpoint.
 *
 * @since 1.0.0
 */

@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "getCallerActions",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = CLIENT_ENDPOINT_TYPE,
                             structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        returnType = {@ReturnType(type = TypeKind.CONNECTOR)},
        isPublic = true
)
public class GetCallerActions extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct clientEndpoint = (BStruct) context.getRefArgument(0);
        BStruct clientConnection = (BStruct) clientEndpoint.getNativeData(CLIENT_CONNECTION);
        context.setReturnValues(clientConnection);
    }
}
