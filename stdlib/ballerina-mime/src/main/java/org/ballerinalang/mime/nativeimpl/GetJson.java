package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.channels.Channels;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL_INDEX;

/**
 *  Get the payload of the Message as a JSON.
 */
@BallerinaFunction(
        packageName = "ballerina.mime",
        functionName = "getJson",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity",
                structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.JSON)},
        isPublic = true
)
public class GetJson extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BJSON result;
        try {
            // Accessing First Parameter Value.
            BStruct entityStruct = (BStruct) this.getRefArgument(context, 0);

            MessageDataSource payload = EntityBodyHandler.getMessageDataSource(entityStruct);
            if (payload != null) {
                if (payload instanceof BJSON) {
                    result = (BJSON) payload;
                } else {
                    // else, build the JSON from the string representation of the payload.
                    result = new BJSON(payload.getMessageAsString());
                }
            } else {
                BStruct byteChannel = (BStruct) entityStruct.getRefField(ENTITY_BYTE_CHANNEL_INDEX);
                EntityBodyChannel channel = (EntityBodyChannel)byteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
                result = new BJSON(Channels.newInputStream(channel));
                EntityBodyHandler.addMessageDataSource(entityStruct, result);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving json payload from message: " + e.getMessage());
        }
        // Setting output value.
        return this.getBValues(result);
    }
}
