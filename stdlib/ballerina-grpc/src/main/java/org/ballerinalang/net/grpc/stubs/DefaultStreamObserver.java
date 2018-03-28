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

import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.GrpcCallableUnitCallBack;
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

import static org.ballerinalang.net.grpc.MessageConstants.BOOLEAN;
import static org.ballerinalang.net.grpc.MessageConstants.DOUBLE;
import static org.ballerinalang.net.grpc.MessageConstants.FLOAT;
import static org.ballerinalang.net.grpc.MessageConstants.INT;
import static org.ballerinalang.net.grpc.MessageConstants.STRING;

/**
 * This is Stream Observer Implementation for gRPC Client Call.
 *
 * @since 1.0.0
 */
public class DefaultStreamObserver implements StreamObserver<Message> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultStreamObserver.class);
    private Map<String, Resource> resourceMap = new HashMap<>();

    public DefaultStreamObserver(Service callbackService) throws
            GrpcClientException {
        if (callbackService == null) {
            throw new GrpcClientException("Error while building the connection. Listener Service does not exist");
        }

        for (Resource resource : callbackService.getResources()) {
            resourceMap.put(resource.getName(), resource);
        }
    }
    
    @Override
    public void onNext(Message value) {
        Resource resource = resourceMap.get(MessageConstants.ON_MESSAGE_RESOURCE);
        if (resource == null) {
            String message = "Error in listener service definition. onNext resource does not exists";
            LOG.error(message);
            throw new RuntimeException(message);
        }
        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        BValue requestParam = getRequestParameter(resource, value);
        if (requestParam != null) {
            signatureParams[0] = requestParam;
        }
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(null);
        Executor.submit(resource, callback, null, null, signatureParams);
    }
    
    @Override
    public void onError(Throwable t) {
        Resource onError = resourceMap.get(MessageConstants.ON_ERROR_RESOURCE);
        if (onError == null) {
            String message = "Error in listener service definition. onError resource does not exists";
            LOG.error(message);
            throw new RuntimeException(message);
        }
        List<ParamDetail> paramDetails = onError.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        if (paramDetails.size() != 1) {
            String message = "Error in onError resource definition. It must have only error params, but have "
                    + paramDetails.size();
            LOG.error(message);
            throw new RuntimeException(message);
        }
        BType errorType = paramDetails.get(1).getVarType();
        BStruct errorStruct = MessageUtils.getConnectorError((BStructType) errorType, t);
        signatureParams[0] = errorStruct;
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(null);
        Executor.submit(onError, callback, null, null, signatureParams);
    }
    
    @Override
    public void onCompleted() {
        Resource onCompleted = resourceMap.get(MessageConstants.ON_COMPLETE_RESOURCE);
        if (onCompleted == null) {
            String message = "Error in listener service definition. onCompleted resource does not exists";
            LOG.error(message);
            throw new RuntimeException(message);
        }
        List<ParamDetail> paramDetails = onCompleted.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(null);
        Executor.submit(onCompleted, callback, null, null, signatureParams);
    }
    
    private BValue getRequestParameter(Resource resource, Message requestMessage) {
        if (resource == null || resource.getParamDetails() == null || resource.getParamDetails().size() > 1) {
            throw new RuntimeException("Invalid resource input arguments. arguments must not be greater than two");
        }
        
        if (resource.getParamDetails().size() == 1) {
            BType requestType = resource.getParamDetails().get(MessageConstants.CALLBACK_MESSAGE_PARAM_INDEX)
                    .getVarType();
            String requestName = resource.getParamDetails().get(MessageConstants.CALLBACK_MESSAGE_PARAM_INDEX)
                    .getVarName();
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
            BStruct requestStruct = BLangConnectorSPIUtil.createBStruct(MessageUtils.getProgramFile(resource),
                    structType.getPackagePath(), structType.getName());
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
