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
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
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

import static org.ballerinalang.net.grpc.MessageConstants.BOOLEAN;
import static org.ballerinalang.net.grpc.MessageConstants.DOUBLE;
import static org.ballerinalang.net.grpc.MessageConstants.FLOAT;
import static org.ballerinalang.net.grpc.MessageConstants.INT;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.STRING;
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
            observerObject = ((BStruct) refType).getNativeData(MessageConstants.RESPONSE_OBSERVER);
        }
        if (observerObject instanceof StreamObserver) {
            return ((StreamObserver<Message>) observerObject);
        }
        return null;
    }
    
    public static BStruct getConnectorError(Context context, Throwable throwable) {
        PackageInfo grpcPackageInfo = context.getProgramFile()
                .getPackageInfo(PROTOCOL_STRUCT_PACKAGE_GRPC);
        StructInfo errorStructInfo = grpcPackageInfo.getStructInfo(MessageConstants.CONNECTOR_ERROR);
        return getConnectorError(errorStructInfo.getType(), throwable);
    }
    
    /**
     * Returns error struct of input type
     * Error type can be either ServerError or ClientError. This utility method is used inside Observer onError
     * method to construct error struct from message.
     *
     * @param errorType this is either ServerError or ClientError.
     * @param error     this is StatusRuntimeException send by opposite party.
     * @return error struct.
     */
    public static BStruct getConnectorError(BStructType errorType, Throwable error) {
        BStruct errorStruct = new BStruct(errorType);
        if (error instanceof StatusRuntimeException) {
            StatusRuntimeException statusException = (StatusRuntimeException) error;
            int status = statusException.getStatus() != null ? statusException.getStatus().getCode().value() : -1;
            String message = statusException.getMessage();
            errorStruct.setStringField(0, message);
            errorStruct.setIntField(0, status);
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
                        responseBuilder.addField(fieldName, generateProtoMessage(bValue, fieldDescriptor
                                .getMessageType()));
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
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;

        if (structType instanceof BStructType) {
            BStruct requestStruct = BLangConnectorSPIUtil.createBStruct(programFile,
                    structType.getPackagePath(), structType.getName());
            for (BStructType.StructField structField : ((BStructType) structType).getStructFields()) {
                String structFieldName = structField.getFieldName();
                if (structField.getFieldType() instanceof BStructType) {
                    BStructType bStructType = (BStructType) structField.getFieldType();
                    if (MessageRegistry.getInstance().getMessageDescriptorMap().containsKey(bStructType.getName())) {
                        Message message = (Message) request.getFields().get(structFieldName);
                        requestStruct.setRefField(refIndex++, (BRefType) generateRequestStruct(message, programFile,
                                structFieldName, structField.getFieldType()));
                    }
                } else {
                    if (request.getFields().containsKey(structFieldName)) {
                        String fieldType = structField.getFieldType().getName();
                        switch (fieldType) {
                            case STRING: {
                                requestStruct.setStringField(stringIndex++, (String) request.getFields().get
                                        (structFieldName));
                                break;
                            }
                            case INT: {
                                requestStruct.setIntField(intIndex++, (Long) request.getFields().get
                                        (structFieldName));
                                break;
                            }
                            case FLOAT: {
                                Float value = (Float) request.getFields().get(structFieldName);
                                if (value != null) {
                                    requestStruct.setFloatField(floatIndex++, Double.parseDouble(value.toString()));
                                }
                                break;
                            }
                            case DOUBLE: {
                                Double value = (Double) request.getFields().get(structFieldName);
                                if (value != null) {
                                    requestStruct.setFloatField(floatIndex++, Double.parseDouble(value.toString()));
                                }
                                break;
                            }
                            case BOOLEAN: {
                                requestStruct.setBooleanField(boolIndex++, (Integer) request.getFields().get
                                        (structFieldName));
                                break;
                            }
                            default: {
                                throw new UnsupportedFieldTypeException("Error while generating request struct. Field" +
                                        " type is not supported : " + fieldType);
                            }
                        }
                    }
                }
            }
            bValue = requestStruct;
        } else {
            Map<String, Object> fields = request.getFields();
            if (fields.size() == 1 && fields.containsKey("value")) {
                fieldName = "value";
            }
            if (fields.containsKey(fieldName)) {
                String fieldType = structType.getName();
                switch (fieldType) {
                    case STRING: {
                        bValue = new BString((String) fields.get(fieldName));
                        break;
                    }
                    case INT: {
                        bValue = new BInteger((Long) fields.get(fieldName));
                        break;
                    }
                    case FLOAT: {
                        Float value = (Float) fields.get(fieldName);
                        if (value != null) {
                            bValue = new BFloat(Double.parseDouble(value.toString()));
                        }
                        break;
                    }
                    case DOUBLE: {
                        Double value = (Double) fields.get(fieldName);
                        if (value != null) {
                            bValue = new BFloat(Double.parseDouble(value.toString()));
                        }
                        break;
                    }
                    case BOOLEAN: {
                        bValue = new BBoolean((Boolean) fields.get(fieldName));
                        break;
                    }
                    default: {
                        throw new UnsupportedFieldTypeException("Error while generating request struct. Field " +
                                "type is not supported : " + fieldType);
                    }
                }
            }
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
