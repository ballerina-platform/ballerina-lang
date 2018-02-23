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

package org.ballerinalang.nativeimpl.socket;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.channels.SocketChannel;

/**
 * Native function to check the client connectivity.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "isConnected",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Socket", structPackage = "ballerina.io"),
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class IsConnected extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct socket = (BStruct) getRefArgument(context, 0);
        SocketChannel socketChannel = (SocketChannel) socket.getNativeData(IOConstants.CLIENT_SOCKET_NAME);
        return getBValues(new BBoolean(socketChannel.isConnected()));
    }
}
