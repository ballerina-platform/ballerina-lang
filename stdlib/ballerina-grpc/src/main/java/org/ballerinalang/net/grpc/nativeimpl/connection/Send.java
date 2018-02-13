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

package org.ballerinalang.net.grpc.nativeimpl.connection;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
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
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function to respond the caller.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "send",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection",
                structPackage = "ballerina.net.grpc"),
        args = {@Argument(name = "response", type = TypeKind.STRING)},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "Http2ConnectorError",
                structPackage = "ballerina.net.grpc"),
        isPublic = true
)
public class Send extends AbstractNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(Send.class);
    @Override
    public BValue[] execute(Context context) {
        log.info("calling send...");
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        StreamObserver<Object> responseObserver = MessageUtils.getStreamObserver(connectionStruct);
        Descriptors.Descriptor outputType = (Descriptors.Descriptor) connectionStruct.getNativeData(MessageConstants
                .RESPONSE_MESSAGE_DEFINITION);

        if (responseObserver == null) {
            return new BValue[0];
        }
        try {
            com.google.protobuf.Message responseMessage = generateProtoMessage(context, outputType);
            responseObserver.onNext(responseMessage);
        } finally {
            responseObserver.onCompleted();
        }

        return new BValue[0];
    }

    public com.google.protobuf.Message generateProtoMessage(Context context, Descriptors.Descriptor outputType) {
        Message.Builder responseBuilder = Message.newBuilder(outputType.getName());
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;
        for (Descriptors.FieldDescriptor fieldDescriptor : outputType.getFields()) {
            String fieldName = fieldDescriptor.getName();
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    double value = getFloatArgument(context, floatIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    float value = Float.parseFloat(String.valueOf(getFloatArgument(context, floatIndex++)));
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    long value = getIntArgument(context, intIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    int value = Integer.parseInt(String.valueOf(getIntArgument(context, intIndex++)));
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    boolean value = getBooleanArgument(context, boolIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    String value = getStringArgument(context, stringIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                default: {
                    throw new UnsupportedFieldTypeException("Error while decoding request message. Field " +
                            "type is not supported : " + fieldDescriptor.getType());
                }
            }
        }
        return responseBuilder.build();
    }
}
