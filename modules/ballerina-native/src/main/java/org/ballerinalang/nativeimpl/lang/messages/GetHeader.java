package org.ballerinalang.nativeimpl.lang.messages;


import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Get the Headers of the Message.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.messages",
        functionName = "getHeader",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "headerName", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets a transport header from the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "The message object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "headerName",
        value = "The header name") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The header value") })
public class GetHeader extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BMessage msg = (BMessage) getArgument(ctx, 0);
        String headerName = getArgument(ctx, 1).stringValue();
        String headerValue = msg.getHeader(headerName);

        if (headerValue == null) {
            //TODO: should NOT handle error for null headers, need to return `ballerina null`
            ErrorHandler.handleUndefineHeader(headerName);
        }
        return getBValues(new BString(headerValue));
    }
}
