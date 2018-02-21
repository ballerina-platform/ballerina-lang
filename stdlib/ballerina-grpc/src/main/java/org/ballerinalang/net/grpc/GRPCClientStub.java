package org.ballerinalang.net.grpc;

import com.google.protobuf.Descriptors;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.stub.ClientCalls;
import org.ballerinalang.net.grpc.definition.ServiceProto;
import org.ballerinalang.net.grpc.exception.GrpcServerException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * The stockquote service definition.
 * </pre>
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.7.0)",
        comments = "Source: xx.proto")
public final class GRPCClientStub {
private static ServiceProto grpcServiceProto;
    private static Descriptors.FileDescriptor fileDescriptor;
    private static String serviceFullName;
    private static String serviceName;
    // Static method descriptors that strictly reflect the proto.
    private static Map methodDescriptorMap = new HashMap<Integer, MethodDescriptor<Message, Message>>();

    public GRPCClientStub(ServiceProto serviceProto) {
        grpcServiceProto = serviceProto;
        this.fileDescriptor = serviceProto.getDescriptor();
        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.getFile().getServices().get(0);
        this.serviceFullName = serviceDescriptor.getFullName();
        this.serviceName = serviceDescriptor.getName();
        for (int i = 0; i < serviceDescriptor.getMethods().size(); i++) {
            String methodName = serviceDescriptor.getMethods().get(i).getName();
            String reqMessageName = serviceDescriptor.getMethods().get(i).getInputType().getName();
            String resMessageName = serviceDescriptor.getMethods().get(i).getOutputType().getName();
            MethodDescriptor<Message, Message> methodExecute =
                    MethodDescriptor.<Message, Message>newBuilder()
                            .setType(getMethodType(i))
                            .setFullMethodName(generateFullMethodName(
                                    serviceFullName, methodName))
                            .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                    (Message) Message.newBuilder(reqMessageName).build()))
                            .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                    (Message) Message.newBuilder(resMessageName).build()))
                            .setSchemaDescriptor(new MessageServiceMethodDescriptorSupplier(methodName, fileDescriptor))
                            .build();
            methodDescriptorMap.put(i, methodExecute);
        }
    }

    public static MethodDescriptor.MethodType getMethodType(int methodID) {

        com.google.protobuf.DescriptorProtos.MethodDescriptorProto proto = grpcServiceProto.getSet()
                .getService(0).getMethodList().get(methodID);
        if (proto.getClientStreaming() && proto.getServerStreaming())
            return MethodDescriptor.MethodType.BIDI_STREAMING;
        else if (!(proto.getClientStreaming() || proto.getServerStreaming())) {
            return MethodDescriptor.MethodType.UNARY;
        } else if (proto.getServerStreaming()) {
            return MethodDescriptor.MethodType.SERVER_STREAMING;
        } else if (proto.getClientStreaming()) {
            return MethodDescriptor.MethodType.CLIENT_STREAMING;
        } else {
            return MethodDescriptor.MethodType.UNKNOWN;
        }
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public GRPCNonBlockingStub newNonBlockingStub(Channel channel) {

        return new GRPCNonBlockingStub(channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public GRPCBlockingStub newBlockingStub(Channel channel) {

        return new GRPCBlockingStub(channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public GRPCFutureStub newFutureStub(Channel channel) {

        return new GRPCFutureStub(channel);
    }

    public static ServiceProto getGrpcServiceProto() {

        return grpcServiceProto;
    }

    /**
     * <pre>
     * The stockquote service definition.
     * </pre>
     */
    public static abstract class MessageServiceImplBase implements io.grpc.BindableService {

        /**
         */
        public void getMethod(Message request,
                              io.grpc.stub.StreamObserver<Message> responseObserver, int methodID) {

            MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(methodID);
            if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                asyncUnimplementedUnaryCall((MethodDescriptor<Message, Message>) methodDescriptorMap.get(methodID),
                        responseObserver);
            } else {
                throw new RuntimeException("");
            }
        }

        /**
         */
        public io.grpc.stub.StreamObserver<Message> getMethod(
                io.grpc.stub.StreamObserver<Message> responseObserver, int methodID) {

            MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(methodID);
            if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                asyncUnimplementedUnaryCall((MethodDescriptor<Message, Message>) methodDescriptorMap.get(methodID),
                        responseObserver);
                return null;
            } else if (methodType.equals(MethodDescriptor.MethodType.SERVER_STREAMING)) {
                return asyncUnimplementedStreamingCall((MethodDescriptor<Message, Message>) methodDescriptorMap
                                .get(methodID),
                        responseObserver);
            } else if (methodType.equals(MethodDescriptor.MethodType.CLIENT_STREAMING)) {
                return asyncUnimplementedStreamingCall((MethodDescriptor<Message, Message>) methodDescriptorMap
                                .get(methodID),
                        responseObserver);
            } else if (methodType.equals(MethodDescriptor.MethodType.BIDI_STREAMING)) {
                return asyncUnimplementedStreamingCall((MethodDescriptor<Message, Message>) methodDescriptorMap
                                .get(methodID),
                        responseObserver);
            } else {
                throw new RuntimeException("errr");
            }
        }

        @Override
        public final ServerServiceDefinition bindService() {

            ServerServiceDefinition.Builder builder = ServerServiceDefinition.builder(getServiceDescriptor());
            for (int i = 0; i < methodDescriptorMap.size(); i++) {
                MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(i);
                if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                    builder = builder.addMethod(
                            (MethodDescriptor<Message, Message>) methodDescriptorMap.get(i),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            Message,
                                            Message>(
                                            this, i)));
                } else if (methodType.equals(MethodDescriptor.MethodType.SERVER_STREAMING)) {
                    builder = builder.addMethod(
                            (MethodDescriptor<Message, Message>) methodDescriptorMap.get(i),
                            asyncServerStreamingCall(
                                    new MethodHandlers<
                                            Message,
                                            Message>(
                                            this, i)));
                } else if (methodType.equals(MethodDescriptor.MethodType.CLIENT_STREAMING)) {
                    builder = builder.addMethod(
                            (MethodDescriptor<Message, Message>) methodDescriptorMap.get(i),
                            asyncClientStreamingCall(
                                    new MethodHandlers<
                                            Message,
                                            Message>(
                                            this, i)));
                } else if (methodType.equals(MethodDescriptor.MethodType.BIDI_STREAMING)) {
                    builder = builder.addMethod(
                            (MethodDescriptor<Message, Message>) methodDescriptorMap.get(i),
                            asyncBidiStreamingCall(
                                    new MethodHandlers<
                                            Message,
                                            Message>(
                                            this, i)));
                } else {
                    throw new RuntimeException("invalid type of method");
                }
            }
            return builder.build();
        }
    }

    /**
     * <pre>
     * The stockquote service definition.
     * </pre>
     */
    public static final class GRPCNonBlockingStub extends io.grpc.stub.AbstractStub<GRPCNonBlockingStub> {

        private GRPCNonBlockingStub(Channel channel) {

            super(channel);
        }

        private GRPCNonBlockingStub(Channel channel,
                                    CallOptions callOptions) {

            super(channel, callOptions);
        }

        @Override
        protected GRPCNonBlockingStub build(Channel channel,
                                            CallOptions callOptions) {

            return new GRPCNonBlockingStub(channel, callOptions);
        }

        public io.grpc.stub.StreamObserver<Message> executeNonBlockingClientStreaming(
                io.grpc.stub.StreamObserver<Message> responseObserver, int methodID) {

            return asyncClientStreamingCall(
                    getChannel().newCall((MethodDescriptor<Message, Message>) methodDescriptorMap.get(methodID), getCallOptions()), responseObserver);
        }

        /**
         */
        public void executeNonBlockingUnary(Message request,
                                            io.grpc.stub.StreamObserver<Message> responseObserver, int methodID) {

            MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(methodID);
            if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                ClientCalls.asyncUnaryCall(
                        getChannel().newCall((MethodDescriptor<Message, Message>) methodDescriptorMap.get(methodID),
                                getCallOptions()), request, responseObserver);

            }

        }

        public void executeNonBlockingServerStreaming(Message request,
                                                      io.grpc.stub.StreamObserver<Message> responseObserver, int methodID) {

            asyncServerStreamingCall(
                    getChannel().newCall((MethodDescriptor<Message, Message>) methodDescriptorMap.get(methodID), getCallOptions()), request, responseObserver);
        }
    }

    /**
     * <pre>
     * The stockquote service definition.
     * </pre>
     */
    public static final class GRPCBlockingStub extends io.grpc.stub.AbstractStub<GRPCBlockingStub> {

        private GRPCBlockingStub(Channel channel) {

            super(channel);
        }

        private GRPCBlockingStub(Channel channel,
                                 CallOptions callOptions) {

            super(channel, callOptions);
        }

        @Override
        protected GRPCBlockingStub build(Channel channel,
                                         CallOptions callOptions) {

            return new GRPCBlockingStub(channel, callOptions);
        }

        /**
         */
        public Message executeBlockingUnary(Message request, int methodID) {

            MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(methodID);
            if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                return blockingUnaryCall(
                        getChannel(), (MethodDescriptor<Message, Message>) methodDescriptorMap.get(methodID),
                        getCallOptions(), request);
            } else {
                throw new RuntimeException("invalid method type");
            }

        }

    }

    /**
     * <pre>
     * The stockquote service definition.
     * </pre>
     */
    public static final class GRPCFutureStub extends io.grpc.stub.AbstractStub<GRPCFutureStub> {

        private GRPCFutureStub(Channel channel) {

            super(channel);
        }

        private GRPCFutureStub(Channel channel,
                               CallOptions callOptions) {

            super(channel, callOptions);
        }

        @Override
        protected GRPCFutureStub build(Channel channel,
                                       CallOptions callOptions) {

            return new GRPCFutureStub(channel, callOptions);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<Message> getMethod(
                Message request, int methodID) {

            MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(methodID);
            if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                return futureUnaryCall(
                        getChannel().newCall((MethodDescriptor<Message, Message>) methodDescriptorMap.get(methodID),
                                getCallOptions()), request);

            } else {
                return null;
            }
        }

    }

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {

        private final MessageServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(MessageServiceImplBase serviceImpl, int methodId) {

            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {

            MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(methodId);
            if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                serviceImpl.getMethod((Message) request,
                        (io.grpc.stub.StreamObserver<Message>) responseObserver, methodId);
            } else {
                throw new AssertionError();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {

            MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(methodId);
            if (methodType.equals(MethodDescriptor.MethodType.CLIENT_STREAMING)) {
                return (io.grpc.stub.StreamObserver<Req>) serviceImpl.getMethod(
                        (io.grpc.stub.StreamObserver<Message>) responseObserver, methodId);
            } else {
                throw new AssertionError();
            }
        }
    }

    private static abstract class MessageServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {

        Descriptors.FileDescriptor fileDescriptor;

        public MessageServiceBaseDescriptorSupplier() {

        }

        MessageServiceBaseDescriptorSupplier(Descriptors.FileDescriptor fileDescriptor) {

            this.fileDescriptor = fileDescriptor;
        }

        @Override
        public Descriptors.FileDescriptor getFileDescriptor() {

            return this.fileDescriptor;
        }

        @Override
        public Descriptors.ServiceDescriptor getServiceDescriptor() {

            return getFileDescriptor().findServiceByName(serviceName);
        }
    }

    private static final class MessageServiceFileDescriptorSupplier
            extends MessageServiceBaseDescriptorSupplier {

        MessageServiceFileDescriptorSupplier(Descriptors.FileDescriptor fileDescriptor) {

        }
    }

    private static final class MessageServiceMethodDescriptorSupplier
            extends MessageServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {

        private final String methodName;

        MessageServiceMethodDescriptorSupplier(String methodName,
                                               Descriptors.FileDescriptor fileDescriptor) {

            this.fileDescriptor = fileDescriptor;
            this.methodName = methodName;
            List<Descriptors.Descriptor> messageDiscriptors = fileDescriptor.getMessageTypes();
            for (Descriptors.Descriptor descriptor : messageDiscriptors) {
                try {
                    MessageRegistry.getInstance().addMessageDescriptor(descriptor.getName(), descriptor);
                } catch (GrpcServerException e) {
                    // TODO: 2/6/18 proper log
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Descriptors.MethodDescriptor getMethodDescriptor() {

            return getServiceDescriptor().findMethodByName(methodName);
        }
    }

    private static volatile ServiceDescriptor serviceDescriptor;

    public static ServiceDescriptor getServiceDescriptor() {

        ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (GRPCClientStub.class) {
                result = serviceDescriptor;
                if (result == null) {
                    ServiceDescriptor.Builder builder = ServiceDescriptor.newBuilder(serviceFullName)
                            .setSchemaDescriptor(new MessageServiceFileDescriptorSupplier(fileDescriptor));
                    for (int i = 0; i < methodDescriptorMap.size(); i++) {
                        builder = builder.addMethod((MethodDescriptor<Message, Message>) methodDescriptorMap.get(i));
                    }
                    serviceDescriptor = builder.build();
                }
            }
        }
        return result;
    }
}
