package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.Constants;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Get the Headers of the Message.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.messages",
        functionName = "getHeader",
        args = {@Argument(name = "request", type = TypeEnum.STRUCT, structType = "Request",
                          structPackage = "ballerina.net.http"),
                @Argument(name = "headerName", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets a transport header from the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "request",
        value = "The request message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "headerName",
        value = "The header name") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The header value") })
public class GetHeader extends AbstractNativeFunction {

    public BValue[] execute(Context context) {
        BStruct requestStruct = (BStruct) getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(Constants.TRANSPORT_MESSAGE);

        String headerName = getStringArgument(context, 0);
        String headerValue = httpCarbonMessage.getHeader(headerName);

//        if (headerValue == null) {
//            TODO: should NOT handle error for null headers, need to return `ballerina null`
//            ErrorHandler.handleUndefineHeader(headerName);
//        }
        return getBValues(new BString(headerValue));
    }
}
