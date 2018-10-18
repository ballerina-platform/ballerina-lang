/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.socket.endpoint.tcp.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * Get the connection responder instance binds to the client socket endpoint.
 *
 * @since 0.983.0
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "getCallerActions",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Client", structPackage = SOCKET_PACKAGE),
        returnType = {@ReturnType(type = TypeKind.OBJECT, structType = "CallerAction", structPackage =
                SOCKET_PACKAGE)},
        isPublic = true
)
public class GetCallerActions extends BlockingNativeCallableUnit {
    
    @Override
    public void execute(Context context) {
        BMap<String, BValue> endpoint = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> connection = (BMap<String, BValue>) endpoint.get("callerAction");
        connection.addNativeData(SOCKET_KEY, endpoint.getNativeData(SOCKET_KEY));
        context.setReturnValues(connection);
    }
}
