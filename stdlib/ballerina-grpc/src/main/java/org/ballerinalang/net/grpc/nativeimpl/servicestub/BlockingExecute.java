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
package org.ballerinalang.net.grpc.nativeimpl.servicestub;

import com.google.protobuf.Descriptors;
import io.grpc.MethodDescriptor;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.GrpcBlockingStub;

import static org.ballerinalang.net.grpc.MessageConstants.CONNECTOR_ERROR;
import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.SERVICE_STUB;
import static org.ballerinalang.net.grpc.MessageConstants.SERVICE_STUB_REF_INDEX;

/**
 * {@code BlockingExecute} is the BlockingExecute action implementation of the gRPC Connector.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "blockingExecute",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = SERVICE_STUB,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {
                @Argument(name = "methodID", type = TypeKind.STRING),
                @Argument(name = "payload", type = TypeKind.ANY)

        },
        returnType = {
                @ReturnType(type = TypeKind.ANY),
                @ReturnType(type = TypeKind.STRUCT, structType = CONNECTOR_ERROR,
                        structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        },
        isPublic = true
)
public class BlockingExecute extends AbstractExecute {

    @Override
    public void execute(Context context) {
        BStruct serviceStub = (BStruct) context.getRefArgument(SERVICE_STUB_REF_INDEX);
        if (serviceStub == null) {
            notifyErrorReply(context, "Error while getting connector. gRPC service stub " +
                    "is not initialized properly");
            return;
        }

        Object connectionStub = serviceStub.getNativeData(SERVICE_STUB);
        if (connectionStub == null) {
            notifyErrorReply(context, "Error while getting connection stub. gRPC Client " +
                    "connector is not initialized properly");
        }
        String methodName = context.getStringArgument(0);
        if (methodName == null) {
            notifyErrorReply(context, "Error while processing the request. RPC endpoint " +
                    "doesn't set properly");
            return;
        }
        com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = MessageRegistry.getInstance()
                .getMethodDescriptor(methodName);
        if (methodDescriptor == null) {
            notifyErrorReply(context, "No registered method descriptor for '" + methodName + "'");
            return;
        }
        if (connectionStub instanceof GrpcBlockingStub) {
            BValue payloadBValue = context.getRefArgument(1);
            Message requestMsg = MessageUtils.generateProtoMessage(payloadBValue, methodDescriptor.getInputType());
            GrpcBlockingStub grpcBlockingStub = (GrpcBlockingStub) connectionStub;
            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                //TODO : check whether we can support blocking server streaming.
                if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                    Message responseMsg = grpcBlockingStub.executeUnary(requestMsg, methodName);
                    Descriptors.Descriptor outputDescriptor = methodDescriptor.getOutputType();
                    BValue responseBValue = MessageUtils.generateRequestStruct(responseMsg, context.getProgramFile(),
                            outputDescriptor.getName(), getBalType(outputDescriptor.getName(), context));
                    context.setReturnValues(responseBValue);
                    return;
                } else {
                    notifyErrorReply(context, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                }
            } catch (RuntimeException | GrpcClientException e) {
                notifyErrorReply(context, "gRPC Client Connector Error :" + e.getMessage());
            }
        }
        notifyErrorReply(context, "Error while processing the request message. Connection Sub " +
                "type not supported");
    }
}
