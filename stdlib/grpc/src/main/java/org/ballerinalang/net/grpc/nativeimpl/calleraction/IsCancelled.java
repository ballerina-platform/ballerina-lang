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
package org.ballerinalang.net.grpc.nativeimpl.calleraction;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.listener.ServerCallHandler;

import static org.ballerinalang.net.grpc.GrpcConstants.CALLER;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;

/**
 * Extern function to check whether caller has terminated the connection in between.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "isCancelled",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CALLER,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class IsCancelled {

    public static boolean isCancelled(Strand strand, ObjectValue endpointClient) {
        StreamObserver responseObserver = MessageUtils.getResponseObserver(endpointClient);

        if (responseObserver instanceof ServerCallHandler.ServerCallStreamObserver) {
            ServerCallHandler.ServerCallStreamObserver serverCallStreamObserver = (ServerCallHandler
                    .ServerCallStreamObserver) responseObserver;
            return serverCallStreamObserver.isCancelled();
        } else {
            return Boolean.FALSE;
        }
    }
}
