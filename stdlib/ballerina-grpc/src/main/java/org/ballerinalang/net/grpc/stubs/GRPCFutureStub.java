package org.ballerinalang.net.grpc.stubs;

import io.grpc.CallOptions;
import io.grpc.Channel;

/**.
 * .
 */
public class GRPCFutureStub extends io.grpc.stub.AbstractStub<GRPCFutureStub>  {
    public GRPCFutureStub(Channel channel) {
        super(channel);
    }
    
    protected GRPCFutureStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }
    
    @Override
    protected GRPCFutureStub build(Channel channel, CallOptions callOptions) {
        return  new GRPCFutureStub(channel, callOptions);
    }
}
