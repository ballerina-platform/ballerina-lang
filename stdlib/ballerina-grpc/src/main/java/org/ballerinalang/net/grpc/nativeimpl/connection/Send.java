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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
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
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                structPackage = "ballerina.net.grpc"),
        isPublic = true
)
public class Send extends AbstractNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(Send.class);

    @Override
    public BValue[] execute(Context context) {
        log.info("calling send...");
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        BValue responseValue = getRefArgument(context, 1);
        StreamObserver<Message> responseObserver = MessageUtils.getStreamObserver(connectionStruct);
        Descriptors.Descriptor outputType = (Descriptors.Descriptor) connectionStruct.getNativeData(MessageConstants
                .RESPONSE_MESSAGE_DEFINITION);

        if (responseObserver == null) {
            return new BValue[0];
        }
        try {
            Message responseMessage = (Message) generateProtoMessage(responseValue, outputType);
            responseObserver.onNext(responseMessage);
            return new BValue[0];
        } catch (Throwable e) {
            log.error("Error while sending client response.", e);
            return getBValues(MessageUtils.getServerConnectorError(context, e));
        }
    }

    private com.google.protobuf.Message generateProtoMessage(BValue responseValue, Descriptors.Descriptor outputType) {
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
                    double value = 0F;
                    if (responseValue instanceof BStruct) {
                        value = ((BStruct) responseValue).getFloatField(floatIndex++);
                    } else {
                        if (responseValue instanceof BFloat) {
                            value = ((BFloat) responseValue).value();
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    float value = 0F;
                    if (responseValue instanceof BStruct) {
                        value = Float.parseFloat(String.valueOf(((BStruct) responseValue).getFloatField(floatIndex++)));
                    } else {
                        if (responseValue instanceof BFloat) {
                            value = Float.parseFloat(String.valueOf(((BFloat) responseValue).value()));
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    long value = 0;
                    if (responseValue instanceof BStruct) {
                        value = ((BStruct) responseValue).getIntField(intIndex++);
                    } else {
                        if (responseValue instanceof BInteger) {
                            value = ((BInteger) responseValue).value();
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    int value = 0;
                    if (responseValue instanceof BStruct) {
                        value = Integer.parseInt(String.valueOf(((BStruct) responseValue).getIntField(intIndex++)));
                    } else {
                        if (responseValue instanceof BInteger) {
                            value = Integer.parseInt(String.valueOf(((BInteger) responseValue).value()));
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    boolean value = false;
                    if (responseValue instanceof BStruct) {
                        value = ((BStruct) responseValue).getBooleanField(boolIndex++) > 0;
                    } else {
                        if (responseValue instanceof BBoolean) {
                            value = ((BBoolean) responseValue).value();
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    String value = null;
                    if (responseValue instanceof BStruct) {
                        value = ((BStruct) responseValue).getStringField(stringIndex++);
                    } else {
                        if (responseValue instanceof BString) {
                            value = ((BString) responseValue).value();
                        }
                    }
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
