package org.ballerinalang.net.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.ServiceProto;
import org.ballerinalang.net.grpc.stubs.GRPCBlockingStub;
import org.ballerinalang.net.grpc.stubs.GRPCFutureStub;
import org.ballerinalang.net.grpc.stubs.GRPCNonBlockingStub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.grpc.MethodDescriptor.generateFullMethodName;


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
    private static Map methodDescriptorMap = new HashMap<Integer, Descriptors.MethodDescriptor>();
    
    public static Descriptors.MethodDescriptor getMethodDescriptorMap(String methodName) {
        return new MessageServiceMethodDescriptorSupplier(methodName,
                fileDescriptor).getMethodDescriptor();
    }
    
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
                                    (org.ballerinalang.net.grpc.Message) org.ballerinalang.net.grpc.Message
                                            .newBuilder(reqMessageName).build()))
                            .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                    (org.ballerinalang.net.grpc.Message) org.ballerinalang.net.grpc.Message
                                            .newBuilder(resMessageName).build()))
                            .setSchemaDescriptor(new MessageServiceMethodDescriptorSupplier(methodName,
                                    fileDescriptor))
                            .build();
            methodDescriptorMap.put(i, methodExecute);
        }
    }
    
    public static MethodDescriptor.MethodType getMethodType(int methodID) {
        
        com.google.protobuf.DescriptorProtos.MethodDescriptorProto proto = grpcServiceProto.getSet()
                .getService(0).getMethodList().get(methodID);
        if (proto.getClientStreaming() && proto.getServerStreaming()) {
            return MethodDescriptor.MethodType.BIDI_STREAMING;
        } else if (!(proto.getClientStreaming() || proto.getServerStreaming())) {
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
     * Creates a new async stub that supports all call types for the service.
     */
    public GRPCNonBlockingStub newNonBlockingStub(Channel channel) {
        
        return new GRPCNonBlockingStub(channel);
    }
    
    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service.
     */
    public GRPCBlockingStub newBlockingStub(Channel channel) {
        
        return new GRPCBlockingStub(channel);
    }
    
    public static Map getMethodDescriptorMap() {
        return methodDescriptorMap;
    }
    
    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service.
     */
    public GRPCFutureStub newFutureStub(Channel channel) {
        
        return new GRPCFutureStub(channel);
    }
    
    public static ServiceProto getGrpcServiceProto() {
        
        return grpcServiceProto;
    }
    
    /**
     * .
     * .
     */
    private abstract static class MessageServiceBaseDescriptorSupplier
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
    
    /**.
     * .
     */
    public static final class MessageServiceMethodDescriptorSupplier
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
                    // todo: 2/6/18 proper log
                    throw new RuntimeException(" Error ", e);
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
