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

import java.util.Map;


/**
 * This is Bidirectional Streaming Method Implementation for gRPC Service Call.
 *
 * @since 1.0.0
 */
public class BidirectionalStreamingListener extends MethodListener implements ServerCalls
        .BidiStreamingMethod<Message, Message> {
    
    private final Map<String, Resource> resourceMap;

    public BidirectionalStreamingListener(Descriptors.MethodDescriptor methodDescriptor, Map<String, Resource>
            resourceMap) {
        super(methodDescriptor);
        this.resourceMap = resourceMap;
    }
    
    @Override
    public StreamObserver<Message> invoke(StreamObserver<Message> responseObserver) {
        Resource onOpen = resourceMap.get(GrpcConstants.ON_OPEN_RESOURCE);
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(null);
        Executor.submit(onOpen, callback, null, null, computeMessageParams
                (onOpen, null, responseObserver));
        
        return new DefaultStreamObserver(resourceMap, responseObserver);
    }
}
