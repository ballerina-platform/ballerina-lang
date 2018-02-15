package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityBodyReader;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.runtime.message.BlobDataSource;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.channels.Channels;

/**
 * Get the payload of the Message as a JSON.
 */
@BallerinaFunction(
        packageName = "ballerina.mime",
        functionName = "getText",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity",
                structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetText extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BString result;
        try {
            // Accessing First Parameter Value.
            BStruct entityStruct = (BStruct) this.getRefArgument(context, 0);
            StringDataSource stringDataSource;
            MessageDataSource payload = EntityBodyHandler.getMessageDataSource(entityStruct);
            if (payload != null) {
                result = new BString(payload.getMessageAsString());
            } else {
                result = EntityBodyHandler.readStringDataSource(entityStruct);
                EntityBodyHandler.addMessageDataSource(entityStruct, new StringDataSource(result.toString()));
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving json payload from message: " + e.getMessage());
        }
        // Setting output value.
        return this.getBValues(result);
    }
}
