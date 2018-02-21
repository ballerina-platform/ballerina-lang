package org.ballerinalang.net.grpc.nativeimpl.connection;

import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function to inform the caller, server finished sending messages.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "complete",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection",
                structPackage = "ballerina.net.grpc"),
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                structPackage = "ballerina.net.grpc"),
        isPublic = true
)
public class Complete extends AbstractNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(Complete.class);
    @Override
    public BValue[] execute(Context context) {
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        StreamObserver responseObserver = MessageUtils.getStreamObserver(connectionStruct);
        if (responseObserver == null) {
            return new BValue[0];
        }
        try {
            responseObserver.onCompleted();
        } catch (Throwable e) {
            log.error("Error while sending client response.", e);
            return getBValues(MessageUtils.getServerConnectorError(context, e));
        }
        return new BValue[0];
    }
}
