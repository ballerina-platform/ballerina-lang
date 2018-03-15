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

import com.google.protobuf.Descriptors;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function to respond the caller.
 *
 * @since 0.96.1
 */
@BallerinaAction(
        packageName = "ballerina.net.grpc",
        actionName = "send",
        connectorName = "ServerConnector",
        args = {
                @Argument(name = "response", type = TypeKind.ANY)
            
        },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                        structPackage = "ballerina.net.grpc")
        }
)
public class Send extends BlockingNativeCallableUnit {
    private static final Logger LOG = LoggerFactory.getLogger(Send.class);
    
    @Override
    public void execute(Context context) {
        BConnector bConnector = (BConnector) context.getRefArgument(0);
        BValue responseValue = context.getRefArgument(1);
        StreamObserver<Message> responseObserver = MessageUtils.getStreamObserver(bConnector);
        Descriptors.Descriptor outputType = (Descriptors.Descriptor) bConnector.getNativeData(MessageConstants
                .RESPONSE_MESSAGE_DEFINITION);
        
        if (responseObserver == null) {
            return;
        }
        try {
            Message responseMessage = MessageUtils.generateProtoMessage(responseValue, outputType);
            responseObserver.onNext(responseMessage);
        } catch (Throwable e) {
            LOG.error("Error while sending client response.", e);
            context.setError(MessageUtils.getConnectorError(context, e));
        }
    }
}
