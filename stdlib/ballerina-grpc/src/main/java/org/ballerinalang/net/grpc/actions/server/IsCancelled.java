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
package org.ballerinalang.net.grpc.actions.server;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.MessageUtils;

/**
 * Native function to check whether caller has terminated the connection in between.
 *
 * @since 0.96.1
 */
@BallerinaAction(
        packageName = "ballerina.net.grpc",
        actionName = "isCancelled",
        connectorName = "ServerConnector",
        returnType = {
                @ReturnType(type = TypeKind.BOOLEAN)
        }
)
public class IsCancelled extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BConnector bConnector = (BConnector) context.getRefArgument(0);
        StreamObserver responseObserver = MessageUtils.getStreamObserver(bConnector);
        if (responseObserver == null) {
            return;
        }
        if (responseObserver instanceof ServerCallStreamObserver) {
            ServerCallStreamObserver serverCallStreamObserver = (ServerCallStreamObserver) responseObserver;
            context.setReturnValues(new BBoolean(serverCallStreamObserver.isCancelled()));
        }
    }
}
