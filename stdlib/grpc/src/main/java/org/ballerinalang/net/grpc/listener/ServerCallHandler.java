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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.CallStreamObserver;
import org.ballerinalang.net.grpc.GrpcCallableUnitCallBack;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServerCall;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.exception.ServerRuntimeException;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ballerinalang.net.grpc.GrpcConstants.LISTENER_CONNECTION_FIELD;
import static org.ballerinalang.net.grpc.GrpcConstants.LISTENER_ID_FIELD;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.MessageUtils.getHeaderStruct;
import static org.ballerinalang.net.grpc.MessageUtils.getProgramFile;

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
    private static final Logger LOG = LoggerFactory.getLogger(ServerCallHandler.class);

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
    private BValue getConnectionParameter(Resource resource, StreamObserver responseObserver) {
        ProgramFile programFile = getProgramFile(resource);
        // generate client responder struct on request message with response observer and response msg type.
        BMap<String, BValue> clientEndpoint = BLangConnectorSPIUtil.createBStruct(programFile,
                GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC, GrpcConstants.CALLER_ACTION);
        clientEndpoint.addNativeData(GrpcConstants.RESPONSE_OBSERVER, responseObserver);
        clientEndpoint.addNativeData(GrpcConstants.RESPONSE_MESSAGE_DEFINITION, methodDescriptor.getOutputType());

        // create endpoint type instance on request.
        BMap<String, BValue> endpoint = BLangConnectorSPIUtil.createBStruct(programFile,
                GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC, GrpcConstants.SERVICE_ENDPOINT_TYPE);
        endpoint.put(LISTENER_CONNECTION_FIELD, clientEndpoint);
        endpoint.put(LISTENER_ID_FIELD, new BInteger(responseObserver.hashCode()));
        return endpoint;
    }

    /**
     * Checks whether service method has a response message.
     *
     * @return true if method response is empty, false otherwise
     */
    private boolean isEmptyResponse() {
        return methodDescriptor != null && MessageUtils.isEmptyResponse(methodDescriptor.getOutputType());
    }

    void onErrorInvoke(Resource resource, StreamObserver responseObserver, Message error) {
        if (resource == null) {
            String message = "Error in listener service definition. onError resource does not exists";
            LOG.error(message);
            throw new ServerRuntimeException(message);
        }
        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(resource, responseObserver);
        BType errorType = paramDetails.get(1).getVarType();
        BError errorStruct = MessageUtils.getConnectorError((BErrorType) errorType, error.getError());
        signatureParams[1] = errorStruct;
        BMap<String, BValue> headerStruct = getHeaderStruct(resource);
        if (headerStruct != null) {
            headerStruct.addNativeData(MESSAGE_HEADERS, error.getHeaders());
        }

        if (headerStruct != null && signatureParams.length == 3) {
            signatureParams[2] = headerStruct;
        }
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(null);
        Executor.submit(resource, callback, null, null, signatureParams);
    }

    void onMessageInvoke(Resource resource, Message request, StreamObserver responseObserver) {
        CallableUnitCallback callback = new GrpcCallableUnitCallBack(responseObserver, isEmptyResponse());
        Executor.submit(resource, callback, null, null, computeMessageParams(resource, request, responseObserver));
    }

    BValue[] computeMessageParams(Resource resource, Message request, StreamObserver responseObserver) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(resource, responseObserver);
        BMap<String, BValue> headerStruct = getHeaderStruct(resource);
        if (headerStruct != null) {
            headerStruct.addNativeData(MESSAGE_HEADERS, request.getHeaders());
        }
        BValue requestParam = request != null ? request.getbMessage() : null;
        if (requestParam != null) {
            signatureParams[1] = requestParam;
        }
        if (headerStruct != null) {
            signatureParams[signatureParams.length - 1] = headerStruct;
        }
        return signatureParams;
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
