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

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.ballerinalang.bre.bvm.BLangVMErrors.PACKAGE_BUILTIN;
import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageHeaders.METADATA_KEY;

/**
 * Util methods to generate protobuf message.
 *
 * @since 1.0.0
 */
public class MessageUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MessageUtils.class);
    private static final String UNKNOWN_ERROR = "Unknown Error";

    public static BStruct getHeaderStruct(Resource resource) {
        if (resource == null || resource.getParamDetails() == null) {
            throw new RuntimeException("Invalid resource input arguments");
        }
        BStruct headerStruct = null;
        for (ParamDetail detail : resource.getParamDetails()) {
            BType paramType = detail.getVarType();
            if (paramType != null && PROTOCOL_STRUCT_PACKAGE_GRPC.equals(paramType.getPackagePath()) &&
                    "Headers".equals(paramType.getName())) {
                headerStruct = BLangConnectorSPIUtil.createBStruct(getProgramFile(resource),
                        paramType.getPackagePath(), paramType.getName());
                break;
            }
        }
        return headerStruct;
    }

    public static io.grpc.Context getContextHeader(BRefValueArray headerValues) {

        // Set response headers.
        if (headerValues.size() != 0) {
            if (headerValues.size() > 1) {
                LOG.warn("No of Header objects found in input params:" + headerValues.size() + " , gRPC service can " +
                        "only execute the first header object");
            }
            BStruct headerValue = (BStruct) headerValues.getBValue(0);
            MessageHeaders metadata = (MessageHeaders) headerValue.getNativeData(METADATA_KEY);
            if (metadata != null) {
                return io.grpc.Context.current().withValue(MessageHeaders.DATA_KEY, metadata);
            }
        }
        return null;
    }



    public static MessageHeaders getMessageHeaders(BRefValueArray headerValues) {

        // Set request headers.
        MessageHeaders metadata = null;
        if (headerValues.size() != 0) {
            if (headerValues.size() > 1) {
                LOG.warn("No of Header objects found in input params:" + headerValues.size() + " , gRPC service can " +
                        "only execute the first header object");
            }
            BStruct headerValue = (BStruct) headerValues.getBValue(0);
            metadata = (MessageHeaders) headerValue.getNativeData(METADATA_KEY);
        }
        return metadata;
    }

    public static StreamObserver<Message> getResponseObserver(BRefType refType) {
        Object observerObject = null;
        if (refType instanceof BStruct) {
            observerObject = ((BStruct) refType).getNativeData(GrpcConstants.RESPONSE_OBSERVER);
        }
        if (observerObject instanceof StreamObserver) {
            return ((StreamObserver<Message>) observerObject);
        }
        return null;
    }
    
    public static BStruct getConnectorError(Context context, Throwable throwable) {
        ProgramFile progFile = context.getProgramFile();
        PackageInfo errorPackageInfo = progFile.getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return getConnectorError(errorStructInfo.getType(), throwable);
    }
    
    /**
     * Returns error struct of input type
     * Error type is generic ballerina error type. This utility method is used inside Observer onError
     * method to construct error struct from message.
     *
     * @param errorType this is ballerina generic error type.
     * @param error     this is StatusRuntimeException send by opposite party.
     * @return error struct.
     */
    public static BStruct getConnectorError(BStructType errorType, Throwable error) {
        BStruct errorStruct = new BStruct(errorType);
        if (error instanceof StatusRuntimeException) {
            StatusRuntimeException statusException = (StatusRuntimeException) error;
            String status = statusException.getStatus() != null ? statusException.getStatus().toString() : "";
            String message = status + statusException.getMessage();
            errorStruct.setStringField(0, message);
        } else {
            if (error.getMessage() == null) {
                errorStruct.setStringField(0, UNKNOWN_ERROR);
            } else {
                errorStruct.setStringField(0, error.getMessage());
            }
        }
        return errorStruct;
    }
    
    public static ProgramFile getProgramFile(Resource resource) {
        return resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
    }
    
    /**
     * Handles failures in GRPC callable unit callback.
     *
     * @param streamObserver observer used the send the error back
     * @param error          error message struct
     */
    static void handleFailure(StreamObserver<Message> streamObserver, BStruct error) {
        String errorMsg = error.getStringField(0);
        LOG.error(errorMsg);
        ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
        if (streamObserver != null) {
            streamObserver.onError(new StatusRuntimeException(Status.fromCodeValue(Status.Code.INTERNAL.value())
                    .withDescription(errorMsg)));
        }
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
        Integer wireType = GrpcConstants.WIRE_TYPE_MAP.get(fieldType.toProto());
        if (wireType != null) {
            return wireType;
        } else {
            // Returns embedded messages, packed repeated fields message type, if field type doesn't map with the
            // predefined proto types.
            return ServiceProtoConstants.MESSAGE_WIRE_TYPE;
        }
    }

    public static void setNestedMessages(Descriptors.Descriptor resMessage, MessageRegistry messageRegistry) {

        for (Descriptors.Descriptor nestedType : resMessage.getNestedTypes()) {
            messageRegistry.addMessageDescriptor(nestedType.getName(), nestedType);
        }
        for (Descriptors.FieldDescriptor msgField : resMessage.getFields()) {
            if (Descriptors.FieldDescriptor.Type.MESSAGE.equals(msgField.getType())) {
                Descriptors.Descriptor msgType = msgField.getMessageType();
                messageRegistry.addMessageDescriptor(msgType.getName(), msgType);
            }
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
    
    /**
     * Returns protobuf message corresponding to the B7a message.
     *
     * @param responseValue B7a message.
     * @param outputType    protobuf message type.
     * @return generated protobuf message.
     */
    public static Message generateProtoMessage(BValue responseValue, Descriptors.Descriptor outputType) {
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
                    double value = 0D;
                    if (responseValue instanceof BStruct) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BStruct) responseValue).getRefField(refIndex++);
                            BFloatArray valueArray = (BFloatArray) bValue;
                            Double[] messages = new Double[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                double indexValue = valueArray.get(i);
                                messages[i] = indexValue;
                            }
                            responseBuilder.addField(fieldName, messages);
                        } else {
                            value = ((BStruct) responseValue).getFloatField(floatIndex++);
                            responseBuilder.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BFloat) {
                            value = ((BFloat) responseValue).value();
                        }
                        responseBuilder.addField(fieldName, value);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    float value = 0F;
                    if (responseValue instanceof BStruct) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BStruct) responseValue).getRefField(refIndex++);
                            BFloatArray valueArray = (BFloatArray) bValue;
                            Float[] messages = new Float[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                float indexValue = Float.parseFloat(String.valueOf(valueArray.get(i)));
                                messages[i] = indexValue;
                            }
                            responseBuilder.addField(fieldName, messages);
                        } else {
                            value = Float.parseFloat(String.valueOf(((BStruct) responseValue).getFloatField
                                    (floatIndex++)));
                            responseBuilder.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BFloat) {
                            value = Float.parseFloat(String.valueOf(((BFloat) responseValue).value()));
                        }
                        responseBuilder.addField(fieldName, value);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    long value = 0;
                    if (responseValue instanceof BStruct) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BStruct) responseValue).getRefField(refIndex++);
                            BIntArray valueArray = (BIntArray) bValue;
                            Long[] messages = new Long[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                long indexValue = valueArray.get(i);
                                messages[i] = indexValue;
                            }
                            responseBuilder.addField(fieldName, messages);
                        } else {
                            value = ((BStruct) responseValue).getIntField(intIndex++);
                            responseBuilder.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BInteger) {
                            value = ((BInteger) responseValue).value();
                            responseBuilder.addField(fieldName, value);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    int value = 0;
                    if (responseValue instanceof BStruct) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BStruct) responseValue).getRefField(refIndex++);
                            BIntArray valueArray = (BIntArray) bValue;
                            Integer[] messages = new Integer[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                int indexValue = Integer.parseInt(String.valueOf(valueArray.get(i)));
                                messages[i] = indexValue;
                            }
                            responseBuilder.addField(fieldName, messages);
                        } else {
                            value = Integer.parseInt(String.valueOf(((BStruct) responseValue).getIntField(intIndex++)));
                            responseBuilder.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BInteger) {
                            value = Integer.parseInt(String.valueOf(((BInteger) responseValue).value()));
                        }
                        responseBuilder.addField(fieldName, value);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    boolean value = false;
                    if (responseValue instanceof BStruct) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BStruct) responseValue).getRefField(refIndex++);
                            BBooleanArray valueArray = (BBooleanArray) bValue;
                            Boolean[] messages = new Boolean[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                int indexValue = valueArray.get(i);
                                messages[i] = indexValue != 0;
                            }
                            responseBuilder.addField(fieldName, messages);
                        } else {
                            value = ((BStruct) responseValue).getBooleanField(boolIndex++) > 0;
                            responseBuilder.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BBoolean) {
                            value = ((BBoolean) responseValue).value();
                            responseBuilder.addField(fieldName, value);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    String value = null;
                    if (responseValue instanceof BStruct) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BStruct) responseValue).getRefField(refIndex++);
                            BStringArray valueArray = (BStringArray) bValue;
                            String[] messages = new String[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                String indexValue = valueArray.get(i);
                                messages[i] = indexValue;
                            }
                            responseBuilder.addField(fieldName, messages);
                        } else {
                            value = ((BStruct) responseValue).getStringField(stringIndex++);
                            responseBuilder.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BString) {
                            value = ((BString) responseValue).value();
                        }
                        responseBuilder.addField(fieldName, value);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                    if (responseValue instanceof BStruct) {
                        BValue bValue = ((BStruct) responseValue).getRefField(refIndex++);
                        responseBuilder.addField(fieldName, fieldDescriptor.getEnumType().findValueByName(bValue
                                .stringValue()));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                    if (responseValue instanceof BStruct) {
                        BValue bValue = ((BStruct) responseValue).getRefField(refIndex++);
                        if (fieldDescriptor.isRepeated() && (bValue instanceof BRefValueArray)) {
                            BRefValueArray valueArray = (BRefValueArray) bValue;
                            Message[] messages = new Message[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                BValue value = valueArray.get(i);
                                messages[i] = generateProtoMessage(value, fieldDescriptor.getMessageType());
                            }
                            responseBuilder.addField(fieldName, messages);
                        } else {
                            responseBuilder.addField(fieldName, generateProtoMessage(bValue, fieldDescriptor
                                    .getMessageType()));
                        }
                    }
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

    public static BValue generateRequestStruct(Message request, ProgramFile programFile, String fieldName, BType
            structType) {

        BValue bValue = null;
        Map<String, Object> fields = request.getFields();
        if (fields.size() == 1 && fields.containsKey("value")) {
            fieldName = "value";
        }

        if (TypeKind.STRING.typeName().equals(structType.getName())) {
            bValue = new BString((String) fields.get(fieldName));
        } else if (TypeKind.INT.typeName().equals(structType.getName())) {
            bValue = new BInteger((Long) fields.get(fieldName));
        } else if (TypeKind.FLOAT.typeName().equals(structType.getName())) {
            Float value = (Float) request.getFields().get(fieldName);
            if (value != null) {
                bValue = new BFloat(Double.parseDouble(value.toString()));
            }
        } else if (TypeKind.BOOLEAN.typeName().equals(structType.getName())) {
            bValue = new BBoolean((Boolean) fields.get(fieldName));
        } else if (structType instanceof BStructType) {
            BStruct requestStruct = BLangConnectorSPIUtil.createBStruct(programFile,
                    structType.getPackagePath(), structType.getName());
            int stringIndex = 0;
            int intIndex = 0;
            int floatIndex = 0;
            int boolIndex = 0;
            int refIndex = 0;
            for (BStructType.StructField structField : ((BStructType) structType).getStructFields()) {
                String structFieldName = structField.getFieldName();
                BType structFieldType = structField.getFieldType();
                if (TypeKind.STRING.typeName().equals(structFieldType.getName())) {
                    BString bStringValue = (BString) generateRequestStruct(request, programFile, structFieldName,
                            structFieldType);
                    requestStruct.setStringField(stringIndex++, bStringValue.stringValue());
                } else if (TypeKind.INT.typeName().equals(structFieldType.getName())) {
                    BInteger bIntegerValue = (BInteger) generateRequestStruct(request, programFile, structFieldName,
                            structFieldType);
                    requestStruct.setIntField(intIndex++, bIntegerValue.value());
                } else if (TypeKind.FLOAT.typeName().equals(structFieldType.getName())) {
                    BFloat bFloatValue = (BFloat) generateRequestStruct(request, programFile, structFieldName,
                            structFieldType);
                    requestStruct.setFloatField(floatIndex++, bFloatValue.value());
                } else if (TypeKind.BOOLEAN.typeName().equals(structFieldType.getName())) {
                    BBoolean bBooleanValue = (BBoolean) generateRequestStruct(request, programFile, structFieldName,
                            structFieldType);
                    requestStruct.setBooleanField(boolIndex++, bBooleanValue.value() ? 1 : 0);
                } else if (structFieldType instanceof BStructType) {
                    if (MessageRegistry.getInstance().getMessageDescriptorMap().containsKey(
                            structFieldType.getName())) {
                        Message message = (Message) fields.get(structFieldName);
                        requestStruct.setRefField(refIndex++, (BRefType) generateRequestStruct(message, programFile,
                                structFieldName, structField.getFieldType()));
                    }
                } else if (structFieldType instanceof BArrayType) {
                    long arrayIndex = 0;
                    BArrayType fieldArrayType = (BArrayType) structFieldType;
                    BType elementType = fieldArrayType.getElementType();
                    if (!(fields.get(structFieldName) instanceof List)) {
                        throw new RuntimeException("Error while creating message struct. message value should be an " +
                                "instance of List");
                    }
                    if (TypeKind.STRING.typeName().equals(elementType.getName())) {
                        List<String> messages = (List<String>) fields.get(structFieldName);
                        BStringArray bArrayValue = new BStringArray();
                        for (String stringValue : messages) {
                            bArrayValue.add(arrayIndex++, stringValue);
                        }
                        requestStruct.setRefField(refIndex++, bArrayValue);
                    } else if (TypeKind.INT.typeName().equals(elementType.getName())) {
                        List<Long> messages = (List<Long>) fields.get(structFieldName);
                        BIntArray bArrayValue = new BIntArray();
                        for (Long integerValue : messages) {
                            bArrayValue.add(arrayIndex++, integerValue);
                        }
                        requestStruct.setRefField(refIndex++, bArrayValue);
                    } else if (TypeKind.FLOAT.typeName().equals(elementType.getName())) {
                        List<Float> messages = (List<Float>) fields.get(structFieldName);
                        BFloatArray bArrayValue = new BFloatArray();
                        for (Float floatValue : messages) {
                            bArrayValue.add(arrayIndex++, Double.parseDouble(floatValue.toString()));
                        }
                        requestStruct.setRefField(refIndex++, bArrayValue);
                    } else if (TypeKind.BOOLEAN.typeName().equals(elementType.getName())) {
                        List<Boolean> messages = (List<Boolean>) fields.get(structFieldName);
                        BBooleanArray bArrayValue = new BBooleanArray();
                        for (Boolean booleanValue : messages) {
                            bArrayValue.add(arrayIndex++, booleanValue ? 1 : 0);
                        }
                        requestStruct.setRefField(refIndex++, bArrayValue);
                    } else if (elementType instanceof BStructType) {
                        List<Message> messages = (List<Message>) fields.get(structFieldName);
                        BRefValueArray bArrayValue = new BRefValueArray(elementType);
                        for (Message message : messages) {
                            bArrayValue.add(arrayIndex++, (BRefType) generateRequestStruct(message, programFile,
                                    structFieldName, elementType));
                        }
                        requestStruct.setRefField(refIndex++, bArrayValue);
                    }
                }
            }
            bValue = requestStruct;
        }

        return bValue;
    }
    
    /**
     * Util method to get method type.
     *
     * @param methodDescriptorProto method descriptor proto.
     * @return service method type.
     */
    public static MethodDescriptor.MethodType getMethodType(DescriptorProtos.MethodDescriptorProto
                                                                    methodDescriptorProto) {
        if (methodDescriptorProto.getClientStreaming() && methodDescriptorProto.getServerStreaming()) {
            return MethodDescriptor.MethodType.BIDI_STREAMING;
        } else if (!(methodDescriptorProto.getClientStreaming() || methodDescriptorProto.getServerStreaming())) {
            return MethodDescriptor.MethodType.UNARY;
        } else if (methodDescriptorProto.getServerStreaming()) {
            return MethodDescriptor.MethodType.SERVER_STREAMING;
        } else if (methodDescriptorProto.getClientStreaming()) {
            return MethodDescriptor.MethodType.CLIENT_STREAMING;
        } else {
            return MethodDescriptor.MethodType.UNKNOWN;
        }
    }
    
    /**
     * Checks whether method has response message.
     *
     * @param messageDescriptor Message Descriptor
     * @return true if method response is empty, false otherwise
     */
    public static boolean isEmptyResponse(Descriptors.Descriptor messageDescriptor) {
        if (messageDescriptor == null) {
            return false;
        }
        List<Descriptors.Descriptor> descriptors = com.google.protobuf.EmptyProto.getDescriptor()
                .getMessageTypes();
        for (Descriptors.Descriptor descriptor : descriptors) {
            if (descriptor.equals(messageDescriptor)) {
                return true;
            }
        }
        return false;
    }
}
