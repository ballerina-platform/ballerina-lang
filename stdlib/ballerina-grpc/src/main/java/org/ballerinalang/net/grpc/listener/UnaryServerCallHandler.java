package org.ballerinalang.net.grpc.listener;

import com.google.protobuf.Descriptors;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.grpc.ServerCall;
import org.ballerinalang.net.grpc.Status;

/**
 * Unary service call handler.
 * This is registered in Unary and server streaming services.
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
    public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call) {

        if (!call.getMethodDescriptor().getType().clientSendsOneMessage()) {
            throw new RuntimeException("asyncUnaryRequestCall is only for clientSendsOneMessage methods");
        }
        ServerCallStreamObserver<RespT> responseObserver =
                new ServerCallStreamObserver<>(call);

        return new UnaryServerCallListener(responseObserver, call);
    }

    private final class UnaryServerCallListener extends ServerCall.Listener<ReqT> {

        private final ServerCall<ReqT, RespT> call;
        private final ServerCallStreamObserver<RespT> responseObserver;
        private boolean canInvoke = true;
        private ReqT request;

        // Non private to avoid synthetic class
        UnaryServerCallListener(
                ServerCallStreamObserver<RespT> responseObserver,
                ServerCall<ReqT, RespT> call) {

            this.call = call;
            this.responseObserver = responseObserver;
        }

        @Override
        public void onMessage(ReqT request) {

            if (this.request != null) {
                // Safe to close the call, because the application has not yet been invoked
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
                // Safe to close the call, because the application has not yet been invoked
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
