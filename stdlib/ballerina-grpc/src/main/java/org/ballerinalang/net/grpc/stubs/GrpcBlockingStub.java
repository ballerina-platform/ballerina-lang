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

import java.util.Map;

import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;

/**
 * This class handles Non Blocking client connector.
 */
public class GrpcBlockingStub extends io.grpc.stub.AbstractStub<GrpcBlockingStub> {
    private Map<String, MethodDescriptor<Message, Message>> descriptorMap;

    public GrpcBlockingStub(Channel channel, Map<String, MethodDescriptor<Message, Message>> descriptorMap) {
        super(channel);
        this.descriptorMap = descriptorMap;
    }

    private GrpcBlockingStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }

    @Override
    protected GrpcBlockingStub build(Channel channel, CallOptions callOptions) {
        return new GrpcBlockingStub(channel, callOptions);
    }

    public java.util.Iterator<Message> executeServerStreaming(
            Message request, String methodID) {
        MethodDescriptor<Message, Message> methodDescriptor = descriptorMap.get(methodID);
        return blockingServerStreamingCall(
                getChannel(), methodDescriptor, getCallOptions(), request);
    }

    /**
     * Executes a unary call and blocks on the response.
     *
     * @param request  request message.
     * @param methodID method name
     * @return the single response message.
     */
    public Message executeUnary(Message request, String methodID) {
        MethodDescriptor<Message, Message> methodDescriptor = descriptorMap.get(methodID);
        return blockingUnaryCall(getChannel(), methodDescriptor, getCallOptions(), request);
    }
}
