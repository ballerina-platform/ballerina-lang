package org.ballerinalang.net.http.nativeimpl.response;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpUtil;

/**
 * Get the Headers of the Message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http.response",
        functionName = "getHeader",
        args = {@Argument(name = "res", type = TypeKind.STRUCT, structType = "Response",
                structPackage = "ballerina.net.http"),
                @Argument(name = "headerName", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetHeader extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        return HttpUtil.getHeader(context, this, false);
    }
}
