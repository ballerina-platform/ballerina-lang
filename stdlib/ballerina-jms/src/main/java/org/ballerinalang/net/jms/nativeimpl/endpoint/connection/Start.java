package org.ballerinalang.net.jms.nativeimpl.endpoint.connection;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

/**
 * Get the ID of the connection.
 *
 * @since 0.970
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jms",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection", structPackage = "ballerina.jms"),
        isPublic = true
)
public class Start implements NativeCallableUnit {
    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
