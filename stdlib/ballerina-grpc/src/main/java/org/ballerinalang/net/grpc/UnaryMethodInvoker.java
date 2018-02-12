/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.net.grpc;

import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;


/**
 * This is Unary Method Implementation for gRPC Service Call.
 */
public class UnaryMethodInvoker implements UnaryMethod<Object, Object> {

    private final Service service;
    private final Resource resource;

    public UnaryMethodInvoker(Service service, Resource resource) {
        this.service = service;
        this.resource = resource;
    }

    @Override
    public void invoke(Object request, StreamObserver<Object> responseObserver) {
        try {
            BType requestType = resource.getParamDetails().get(ServiceProtoConstants.REQUEST_MESSAGE_INDEX)
                    .getVarType();
            BType responseType = resource.getParamDetails().get(ServiceProtoConstants.RESPONSE_MESSAGE_INDEX)
                    .getVarType();
            BStruct requestStruct = generateRequestStruct((Message) request, requestType);
            requestStruct.addNativeData(MessageConstants.PROTO_MESSAGE, request);
            BStruct responseStruct = ConnectorUtils.createStruct(resource, service.getPackage(),
                    responseType.getName());

            BValue[] bValues = new BValue[]{requestStruct, responseStruct};
            Executor.execute(resource, null, bValues);

            BStruct bValue = (BStruct) bValues[1];

/*            responseObserver.onError(new StatusRuntimeException(Status.fromCode(Status.Code.INVALID_ARGUMENT)
                    .withDescription("You supplied an incorrect object ID")));*/
            responseObserver.onNext(generateProtoMessage(bValue, (BStructType) responseType));
        } finally {
            responseObserver.onCompleted();
        }
    }

    private BStruct generateRequestStruct(Message request, BType structType) {
        BStruct requestStruct = ConnectorUtils.createStruct(resource, service.getPackage(), structType.getName());
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;
        for (BStructType.StructField structField : ((BStructType) structType).getStructFields()) {
            String fieldName = structField.getFieldName();
            if (structField.getFieldType() instanceof BRefType) {
                BType bType = structField.getFieldType();
                if (MessageRegistry.getInstance().getMessageDescriptorMap().containsKey(bType.getName())) {
                    Message message = (Message) request.getFields().get(fieldName);
                    requestStruct.setRefField(refIndex++, generateRequestStruct(message, structField
                            .getFieldType()));
                }
            } else {
                if (request.getFields().containsKey(fieldName)) {
                    String fieldType = structField.getFieldType().getName();
                    switch (fieldType) {
                        case "string": {
                            requestStruct.setStringField(stringIndex++, (String) request.getFields().get(fieldName));
                            break;
                        }
                        case "int": {
                            requestStruct.setIntField(intIndex++, (Long) request.getFields().get
                                    (fieldName));
                            break;
                        }
                        case "float": {
                            Float value = (Float) request.getFields().get(fieldName);
                            if (value != null) {
                                requestStruct.setFloatField(floatIndex++, Double.parseDouble(value.toString()));
                            }
                            break;
                        }
                        case "double": {
                            Double value = (Double) request.getFields().get(fieldName);
                            if (value != null) {
                                requestStruct.setFloatField(floatIndex++, Double.parseDouble(value.toString()));
                            }
                            break;
                        }
                        case "boolean": {
                            requestStruct.setBooleanField(boolIndex++, (Integer) request.getFields().get
                                    (fieldName));
                            break;
                        }
                        default: {
                            throw new UnsupportedFieldTypeException("Error while generating request struct. Field " +
                                    "type is not supported : " + fieldType);
                        }
                    }
                }
            }
        }
        return requestStruct;
    }


    private com.google.protobuf.Message generateProtoMessage(BStruct bValue, BStructType structType) {
        Message.Builder responseBuilder = Message.newBuilder(structType.getName());
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;
        for (BStructType.StructField structField : structType.getStructFields()) {
            String fieldName = structField.getFieldName();
            AnnAttrValue fieldAnnValue = null;
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
}
