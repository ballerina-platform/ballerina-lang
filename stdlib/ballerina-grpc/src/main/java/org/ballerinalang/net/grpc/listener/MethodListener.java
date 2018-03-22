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
package org.ballerinalang.net.grpc.listener;

import com.google.protobuf.Descriptors;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.Map;

import static org.ballerinalang.net.grpc.MessageConstants.BOOLEAN;
import static org.ballerinalang.net.grpc.MessageConstants.DOUBLE;
import static org.ballerinalang.net.grpc.MessageConstants.FLOAT;
import static org.ballerinalang.net.grpc.MessageConstants.INT;
import static org.ballerinalang.net.grpc.MessageConstants.STRING;
import static org.ballerinalang.net.grpc.MessageUtils.getProgramFile;

/**
 * Abstract Method listener.
 * This provide method for all method listeners.
 *
 * @since 1.0.0
 */
public abstract class MethodListener {
    
    private Descriptors.MethodDescriptor methodDescriptor;

    public MethodListener(Descriptors.MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    /**
     * Returns endpoint instance which is used to respond to the client.
     *
     * @param responseObserver client responder instance.
     * @return instance of endpoint type.
     */
    BValue getConnectionParameter(Resource resource, StreamObserver<Message> responseObserver) {
        ProgramFile programFile = getProgramFile(resource);
        // generate client responder struct on request message with response observer and response msg type.
        BStruct clientEndpoint = BLangConnectorSPIUtil.createBStruct(programFile,
                MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC, MessageConstants.CLIENT_RESPONDER);
        clientEndpoint.setIntField(0, responseObserver.hashCode());
        clientEndpoint.addNativeData(MessageConstants.RESPONSE_OBSERVER, responseObserver);
        clientEndpoint.addNativeData(MessageConstants.RESPONSE_MESSAGE_DEFINITION, methodDescriptor.getOutputType());

        // create endpoint type instance on request.
        BStruct endpoint = BLangConnectorSPIUtil.createBStruct(programFile,
                MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC, MessageConstants.SERVICE_ENDPOINT_TYPE);
        endpoint.setRefField(0, clientEndpoint);
        return endpoint;
    }

    /**
     * Returns BValue object corresponding to the protobuf request message.
     *
     * @param requestMessage protobuf request message.
     * @return b7a message.
     */
    BValue getRequestParameter(Resource resource, Message requestMessage) {
        if (resource == null || resource.getParamDetails() == null || resource.getParamDetails().size() > 2) {
            throw new RuntimeException("Invalid resource input arguments. arguments must not be greater than two");
        }
        
        if (resource.getParamDetails().size() == 2) {
            BType requestType = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_PARAM_INDEX)
                    .getVarType();
            String requestName = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_PARAM_INDEX)
                    .getVarName();
            return generateRequestStruct(requestMessage, getProgramFile(resource), requestName, requestType);
        } else {
            return null;
        }
    }

    /**
     * Checks whether service method has a response message.
     *
     * @return true if method response is empty, false otherwise
     */
    boolean isEmptyResponse() {
        return methodDescriptor != null && MessageUtils.isEmptyResponse(methodDescriptor.getOutputType());
    }

    private BValue generateRequestStruct(Message request, ProgramFile programFile, String fieldName, BType structType) {
        BValue bValue = null;
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;
        
        if (structType instanceof BStructType) {
            BStruct requestStruct = BLangConnectorSPIUtil.createBStruct(programFile, structType
                    .getPackagePath(), structType.getName());
            for (BStructType.StructField structField : ((BStructType) structType).getStructFields()) {
                String structFieldName = structField.getFieldName();
                if (structField.getFieldType() instanceof BRefType) {
                    BType bType = structField.getFieldType();
                    if (MessageRegistry.getInstance().getMessageDescriptorMap().containsKey(bType.getName())) {
                        Message message = (Message) request.getFields().get(structFieldName);
                        requestStruct.setRefField(refIndex++, (BRefType) generateRequestStruct(message,
                                programFile, structFieldName, structField.getFieldType()));
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
}
