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
package org.ballerinalang.net.grpc;

import com.google.protobuf.Descriptors;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Base64;
import java.util.List;


/**
 * Message Utils.
 */
public class MessageUtils {
    private static final String IO_EXCEPTION_OCCURED = "I/O exception occurred";

    public static BValue[] getHeader(Context context, AbstractNativeFunction abstractNativeFunction) {
        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = getHeaderValue(headerName);

        return abstractNativeFunction.getBValues(new BString(headerValue));
    }

    private static String getHeaderValue(String keyName) {
        String headerValue = null;
        if (MessageContext.isPresent()) {
            MessageContext messageContext = MessageContext.DATA_KEY.get();
            if (keyName.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                Metadata.Key<byte[]> key = Metadata.Key.of(keyName, Metadata.BINARY_BYTE_MARSHALLER);
                byte[] byteValues = messageContext.get(key);
                // Referred : https://stackoverflow
                // .com/questions/1536054/how-to-convert-byte-array-to-string-and-vice-versa
                // https://stackoverflow.com/questions/2418485/how-do-i-convert-a-byte-array-to-base64-in-java
                headerValue = byteValues != null ? Base64.getEncoder().encodeToString(byteValues) : null;
            } else {
                Metadata.Key<String> key = Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER);
                headerValue =  messageContext.get(key);
            }
        }
        return headerValue;
    }

    public static StreamObserver<Message> getStreamObserver(BStruct struct) {
        Object observerObject = struct.getNativeData(MessageConstants.STREAM_OBSERVER);
        if (observerObject instanceof StreamObserver) {
            return ((StreamObserver<Message>) observerObject);
        }
        return null;
    }

    public static BStruct getServerConnectorError(Context context, Throwable throwable) {
        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo(MessageConstants.PROTOCOL_PACKAGE_GRPC);
        StructInfo errorStructInfo = httpPackageInfo.getStructInfo(MessageConstants.HTTP2_CONNECTOR_ERROR);
        BStruct httpConnectorError = new BStruct(errorStructInfo.getType());
        if (throwable.getMessage() == null) {
            httpConnectorError.setStringField(0, IO_EXCEPTION_OCCURED);
        } else {
            httpConnectorError.setStringField(0, throwable.getMessage());
        }
        return httpConnectorError;
    }

    /**
     * Returns wire type corresponding to the field descriptor type.
     * <p>
     * 0 -> int32, int64, uint32, uint64, sint32, sint64, bool, enum
     * 1 -> fixed64, sfixed64, double
     * 2 -> string, bytes, embedded messages, packed repeated fields
     * 5 -> fixed32, sfixed32, float
     *
     * @param fieldType field descriptor type
     * @return wire type
     */
    static int getFieldWireType(Descriptors.FieldDescriptor.Type fieldType) {
        if (fieldType == null) {
            return ServiceProtoConstants.INVALID_WIRE_TYPE;
        }
        Integer wireType = MessageConstants.WIRE_TYPE_MAP.get(fieldType.toProto());
        if (wireType != null) {
            return wireType;
        } else {
            // Returns embedded messages, packed repeated fields message type, if field type doesn't map with the
            // predefined proto types.
            return ServiceProtoConstants.MESSAGE_WIRE_TYPE;
        }
    }

    /**
     * Check whether message object is an array.
     *
     * @param object message object
     * @return true if object is array, false otherwise.
     */
    static boolean isArray(Object object) {
        return object != null && object.getClass().isArray();
    }

    public static Annotation getServiceConfigAnnotation(Service service, String pkgPath) {
        List<Annotation> annotationList = service.getAnnotationList(pkgPath, MessageConstants.ANN_NAME_CONFIG);

        if (annotationList == null) {
            return null;
        }

        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple service configuration annotations found in service: " + service.getName());
        }

        return annotationList.isEmpty() ? null : annotationList.get(0);
    }

    public static Annotation getMessageListenerAnnotation(Service service, String pkgPath) {
        List<Annotation> annotationList = service.getAnnotationList(pkgPath, MessageConstants.ANN_MESSAGE_LISTENER);

        if (annotationList == null) {
            return null;
        }

        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple service configuration annotations found in service: " + service.getName());
        }

        return annotationList.isEmpty() ? null : annotationList.get(0);
    }
/*    public static com.google.protobuf.Message generateProtoMessage(BStruct bValue, BStructType structType) {
        Message.Builder responseBuilder = Message.newBuilder(structType.getName());
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;
        for (BStructType.StructField structField : structType.getStructFields()) {
            String fieldName = structField.getFieldName();
            if (structField.getFieldType() instanceof BArrayType) {
                BType bType = ((BArrayType) structField.getFieldType()).getElementType();
                String fieldType = bType.getName();
                switch (fieldType) {
                    case "string": {
                        BStringArray valueArray = (BStringArray) bValue.getRefField(stringIndex++);
                        int arraySize = (int) valueArray.size();
                        String[] values = new String[arraySize];
                        for (int i = 0; i < arraySize; i++) {
                            values[i] = String.valueOf(valueArray.get(i));
                        }
                        responseBuilder.addField(fieldName, values);
                        break;
                    }
                    case "int": {
                        BIntArray valueArray = (BIntArray) bValue.getRefField(stringIndex++);
                        int arraySize = (int) valueArray.size();
                        int[] values = new int[arraySize];
                        for (int i = 0; i < arraySize; i++) {
                            values[i] = Integer.parseInt(String.valueOf(valueArray.get(i)));
                        }
                        responseBuilder.addField(fieldName, values);
                        break;
                    }
                    case "float": {
                        BFloatArray valueArray = (BFloatArray) bValue.getRefField(stringIndex++);
                        int arraySize = (int) valueArray.size();
                        float[] values = new float[arraySize];
                        for (int i = 0; i < arraySize; i++) {
                            values[i] = Float.parseFloat(String.valueOf(valueArray.get(i)));
                        }
                        responseBuilder.addField(fieldName, values);
                        break;
                    }
                    case "boolean": {
                        BBooleanArray valueArray = (BBooleanArray) bValue.getRefField(stringIndex++);
                        int arraySize = (int) valueArray.size();
                        boolean[] values = new boolean[arraySize];
                        for (int i = 0; i < arraySize; i++) {
                            values[i] = Boolean.parseBoolean(String.valueOf(valueArray.get(i)));
                        }
                        responseBuilder.addField(fieldName, values);
                        break;
                    }
                    default: {
                        BRefValueArray valueArray = (BRefValueArray) bValue.getRefField(stringIndex++);
                        int arraySize = (int) valueArray.size();
                        if (MessageRegistry.getInstance().getMessageDescriptorMap().containsKey(bType.getName())) {
                            Object[] values = new Object[arraySize];
                            for (int i = 0; i < arraySize; i++) {
                                values[i] = generateProtoMessage((BStruct) valueArray.get(i).value(), (BStructType)
                                        bType);
                            }
                            responseBuilder.addField(fieldName, values);
                        }
                        break;
                    }
                }
            } else if (structField.getFieldType() instanceof BRefType) {
                BType bType = structField.getFieldType();
                if (MessageRegistry.getInstance().getMessageDescriptorMap().containsKey(bType.getName())) {
                    BStruct value = (BStruct) bValue.getRefField(refIndex++).value();
                    responseBuilder.addField(fieldName, generateProtoMessage(value,
                            (BStructType) structField.getFieldType()));
                }
            } else {
                String fieldType = structField.getFieldType().getName();
                switch (fieldType) {
                    case "string": {
                        String value = bValue.getStringField(stringIndex++);
                        responseBuilder.addField(fieldName, value);
                        break;
                    }
                    case "int": {
                        long value = bValue.getIntField(intIndex++);
                        responseBuilder.addField(fieldName, value);
                        break;
                    }
                    case "float": {
                        float value = Float.parseFloat(String.valueOf(bValue.getFloatField(floatIndex++)));
                        responseBuilder.addField(fieldName, value);
                        break;
                    }
                    case "boolean": {
                        int value = bValue.getBooleanField(boolIndex++);
                        responseBuilder.addField(fieldName, value != 0);
                        break;
                    }
                    default: {
                        throw new UnsupportedFieldTypeException("Error while generating response struct. Field " +
                                "type is not supported : " + fieldType);
                    }
                }
            }
        }
        return responseBuilder.build();
    }

    public static com.google.protobuf.Message generateProtoMessage(BStruct bValue, Descriptors.Descriptor outputType) {
        Message.Builder responseBuilder = Message.newBuilder(outputType.getName());
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;
        for (Descriptors.FieldDescriptor fieldDescriptor : outputType.getFields()) {
            Descriptors.FieldDescriptor.Type fieldType = fieldDescriptor.getType();
            String fieldName = fieldDescriptor.getName();
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    double value = bValue.getFloatField(floatIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    float value = Float.parseFloat(String.valueOf(bValue.getFloatField(floatIndex++)));
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE: {
                    long value = bValue.getIntField(intIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE: {
                    long value = bValue.getIntField(intIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE: {
                    long value = bValue.getIntField(intIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    long value = bValue.getIntField(intIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    long value = bValue.getIntField(intIndex++);
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    int value = bValue.getBooleanField(boolIndex++);
                    responseBuilder.addField(fieldName, value != 0);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    String value = bValue.getStringField(stringIndex++);
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
    }*/
}
