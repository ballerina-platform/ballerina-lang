package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpUtil;

/**
 * Native function to get payload in parts
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getMultiparts",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Request",
                             structPackage = "ballerina.net.http"),
        returnType = {@ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class GetMultiparts extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        return getBValues(HttpUtil.getMultipartData(context, this, true));
    }
}
