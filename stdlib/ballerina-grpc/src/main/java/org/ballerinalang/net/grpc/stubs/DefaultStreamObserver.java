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
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.GrpcCallableUnitCallBack;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.MessageUtils.updateContextProperties;

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
        Executor.submit(resource, callback, updateContextProperties(null), null, signatureParams);
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
        BType errorType = paramDetails.get(0).getVarType();
        BStruct errorStruct = MessageUtils.getConnectorError((BStructType) errorType, t);
        signatureParams[0] = errorStruct;
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(null);
        Executor.submit(onError, callback, updateContextProperties(null), null, signatureParams);
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
        Executor.submit(onCompleted, callback, updateContextProperties(null), null, signatureParams);
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
            return MessageUtils.generateRequestStruct(requestMessage, MessageUtils.getProgramFile(resource),
                    requestName, requestType);
        } else {
            return null;
        }
    }
}
