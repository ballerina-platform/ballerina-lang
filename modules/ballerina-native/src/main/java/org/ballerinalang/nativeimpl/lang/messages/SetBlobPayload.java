package org.ballerinalang.nativeimpl.lang.messages;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.Constants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.runtime.message.BinrayMessageDataSource;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageUtil;

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
        byte[] payload = getBlobArgument(ctx, 0);
        // Clone the message without content
        BinrayMessageDataSource binaryCarbonMessage = new BinrayMessageDataSource(payload);
        CarbonMessage cmsg = MessageUtil.cloneCarbonMessageWithOutData(msg.value());
        msg.setValue(cmsg);
        msg.setMessageDataSource(binaryCarbonMessage);
        msg.setHeader(Constants.CONTENT_TYPE, Constants.OCTET_STREAM);
        return VOID_RETURN;
    }
}

