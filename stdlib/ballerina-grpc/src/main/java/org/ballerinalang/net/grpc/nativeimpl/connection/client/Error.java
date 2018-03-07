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

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function to send server error the caller.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = MessageConstants.PROTOCOL_PACKAGE_GRPC,
        functionName = "error",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = MessageConstants.CLIENT_CONNECTION,
                structPackage = MessageConstants.PROTOCOL_PACKAGE_GRPC),
        args = {@Argument(name = "serverError", type = TypeKind.STRUCT, structType = "ServerError",
                structPackage = MessageConstants.PROTOCOL_PACKAGE_GRPC)},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                structPackage = MessageConstants.PROTOCOL_PACKAGE_GRPC),
        isPublic = true
)
public class Error extends AbstractNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(Error.class);

    @Override
    public BValue[] execute(Context context) {
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        BValue responseValue = getRefArgument(context, 1);
        if (responseValue instanceof BStruct) {
            BStruct responseStruct = (BStruct) responseValue;
            int statusCode = Integer.parseInt(String.valueOf(responseStruct.getIntField(0)));
            String errorMsg = responseStruct.getStringField(0);
            StreamObserver responseObserver = MessageUtils.getStreamObserver(connectionStruct);
            if (responseObserver == null) {
                return getBValues(MessageUtils.getServerConnectorError(context, new StatusRuntimeException(Status
                        .fromCode(Status.INTERNAL.getCode()).withDescription("Error while sending the error. Response" +
                                " observer not found."))));
            }
            responseObserver.onError(new StatusRuntimeException(Status.fromCodeValue(statusCode).withDescription
                    (errorMsg)));
        }
        return new BValue[0];
    }
}
