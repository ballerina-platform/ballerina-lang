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
package org.ballerinalang.net.grpc.listener;

import com.google.protobuf.Descriptors;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.GrpcCallableUnitCallBack;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * This is Client Streaming Method Implementation for gRPC Service Call.
 *
 * @since 1.0.0
 */
public class ClientStreamingListener extends MethodListener implements ServerCalls
        .ClientStreamingMethod<Message, Message> {
    
    private final Map<String, Resource> resourceMap;
    private static final Logger LOG = LoggerFactory.getLogger(ClientStreamingListener.class);
    
    public ClientStreamingListener(Descriptors.MethodDescriptor methodDescriptor, Map<String, Resource> resourceMap) {
        super(methodDescriptor);
        this.resourceMap = resourceMap;
    }
    
    @Override
    public StreamObserver<Message> invoke(StreamObserver<Message> responseObserver) {
        Resource onOpen = resourceMap.get(MessageConstants.ON_OPEN_RESOURCE);
        List<ParamDetail> paramDetails = onOpen.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(onOpen, responseObserver);
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, Boolean.FALSE);
        Executor.submit(onOpen, callback, null, null, signatureParams);

        return new StreamObserver<Message>() {
            @Override
            public void onNext(Message value) {
                Resource onMessage = resourceMap.get(MessageConstants.ON_MESSAGE_RESOURCE);
                List<ParamDetail> paramDetails = onMessage.getParamDetails();
                BValue[] signatureParams = new BValue[paramDetails.size()];
                signatureParams[0] = getConnectionParameter(onMessage, responseObserver);
                BValue requestParam = getRequestParameter(onMessage, value);
                if (requestParam != null) {
                    signatureParams[1] = requestParam;
                }
                CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, Boolean.FALSE);
                Executor.submit(onMessage, callback, null, null, signatureParams);
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
                signatureParams[0] = getConnectionParameter(onError, responseObserver);
                if (paramDetails.size() != 2) {
                    String message = "Error in onError resource definition. It must have two input params, but have "
                            + paramDetails.size();
                    LOG.error(message);
                    throw new RuntimeException(message);
                }
                BType errorType = paramDetails.get(1).getVarType();
                BStruct errorStruct = MessageUtils.getConnectorError((BStructType) errorType, t);
                signatureParams[1] = errorStruct;
                CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, Boolean.FALSE);
                Executor.submit(onError, callback, null, null, signatureParams);
            }

            @Override
            public void onCompleted() {
                Resource onCompleted = resourceMap.get(MessageConstants.ON_COMPLETE_RESOURCE);
                if (onCompleted == null) {
                    String message = "Error in listener service definition. onError resource does not exists";
                    LOG.error(message);
                    throw new RuntimeException(message);
                }
                List<ParamDetail> paramDetails = onCompleted.getParamDetails();
                BValue[] signatureParams = new BValue[paramDetails.size()];
                signatureParams[0] = getConnectionParameter(onCompleted, responseObserver);
                CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, isEmptyResponse());
                Executor.submit(onCompleted, callback, null, null, signatureParams);
            }
        };
    }
}
