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
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.StreamObserver;
import org.wso2.transport.http.netty.contract.HttpClientConnector;

import static org.ballerinalang.net.grpc.ClientCalls.asyncBidiStreamingCall;
import static org.ballerinalang.net.grpc.ClientCalls.asyncClientStreamingCall;
import static org.ballerinalang.net.grpc.ClientCalls.asyncServerStreamingCall;
import static org.ballerinalang.net.grpc.ClientCalls.asyncUnaryCall;

/**
 * This class handles Non Blocking client connection.
 *
 * @since 1.0.0
 */
public class NonBlockingStub extends AbstractStub<NonBlockingStub> {

    public NonBlockingStub(HttpClientConnector clientConnector, Struct endpointConfig) {
        super(clientConnector, endpointConfig);
    }

    private NonBlockingStub(HttpClientConnector connector, Struct endpointConfig, CallOptions callOptions) {
        super(connector, endpointConfig, callOptions);
    }

    @Override
    protected NonBlockingStub build(HttpClientConnector connector, Struct endpointConfig, CallOptions callOptions) {
        return new NonBlockingStub(connector, endpointConfig, callOptions);
    }

    /**
     * Executes server streaming non blocking call.
     *
     * @param request  request message.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public <ReqT, RespT> void executeServerStreaming(ReqT request, StreamObserver<RespT> responseObserver,
                                       MethodDescriptor<ReqT, RespT> methodDescriptor, CallableUnitCallback
                                               callback) {
        ClientCall<ReqT, RespT> call = new ClientCallImpl<>(getConnector(), createOutboundRequest(),
                methodDescriptor, getCallOptions(), callback);
        asyncServerStreamingCall(call, request, responseObserver);
    }

    /**
     * Executes client streaming non blocking call.
     *
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public <ReqT, RespT> StreamObserver<ReqT> executeClientStreaming(StreamObserver<RespT>
                                                                               responseObserver,
                                                                       MethodDescriptor<ReqT, RespT>
                                                                               methodDescriptor, CallableUnitCallback
                                                                  callback) {
        ClientCall<ReqT, RespT> call = new ClientCallImpl<>(getConnector(), createOutboundRequest(),
                methodDescriptor, getCallOptions(), callback);
        return asyncClientStreamingCall(call, responseObserver);
    }

    /**
     * Executes unary non blocking call.
     *
     * @param request  request message.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public <ReqT, RespT> void executeUnary(ReqT request, StreamObserver<RespT> responseObserver,
                                           MethodDescriptor<ReqT, RespT> methodDescriptor, CallableUnitCallback
                                                   callback) {

        ClientCall<ReqT, RespT> call = new ClientCallImpl<>(getConnector(), createOutboundRequest(),
                methodDescriptor, getCallOptions(), callback);
        asyncUnaryCall(call, request, responseObserver);
    }

    /**
     * Executes bidirectional streaming non blocking call.
     *
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public <ReqT, RespT> StreamObserver<ReqT> executeBidiStreaming(StreamObserver<RespT>
                                                                               responseObserver,
                                                                     MethodDescriptor<ReqT, RespT>
                                                                             methodDescriptor, CallableUnitCallback
                                                                callback) {
        ClientCall<ReqT, RespT> call = new ClientCallImpl<>(getConnector(), createOutboundRequest(), methodDescriptor,
                getCallOptions(), callback);
        return asyncBidiStreamingCall(call, responseObserver);
    }


}
