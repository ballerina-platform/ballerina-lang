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
import org.ballerinalang.connector.api.ConnectorUtils;
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
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;

import java.util.Map;

/**
 * Abstract Method listener.
 * This provide method for all method listener child classes.
 */
public abstract class MethodListener {

    private Descriptors.MethodDescriptor methodDescriptor;
    public Resource resource;

    public MethodListener(Descriptors.MethodDescriptor methodDescriptor, Resource resource) {
        this.methodDescriptor = methodDescriptor;
        this.resource = resource;
    }

    BValue getConnectionParameter(StreamObserver<Message> responseObserver) {
        BStruct connection = ConnectorUtils.createStruct(resource,
                MessageConstants.PROTOCOL_PACKAGE_GRPC, MessageConstants.SERVER_CONNECTION);
        connection.setIntField(0, responseObserver.hashCode());
        connection.addNativeData(MessageConstants.STREAM_OBSERVER, responseObserver);
        connection.addNativeData(MessageConstants.RESPONSE_MESSAGE_DEFINITION, methodDescriptor.getOutputType());
        return connection;
    }

    BValue getRequestParameter(Message requestMessage) {
        if (resource == null || resource.getParamDetails() == null || resource.getParamDetails().size() > 2) {
            throw new RuntimeException("Invalid resource input arguments. arguments must not be greater than two");
        }

        if (resource.getParamDetails().size() == 2) {
            BType requestType = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_INDEX)
                    .getVarType();
            String requestName = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_INDEX).getVarName();

            return generateRequestStruct(requestMessage, requestName, requestType);
        } else {
            return null;
        }
    }

    private BValue generateRequestStruct(Message request, String fieldName, BType structType) {
        BValue bValue = null;
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;

        if (structType instanceof BStructType) {
            BStruct requestStruct = ConnectorUtils.createStruct(resource, structType.getPackagePath(), structType
                    .getName());
            for (BStructType.StructField structField : ((BStructType) structType).getStructFields()) {
                String structFieldName = structField.getFieldName();
                if (structField.getFieldType() instanceof BRefType) {
                    BType bType = structField.getFieldType();
                    if (MessageRegistry.getInstance().getMessageDescriptorMap().containsKey(bType.getName())) {
                        Message message = (Message) request.getFields().get(structFieldName);
                        requestStruct.setRefField(refIndex++, (BRefType) generateRequestStruct(message,
                                structFieldName, structField.getFieldType()));
                    }
                } else {
                    if (request.getFields().containsKey(structFieldName)) {
                        String fieldType = structField.getFieldType().getName();
                        switch (fieldType) {
                            case "string": {
                                requestStruct.setStringField(stringIndex++, (String) request.getFields().get
                                        (structFieldName));
                                break;
                            }
                            case "int": {
                                requestStruct.setIntField(intIndex++, (Long) request.getFields().get
                                        (structFieldName));
                                break;
                            }
                            case "float": {
                                Float value = (Float) request.getFields().get(structFieldName);
                                if (value != null) {
                                    requestStruct.setFloatField(floatIndex++, Double.parseDouble(value.toString()));
                                }
                                break;
                            }
                            case "double": {
                                Double value = (Double) request.getFields().get(structFieldName);
                                if (value != null) {
                                    requestStruct.setFloatField(floatIndex++, Double.parseDouble(value.toString()));
                                }
                                break;
                            }
                            case "boolean": {
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
                    case "string": {
                        bValue = new BString((String) fields.get(fieldName));
                        break;
                    }
                    case "int": {
                        bValue = new BInteger((Long) fields.get(fieldName));
                        break;
                    }
                    case "float": {
                        Float value = (Float) fields.get(fieldName);
                        if (value != null) {
                            bValue = new BFloat(Double.parseDouble(value.toString()));
                        }
                        break;
                    }
                    case "double": {
                        Double value = (Double) fields.get(fieldName);
                        if (value != null) {
                            bValue = new BFloat(Double.parseDouble(value.toString()));
                        }
                        break;
                    }
                    case "boolean": {
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
