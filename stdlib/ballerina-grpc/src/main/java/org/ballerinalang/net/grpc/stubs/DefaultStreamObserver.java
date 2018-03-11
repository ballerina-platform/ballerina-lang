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
package org.ballerinalang.net.grpc.stubs;

import com.google.protobuf.Descriptors;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
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
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is Stream Observer Implementation for gRPC Client Call.
 */
public class DefaultStreamObserver implements StreamObserver<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStreamObserver.class);
    private Map<String, Resource> resourceMap = new HashMap<>();
    private StreamObserver<Message> requestSender = null;
    private Descriptors.Descriptor requestType = null;

    public DefaultStreamObserver(Context context, String serviceName) throws
            GrpcClientException {
        // TODO: 3/10/18 Fix
//        BallerinaGrpcServerConnector grpcServerConnector = (BallerinaGrpcServerConnector) ConnectorUtils.
//                getBallerinaServerConnector(context, MessageConstants.PROTOCOL_PACKAGE_GRPC);
//        Service listenerService = null;
//        if (listenerService == null) {
//            throw new GrpcClientException("Error while building the connection. Listener Service does not exist");
//        }
//
//        for (Resource resource : listenerService.getResources()) {
//            resourceMap.put(resource.getName(), resource);
//        }
    }

    public void registerRequestSender(StreamObserver<Message> requestSender, Descriptors.Descriptor requestType)
            throws GrpcClientException {
        if (requestType == null && requestSender == null) {
            throw new GrpcClientException("Error while building the connection. request type and request sender " +
                    "does not exist");
        }
        this.requestSender = requestSender;
        this.requestType = requestType;
    }

    public StreamObserver<Message> getRequestSender() {
        return requestSender;
    }

    public Descriptors.Descriptor getRequestType() {
        return requestType;
    }

    private BValue getConnectionParameter(StreamObserver<Message> requestSender, Resource resource, Descriptors
            .Descriptor inputType) {
        BStruct connection = ConnectorUtils.createStruct(resource,
                MessageConstants.PROTOCOL_PACKAGE_GRPC, MessageConstants.CLIENT_CONNECTION);
        connection.addNativeData(MessageConstants.STREAM_OBSERVER, requestSender);
        connection.addNativeData(MessageConstants.REQUEST_MESSAGE_DEFINITION, inputType);
        return connection;
    }

    @Override
    public void onNext(Message value) {
        Resource resource = resourceMap.get(MessageConstants.ON_MESSAGE_RESOURCE);
        if (resource == null) {
            String message = "Error in listener service definition. onNext resource does not exists";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(requestSender, resource, requestType);
        BValue requestParam = getRequestParameter(resource, value);
        if (requestParam != null) {
            signatureParams[1] = requestParam;
        }
        Executor.execute(resource, null, signatureParams);
    }
    
    @Override
    public void onError(Throwable t) {
        Resource onError = resourceMap.get(MessageConstants.ON_ERROR_RESOURCE);
        if (onError == null) {
            String message = "Error in listener service definition. onError resource does not exists";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
        List<ParamDetail> paramDetails = onError.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(requestSender, onError, requestType);
        if (paramDetails.size() != 2) {
            String message = "Error in onError resource definition. It must have two input params, but have "
                    + paramDetails.size();
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
        BStruct errorStruct = MessageUtils.getConnectorError(onError, paramDetails.get(1).getVarType(), t);
        signatureParams[1] = errorStruct;
        Executor.execute(onError, null, signatureParams);
    }
    
    @Override
    public void onCompleted() {
        Resource onCompleted = resourceMap.get(MessageConstants.ON_COMPLETE_RESOURCE);
        if (onCompleted == null) {
            String message = "Error in listener service definition. onCompleted resource does not exists";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
        List<ParamDetail> paramDetails = onCompleted.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(requestSender, onCompleted, requestType);
        Executor.execute(onCompleted, null, signatureParams);
    }

    private BValue getRequestParameter(Resource resource, Message requestMessage) {
        if (resource == null || resource.getParamDetails() == null || resource.getParamDetails().size() > 2) {
            throw new RuntimeException("Invalid resource input arguments. arguments must not be greater than two");
        }

        if (resource.getParamDetails().size() == 2) {
            BType requestType = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_INDEX)
                    .getVarType();
            String requestName = resource.getParamDetails().get(MessageConstants.REQUEST_MESSAGE_INDEX).getVarName();
            return generateRequestStruct(requestMessage, requestName, requestType, resource);
        } else {
            return null;
        }
    }

    private BValue generateRequestStruct(Message request, String fieldName, BType structType, Resource resource) {
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
                                structFieldName, structField.getFieldType(), resource));
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
