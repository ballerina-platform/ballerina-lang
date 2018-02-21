package org.ballerinalang.net.grpc.stubs;

import com.google.protobuf.Message;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.GRPCClientStub;

import static io.grpc.stub.ClientCalls.blockingUnaryCall;

public class GRPCBlockingStub extends io.grpc.stub.AbstractStub<GRPCBlockingStub> {
    
    public GRPCBlockingStub(Channel channel) {
        super(channel);
    }
    
    protected GRPCBlockingStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }
    
    @Override
    protected GRPCBlockingStub build(Channel channel, CallOptions callOptions) {
        return new GRPCBlockingStub(channel, callOptions);
    }
    
    /**
     * .
     * @param request .
     * @param methodID .
     * @return .
     */
    public Message executeBlockingUnary(Message request, int methodID) {
        
        MethodDescriptor.MethodType methodType = GRPCClientStub.getMethodType(methodID);
        if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
            return blockingUnaryCall(
                    getChannel(), (MethodDescriptor<Message, Message>) GRPCClientStub.getMethodDescriptorMap().get(methodID),
                    getCallOptions(), request);
        } else {
            throw new RuntimeException("invalid method type");
        }
        
    }
}
