package org.wso2.ballerina.core.nativeimpl.connectors.http.function;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

import static org.wso2.ballerina.core.nativeimpl.connectors.http.Constants.HTTP_METHOD;

/**
 * Get HTTP Method from the message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getMethod",
        args = {@Argument(name = "message", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
public class GetMethod extends AbstractNativeFunction {
    String httpMethod;
    public BValue[] execute(Context ctx) {
        if (ctx.getCarbonMessage().getProperty(HTTP_METHOD) != null) {
            httpMethod = ctx.getCarbonMessage().getProperty(HTTP_METHOD).toString();
        }
        return getBValues(new BString(httpMethod));
    }
}
