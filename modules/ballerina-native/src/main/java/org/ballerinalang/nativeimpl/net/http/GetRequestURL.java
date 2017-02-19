package org.ballerinalang.nativeimpl.net.http;


import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.nativeimpl.connectors.http.Constants.REQUEST_URL;

/**
 * Get the request url of the Message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getRequestURL",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the request URL from the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "The message object") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The request URL value") })
public class GetRequestURL extends AbstractNativeFunction {
    String requestURL = "";
    public BValue[] execute(Context ctx) {
        if (ctx.getCarbonMessage().getProperty(REQUEST_URL) != null) {
            requestURL = ctx.getCarbonMessage().getProperty(REQUEST_URL).toString();
        }
        return getBValues(new BString(requestURL));
    }
}
