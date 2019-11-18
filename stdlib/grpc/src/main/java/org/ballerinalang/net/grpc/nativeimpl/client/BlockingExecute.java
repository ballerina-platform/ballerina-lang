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
package org.ballerinalang.net.grpc.nativeimpl.client;

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.DataContext;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.stubs.BlockingStub;

import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.GrpcConstants.METHOD_DESCRIPTORS;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_STUB;
import static org.ballerinalang.net.grpc.GrpcConstants.TAG_KEY_GRPC_MESSAGE_CONTENT;
import static org.ballerinalang.net.grpc.Status.Code.INTERNAL;

/**
 * {@code BlockingExecute} is the BlockingExecute action implementation of the gRPC Connector.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "blockingExecute",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLIENT_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class BlockingExecute extends AbstractExecute {

    @SuppressWarnings("unchecked")
    public static Object blockingExecute(Strand strand, ObjectValue clientEndpoint, String methodName,
                                         Object payloadBValue, Object headerValues) {
        if (clientEndpoint == null) {
            return notifyErrorReply(INTERNAL, "Error while getting connector. gRPC client connector " +
                    "is not initialized properly");
        }

        Object connectionStub = clientEndpoint.getNativeData(SERVICE_STUB);
        if (connectionStub == null) {
            return notifyErrorReply(INTERNAL, "Error while getting connection stub. gRPC Client " +
                    "connector is not initialized properly");
        }

        if (methodName == null) {
            return notifyErrorReply(INTERNAL, "Error while processing the request. RPC endpoint " +
                    "doesn't set properly");
        }
        Map<String, MethodDescriptor> methodDescriptors = (Map<String, MethodDescriptor>) clientEndpoint.getNativeData
                (METHOD_DESCRIPTORS);
        if (methodDescriptors == null) {
            return notifyErrorReply(INTERNAL, "Error while processing the request. method descriptors " +
                    "doesn't set properly");
        }

        com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = methodDescriptors.get(methodName) != null
                ? methodDescriptors.get(methodName).getSchemaDescriptor() : null;
        if (methodDescriptor == null) {
            return notifyErrorReply(INTERNAL, "No registered method descriptor for '" + methodName + "'");
        }

        if (connectionStub instanceof BlockingStub) {
            Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(strand);
            observerContext.ifPresent(ctx -> {
                ctx.addTag(TAG_KEY_GRPC_MESSAGE_CONTENT, payloadBValue.toString());
            });
            Message requestMsg = new Message(methodDescriptor.getInputType().getName(), payloadBValue);
            // Update request headers when request headers exists in the context.
            HttpHeaders headers = null;
            if (headerValues != null && (TypeChecker.getType(headerValues).getTag() == TypeTags.OBJECT_TYPE_TAG)) {
                headers = (HttpHeaders) ((ObjectValue) headerValues).getNativeData(MESSAGE_HEADERS);
            }
            if (headers != null) {
                requestMsg.setHeaders(headers);
            }
            BlockingStub blockingStub = (BlockingStub) connectionStub;
            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {

                    DataContext dataContext = new DataContext(strand, new NonBlockingCallback(strand));
                    blockingStub.executeUnary(requestMsg, methodDescriptors.get(methodName), dataContext);
                } else {
                    return notifyErrorReply(INTERNAL, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                }
            } catch (Exception e) {
                return notifyErrorReply(INTERNAL, "gRPC Client Connector Error :" + e.getMessage());
            }
        } else {
            return notifyErrorReply(INTERNAL, "Error while processing the request message. Connection Sub " +
                    "type not supported");
        }
        return null;
    }
}
