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

import com.google.protobuf.Descriptors;
import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
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
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;

import java.util.List;
import java.util.Map;


/**
 * This is Unary Method Implementation for gRPC Service Call.
 */
public class UnaryMethodListener implements UnaryMethod<Object, Object> {

    private final Descriptors.MethodDescriptor methodDescriptor;
    private final Resource resource;

    public UnaryMethodListener(Descriptors.MethodDescriptor methodDescriptor, Resource resource) {
        this.methodDescriptor = methodDescriptor;
        this.resource = resource;
    }

    @Override
    public void invoke(Object request, StreamObserver<Object> responseObserver) {
/*        try {*/
            BStruct connection = ConnectorUtils.createStruct(resource,
                    MessageConstants.PROTOCOL_PACKAGE_GRPC, MessageConstants.CONNECTION);
            MessageUtils.enrichConnectionInfo(connection, responseObserver);

/*            BType requestType = resource.getParamDetails().get(ServiceProtoConstants.REQUEST_MESSAGE_INDEX)
                    .getVarType();*/
/*            BType responseType = resource.getParamDetails().get(ServiceProtoConstants.RESPONSE_MESSAGE_INDEX)
                    .getVarType();*/
            BValue[] signatureParams = getSignatureParameters((Message) request, responseObserver);
/*            BStruct responseStruct = ConnectorUtils.createStruct(resource, service.getPackage(),
                    responseType.getName());*/

 /*           BValue[] bValues = new BValue[]{requestStruct, responseStruct};*/
            Executor.execute(resource, null, signatureParams);

/*            BStruct bValue = (BStruct) bValues[1];*/

/*            responseObserver.onError(new StatusRuntimeException(Status.fromCode(Status.Code.INVALID_ARGUMENT)
                    .withDescription("You supplied an incorrect object ID")));*/
/*            responseObserver.onNext(generateProtoMessage(bValue, (BStructType) responseType));*/
/*        } finally {
            responseObserver.onCompleted();
        }*/
    }

    public BValue[] getSignatureParameters(Message requestMessage,  StreamObserver<Object> responseObserver) {
        //todo Think of keeping struct type globally rather than creating for each request
        BStruct connection = ConnectorUtils.createStruct(resource,
                MessageConstants.PROTOCOL_PACKAGE_GRPC, MessageConstants.CONNECTION);
        connection.addNativeData(MessageConstants.STREAM_OBSERVER, responseObserver);
        connection.addNativeData(MessageConstants.RESPONSE_MESSAGE_DEFINITION, methodDescriptor.getOutputType());
/*        String msgName = resource.getAnnotationList("ballerina.net.grpc", "methodInfo").get(0).getAnnAttrValue
                ("outputMessage").getStringValue();*/
        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = connection;
        BType requestType = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_INDEX)
                    .getVarType();
        String requestName = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_INDEX).getVarName();
        bValues[1] = generateRequestStruct(requestMessage, requestName, requestType);
//        connection.addNativeData(MessageConstants.RESPONSE_MESSAGE_NAME, requestMessage.getDescriptor().getName());
        //MessageUtils.enrichConnectionInfo(connection, responseObserver);


//        for (int i = 1; i < paramDetails.size(); i++) {
//            //No need for validation as validation already happened at deployment time,
//            //only string parameters can be found here.
//            bValues[i] = generateRequestStruct(requestMessage, paramDetails.get(i).getVarName(), paramDetails.get(i)
//                    .getVarType());
//        }
        return bValues;
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
            if (request.getFields().containsKey(fieldName)) {
                String fieldType = structType.getName();
                switch (fieldType) {
                    case "string": {
                        bValue = new BString((String) request.getFields().get(fieldName));
                        break;
                    }
                    case "int": {
                        bValue = new BInteger((Long) request.getFields().get(fieldName));
                        break;
                    }
                    case "float": {
                        Float value = (Float) request.getFields().get(fieldName);
                        if (value != null) {
                            bValue = new BFloat(Double.parseDouble(value.toString()));
                        }
                        break;
                    }
                    case "double": {
                        Double value = (Double) request.getFields().get(fieldName);
                        if (value != null) {
                            bValue = new BFloat(Double.parseDouble(value.toString()));
                        }
                        break;
                    }
                    case "boolean": {
                        bValue = new BBoolean((Boolean) request.getFields().get(fieldName));
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
