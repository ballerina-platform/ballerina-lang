package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Set the entity body with XML content.
 */
@BallerinaFunction(packageName = "ballerina.mime",
        functionName = "setXml",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity",
                structPackage = "ballerina.mime"),
        args = {@Argument(name = "xmlContent", type = TypeKind.JSON)},
        isPublic = true)
public class SetXml extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct entityStruct = (BStruct) this.getRefArgument(context, 0);
        BXML xmlContent = (BXML) this.getRefArgument(context, 1);
        EntityBodyChannel byteChannel = new EntityBodyChannel(new ByteArrayInputStream(
                xmlContent.getMessageAsString().getBytes(StandardCharsets.UTF_8)));
        MimeUtil.setByteChannelToEntity(context, entityStruct, byteChannel);
        return AbstractNativeFunction.VOID_RETURN;
    }
}
