package org.ballerinalang.net.grpc.nativeimpl.connection;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.MessageUtils;

/**
 * Native function to check whether caller has terminated the connection in between.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "isCancelled",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection",
                structPackage = "ballerina.net.grpc"),
        returnType = @ReturnType(type = TypeKind.BOOLEAN),
        isPublic = true
)
public class IsCancelled extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        StreamObserver responseObserver = MessageUtils.getStreamObserver(connectionStruct);
        if (responseObserver == null) {
            return new BValue[0];
        }
        if (responseObserver instanceof ServerCallStreamObserver) {
            ServerCallStreamObserver serverCallStreamObserver = (ServerCallStreamObserver) responseObserver;
            return new BValue[] {new BBoolean(serverCallStreamObserver.isCancelled())};
        } else {
            return new BValue[0];
        }
    }
}
