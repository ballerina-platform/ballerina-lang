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

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.Message;

import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;

/**
 * This class handles Non Blocking client connection.
 *
 * @since 1.0.0
 */
public class GrpcNonBlockingStub extends io.grpc.stub.AbstractStub<GrpcNonBlockingStub> {

    public GrpcNonBlockingStub(Channel channel) {
        super(channel);
    }

    private GrpcNonBlockingStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }

    @Override
    protected GrpcNonBlockingStub build(Channel channel, CallOptions callOptions) {
        return new GrpcNonBlockingStub(channel, callOptions);
    }

    /**
     * Executes server streaming non blocking call.
     *
     * @param request  request message.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public void executeServerStreaming(Message request, io.grpc.stub.StreamObserver<Message> responseObserver,
                                       MethodDescriptor<Message, Message> methodDescriptor) {
        asyncServerStreamingCall(
                getChannel().newCall(methodDescriptor, getCallOptions()), request, responseObserver);
    }

    /**
     * Executes client streaming non blocking call.
     *
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public io.grpc.stub.StreamObserver<Message> executeClientStreaming(io.grpc.stub.StreamObserver<Message>
                                                                               responseObserver,
                                                                       MethodDescriptor<Message, Message>
                                                                               methodDescriptor) {
        return asyncClientStreamingCall(
                getChannel().newCall(methodDescriptor, getCallOptions()), responseObserver);
    }

    /**
     * Executes unary non blocking call.
     *
     * @param request  request message.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public void executeUnary(Message request,
                             io.grpc.stub.StreamObserver<Message> responseObserver, MethodDescriptor<Message,
            Message> methodDescriptor) {
            asyncUnaryCall(getChannel().newCall(methodDescriptor, getCallOptions()), request, responseObserver);
    }

    /**
     * Executes bidirectional streaming non blocking call.
     *
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public io.grpc.stub.StreamObserver<Message> executeBidiStreaming(io.grpc.stub.StreamObserver<Message>
                                                                               responseObserver,
                                                                     MethodDescriptor<Message, Message>
                                                                             methodDescriptor) {
        return asyncBidiStreamingCall(
                getChannel().newCall(methodDescriptor, getCallOptions()), responseObserver);
    }


}
