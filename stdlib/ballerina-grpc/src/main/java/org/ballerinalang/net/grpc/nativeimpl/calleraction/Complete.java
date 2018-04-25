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

import com.google.protobuf.Descriptors;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.bre.bvm.BLangVMErrors.PACKAGE_BUILTIN;
import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.net.grpc.GrpcConstants.CALLER_ACTION;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_RESPONDER_REF_INDEX;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageUtils.getContextHeader;

/**
 * Native function to inform the caller, server finished sending messages.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "complete",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = CALLER_ACTION,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {
                @Argument(name = "headers", type = TypeKind.STRUCT, structType = "Headers",
                        structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC)
        },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = STRUCT_GENERIC_ERROR, structPackage = PACKAGE_BUILTIN)
        },
        isPublic = true
)
public class Complete extends BlockingNativeCallableUnit {
    private static final Logger LOG = LoggerFactory.getLogger(Complete.class);
    private static final int MESSAGE_HEADER_REF_INDEX = 1;

    @Override
    public void execute(Context context) {
        BStruct endpointClient = (BStruct) context.getRefArgument(CLIENT_RESPONDER_REF_INDEX);
        BValue headerValue = context.getNullableRefArgument(MESSAGE_HEADER_REF_INDEX);
        StreamObserver responseObserver = MessageUtils.getResponseObserver(endpointClient);
        Descriptors.Descriptor outputType = (Descriptors.Descriptor) endpointClient.getNativeData(GrpcConstants
                .RESPONSE_MESSAGE_DEFINITION);
        io.grpc.Context msgContext = getContextHeader(headerValue);

        if (responseObserver == null) {
            context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                    .fromCode(Status.INTERNAL.getCode()).withDescription("Error while initializing connector. " +
                            "response sender does not exist"))));
        } else {
            io.grpc.Context previous = msgContext != null ? msgContext.attach() : null;
            try {
                if (!MessageUtils.isEmptyResponse(outputType)) {
                    responseObserver.onCompleted();
                }
            } catch (Throwable e) {
                LOG.error("Error while sending client response.", e);
                context.setError(MessageUtils.getConnectorError(context, e));
            } finally {
                if (previous != null) {
                    msgContext.detach(previous);
                }
            }
        }
    }
}
