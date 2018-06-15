/*
 * Copyright 2014, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.grpc.listener;

import com.google.protobuf.Descriptors;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.CallStreamObserver;
import org.ballerinalang.net.grpc.GrpcCallableUnitCallBack;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServerCall;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ballerinalang.net.grpc.MessageUtils.getHeaderStruct;
import static org.ballerinalang.net.grpc.MessageUtils.getProgramFile;

/**
 * Interface to initiate processing of incoming remote calls. Advanced applications and generated
 * code will implement this interface to allows servers to invoke service methods.
 *
 * @param <RequestT>  InboundMessage Message
 * @param <ResponseT> OutboundMessage Message
 */

public abstract class ServerCallHandler<RequestT, ResponseT> {

    static final String TOO_MANY_REQUESTS = "Too many requests";
    static final String MISSING_REQUEST = "Half-closed without a request";

    /**
     * Produce a non-{@code null} listener for the incoming call. Implementations are free to call
     * methods on {@code call} before this method has returned.
     * <p>
     * <p>If the implementation throws an exception, {@code call} will be closed with an error.
     * Implementations must not throw an exception if they started processing that may use {@code
     * call} on another thread.
     *
     * @param call object for responding to the remote client.
     * @return listener for processing incoming request messages for {@code call}
     */
    public abstract ServerCall.Listener<RequestT> startCall(ServerCall<RequestT, ResponseT> call);

    /**
     * Server call stream observer.
     * @param <RespT> Response message type.
     */
    public static final class ServerCallStreamObserver<RespT> extends CallStreamObserver<RespT> {

        final ServerCall<?, RespT> call;
        volatile boolean cancelled;
        private boolean sentHeaders;

        // Non private to avoid synthetic class
        ServerCallStreamObserver(ServerCall<?, RespT> call) {

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
        public void onNext(RespT response) {

            if (cancelled) {
                throw Status.Code.CANCELLED.toStatus().withDescription("call already cancelled").asRuntimeException();
            }
            if (!sentHeaders) {
                call.sendHeaders();
                sentHeaders = true;
            }
            call.sendMessage(response);
        }

        @Override
        public void onError(Throwable t) {

            call.close(Status.fromThrowable(t), new DefaultHttpHeaders());
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

    private Descriptors.MethodDescriptor methodDescriptor;
    private static final Logger LOG = LoggerFactory.getLogger(ServerCallHandler.class);

    ServerCallHandler(Descriptors.MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    /**
     * Returns endpoint instance which is used to respond to the client.
     *
     * @param responseObserver client responder instance.
     * @return instance of endpoint type.
     */
    private BValue getConnectionParameter(Resource resource, StreamObserver<ResponseT> responseObserver) {
        ProgramFile programFile = getProgramFile(resource);
        // generate client responder struct on request message with response observer and response msg type.
        BStruct clientEndpoint = BLangConnectorSPIUtil.createBStruct(programFile,
                GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC, GrpcConstants.CALLER_ACTION);
        clientEndpoint.addNativeData(GrpcConstants.RESPONSE_OBSERVER, responseObserver);
        clientEndpoint.addNativeData(GrpcConstants.RESPONSE_MESSAGE_DEFINITION, methodDescriptor.getOutputType());

        // create endpoint type instance on request.
        BStruct endpoint = BLangConnectorSPIUtil.createBStruct(programFile,
                GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC, GrpcConstants.SERVICE_ENDPOINT_TYPE);
        endpoint.setRefField(0, clientEndpoint);
        endpoint.setIntField(0, responseObserver.hashCode());
        return endpoint;
    }

    /**
     * Returns BValue object corresponding to the protobuf request message.
     *
     * @param requestMessage protobuf request message.
     * @return b7a message.
     */
    private BValue getRequestParameter(Resource resource, RequestT requestMessage, boolean isHeaderRequired) {
        if (resource == null || resource.getParamDetails() == null || resource.getParamDetails().size() > 3) {
            throw new RuntimeException("Invalid resource input arguments. arguments must not be greater than three");
        }

        List<ParamDetail> paramDetails = resource.getParamDetails();
        if ((isHeaderRequired && paramDetails.size() == 3) || (!isHeaderRequired && paramDetails.size() == 2)) {
            BType requestType = paramDetails.get(GrpcConstants.REQUEST_MESSAGE_PARAM_INDEX)
                    .getVarType();
            String requestName = paramDetails.get(GrpcConstants.REQUEST_MESSAGE_PARAM_INDEX)
                    .getVarName();
            return MessageUtils.generateRequestStruct((Message) requestMessage, getProgramFile(resource), requestName,
                    requestType);
        } else {
            return null;
        }
    }

    /**
     * Checks whether service method has a response message.
     *
     * @return true if method response is empty, false otherwise
     */
    boolean isEmptyResponse() {
        return methodDescriptor != null && MessageUtils.isEmptyResponse(methodDescriptor.getOutputType());
    }

    void onErrorInvoke(Resource resource, StreamObserver<ResponseT> responseObserver, Throwable t) {
        if (resource == null) {
            String message = "Error in listener service definition. onError resource does not exists";
            LOG.error(message);
            throw new RuntimeException(message);
        }
        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(resource, responseObserver);
        BType errorType = paramDetails.get(1).getVarType();
        BStruct errorStruct = MessageUtils.getConnectorError((BStructureType) errorType, t);
        signatureParams[1] = errorStruct;
        BStruct headerStruct = getHeaderStruct(resource);
        if (headerStruct != null) {
            //headerStruct.addNativeData(METADATA_KEY, TODO: Add headers object);
        }

        if (headerStruct != null && signatureParams.length == 3) {
            signatureParams[2] = headerStruct;
        }
        CallableUnitCallback callback = new GrpcCallableUnitCallBack<>(null);
        Executor.submit(resource, callback, null, null, signatureParams);
    }

    void onMessageInvoke(Resource resource, RequestT request, StreamObserver<ResponseT> responseObserver) {
        CallableUnitCallback callback = new GrpcCallableUnitCallBack<>(responseObserver, isEmptyResponse());
        Executor.submit(resource, callback, null, null, computeMessageParams(resource, request, responseObserver));
    }

    BValue[] computeMessageParams(Resource resource, RequestT request, StreamObserver<ResponseT> responseObserver) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        signatureParams[0] = getConnectionParameter(resource, responseObserver);
        BStruct headerStruct = getHeaderStruct(resource);
        if (headerStruct != null) {
            //headerStruct.addNativeData(METADATA_KEY, TODO: Add headers object);
        }
        BValue requestParam = getRequestParameter(resource, request, (headerStruct != null));
        if (requestParam != null) {
            signatureParams[1] = requestParam;
        }
        if (headerStruct != null) {
            signatureParams[signatureParams.length - 1] = headerStruct;
        }
        return signatureParams;
    }
}
