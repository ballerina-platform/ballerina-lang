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
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;

import java.util.List;
import java.util.Map;

/**
 * This is Bidirectional Streaming Method Implementation for gRPC Service Call.
 */
public class BidirectionalStreamingListener extends MethodListener implements ServerCalls
        .BidiStreamingMethod<Message, Message> {

    public final Map<String, Resource> resourceMap;

    public BidirectionalStreamingListener(Descriptors.MethodDescriptor methodDescriptor, Map<String, Resource>
            resourceMap) {
        super(methodDescriptor, resourceMap.get(MessageConstants.ON_MESSAGE_RESOURCE));
        this.resourceMap = resourceMap;
    }

    @Override
    public StreamObserver<Message> invoke(StreamObserver<Message> responseObserver) {
        Resource onOpen = resourceMap.get(MessageConstants.ON_OPEN_RESOURCE);
        List<ParamDetail> paramDetails = onOpen.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(responseObserver);
        Executor.execute(onOpen, null, signatureParams);

        return new StreamObserver<Message>() {

            @Override
            public void onNext(Message value) {
                List<ParamDetail> paramDetails = resource.getParamDetails();
                BValue[] signatureParams = new BValue[paramDetails.size()];
                signatureParams[0] = getConnectionParameter(responseObserver);
                BValue requestParam = getRequestParameter(value);
                if (requestParam != null) {
                    signatureParams[1] = requestParam;
                }
                Executor.execute(resource, null, signatureParams);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                Resource onCompleted = resourceMap.get(MessageConstants.ON_COMPLETE_RESOURCE);
                List<ParamDetail> paramDetails = onCompleted.getParamDetails();
                BValue[] signatureParams = new BValue[paramDetails.size()];
                signatureParams[0] = getConnectionParameter(responseObserver);
                Executor.execute(onCompleted, null, signatureParams);
            }
        };
    }
}
