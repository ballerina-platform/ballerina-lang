package org.ballerinalang.net.grpc.nativeimpl;

import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.values.BStruct;

/**
 * Actract class of gRPC native functions.
 */
public abstract class AbstractGrpcNativeFunction extends BlockingNativeCallableUnit {
    
    protected io.grpc.ServerBuilder getServiceBuilder(BStruct serviceEndpoint) {
        return (io.grpc.ServerBuilder) serviceEndpoint.getNativeData("serviceBuilder");
    }
    
    protected io.grpc.Server getService(BStruct serviceEndpoint) {
        return (io.grpc.Server) serviceEndpoint.getNativeData("service");
    }
}
