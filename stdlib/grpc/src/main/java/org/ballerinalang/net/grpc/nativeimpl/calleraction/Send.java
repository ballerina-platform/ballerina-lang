/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.net.grpc.nativeimpl.calleraction;

import com.google.protobuf.Descriptors;
import io.netty.handler.codec.http.HttpHeaders;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.GrpcConstants.TAG_KEY_GRPC_MESSAGE_CONTENT;

/**
 * Extern function to respond the caller.
 *
 * @since 0.96.1
 */
public class Send {
    private static final Logger LOG = LoggerFactory.getLogger(Send.class);

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
}
