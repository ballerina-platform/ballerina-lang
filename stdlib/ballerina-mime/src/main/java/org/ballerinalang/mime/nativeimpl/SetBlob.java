package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.runtime.message.BlobDataSource;

import java.io.ByteArrayInputStream;

/**
 * Set the entity body with XML content.
 */
@BallerinaFunction(packageName = "ballerina.mime",
        functionName = "setBlob",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity",
                structPackage = "ballerina.mime"),
        args = {@Argument(name = "blobContent", type = TypeKind.BLOB)},
        isPublic = true)
public class SetBlob extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct entityStruct = (BStruct) this.getRefArgument(context, 0);
        byte[] payload = this.getBlobArgument(context, 0);
        EntityBodyChannel byteChannel = new EntityBodyChannel(new ByteArrayInputStream(
                payload));
        MimeUtil.setByteChannelToEntity(context, entityStruct, byteChannel);
        return AbstractNativeFunction.VOID_RETURN;
    }
}
