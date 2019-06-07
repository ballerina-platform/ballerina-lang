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
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.net.grpc.CallStreamObserver;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServerCall;
import org.ballerinalang.net.grpc.ServiceResource;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.callback.StreamingCallableUnitCallBack;
import org.ballerinalang.net.grpc.callback.UnaryCallableUnitCallBack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.CALLER_ID;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.MessageUtils.getHeaderObject;

/**
 * Interface to initiate processing of incoming remote calls.
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * @since 0.980.0
 */

public abstract class ServerCallHandler {

    static final String TOO_MANY_REQUESTS = "Too many requests";
    static final String MISSING_REQUEST = "Half-closed without a request";
    private Descriptors.MethodDescriptor methodDescriptor;

    ServerCallHandler(Descriptors.MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    /**
     * Returns a listener for the incoming call.
     *
     * @param call object for responding to the remote client.
     * @return listener for processing incoming request messages for {@code call}
     */
    public abstract Listener startCall(ServerCall call);

    /**
     * Receives notifications from an observable stream of response messages from server side.
     *
     */
    public static final class ServerCallStreamObserver implements CallStreamObserver {

        final ServerCall call;
        volatile boolean cancelled;
        private boolean sentHeaders;

        ServerCallStreamObserver(ServerCall call) {
            this.call = call;
        }

        @Override
        public void setMessageCompression(boolean enable) {
            call.setMessageCompression(enable);
        }

        public void setCompression(String compression) {
            call.setCompression(compression);
        }

        @Override
        public void onNext(Message response) {
            if (cancelled) {
                throw Status.Code.CANCELLED.toStatus().withDescription("call already cancelled").asRuntimeException();
            }
            if (!sentHeaders) {
                call.sendHeaders(response.getHeaders());
                sentHeaders = true;
            }
            call.sendMessage(response);
        }

        @Override
        public void onError(Message error) {
            if (!sentHeaders) {
                call.sendHeaders(error.getHeaders());
                sentHeaders = true;
            }
            call.close(Status.fromThrowable(error.getError()), new DefaultHttpHeaders());
        }

        @Override
        public void onCompleted() {
            if (cancelled) {
                throw Status.Code.CANCELLED.toStatus().withDescription("call already cancelled").asRuntimeException();
            } else {
                call.close(Status.Code.OK.toStatus(), new DefaultHttpHeaders());
            }
        }

        @Override
        public boolean isReady() {
            return call.isReady();
        }

        public boolean isCancelled() {
            return call.isCancelled();
        }
    }

    /**
     * Returns endpoint instance which is used to respond to the caller.
     *
     * @param responseObserver client responder instance.
     * @return instance of endpoint type.
     */
    private ObjectValue getConnectionParameter(StreamObserver responseObserver) {
        // generate client responder struct on request message with response observer and response msg type.
        ObjectValue clientEndpoint = BallerinaValues.createObjectValue(GrpcConstants.PROTOCOL_GRPC_PKG_ID,
                GrpcConstants.CALLER);
        clientEndpoint.set(CALLER_ID, responseObserver.hashCode());
        clientEndpoint.addNativeData(GrpcConstants.RESPONSE_OBSERVER, responseObserver);
        clientEndpoint.addNativeData(GrpcConstants.RESPONSE_MESSAGE_DEFINITION, methodDescriptor.getOutputType());
        return clientEndpoint;
    }

    /**
     * Checks whether service method has a response message.
     *
     * @return true if method response is empty, false otherwise
     */
    private boolean isEmptyResponse() {
        return methodDescriptor != null && MessageUtils.isEmptyResponse(methodDescriptor.getOutputType());
    }

    void onErrorInvoke(ServiceResource resource, StreamObserver responseObserver, Message error,
                       ObserverContext context) {
        List<BType> signatureParams = resource.getParamTypes();
        Object[] paramValues = new Object[signatureParams.size() * 2];
        paramValues[0] = getConnectionParameter(responseObserver);
        paramValues[1] = true;

        ErrorValue errorStruct = MessageUtils.getConnectorError(error.getError());
        paramValues[2] = errorStruct;
        paramValues[3] = true;

        ObjectValue headerStruct = null;

        if (resource.isHeaderRequired()) {
            headerStruct = getHeaderObject();
            headerStruct.addNativeData(MESSAGE_HEADERS, error.getHeaders());
        }

        if (headerStruct != null && signatureParams.size() == 3) {
            paramValues[4] = headerStruct;
            paramValues[5] = true;
        }

        Map<String, Object> properties = new HashMap<>();
        if (ObserveUtils.isObservabilityEnabled()) {
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, context);
        }
        CallableUnitCallback callback = new StreamingCallableUnitCallBack(null, context);
        Executor.submit(resource.getScheduler(), resource.getService(), resource.getFunctionName(), callback,
                properties, paramValues);
    }

    void onMessageInvoke(ServiceResource resource, Message request, StreamObserver responseObserver,
                         ObserverContext context) {
        CallableUnitCallback callback = new UnaryCallableUnitCallBack(responseObserver, isEmptyResponse(), context);
        Object[] requestParams = computeMessageParams(resource, request, responseObserver);
        Map<String, Object> properties = new HashMap<>();
        if (ObserveUtils.isObservabilityEnabled()) {
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, context);
        }
        Executor.submit(resource.getScheduler(), resource.getService(), resource.getFunctionName(), callback,
                properties, requestParams);
    }

    Object[] computeMessageParams(ServiceResource resource, Message request, StreamObserver responseObserver) {
        List<BType> signatureParams = resource.getParamTypes();
        Object[] paramValues = new Object[signatureParams.size() * 2];
        paramValues[0] = getConnectionParameter(responseObserver);
        paramValues[1] = true;

        ObjectValue headerStruct = null;
        if (resource.isHeaderRequired()) {
            headerStruct = getHeaderObject();
            headerStruct.addNativeData(MESSAGE_HEADERS, request.getHeaders());
        }
        Object requestParam = request != null ? request.getbMessage() : null;
        if (requestParam != null) {
            paramValues[2] = requestParam;
            paramValues[3] = true;
        }
        if (headerStruct != null && signatureParams.size() == 3) {
            paramValues[4] = headerStruct;
            paramValues[5] = true;
        }
        return paramValues;
    }

    /**
     * Callbacks for consuming incoming RPC messages.
     *
     * <p>Any contexts are guaranteed to arrive before any messages, which are guaranteed before half
     * close, which is guaranteed before completion.
     *
     * <p>Implementations are free to block for extended periods of time. Implementations are not
     * required to be thread-safe.
     */
    public interface Listener {

        /**
         * A request message has been received. For streaming calls, there may be zero or more request
         * messages.
         *
         * @param message a received request message.
         */
        void onMessage(Message message);

        /**
         * The client completed all message sending. However, the call may still be cancelled.
         */
        void onHalfClose();

        /**
         * The call was cancelled and the server is encouraged to abort processing to save resources,
         * since the client will not process any further messages. Cancellations can be caused by
         * timeouts, explicit cancellation by the client, network errors, etc.
         *
         * <p>There will be no further callbacks for the call.
         */
        void onCancel();

        /**
         * The call is considered complete and {@link #onCancel} is guaranteed not to be called.
         * However, the client is not guaranteed to have received all messages.
         *
         * <p>There will be no further callbacks for the call.
         */
        void onComplete();
    }
}
