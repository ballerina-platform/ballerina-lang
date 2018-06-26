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

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.net.grpc.CallStreamObserver;
import org.ballerinalang.net.grpc.ClientCall;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.wso2.transport.http.netty.contract.HttpClientConnector;

import javax.annotation.Nullable;

/**
 * This class handles Non Blocking client connection.
 *
 * @since 1.0.0
 */
public class NonBlockingStub extends AbstractStub<NonBlockingStub> {

    public NonBlockingStub(HttpClientConnector clientConnector, Struct endpointConfig) {
        super(clientConnector, endpointConfig);
    }

    /**
     * Executes server streaming non blocking call.
     *
     * @param request  request message.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public <ReqT, RespT> void executeServerStreaming(ReqT request, StreamObserver<RespT> responseObserver,
                                       MethodDescriptor<ReqT, RespT> methodDescriptor) {

        ClientCall<ReqT, RespT> call = new ClientCall<>(getConnector(), createOutboundRequest(((Message) request)
                .getHeaders()), methodDescriptor);
        call.start(new NonblockingCallListener<>(responseObserver, true));
        try {
            call.sendMessage(request);
            call.halfClose();
        } catch (RuntimeException | Error e) {
            throw cancelThrow(call, e);
        }
    }

    /**
     * Executes client streaming non blocking call.
     *
     * @param requestHeaders request headers.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor.
     */
    public <ReqT, RespT> StreamObserver<ReqT> executeClientStreaming(HttpHeaders requestHeaders,
                                                                     StreamObserver<RespT> responseObserver,
                                                                     MethodDescriptor<ReqT, RespT> methodDescriptor) {
        ClientCall<ReqT, RespT> call = new ClientCall<>(getConnector(), createOutboundRequest(requestHeaders),
                methodDescriptor);
        ClientCallStreamObserver<ReqT> streamObserver = new ClientCallStreamObserver<>(call);
        call.start(new NonblockingCallListener<>(responseObserver, false));
        return streamObserver;
    }

    /**
     * Executes unary non blocking call.
     *
     * @param request  request message.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public <ReqT, RespT> void executeUnary(ReqT request, StreamObserver<RespT> responseObserver,
                                           MethodDescriptor<ReqT, RespT> methodDescriptor) {

        ClientCall<ReqT, RespT> call = new ClientCall<>(getConnector(), createOutboundRequest(((Message) request)
                .getHeaders()), methodDescriptor);
        call.start(new NonblockingCallListener<>(responseObserver, false));
        try {
            call.sendMessage(request);
            call.halfClose();
        } catch (RuntimeException | Error e) {
            throw cancelThrow(call, e);
        }
    }

    /**
     * Executes bidirectional streaming non blocking call.
     *
     * @param requestHeaders request headers.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor
     */
    public <ReqT, RespT> StreamObserver<ReqT> executeBidiStreaming(HttpHeaders requestHeaders, StreamObserver<RespT>
            responseObserver, MethodDescriptor<ReqT, RespT> methodDescriptor) {
        ClientCall<ReqT, RespT> call = new ClientCall<>(getConnector(), createOutboundRequest(requestHeaders),
                methodDescriptor);
        ClientCallStreamObserver<ReqT> streamObserver = new ClientCallStreamObserver<>(call);
        call.start(new NonblockingCallListener<>(responseObserver, true));
        return streamObserver;
    }

    /**
     *  Callbacks for receiving headers, response messages and completion status in non-blocking calls.
     */
    private static final class NonblockingCallListener<RespT> extends AbstractStub.Listener<RespT> {

        private final StreamObserver<RespT> observer;
        private final boolean streamingResponse;
        private boolean firstResponseReceived;

        // Non private to avoid synthetic class
        NonblockingCallListener(StreamObserver<RespT> observer, boolean streamingResponse) {

            this.observer = observer;
            this.streamingResponse = streamingResponse;
        }

        @Override
        public void onHeaders(HttpHeaders headers) {

        }

        @Override
        public void onMessage(RespT message) {

            if (firstResponseReceived && !streamingResponse) {
                throw Status.Code.INTERNAL.toStatus()
                        .withDescription("More than one responses received for unary or client-streaming call")
                        .asRuntimeException();
            }
            firstResponseReceived = true;
            observer.onNext(message);
        }

        @Override
        public void onClose(Status status, HttpHeaders trailers) {

            if (status.isOk()) {
                observer.onCompleted();
            } else {
                observer.onError((RespT) new Message(status.asRuntimeException()));
            }
        }
    }

    private static final class ClientCallStreamObserver<T> implements CallStreamObserver<T> {

        private final ClientCall<T, ?> call;

        // Non private to avoid synthetic class
        ClientCallStreamObserver(ClientCall<T, ?> call) {

            this.call = call;
        }

        @Override
        public void onNext(T value) {

            call.sendMessage(value);
        }

        @Override
        public void onError(T error) {

            call.cancel("Cancelled by client with StreamObserver.onError()", ((Message) error).getError());
        }

        @Override
        public void onCompleted() {

            call.halfClose();
        }

        @Override
        public boolean isReady() {

            return call.isReady();
        }

        @Override
        public void setMessageCompression(boolean enable) {

            call.setMessageCompression(enable);
        }

        public void cancel(@Nullable String message, @Nullable Throwable cause) {

            call.cancel(message, cause);
        }
    }
}
