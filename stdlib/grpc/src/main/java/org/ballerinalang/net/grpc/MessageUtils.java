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
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BFiniteType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.ballerinalang.bre.bvm.BLangVMErrors.ERROR_MESSAGE_FIELD;
import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.net.grpc.GrpcConstants.CONTENT_TYPE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * Util methods to generate protobuf message.
 *
 * @since 1.0.0
 */
public class MessageUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MessageUtils.class);
    private static final String UNKNOWN_ERROR = "Unknown Error";

    /** maximum buffer to be read is 16 KB. */
    private static final int MAX_BUFFER_LENGTH = 16384;

    public static BMap<String, BValue> getHeaderStruct(Resource resource) {
        if (resource == null || resource.getParamDetails() == null) {
            throw new RuntimeException("Invalid resource input arguments");
        }
        BMap<String, BValue> headerStruct = null;
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

    public static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[MAX_BUFFER_LENGTH];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    public static StreamObserver getResponseObserver(BRefType refType) {
        Object observerObject = null;
        if (refType instanceof BMap) {
            observerObject = ((BMap<String, BValue>) refType).getNativeData(GrpcConstants.RESPONSE_OBSERVER);
        }
        if (observerObject instanceof StreamObserver) {
            return ((StreamObserver) observerObject);
        }
        return null;
    }
    
    public static BMap<String, BValue> getConnectorError(Context context, Throwable throwable) {
        ProgramFile progFile = context.getProgramFile();
        PackageInfo errorPackageInfo = progFile.getPackageInfo(BALLERINA_BUILTIN_PKG);
        StructureTypeInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return getConnectorError(errorStructInfo.getType(), throwable);
    }
    
    /**
     * Returns error struct of input type
     * Error type is generic ballerina error type. This utility method is used inside Observer onError
     * method to construct error struct from message.
     *
     * @param errorType this is ballerina generic error type.
     * @param error     this is StatusRuntimeException send by opposite party.
     * @return error value.
     */
    public static BMap<String, BValue> getConnectorError(BStructureType errorType, Throwable error) {
        BMap<String, BValue> errorStruct = new BMap<>(errorType);
        if (error instanceof StatusRuntimeException) {
            StatusRuntimeException statusException = (StatusRuntimeException) error;
            errorStruct.put(ERROR_MESSAGE_FIELD, new BString(statusException.getStatus().toString()));
        } else {
            if (error.getMessage() == null) {
                errorStruct.put(ERROR_MESSAGE_FIELD, new BString(UNKNOWN_ERROR));
            } else {
                errorStruct.put(ERROR_MESSAGE_FIELD, new BString(error.getMessage()));
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
    static void handleFailure(StreamObserver streamObserver, BMap<String, BValue> error) {
        String errorMsg = error.stringValue();
        LOG.error(errorMsg);
        ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
        if (streamObserver != null) {
            streamObserver.onError(new Message(new StatusRuntimeException(Status.fromCodeValue(Status
                    .Code.INTERNAL.value()).withDescription(errorMsg))));
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

    static void setNestedMessages(Descriptors.Descriptor resMessage, MessageRegistry messageRegistry) {
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
        Message responseMessage = new Message(outputType.getName());
        for (Descriptors.FieldDescriptor fieldDescriptor : outputType.getFields()) {
            String fieldName = fieldDescriptor.getName();
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    double value = 0D;
                    if (responseValue instanceof BMap) {
                        BMap<String, BValue> response = (BMap<String, BValue>) responseValue;
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = response.get(fieldName);
                            BFloatArray valueArray = (BFloatArray) bValue;
                            Double[] messages = new Double[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                double indexValue = valueArray.get(i);
                                messages[i] = indexValue;
                            }
                            responseMessage.addField(fieldName, messages);
                        } else {
                            value = ((BFloat) response.get(fieldName)).floatValue();
                            responseMessage.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BFloat) {
                            value = ((BFloat) responseValue).value();
                        }
                        responseMessage.addField(fieldName, value);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    float value = 0F;
                    if (responseValue instanceof BMap) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                            BFloatArray valueArray = (BFloatArray) bValue;
                            Float[] messages = new Float[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                float indexValue = Float.parseFloat(String.valueOf(valueArray.get(i)));
                                messages[i] = indexValue;
                            }
                            responseMessage.addField(fieldName, messages);
                        } else {
                            BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                            value = Float.parseFloat(String.valueOf(bValue));
                            responseMessage.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BFloat) {
                            value = Float.parseFloat(String.valueOf(((BFloat) responseValue).value()));
                        }
                        responseMessage.addField(fieldName, value);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    long value = 0;
                    if (responseValue instanceof BMap) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                            BIntArray valueArray = (BIntArray) bValue;
                            Long[] messages = new Long[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                long indexValue = valueArray.get(i);
                                messages[i] = indexValue;
                            }
                            responseMessage.addField(fieldName, messages);
                        } else {
                            BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                            value = ((BInteger) bValue).intValue();
                            responseMessage.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BInteger) {
                            value = ((BInteger) responseValue).value();
                            responseMessage.addField(fieldName, value);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    int value = 0;
                    if (responseValue instanceof BMap) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                            BIntArray valueArray = (BIntArray) bValue;
                            Integer[] messages = new Integer[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                int indexValue = Integer.parseInt(String.valueOf(valueArray.get(i)));
                                messages[i] = indexValue;
                            }
                            responseMessage.addField(fieldName, messages);
                        } else {
                            value = Integer
                                    .parseInt(String.valueOf(((BMap<String, BValue>) responseValue).get(fieldName)));
                            responseMessage.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BInteger) {
                            value = Integer.parseInt(String.valueOf(((BInteger) responseValue).value()));
                        }
                        responseMessage.addField(fieldName, value);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    boolean value = false;
                    if (responseValue instanceof BMap) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                            BBooleanArray valueArray = (BBooleanArray) bValue;
                            Boolean[] messages = new Boolean[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                int indexValue = valueArray.get(i);
                                messages[i] = indexValue != 0;
                            }
                            responseMessage.addField(fieldName, messages);
                        } else {
                            BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                            value = ((BBoolean) bValue).booleanValue();
                            responseMessage.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BBoolean) {
                            value = ((BBoolean) responseValue).value();
                            responseMessage.addField(fieldName, value);
                        }
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    String value = null;
                    if (responseValue instanceof BMap) {
                        if (fieldDescriptor.isRepeated()) {
                            BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                            BStringArray valueArray = (BStringArray) bValue;
                            String[] messages = new String[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                String indexValue = valueArray.get(i);
                                messages[i] = indexValue;
                            }
                            responseMessage.addField(fieldName, messages);
                        } else {
                            value = ((BMap<String, BValue>) responseValue).get(fieldName).stringValue();
                            responseMessage.addField(fieldName, value);
                        }
                    } else {
                        if (responseValue instanceof BString) {
                            value = ((BString) responseValue).value();
                        }
                        responseMessage.addField(fieldName, value);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM_VALUE: {
                    if (responseValue instanceof BMap) {
                        BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                        responseMessage.addField(fieldName, fieldDescriptor.getEnumType().findValueByName(bValue
                                .stringValue()));
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE: {
                    byte[] value;
                    if (responseValue instanceof BMap) {
                        value = ((BByteArray) ((BMap) responseValue).get(fieldName)).getBytes();
                        responseMessage.addField(fieldName, value);
                    } else if (responseValue instanceof BByteArray) {
                        value = ((BByteArray) responseValue).getBytes();
                        responseMessage.addField(fieldName, value);
                    }
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                    if (responseValue instanceof BMap) {
                        BValue bValue = ((BMap<String, BValue>) responseValue).get(fieldName);
                        if (fieldDescriptor.isRepeated() && (bValue instanceof BRefValueArray)) {
                            BRefValueArray valueArray = (BRefValueArray) bValue;
                            Message[] messages = new Message[(int) valueArray.size()];
                            for (int i = 0; i < valueArray.size(); i++) {
                                BValue value = valueArray.get(i);
                                messages[i] = generateProtoMessage(value, fieldDescriptor.getMessageType());
                            }
                            responseMessage.addField(fieldName, messages);
                        } else {
                            responseMessage.addField(fieldName, generateProtoMessage(bValue, fieldDescriptor
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
        return responseMessage;
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
        } else if (structType instanceof BArrayType) {
            bValue = new BByteArray((byte[]) fields.get(fieldName));
        } else if (structType instanceof BStructureType) {
            BMap<String, BValue> requestStruct = BLangConnectorSPIUtil.createBStruct(programFile,
                    structType.getPackagePath(), structType.getName());
            for (BField structField : ((BStructureType) structType).getFields()) {
                String structFieldName = structField.getFieldName();
                BType structFieldType = structField.getFieldType();
                if (TypeKind.STRING.typeName().equals(structFieldType.getName())) {
                    BString bStringValue = (BString) generateRequestStruct(request, programFile, structFieldName,
                            structFieldType);
                    requestStruct.put(structFieldName, bStringValue);
                } else if (TypeKind.INT.typeName().equals(structFieldType.getName())) {
                    BInteger bIntegerValue = (BInteger) generateRequestStruct(request, programFile, structFieldName,
                            structFieldType);
                    requestStruct.put(structFieldName, bIntegerValue);
                } else if (TypeKind.FLOAT.typeName().equals(structFieldType.getName())) {
                    BFloat bFloatValue = (BFloat) generateRequestStruct(request, programFile, structFieldName,
                            structFieldType);
                    requestStruct.put(structFieldName, bFloatValue);
                } else if (TypeKind.BOOLEAN.typeName().equals(structFieldType.getName())) {
                    BBoolean bBooleanValue = (BBoolean) generateRequestStruct(request, programFile, structFieldName,
                            structFieldType);
                    requestStruct.put(structFieldName, bBooleanValue);
                } else if (structFieldType instanceof BStructureType) {
                    if (MessageRegistry.getInstance().getMessageDescriptorMap().containsKey(
                            structFieldType.getName())) {
                        Message message = (Message) fields.get(structFieldName);
                        requestStruct.put(structFieldName, (BRefType) generateRequestStruct(message, programFile,
                                structFieldName, structField.getFieldType()));
                    }
                } else if (structFieldType instanceof BArrayType) {
                    if (((BArrayType) structFieldType).getElementType().getTag() == TypeTags.BYTE) {
                        BByteArray bByteValue = (BByteArray) generateRequestStruct(request, programFile,
                                structFieldName, structFieldType);
                        requestStruct.put(structFieldName, bByteValue);
                        continue;
                    }
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
                        requestStruct.put(structFieldName, bArrayValue);
                    } else if (TypeKind.INT.typeName().equals(elementType.getName())) {
                        List<Long> messages = (List<Long>) fields.get(structFieldName);
                        BIntArray bArrayValue = new BIntArray();
                        for (Long integerValue : messages) {
                            bArrayValue.add(arrayIndex++, integerValue);
                        }
                        requestStruct.put(structFieldName, bArrayValue);
                    } else if (TypeKind.FLOAT.typeName().equals(elementType.getName())) {
                        List<Float> messages = (List<Float>) fields.get(structFieldName);
                        BFloatArray bArrayValue = new BFloatArray();
                        for (Float floatValue : messages) {
                            bArrayValue.add(arrayIndex++, Double.parseDouble(floatValue.toString()));
                        }
                        requestStruct.put(structFieldName, bArrayValue);
                    } else if (TypeKind.BOOLEAN.typeName().equals(elementType.getName())) {
                        List<Boolean> messages = (List<Boolean>) fields.get(structFieldName);
                        BBooleanArray bArrayValue = new BBooleanArray();
                        for (Boolean booleanValue : messages) {
                            bArrayValue.add(arrayIndex++, booleanValue ? 1 : 0);
                        }
                        requestStruct.put(structFieldName, bArrayValue);
                    } else if (elementType instanceof BStructureType) {
                        List<Message> messages = (List<Message>) fields.get(structFieldName);
                        BRefValueArray bArrayValue = new BRefValueArray(elementType);
                        for (Message message : messages) {
                            bArrayValue.add(arrayIndex++, (BRefType) generateRequestStruct(message, programFile,
                                    structFieldName, elementType));
                        }
                        requestStruct.put(structFieldName, bArrayValue);
                    }
                } else if (structFieldType instanceof BFiniteType) {
                    BValue bEnumValue = new BString((String) request.getFields().get(structField.fieldName));
                    requestStruct.put(structFieldName, bEnumValue);
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

    /** Closes an InputStream, ignoring IOExceptions. */
    static void closeQuietly(InputStream message) {
        try {
            message.close();
        } catch (IOException ignore) {
            // do nothing
        }
    }

    /**
     * Indicates whether or not the given value is a valid gRPC content-type.
     *
     * <p>
     * Referenced from grpc-java implementation.
     *
     * @param contentType gRPC content type
     * @return is valid content type
     */
    public static boolean isGrpcContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        if (CONTENT_TYPE_GRPC.length() > contentType.length()) {
            return false;
        }
        contentType = contentType.toLowerCase(Locale.ENGLISH);
        if (!contentType.startsWith(CONTENT_TYPE_GRPC)) {
            return false;
        }
        if (contentType.length() == CONTENT_TYPE_GRPC.length()) {
            // The strings match exactly.
            return true;
        }
        // The contentType matches, but is longer than the expected string.
        // We need to support variations on the content-type (e.g. +proto, +json) as defined by the gRPC wire spec.
        char nextChar = contentType.charAt(CONTENT_TYPE_GRPC.length());
        return nextChar == '+' || nextChar == ';';
    }

    public static HttpWsConnectorFactory createHttpWsConnectionFactory() {
        return new DefaultHttpWsConnectorFactory();
    }

    public static HttpCarbonMessage createHttpCarbonMessage(boolean isRequest) {
        HttpCarbonMessage httpCarbonMessage;
        if (isRequest) {
            httpCarbonMessage = new HttpCarbonMessage(
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, ""));
        } else {
            httpCarbonMessage = new HttpCarbonMessage(
                    new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        }
        return httpCarbonMessage;
    }

    public static Status httpStatusToGrpcStatus(int httpStatusCode) {
        return httpStatusToGrpcCode(httpStatusCode).toStatus()
                .withDescription("HTTP status code " + httpStatusCode);
    }

    private static Status.Code httpStatusToGrpcCode(int httpStatusCode) {
        if (httpStatusCode >= 100 && httpStatusCode < 200) {
            // 1xx. These headers should have been ignored.
            return Status.Code.INTERNAL;
        }
        switch (httpStatusCode) {
            case HttpURLConnection.HTTP_BAD_REQUEST:  // 400
            case 431:
                return Status.Code.INTERNAL;
            case HttpURLConnection.HTTP_UNAUTHORIZED:  // 401
                return Status.Code.UNAUTHENTICATED;
            case HttpURLConnection.HTTP_FORBIDDEN:  // 403
                return Status.Code.PERMISSION_DENIED;
            case HttpURLConnection.HTTP_NOT_FOUND:  // 404
                return Status.Code.UNIMPLEMENTED;
            case 429:
            case HttpURLConnection.HTTP_BAD_GATEWAY:  // 502
            case HttpURLConnection.HTTP_UNAVAILABLE:  // 503
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:  // 504
                return Status.Code.UNAVAILABLE;
            default:
                return Status.Code.UNKNOWN;
        }
    }

    /**
     * Reads an entire {@link HttpContent} to a new array. After calling this method, the buffer
     * will contain no readable bytes.
     */
    private static byte[] readArray(HttpContent httpContent) {
        if (httpContent == null || httpContent.content() == null) {
            throw new RuntimeException("Http content is null");
        }
        int length = httpContent.content().readableBytes();
        byte[] bytes = new byte[length];
        httpContent.content().readBytes(bytes, 0, length);
        return bytes;
    }

    /**
     * Reads the entire {@link HttpContent} to a new {@link String} with the given charset.
     */
    static String readAsString(HttpContent httpContent, Charset charset) {
        if (charset == null) {
            throw new RuntimeException("Charset cannot be null");
        }
        byte[] bytes = readArray(httpContent);
        return new String(bytes, charset);
    }

    private MessageUtils() {
    }
}
