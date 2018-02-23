package org.ballerinalang.net.grpc.stubs;

import com.google.protobuf.Message;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import org.ballerinalang.net.grpc.GRPCClientStub;

import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;

/** .
 * .
 */
public class GRPCNonBlockingStub extends io.grpc.stub.AbstractStub<GRPCNonBlockingStub>  {
    public GRPCNonBlockingStub(Channel channel) {
        super(channel);
    }
    
    protected GRPCNonBlockingStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }
    
    @Override
    protected GRPCNonBlockingStub build(Channel channel, CallOptions callOptions) {
        return new GRPCNonBlockingStub(channel, callOptions);
    }
    
    /**
     */
    public void executeServerStreaming(Message request, io.grpc.stub.StreamObserver<Message> responseObserver,
                                       int methodID) {
        MethodDescriptor methodDescriptor =(MethodDescriptor<Message, Message>) GRPCClientStub.getMethodDescriptorMap()
                .get(methodID);
        asyncServerStreamingCall(
                getChannel().newCall(methodDescriptor, getCallOptions()), request, responseObserver);
    }
    
    /**
     */
    public io.grpc.stub.StreamObserver<Message> executeClientStreaming(io.grpc.stub.StreamObserver<Message>
                                                                               responseObserver,int methodID) {
        MethodDescriptor methodDescriptor =(MethodDescriptor<Message, Message>) GRPCClientStub.getMethodDescriptorMap()
                .get(methodID);
        return asyncClientStreamingCall(
                getChannel().newCall(methodDescriptor, getCallOptions()), responseObserver);
    }
    /**
     */
    public void executeUnary(Message request,
                                        io.grpc.stub.StreamObserver<Message> responseObserver, int methodID) {
        
        MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(methodID);
        if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
            ClientCalls.asyncUnaryCall(
                    getChannel().newCall((MethodDescriptor<Message, Message>) GRPCClientStub.getMethodDescriptorMap()
                                    .get(methodID), getCallOptions()), request, responseObserver);
            
        }
        
    }
}
