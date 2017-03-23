package org.ballerinalang.nativeimpl.lang.messages;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBufferedInputstream;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function to get buffered input stream from the message payload.
 * ballerina.model.messages:getByteStreamPayload
 */
@BallerinaFunction(
        packageName = "ballerina.lang.messages",
        functionName = "getByteStreamPayload",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.BUFFEREDINPUTSTREAM)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the byte stream, coming in the message payload as a buffered input stream")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "The message object")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "bufferedinputstream",
        value = "The byte stream in a buffer")})
public class GetByteStreamPayload extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BBufferedInputstream result;
        try {
            BMessage msg = (BMessage) getArgument(context, 0);
            result = new BBufferedInputstream(msg.value().getInputStream());
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving byte stream payload. Reason: " + e.getMessage());
        }
        return getBValues(result);
    }
}
