package org.ballerinalang.net.http.nativeimpl.response;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.util.RequestResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the Headers of the Message.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.messages",
        functionName = "getHeader",
        args = {@Argument(name = "res", type = TypeEnum.STRUCT, structType = "Response",
                structPackage = "ballerina.net.http"),
                @Argument(name = "headerName", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets a transport header from the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "res",
        value = "The response message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "headerName",
        value = "The header name") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The header value") })
public class GetHeader extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(GetHeader.class);

    public BValue[] execute(Context context) {
        return RequestResponseUtil.getHeader(context, this, log);
    }
}
