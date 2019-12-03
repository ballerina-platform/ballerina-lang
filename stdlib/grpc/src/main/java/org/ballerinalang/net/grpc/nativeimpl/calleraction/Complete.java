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
import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE;
import static org.ballerinalang.net.grpc.GrpcConstants.CALLER;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;

/**
 * Extern function to inform the caller, server finished sending messages.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "complete",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CALLER,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class Complete {
    private static final Logger LOG = LoggerFactory.getLogger(Complete.class);

    public static Object complete(Strand strand, ObjectValue endpointClient) {
        StreamObserver responseObserver = MessageUtils.getResponseObserver(endpointClient);
        Descriptors.Descriptor outputType = (Descriptors.Descriptor) endpointClient.getNativeData(GrpcConstants
                .RESPONSE_MESSAGE_DEFINITION);
        Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(strand);

        if (responseObserver == null) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while initializing " +
                            "connector. response sender does not exist")));
        } else {
            try {
                if (!MessageUtils.isEmptyResponse(outputType)) {
                    responseObserver.onCompleted();
                }
                observerContext.ifPresent(ctx -> ctx.addTag(TAG_KEY_HTTP_STATUS_CODE,
                        HttpResponseStatus.OK.codeAsText().toString()));
            } catch (Exception e) {
                LOG.error("Error while sending complete message to caller.", e);
                return MessageUtils.getConnectorError(e);
            }
        }
        return null;
    }
}
