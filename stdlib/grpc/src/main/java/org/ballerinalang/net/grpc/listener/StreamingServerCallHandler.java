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
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.ServerCall;
import org.ballerinalang.net.grpc.ServiceResource;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.callback.StreamingCallableUnitCallBack;
import org.ballerinalang.net.grpc.callback.UnaryCallableUnitCallBack;
import org.ballerinalang.net.grpc.exception.GrpcServerException;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface to initiate processing of incoming remote calls for streaming services.
 * This is used in client and bidirectional streaming services.
 * @since 0.980.0
 */
public class StreamingServerCallHandler extends ServerCallHandler {

    private final Map<String, ServiceResource> resourceMap;

    public StreamingServerCallHandler(Descriptors.MethodDescriptor methodDescriptor, Map<String, ServiceResource>
            resourceMap) throws GrpcServerException {
        super(methodDescriptor);
        validateStreamingResources(resourceMap);
        this.resourceMap = resourceMap;
    }

    @Override
    public Listener startCall(ServerCall call) {
        ServerCallStreamObserver responseObserver = new ServerCallStreamObserver(call);
        StreamObserver requestObserver = invoke(responseObserver, call.getObserverContext());
        return new StreamingServerCallListener(requestObserver, responseObserver);
    }

    private StreamObserver invoke(StreamObserver responseObserver, ObserverContext context) {
        ServiceResource onOpen = resourceMap.get(GrpcConstants.ON_OPEN_RESOURCE);
        StreamingCallableUnitCallBack callback = new StreamingCallableUnitCallBack(responseObserver, context);
        Map<String, Object> properties = new HashMap<>();
        if (ObserveUtils.isObservabilityEnabled()) {
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, context);
        }
        Executor.submit(onOpen.getScheduler(), onOpen.getService(), onOpen.getFunctionName(), callback, properties,
                computeMessageParams(onOpen, null, responseObserver));
        callback.available.acquireUninterruptibly();

        return new StreamObserver() {
            @Override
            public void onNext(Message value) {
                ServiceResource onMessage = resourceMap.get(GrpcConstants.ON_MESSAGE_RESOURCE);

                CallableUnitCallback callback = new StreamingCallableUnitCallBack(responseObserver, context);
                Executor.submit(onMessage.getScheduler(), onMessage.getService(), onMessage.getFunctionName(),
                        callback, properties, computeMessageParams(onMessage, value, responseObserver));
            }

            @Override
            public void onError(Message error) {
                ServiceResource onError = resourceMap.get(GrpcConstants.ON_ERROR_RESOURCE);
                onErrorInvoke(onError, responseObserver, error, context);
            }

            @Override
            public void onCompleted() {
                ServiceResource onCompleted = resourceMap.get(GrpcConstants.ON_COMPLETE_RESOURCE);
                CallableUnitCallback callback = new UnaryCallableUnitCallBack(responseObserver, Boolean.FALSE, context);
                Executor.submit(onCompleted.getScheduler(), onCompleted.getService(), onCompleted.getFunctionName(),
                        callback, properties, computeMessageParams(onCompleted, null, responseObserver));
            }
        };
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

    private void validateStreamingResources(Map<String, ServiceResource> resourceMap) throws GrpcServerException {
        if (resourceMap.isEmpty()) {
            throw new GrpcServerException("Streaming service resources don't exist.");
        }
        if (!resourceMap.containsKey(GrpcConstants.ON_OPEN_RESOURCE)) {
            throw new GrpcServerException("Streaming service onOpen resource doesn't exist.");
        }
        if (!resourceMap.containsKey(GrpcConstants.ON_MESSAGE_RESOURCE)) {
            throw new GrpcServerException("Streaming service onMessage resource doesn't exist.");
        }
        if (!resourceMap.containsKey(GrpcConstants.ON_ERROR_RESOURCE)) {
            throw new GrpcServerException("Streaming service onError resource doesn't exist.");
        }
        if (!resourceMap.containsKey(GrpcConstants.ON_COMPLETE_RESOURCE)) {
            throw new GrpcServerException("Streaming service onComplete resource doesn't exist.");
        }
    }
}
