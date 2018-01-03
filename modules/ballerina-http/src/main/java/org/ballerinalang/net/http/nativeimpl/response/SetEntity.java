package org.ballerinalang.net.http.nativeimpl.response;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpUtil;

/**
 * Set the entity of the response.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "setEntity",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Response",
                             structPackage = "ballerina.net.http"),
        args = {@Argument(name = "entity", type = TypeKind.STRUCT)},
        isPublic = true
)
public class SetEntity extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        return HttpUtil.setEntity(context, this, false);
    }
}
