package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.channels.Channels;

/**
 *  Get the payload of the Message as a JSON.
 */
@BallerinaFunction(
        packageName = "ballerina.mime",
        functionName = "getXml",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity",
                structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class GetXml extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BXML result;
        try {
            // Accessing First Parameter Value.
            BStruct entityStruct = (BStruct) this.getRefArgument(context, 0);

            MessageDataSource messageDataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
            if (messageDataSource != null) {
                if (messageDataSource instanceof BXML) {
                    result = (BXML) messageDataSource;
                } else {
                    // else, build the XML from the string representation of the payload.
                    result = XMLUtils.parse(messageDataSource.getMessageAsString());
                }
            } else {
                EntityBodyChannel channel = MimeUtil.extractEntityBodyChannel(entityStruct);
                result = XMLUtils.parse(Channels.newInputStream(channel));
                EntityBodyHandler.addMessageDataSource(entityStruct, result);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving json payload from message: " + e.getMessage());
        }
        // Setting output value.
        return this.getBValues(result);
    }
}
