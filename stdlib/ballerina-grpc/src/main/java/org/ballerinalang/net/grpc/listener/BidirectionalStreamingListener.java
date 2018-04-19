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
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.grpc.GrpcCallableUnitCallBack;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * This is Bidirectional Streaming Method Implementation for gRPC Service Call.
 *
 * @since 1.0.0
 */
public class BidirectionalStreamingListener extends MethodListener implements ServerCalls
        .BidiStreamingMethod<Message, Message> {
    
    private final Map<String, Resource> resourceMap;
    private static final Logger LOG = LoggerFactory.getLogger(BidirectionalStreamingListener.class);
    
    public BidirectionalStreamingListener(Descriptors.MethodDescriptor methodDescriptor, Map<String, Resource>
            resourceMap) {
        super(methodDescriptor);
        this.resourceMap = resourceMap;
    }
    
    @Override
    public StreamObserver<Message> invoke(StreamObserver<Message> responseObserver) {
        Resource onOpen = resourceMap.get(GrpcConstants.ON_OPEN_RESOURCE);
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, Boolean.FALSE);
        Executor.submit(onOpen, callback, null, null, computeMessageParams
                (onOpen, null, responseObserver));
        
        return new StreamObserver<Message>() {
            @Override
            public void onNext(Message value) {
                Resource onMessage = resourceMap.get(GrpcConstants.ON_MESSAGE_RESOURCE);
                CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, isEmptyResponse());
                Executor.submit(onMessage, callback, null, null, computeMessageParams
                        (onMessage, value, responseObserver));
            }
            
            @Override
            public void onError(Throwable t) {
                Resource onError = resourceMap.get(GrpcConstants.ON_ERROR_RESOURCE);
                onErrorInvoke(onError, responseObserver, t);
            }
            
            @Override
            public void onCompleted() {
                Resource onCompleted = resourceMap.get(GrpcConstants.ON_COMPLETE_RESOURCE);
                if (onCompleted == null) {
                    String message = "Error in listener service definition. onError resource does not exists";
                    LOG.error(message);
                    throw new RuntimeException(message);
                }
                CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, Boolean.FALSE);
                Executor.submit(onCompleted, callback, null, null, computeMessageParams
                        (onCompleted, null, responseObserver));
            }
        };
    }
}
