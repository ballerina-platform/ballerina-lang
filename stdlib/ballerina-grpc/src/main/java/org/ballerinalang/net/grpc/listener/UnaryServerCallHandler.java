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
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.grpc.ServerCall;
import org.ballerinalang.net.grpc.Status;

/**
 * Interface to initiate processing of incoming remote calls for unary services.
 * This is used in unary and server streaming services.
 *
 * @param <ReqT> Request message type.
 * @param <RespT> Response message type.
 */
public class UnaryServerCallHandler<ReqT, RespT> extends ServerCallHandler<ReqT, RespT> {

    public Resource resource;

    public UnaryServerCallHandler(Descriptors.MethodDescriptor methodDescriptor, Resource resource) {
        super(methodDescriptor);
        this.resource = resource;
    }

    @Override
    public Listener<ReqT> startCall(ServerCall<ReqT, RespT> call) {
        if (!call.getMethodDescriptor().getType().clientSendsOneMessage()) {
            throw new RuntimeException("asyncUnaryRequestCall is only for clientSendsOneMessage methods");
        }
        ServerCallStreamObserver<RespT> responseObserver =
                new ServerCallStreamObserver<>(call);
        return new UnaryServerCallListener(responseObserver, call);
    }

    private final class UnaryServerCallListener extends Listener<ReqT> {

        private final ServerCall<ReqT, RespT> call;
        private final ServerCallStreamObserver<RespT> responseObserver;
        private boolean canInvoke = true;
        private ReqT request;

        UnaryServerCallListener(
                ServerCallStreamObserver<RespT> responseObserver,
                ServerCall<ReqT, RespT> call) {
            this.call = call;
            this.responseObserver = responseObserver;
        }

        @Override
        public void onMessage(ReqT request) {
            if (this.request != null) {
                call.close(
                        Status.Code.INTERNAL.toStatus().withDescription(TOO_MANY_REQUESTS), new
                                DefaultHttpHeaders());
                canInvoke = false;
                return;
            }
            this.request = request;
        }

        @Override
        public void onHalfClose() {
            if (!canInvoke) {
                return;
            }
            if (request == null) {
                call.close(
                        Status.Code.INTERNAL.toStatus().withDescription(MISSING_REQUEST), new DefaultHttpHeaders());
                return;
            }
            invoke(request, responseObserver);
        }

        @Override
        public void onCancel() {
            responseObserver.cancelled = true;
        }

        @Override
        public void onReady() {
        }

        public void invoke(ReqT request, ServerCallStreamObserver<RespT> responseObserver) {
            onMessageInvoke(resource, request, responseObserver);
        }
    }
}
