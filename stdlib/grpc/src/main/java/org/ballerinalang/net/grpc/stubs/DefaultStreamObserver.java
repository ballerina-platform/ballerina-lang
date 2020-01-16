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

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServiceResource;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.callback.ClientCallableUnitCallBack;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.MessageUtils.getHeaderObject;

/**
 * This is Stream Observer Implementation for gRPC Client Call.
 *
 * @since 1.0.0
 */
public class DefaultStreamObserver implements StreamObserver {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultStreamObserver.class);
    private Map<String, ServiceResource> resourceMap = new HashMap<>();
    
    public DefaultStreamObserver(BRuntime runtime, ObjectValue callbackService) throws
            GrpcClientException {
        if (callbackService == null) {
            throw new GrpcClientException("Error while building the connection. Listener Service does not exist");
        }
        for (AttachedFunction function : callbackService.getType().getAttachedFunctions()) {
            resourceMap.put(function.getName(), new ServiceResource(runtime, callbackService, function));
        }
    }
    
    @Override
    public void onNext(Message value) {
        ServiceResource resource = resourceMap.get(GrpcConstants.ON_MESSAGE_RESOURCE);
        if (resource == null) {
            String message = "Error in listener service definition. onNext resource does not exists";
            LOG.error(message);
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription(message)));
        }
        List<BType> signatureParams = resource.getParamTypes();
        Object[] paramValues = new Object[signatureParams.size() * 2];

        ObjectValue headerObject = null;
        if (resource.isHeaderRequired()) {
            headerObject = getHeaderObject();
            headerObject.addNativeData(MESSAGE_HEADERS, value.getHeaders());
        }
        Object requestParam = value.getbMessage();
        if (requestParam != null) {
            paramValues[0] = requestParam;
            paramValues[1] = true;
        }
        if (headerObject != null && signatureParams.size() == 2) {
            paramValues[2] = headerObject;
            paramValues[3] = true;
        }
        CallableUnitCallback callback = new ClientCallableUnitCallBack();
        resource.getRuntime().invokeMethodAsync(resource.getService(), resource.getFunctionName(), callback, null,
                paramValues);
    }
    
    @Override
    public void onError(Message error) {
        ServiceResource onError = resourceMap.get(GrpcConstants.ON_ERROR_RESOURCE);
        if (onError == null) {
            String message = "Error in listener service definition. onError resource does not exists";
            LOG.error(message);
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription(message)));
        }
        List<BType> signatureParams = onError.getParamTypes();
        Object[] paramValues = new Object[signatureParams.size() * 2];
        ObjectValue headerObject = null;
        if (onError.isHeaderRequired()) {
            headerObject = getHeaderObject();
            headerObject.addNativeData(MESSAGE_HEADERS, error.getHeaders());
        }

        ErrorValue errorStruct = MessageUtils.getConnectorError(error.getError());
        paramValues[0] = errorStruct;
        paramValues[1] = true;

        if (headerObject != null && signatureParams.size() == 2) {
            paramValues[2] = headerObject;
            paramValues[3] = true;
        }
        CallableUnitCallback callback = new ClientCallableUnitCallBack();
        onError.getRuntime().invokeMethodAsync(onError.getService(), onError.getFunctionName(), callback, null,
                paramValues);
    }
    
    @Override
    public void onCompleted() {
        ServiceResource onCompleted = resourceMap.get(GrpcConstants.ON_COMPLETE_RESOURCE);
        if (onCompleted == null) {
            String message = "Error in listener service definition. onCompleted resource does not exists";
            LOG.error(message);
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription(message)));
        }
        List<BType> signatureParams = onCompleted.getParamTypes();
        Object[] paramValues = new Object[signatureParams.size() * 2];
        CallableUnitCallback callback = new ClientCallableUnitCallBack();
        onCompleted.getRuntime().invokeMethodAsync(onCompleted.getService(), onCompleted.getFunctionName(), callback,
                null, paramValues);
    }
}
