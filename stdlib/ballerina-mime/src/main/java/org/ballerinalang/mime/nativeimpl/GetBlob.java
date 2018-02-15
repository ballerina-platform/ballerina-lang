package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityBodyReader;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.runtime.message.BlobDataSource;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.nio.channels.Channels;

/**
 * Get the payload of the Message as a JSON.
 */
@BallerinaFunction(
        packageName = "ballerina.mime",
        functionName = "getBlob",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity",
                structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.BLOB)},
        isPublic = true
)
public class GetBlob extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BlobDataSource result;
        try {
            // Accessing First Parameter Value.
            BStruct entityStruct = (BStruct) this.getRefArgument(context, 0);
            MessageDataSource messageDataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
            if (messageDataSource != null) {
                result = (BlobDataSource) messageDataSource;
            } else {
                result = EntityBodyHandler.readBinaryDataSource(entityStruct);
                EntityBodyHandler.addMessageDataSource(entityStruct, result);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving json payload from message: " + e.getMessage());
        }
        // Setting output value.
        return this.getBValues(new BBlob(result.getValue()));
    }
}
