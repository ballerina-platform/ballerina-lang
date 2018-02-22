package org.ballerinalang.net.grpc.stubs;

import io.grpc.CallOptions;
import io.grpc.Channel;

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
}
