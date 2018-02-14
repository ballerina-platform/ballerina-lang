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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Set the entity body with JSON content.
 */
@BallerinaFunction(packageName = "ballerina.mime",
        functionName = "setText",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity",
                structPackage = "ballerina.mime"),
        args = {@Argument(name = "textContent", type = TypeKind.STRING)},
        isPublic = true)
public class SetText extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct entityStruct = (BStruct) this.getRefArgument(context, 0);
        String textContent = this.getStringArgument(context, 1);
        EntityBodyChannel byteChannel = new EntityBodyChannel(new ByteArrayInputStream(
                textContent.getBytes(StandardCharsets.UTF_8)));
        MimeUtil.setByteChannelToEntity(context, entityStruct, byteChannel);
        return AbstractNativeFunction.VOID_RETURN;
    }
}
