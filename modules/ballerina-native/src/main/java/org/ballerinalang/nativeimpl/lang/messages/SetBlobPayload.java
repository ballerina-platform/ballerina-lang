package org.ballerinalang.nativeimpl.lang.messages;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.Constants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.wso2.carbon.messaging.BinaryCarbonMessage;

import java.nio.ByteBuffer;

/**
 * Set the payload of the Message as a binary (blob).
 */
@BallerinaFunction(
        packageName = "ballerina.lang.messages",
        functionName = "setBlobPayload",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "payload", type = TypeEnum.BLOB)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets the message payload using a binary(blob) object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "The current message object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "payload",
        value = "The binary(blob) payload object") })
public class SetBlobPayload extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context ctx) {
        // Accessing First Parameter Value.
        BMessage msg = (BMessage) getRefArgument(ctx, 0);
        BBlob payload = (BBlob) getRefArgument(ctx, 1);
        // Clone the message without content

        BinaryCarbonMessage binaryCarbonMessage = new BinaryCarbonMessage(ByteBuffer.wrap(payload.blobValue()), true);
        binaryCarbonMessage.setHeaders(msg.getHeaders());
        msg.setValue(binaryCarbonMessage);
        msg.setHeader(Constants.CONTENT_TYPE, Constants.OCTET_STREAM);
        return VOID_RETURN;
    }
}

