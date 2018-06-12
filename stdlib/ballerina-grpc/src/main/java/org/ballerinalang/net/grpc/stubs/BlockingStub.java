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


import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.net.grpc.CallOptions;
import org.ballerinalang.net.grpc.ClientCall;
import org.ballerinalang.net.grpc.ClientCallImpl;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.wso2.transport.http.netty.contract.HttpClientConnector;

import static org.ballerinalang.net.grpc.ClientCalls.blockingServerStreamingCall;
import static org.ballerinalang.net.grpc.ClientCalls.blockingUnaryCall;

/**
 * This class handles Blocking client connection.
 *
 * @since 1.0.0
 */
public class BlockingStub extends AbstractStub<BlockingStub> {

    public BlockingStub(HttpClientConnector clientConnector, Struct endpointConfig) {
        super(clientConnector, endpointConfig);
    }

    private BlockingStub(HttpClientConnector connector, Struct endpointConfig, CallOptions callOptions) {
        super(connector, endpointConfig, callOptions);
    }

    @Override
    protected BlockingStub build(HttpClientConnector connector, Struct endpointConfig, CallOptions callOptions) {
        return new BlockingStub(connector, endpointConfig, callOptions);
    }

    /**
     * Executes server streaming call and blocks on the response.
     *
     * @param request  request message.
     * @param methodDescriptor method descriptor
     * @return  response message iterator.
     */
    public java.util.Iterator<Message> executeServerStreaming(Message request, MethodDescriptor<Message, Message>
            methodDescriptor, CallableUnitCallback callback) {
        return blockingServerStreamingCall(
                getConnector(), methodDescriptor, getCallOptions(), request, callback);
    }

    /**
     * Executes a unary call and blocks on the response.
     *
     * @param request  request message.
     * @param methodDescriptor method descriptor
     * @return the single response message.
     */
    public <ReqT, RespT> RespT executeUnary(ReqT request, MethodDescriptor<ReqT, RespT> methodDescriptor,
                                CallableUnitCallback callback) {

        ClientCall<ReqT, RespT> call = new ClientCallImpl<ReqT, RespT>(getConnector(), createOutboundRequest(),
                methodDescriptor, getCallOptions(), callback);
        return blockingUnaryCall(call, request);
    }
}
