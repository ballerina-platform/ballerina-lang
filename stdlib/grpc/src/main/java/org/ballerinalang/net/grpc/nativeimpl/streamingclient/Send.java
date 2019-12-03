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
package org.ballerinalang.net.grpc.nativeimpl.streamingclient;

import com.google.protobuf.Descriptors;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.REQUEST_SENDER;
import static org.ballerinalang.net.grpc.GrpcConstants.TAG_KEY_GRPC_MESSAGE_CONTENT;

/**
 * Extern function for streaming respond to the server.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = GrpcConstants.PROTOCOL_PACKAGE_GRPC,
        functionName = "send",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = GrpcConstants.STREAMING_CLIENT,
                structPackage = GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class Send {

    private static final Logger LOG = LoggerFactory.getLogger(Send.class);

    public static Object send(Strand strand, ObjectValue streamConnection, Object responseValue) {
        StreamObserver requestSender = (StreamObserver) streamConnection.getNativeData(REQUEST_SENDER);
        if (requestSender == null) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while sending the " +
                            "message. endpoint does not exist")));
        } else {
            Descriptors.Descriptor inputType = (Descriptors.Descriptor) streamConnection.getNativeData(GrpcConstants
                    .REQUEST_MESSAGE_DEFINITION);
            try {
                // Add message content to observer context.
                Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(strand);
                observerContext.ifPresent(ctx -> ctx.addTag(TAG_KEY_GRPC_MESSAGE_CONTENT, responseValue.toString()));

                Message requestMessage = new Message(inputType.getName(), responseValue);
                requestSender.onNext(requestMessage);
            } catch (Exception e) {
                LOG.error("Error while sending request message to server.", e);
                return MessageUtils.getConnectorError(e);
            }
        }
        return null;
    }
}
