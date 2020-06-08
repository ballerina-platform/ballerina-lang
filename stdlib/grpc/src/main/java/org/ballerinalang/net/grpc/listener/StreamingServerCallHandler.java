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

package org.ballerinalang.net.grpc.listener;

import com.google.protobuf.Descriptors;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServerCall;
import org.ballerinalang.net.grpc.ServiceResource;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.callback.StreamingCallableUnitCallBack;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.ERROR_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.ITERATOR_LOCK;
import static org.ballerinalang.net.grpc.GrpcConstants.ITERATOR_OBJECT_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.LISTENER_LOCK;
import static org.ballerinalang.net.grpc.GrpcConstants.NEXT_MESSAGE;

/**
 * Interface to initiate processing of incoming remote calls for streaming services.
 * This is used in client and bidirectional streaming services.
 * @since 0.980.0
 */
public class StreamingServerCallHandler extends ServerCallHandler {

    private final ServiceResource resource;
    private final BType inputType;

    public StreamingServerCallHandler(Descriptors.MethodDescriptor methodDescriptor, ServiceResource resource,
                                      BType inputType) throws GrpcServerException {
        super(methodDescriptor);
        if (resource == null) {
            throw new GrpcServerException("Streaming service resource doesn't exist.");
        }
        this.resource = resource;
        this.inputType = inputType;
    }

    @Override
    public Listener startCall(ServerCall call) {
        ServerCallStreamObserver responseObserver = new ServerCallStreamObserver(call);
        StreamObserver requestObserver = invoke(responseObserver, call);
        return new StreamingServerCallHandler.StreamingServerCallListener(requestObserver, responseObserver);
    }

    private StreamObserver invoke(StreamObserver responseObserver, ServerCall call) {
        ObserverContext context = call.getObserverContext();
        ObjectValue streamIterator = BallerinaValues.createObjectValue(GrpcConstants.PROTOCOL_GRPC_PKG_ID,
                ITERATOR_OBJECT_NAME, new Object[1]);
        Semaphore listenerSemaphore = new Semaphore(1, true);
        Semaphore iteratorSemaphore = new Semaphore(0, true);
        streamIterator.addNativeData(LISTENER_LOCK, listenerSemaphore);
        streamIterator.addNativeData(ITERATOR_LOCK, iteratorSemaphore);
        streamIterator.addNativeData(CLIENT_ENDPOINT_TYPE, getConnectionParameter(responseObserver));
        StreamValue requestStream = new StreamValue(new BStreamType(inputType), streamIterator);
        onStreamInvoke(resource, requestStream, call.getHeaders(), responseObserver, context);
        return new StreamingServerRequestObserver(streamIterator, iteratorSemaphore, listenerSemaphore);
    }

    private static final class StreamingServerRequestObserver implements StreamObserver {
        
        private final ObjectValue streamIterator;
        private final Semaphore iteratorSemaphore;
        private final Semaphore listenerSemaphore;

        StreamingServerRequestObserver(ObjectValue streamIterator, Semaphore iteratorSemaphore,
                                       Semaphore listenerSemaphore) {
            this.streamIterator = streamIterator;
            this.iteratorSemaphore = iteratorSemaphore;
            this.listenerSemaphore = listenerSemaphore;
        }

        @Override
        public void onNext(Message value) {
            try {
                listenerSemaphore.acquire();
                streamIterator.addNativeData(NEXT_MESSAGE, value);
                iteratorSemaphore.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                String message = "Internal error occurred. The current thread got interrupted";
                throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription(message)));
            }
        }

        @Override
        public void onError(Message error) {
            ErrorValue errorStruct = MessageUtils.getConnectorError(error.getError());
            streamIterator.addNativeData(ERROR_MESSAGE, errorStruct);
            iteratorSemaphore.release();
        }

        @Override
        public void onCompleted() {
            try {
                listenerSemaphore.acquire();
                streamIterator.addNativeData(NEXT_MESSAGE, null);
                iteratorSemaphore.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                String message = "Internal error occurred. The current thread got interrupted";
                throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription(message)));
            }
        }
    }

    private static final class StreamingServerCallListener implements Listener {

        private final StreamObserver requestObserver;
        private final ServerCallStreamObserver responseObserver;
        private boolean halfClosed = false;

        // Non private to avoid synthetic class
        StreamingServerCallListener(
                StreamObserver requestObserver,
                ServerCallStreamObserver responseObserver) {
            this.requestObserver = requestObserver;
            this.responseObserver = responseObserver;
        }

        @Override
        public void onMessage(Message request) {
            requestObserver.onNext(request);
        }

        @Override
        public void onHalfClose() {
            halfClosed = true;
            requestObserver.onCompleted();
        }

        @Override
        public void onCancel() {
            responseObserver.cancelled = true;
            if (!halfClosed) {
                Message message = new Message(Status.Code.CANCELLED.toStatus()
                        .withDescription("cancelled before receiving half close")
                        .asRuntimeException());
                requestObserver.onError(message);
            }
        }

        @Override
        public void onComplete() {
            // Additional logic when closing the stream at server side.
        }
    }

    void onStreamInvoke(ServiceResource resource, StreamValue requestStream, HttpHeaders headers,
                        StreamObserver responseObserver, ObserverContext context) {
        Object[] requestParams = computeResourceParams(resource, requestStream, headers, responseObserver);
        Map<String, Object> properties = new HashMap<>();
        if (ObserveUtils.isObservabilityEnabled()) {
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, context);
        }
        StreamingCallableUnitCallBack callback = new StreamingCallableUnitCallBack(responseObserver, context);
        resource.getRuntime().invokeMethodAsync(resource.getService(), resource.getFunctionName(), callback,
                properties, requestParams);
    }
}
