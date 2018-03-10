package org.ballerinalang.net.grpc.nativeimpl;

import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.natives.AbstractNativeFunction;

public abstract class AbstractGrpcNativeFunction extends AbstractNativeFunction {
    protected io.grpc.ServerBuilder getServiceBuilder(Struct serviceEndpoint) {
        return (io.grpc.ServerBuilder) serviceEndpoint.getNativeData("serviceBuilder");
    }
}
