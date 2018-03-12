package org.ballerinalang.net.grpc.nativeimpl;

import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.Struct;

/**
 * Actract class of gRPC native functions.
 */
public abstract class AbstractGrpcNativeFunction extends BlockingNativeCallableUnit {
    
    protected io.grpc.ServerBuilder getServiceBuilder(Struct serviceEndpoint) {
        return (io.grpc.ServerBuilder) serviceEndpoint.getNativeData("serviceBuilder");
    }
    
    protected io.grpc.Server getService(Struct serviceEndpoint) {
        return (io.grpc.Server) serviceEndpoint.getNativeData("service");
    }
}
