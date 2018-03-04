package org.ballerinalang.test.util.grpc.client.helloworld;

import io.grpc.stub.ClientCalls;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.7.0)",
        comments = "Source: helloWorld.proto")
public final class HelloWorldGrpc {
    
    private HelloWorldGrpc() {
    }
    
    public static final String SERVICE_NAME = "helloWorld";
    
    // Static method descriptors that strictly reflect the proto.
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<com.google.protobuf.StringValue,
            com.google.protobuf.StringValue> METHOD_HELLO =
            io.grpc.MethodDescriptor.<com.google.protobuf.StringValue, com.google.protobuf.StringValue>newBuilder()
                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(
                            "helloWorld", "hello"))
                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            com.google.protobuf.StringValue.getDefaultInstance()))
                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            com.google.protobuf.StringValue.getDefaultInstance()))
                    .setSchemaDescriptor(new HelloWorldMethodDescriptorSupplier("hello"))
                    .build();
    
    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static HelloWorldStub newStub(io.grpc.Channel channel) {
        return new HelloWorldStub(channel);
    }
    
    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static HelloWorldBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        return new HelloWorldBlockingStub(channel);
    }
    
    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static HelloWorldFutureStub newFutureStub(
            io.grpc.Channel channel) {
        return new HelloWorldFutureStub(channel);
    }
    
    /**
     */
    public  abstract static class HelloWorldImplBase implements io.grpc.BindableService {
        
        /**
         */
        public void hello(com.google.protobuf.StringValue request,
                          io.grpc.stub.StreamObserver<com.google.protobuf.StringValue> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_HELLO, responseObserver);
        }
        
        @Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            METHOD_HELLO,
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            com.google.protobuf.StringValue,
                                            com.google.protobuf.StringValue>(
                                            this, METHODID_HELLO)))
                    .build();
        }
    }
    
    /**
     */
    public static final class HelloWorldStub extends io.grpc.stub.AbstractStub<HelloWorldStub> {
        private HelloWorldStub(io.grpc.Channel channel) {
            super(channel);
        }
        
        private HelloWorldStub(io.grpc.Channel channel,
                               io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }
        
        @Override
        protected HelloWorldStub build(io.grpc.Channel channel,
                                       io.grpc.CallOptions callOptions) {
            return new HelloWorldStub(channel, callOptions);
        }
        
        /**
         */
        public void hello(com.google.protobuf.StringValue request,
                          io.grpc.stub.StreamObserver<com.google.protobuf.StringValue> responseObserver) {
            ClientCalls.asyncUnaryCall(
                    getChannel().newCall(METHOD_HELLO, getCallOptions()), request, responseObserver);
        }
    }
    
    /**
     */
    public static final class HelloWorldBlockingStub extends io.grpc.stub.AbstractStub<HelloWorldBlockingStub> {
        private HelloWorldBlockingStub(io.grpc.Channel channel) {
            super(channel);
        }
        
        private HelloWorldBlockingStub(io.grpc.Channel channel,
                                       io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }
        
        @Override
        protected HelloWorldBlockingStub build(io.grpc.Channel channel,
                                               io.grpc.CallOptions callOptions) {
            return new HelloWorldBlockingStub(channel, callOptions);
        }
        
        /**
         */
        public com.google.protobuf.StringValue hello(com.google.protobuf.StringValue request) {
            return blockingUnaryCall(
                    getChannel(), METHOD_HELLO, getCallOptions(), request);
        }
    }
    
    /**
     */
    public static final class HelloWorldFutureStub extends io.grpc.stub.AbstractStub<HelloWorldFutureStub> {
        private HelloWorldFutureStub(io.grpc.Channel channel) {
            super(channel);
        }
        
        private HelloWorldFutureStub(io.grpc.Channel channel,
                                     io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }
        
        @Override
        protected HelloWorldFutureStub build(io.grpc.Channel channel,
                                             io.grpc.CallOptions callOptions) {
            return new HelloWorldFutureStub(channel, callOptions);
        }
        
        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.StringValue> hello(
                com.google.protobuf.StringValue request) {
            return futureUnaryCall(
                    getChannel().newCall(METHOD_HELLO, getCallOptions()), request);
        }
    }
    
    private static final int METHODID_HELLO = 0;
    
    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final HelloWorldImplBase serviceImpl;
        private final int methodId;
        
        MethodHandlers(HelloWorldImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_HELLO:
                    serviceImpl.hello((com.google.protobuf.StringValue) request,
                            (io.grpc.stub.StreamObserver<com.google.protobuf.StringValue>) responseObserver);
                    break;
                default:
                    throw new AssertionError();
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }
    }
    
    private  abstract static class HelloWorldBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        HelloWorldBaseDescriptorSupplier() {
        }
        
        @Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return HelloServiceProto.getDescriptor();
        }
        
        @Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("helloWorld");
        }
    }
    
    private static final class HelloWorldFileDescriptorSupplier
            extends HelloWorldBaseDescriptorSupplier {
        HelloWorldFileDescriptorSupplier() {
        }
    }
    
    private static final class HelloWorldMethodDescriptorSupplier
            extends HelloWorldBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;
        
        HelloWorldMethodDescriptorSupplier(String methodName) {
            this.methodName = methodName;
        }
        
        @Override
        public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
            return getServiceDescriptor().findMethodByName(methodName);
        }
    }
    
    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;
    
    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (HelloWorldGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new HelloWorldFileDescriptorSupplier())
                            .addMethod(METHOD_HELLO)
                            .build();
                }
            }
        }
        return result;
    }
}
