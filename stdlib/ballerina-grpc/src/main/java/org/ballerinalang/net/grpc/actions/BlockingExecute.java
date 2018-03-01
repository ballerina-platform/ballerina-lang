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
package org.ballerinalang.net.grpc.actions;

import com.google.protobuf.Descriptors;
import io.grpc.MethodDescriptor;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.GrpcBlockingStub;
import org.ballerinalang.net.grpc.utils.MessageUtil;

/**
 * {@code BlockingExecute} is the BlockingExecute action implementation of the gRPC Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.grpc",
        actionName = "blockingExecute",
        connectorName = "GRPCConnector",
        args = {
                @Argument(name = "methodID", type = TypeKind.STRING),
                @Argument(name = "payload", type = TypeKind.ANY)

        },
        returnType = {
                @ReturnType(type = TypeKind.ANY),
                @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                        structPackage = "ballerina.net.grpc"),
        },
        connectorArgs = {
                @Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT)
        }
)
public class BlockingExecute extends AbstractExecute {

    @Override
    public ConnectorFuture execute(Context context) {
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        if (bConnector == null) {
            return notifyErrorReply(context, "Error while getting connector. gRPC Client connector is not " +
                    "initialized properly");
        }

        Object connectionStub = bConnector.getnativeData("stub");
        if (connectionStub == null) {
            return notifyErrorReply(context, "Error while getting connection stub. gRPC Client connector is not " +
                    "initialized properly");
        }
        String methodName = getStringArgument(context, 0);
        if (methodName == null) {
            return notifyErrorReply(context, "Error while processing the request. RPC endpoint doesn't set properly");
        }
        com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = MessageRegistry.getInstance()
                .getMethodDescriptor(methodName);

        if (connectionStub instanceof GrpcBlockingStub) {
            BValue payloadBValue = getRefArgument(context, 1);
            Message requestMsg = MessageUtil.generateProtoMessage(payloadBValue, methodDescriptor.getInputType());
            GrpcBlockingStub grpcBlockingStub = (GrpcBlockingStub) connectionStub;
            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                //TODO : check whether we can support blocking server streaming.
                if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                    Message responseMsg = grpcBlockingStub.executeUnary(requestMsg, methodName);
                    Descriptors.Descriptor outputDescriptor = methodDescriptor.getOutputType();
                    BValue responseBValue = MessageUtil.generateRequestStruct(responseMsg, outputDescriptor.getName(),
                            getBalType(outputDescriptor.getName(), context), context);
                    return notifyReply(responseBValue);
                } else {
                    return notifyErrorReply(context, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                }
            } catch (RuntimeException | GrpcClientException e) {
                return notifyErrorReply(context, "gRPC Client Connector Error :" + e.getMessage());
            }
        }
        return notifyErrorReply(context, "Error while processing the request message. Connection Sub type not " +
                "supported");
    }
}
