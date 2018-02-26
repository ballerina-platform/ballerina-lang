/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.connection.client;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageUtils;

/**
 * Native function to check whether caller has terminated the connection in between.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = MessageConstants.PROTOCOL_PACKAGE_GRPC,
        functionName = "isCancelled",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = MessageConstants.CLIENT_CONNECTION,
                structPackage = MessageConstants.PROTOCOL_PACKAGE_GRPC),
        returnType = @ReturnType(type = TypeKind.BOOLEAN),
        isPublic = true
)
public class IsCancelled extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        StreamObserver responseObserver = MessageUtils.getStreamObserver(connectionStruct);
        if (responseObserver == null) {
            return new BValue[0];
        }
        if (responseObserver instanceof ServerCallStreamObserver) {
            ServerCallStreamObserver serverCallStreamObserver = (ServerCallStreamObserver) responseObserver;
            return new BValue[] {new BBoolean(serverCallStreamObserver.isCancelled())};
        } else {
            return new BValue[0];
        }
    }
}
