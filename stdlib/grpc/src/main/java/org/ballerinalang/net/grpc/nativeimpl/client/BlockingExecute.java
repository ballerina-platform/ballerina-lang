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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.BlockingStub;
import org.ballerinalang.net.http.DataContext;

import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_REF_INDEX;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;
import static org.ballerinalang.net.grpc.GrpcConstants.METHOD_DESCRIPTORS;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_STUB;

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
    private static final int MESSAGE_HEADER_REF_INDEX = 2;

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> clientEndpoint = (BMap<String, BValue>) context.getRefArgument(CLIENT_ENDPOINT_REF_INDEX);
        if (clientEndpoint == null) {
            notifyErrorReply(context, "Error while getting connector. gRPC client connector " +
                    "is not initialized properly");
            callback.notifySuccess();
            return;
        }
        
        Object connectionStub = clientEndpoint.getNativeData(SERVICE_STUB);
        if (connectionStub == null) {
            notifyErrorReply(context, "Error while getting connection stub. gRPC Client " +
                    "connector is not initialized properly");
            callback.notifySuccess();
            return;
        }
        String methodName = context.getStringArgument(0);
        if (methodName == null) {
            notifyErrorReply(context, "Error while processing the request. RPC endpoint " +
                    "doesn't set properly");
            callback.notifySuccess();
            return;
        }
        Map<String, MethodDescriptor> methodDescriptors = (Map<String, MethodDescriptor>) clientEndpoint.getNativeData
                (METHOD_DESCRIPTORS);
        if (methodDescriptors == null) {
            notifyErrorReply(context, "Error while processing the request. method descriptors " +
                    "doesn't set properly");
            callback.notifySuccess();
            return;
        }
        
        com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = methodDescriptors.get(methodName) != null
                ? methodDescriptors.get(methodName).getSchemaDescriptor() : null;
        if (methodDescriptor == null) {
            notifyErrorReply(context, "No registered method descriptor for '" + methodName + "'");
            callback.notifySuccess();
            return;
        }
        
        if (connectionStub instanceof BlockingStub) {
            BValue payloadBValue = context.getRefArgument(1);
            Message requestMsg = new Message(methodDescriptor.getInputType().getName(), payloadBValue);
            // Update request headers when request headers exists in the context.
            BValue headerValues = context.getNullableRefArgument(MESSAGE_HEADER_REF_INDEX);
            HttpHeaders headers = null;
            if (headerValues != null && headerValues.getType().getTag() == TypeTags.OBJECT_TYPE_TAG) {
                headers = (HttpHeaders) ((BMap<String, BValue>) headerValues).getNativeData(MESSAGE_HEADERS);
            }
            if (headers != null) {
                requestMsg.setHeaders(headers);
            }
            BlockingStub blockingStub = (BlockingStub) connectionStub;
            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {

                    DataContext dataContext = new DataContext(context, callback, null);
                    blockingStub.executeUnary(requestMsg, methodDescriptors.get(methodName),
                            dataContext);
                } else {
                    notifyErrorReply(context, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                    callback.notifySuccess();
                }
            } catch (RuntimeException | GrpcClientException e) {
                notifyErrorReply(context, "gRPC Client Connector Error :" + e.getMessage());
                callback.notifySuccess();
            }
        } else {
            notifyErrorReply(context, "Error while processing the request message. Connection Sub " +
                    "type not supported");
            callback.notifySuccess();
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
