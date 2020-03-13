/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.net.grpc.nativeimpl.caller;

import com.google.protobuf.Descriptors;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.grpc.listener.ServerCallHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.STATUS_CODE_GROUP_SUFFIX;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE_GROUP;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.GrpcConstants.TAG_KEY_GRPC_MESSAGE_CONTENT;
import static org.ballerinalang.net.grpc.MessageUtils.getMappingHttpStatusCode;

/**
 * Utility methods represents actions for the caller.
 *
 * @since 1.0.0
 */
public class FunctionUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FunctionUtils.class);

    /**
     * Extern function to inform the caller, server finished sending messages.
     *
     * @param endpointClient caller instance.
     * @return Error if there is an error while informing the caller, else returns nil
     */
    public static Object externComplete(ObjectValue endpointClient) {
        StreamObserver responseObserver = MessageUtils.getResponseObserver(endpointClient);
        Descriptors.Descriptor outputType = (Descriptors.Descriptor) endpointClient.getNativeData(GrpcConstants
                .RESPONSE_MESSAGE_DEFINITION);
        Optional<ObserverContext> observerContext =
                ObserveUtils.getObserverContextOfCurrentFrame(Scheduler.getStrand());

        if (responseObserver == null) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while initializing " +
                            "connector. response sender does not exist")));
        } else {
            try {
                if (!MessageUtils.isEmptyResponse(outputType)) {
                    responseObserver.onCompleted();
                }
                observerContext.ifPresent(ctx -> ctx.addTag(TAG_KEY_HTTP_STATUS_CODE_GROUP,
                        HttpResponseStatus.OK.code() / 100 + STATUS_CODE_GROUP_SUFFIX));
            } catch (Exception e) {
                LOG.error("Error while sending complete message to caller.", e);
                return MessageUtils.getConnectorError(e);
            }
        }
        return null;
    }

    /**
     * Extern function to check whether caller has terminated the connection in between.
     *
     * @param endpointClient caller instance.
     * @return True if caller has terminated the connection, false otherwise.
     */
    public static boolean externIsCancelled(ObjectValue endpointClient) {
        StreamObserver responseObserver = MessageUtils.getResponseObserver(endpointClient);

        if (responseObserver instanceof ServerCallHandler.ServerCallStreamObserver) {
            ServerCallHandler.ServerCallStreamObserver serverCallStreamObserver = (ServerCallHandler
                    .ServerCallStreamObserver) responseObserver;
            return serverCallStreamObserver.isCancelled();
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * Extern function to respond the caller.
     *
     * @param endpointClient caller instance.
     * @param responseValue response message.
     * @param headerValues custom metadata to pass with response.
     * @return Error if there is an error while responding the caller, else returns nil
     */
    public static Object externSend(ObjectValue endpointClient, Object responseValue,
                                    Object headerValues) {
        StreamObserver responseObserver = MessageUtils.getResponseObserver(endpointClient);
        Descriptors.Descriptor outputType = (Descriptors.Descriptor) endpointClient.getNativeData(GrpcConstants
                .RESPONSE_MESSAGE_DEFINITION);
        Optional<ObserverContext> observerContext =
                ObserveUtils.getObserverContextOfCurrentFrame(Scheduler.getStrand());

        if (responseObserver == null) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while initializing " +
                            "connector. Response sender does not exist")));
        } else {
            try {
                // If there is no response message like conn -> send(), system doesn't send the message.
                if (!MessageUtils.isEmptyResponse(outputType)) {
                    //Message responseMessage = MessageUtils.generateProtoMessage(responseValue, outputType);
                    Message responseMessage = new Message(outputType.getName(), responseValue);
                    // Update response headers when request headers exists in the context.
                    HttpHeaders headers = null;
                    if (headerValues != null &&
                            (TypeChecker.getType(headerValues).getTag() == TypeTags.OBJECT_TYPE_TAG)) {
                        headers = (HttpHeaders) ((ObjectValue) headerValues).getNativeData(MESSAGE_HEADERS);
                    }
                    if (headers != null) {
                        responseMessage.setHeaders(headers);
                        headers.entries().forEach(
                                x -> observerContext.ifPresent(ctx -> ctx.addTag(x.getKey(), x.getValue())));
                    }
                    responseObserver.onNext(responseMessage);
                    observerContext.ifPresent(ctx -> ctx.addTag(TAG_KEY_GRPC_MESSAGE_CONTENT,
                            responseValue.toString()));
                }
            } catch (Exception e) {
                LOG.error("Error while sending client response.", e);
                return MessageUtils.getConnectorError(e);
            }
        }
        return null;
    }

    /**
     * Extern function to send server error the caller.
     *
     * @param endpointClient caller instance.
     * @param statusCode gRPC error status code.
     * @param errorMsg error message.
     * @param headerValues custom metadata to pass with response.
     * @return Error if there is an error while responding the caller, else returns nil
     */
    public static Object externSendError(ObjectValue endpointClient, long statusCode, String errorMsg,
                                         Object headerValues) {
        StreamObserver responseObserver = MessageUtils.getResponseObserver(endpointClient);
        Optional<ObserverContext> observerContext =
                ObserveUtils.getObserverContextOfCurrentFrame(Scheduler.getStrand());
        if (responseObserver == null) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while sending the " +
                            "error. Response observer not found.")));
        } else {
            try {
                // Update response headers when request headers exists in the context.
                HttpHeaders headers = null;
                Message errorMessage = new Message(new StatusRuntimeException(Status.fromCodeValue((int) statusCode)
                        .withDescription(errorMsg)));
                if (headerValues != null &&
                        (TypeChecker.getType(headerValues).getTag() == TypeTags.OBJECT_TYPE_TAG)) {
                    headers = (HttpHeaders) ((ObjectValue) headerValues).getNativeData(MESSAGE_HEADERS);
                }
                if (headers != null) {
                    errorMessage.setHeaders(headers);
                    headers.entries().forEach(
                            x -> observerContext.ifPresent(ctx -> ctx.addTag(x.getKey(), x.getValue())));
                }
                observerContext.ifPresent(ctx -> ctx.addTag(TAG_KEY_HTTP_STATUS_CODE_GROUP,
                        getMappingHttpStatusCode((int) statusCode) / 100 + STATUS_CODE_GROUP_SUFFIX));
                responseObserver.onError(errorMessage);
            } catch (Exception e) {
                LOG.error("Error while sending error to caller.", e);
                return MessageUtils.getConnectorError(e);
            }
        }
        return null;
    }
}
