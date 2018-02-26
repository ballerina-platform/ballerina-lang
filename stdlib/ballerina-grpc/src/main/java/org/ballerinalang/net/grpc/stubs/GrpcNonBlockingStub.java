/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.stubs;

import com.google.protobuf.Message;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;

import java.util.Map;

import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;

/**
 * This class handles Non Blocking client connector.
 */
public class GrpcNonBlockingStub extends io.grpc.stub.AbstractStub<GrpcNonBlockingStub> {
    private Map<String, MethodDescriptor<Message, Message>> descriptorMap;

    public GrpcNonBlockingStub(Channel channel, Map<String, MethodDescriptor<Message, Message>> descriptorMap) {
        super(channel);
        this.descriptorMap = descriptorMap;
    }

    private GrpcNonBlockingStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }

    @Override
    protected GrpcNonBlockingStub build(Channel channel, CallOptions callOptions) {
        return new GrpcNonBlockingStub(channel, callOptions);
    }

    public void executeServerStreaming(Message request, io.grpc.stub.StreamObserver<Message> responseObserver,
                                       String methodID) {
        MethodDescriptor<Message, Message> methodDescriptor = descriptorMap.get(methodID);
        asyncServerStreamingCall(
                getChannel().newCall(methodDescriptor, getCallOptions()), request, responseObserver);
    }

    public io.grpc.stub.StreamObserver<Message> executeClientStreaming(io.grpc.stub.StreamObserver<Message>
                                                                               responseObserver, String methodID) {
        MethodDescriptor<Message, Message> methodDescriptor = descriptorMap.get(methodID);
        return asyncClientStreamingCall(
                getChannel().newCall(methodDescriptor, getCallOptions()), responseObserver);
    }

    public void executeUnary(Message request,
                             io.grpc.stub.StreamObserver<Message> responseObserver, String methodID) {
            ClientCalls.asyncUnaryCall(
                    getChannel().newCall(descriptorMap.get(methodID),
                            getCallOptions()), request, responseObserver);

    }
}
