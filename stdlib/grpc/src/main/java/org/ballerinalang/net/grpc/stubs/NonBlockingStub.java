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
import org.ballerinalang.net.grpc.CallStreamObserver;
import org.ballerinalang.net.grpc.ClientCall;
import org.ballerinalang.net.grpc.DataContext;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.wso2.transport.http.netty.contract.HttpClientConnector;

/**
 * This class handles Non Blocking client connection.
 *
 * @since 0.980.0
 */
public class NonBlockingStub extends AbstractStub {

    public NonBlockingStub(HttpClientConnector clientConnector, String urlString) {
        super(clientConnector, urlString);
    }

    /**
     * Executes server streaming non blocking call.
     *
     * @param request  request message.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor.
     * @param context Data Context.
     * @throws Exception if an error occur while processing client call.
     */
    public void executeServerStreaming(Message request, StreamObserver responseObserver,
                                       MethodDescriptor methodDescriptor, DataContext context) throws Exception {
        ClientCall call = new ClientCall(getConnector(), createOutboundRequest(request.getHeaders()),
                methodDescriptor, context);
        call.start(new NonblockingCallListener(responseObserver, true));
        try {
            call.sendMessage(request);
            call.halfClose();
        } catch (Exception e) {
            cancelThrow(call, e);
        }
    }

    /**
     * Executes client streaming non blocking call.
     *
     * @param requestHeaders request headers.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor.
     * @param context Data Context.
     *
     * @return streaming observer
     */
    public StreamObserver executeClientStreaming(HttpHeaders requestHeaders,
                                                 StreamObserver responseObserver,
                                                 MethodDescriptor methodDescriptor,
                                                 DataContext context) {
        ClientCall call = new ClientCall(getConnector(), createOutboundRequest(requestHeaders), methodDescriptor,
                context);
        ClientCallStreamObserver streamObserver = new ClientCallStreamObserver(call);
        call.start(new NonblockingCallListener(responseObserver, false));
        return streamObserver;
    }

    /**
     * Executes unary non blocking call.
     *
     * @param request  request message.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor.
     * @param context Observer Context.
     * @throws Exception if an error occur while processing client call.
     */
    public void executeUnary(Message request, StreamObserver responseObserver,
                                           MethodDescriptor methodDescriptor, DataContext context) throws Exception {
        ClientCall call = new ClientCall(getConnector(), createOutboundRequest(request.getHeaders()),
                methodDescriptor, context);
        call.start(new NonblockingCallListener(responseObserver, false));
        try {
            call.sendMessage(request);
            call.halfClose();
        } catch (Exception e) {
            cancelThrow(call, e);
        }
    }

    /**
     * Executes bidirectional streaming non blocking call.
     *
     * @param requestHeaders request headers.
     * @param responseObserver response Observer.
     * @param methodDescriptor method descriptor.
     * @param context Observer Context.
     *
     * @return streaming observer
     */
    public StreamObserver executeBidiStreaming(HttpHeaders requestHeaders, StreamObserver responseObserver,
                                               MethodDescriptor methodDescriptor, DataContext context) {
        ClientCall call = new ClientCall(getConnector(), createOutboundRequest(requestHeaders), methodDescriptor,
                context);
        ClientCallStreamObserver streamObserver = new ClientCallStreamObserver(call);
        call.start(new NonblockingCallListener(responseObserver, true));
        return streamObserver;
    }

    /**
     *  Callbacks for receiving headers, response messages and completion status in non-blocking calls.
     */
    private static final class NonblockingCallListener implements Listener {

        private final StreamObserver observer;
        private final boolean streamingResponse;
        private boolean firstResponseReceived;

        // Non private to avoid synthetic class
        NonblockingCallListener(StreamObserver observer, boolean streamingResponse) {
            this.observer = observer;
            this.streamingResponse = streamingResponse;
        }

        @Override
        public void onHeaders(HttpHeaders headers) {
            // Headers are processed at client connector listener. Do not need to further process.
        }

        @Override
        public void onMessage(Message message) {
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
                observer.onError(new Message(status.asRuntimeException()));
            }
        }
    }

    private static final class ClientCallStreamObserver implements CallStreamObserver {

        private final ClientCall call;

        // Non private to avoid synthetic class
        ClientCallStreamObserver(ClientCall call) {
            this.call = call;
        }

        @Override
        public void onNext(Message value) {
            call.sendMessage(value);
        }

        @Override
        public void onError(Message error) {
            call.cancel("Cancelled by client with StreamObserver.onError()", error.getError());
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

    }
}
