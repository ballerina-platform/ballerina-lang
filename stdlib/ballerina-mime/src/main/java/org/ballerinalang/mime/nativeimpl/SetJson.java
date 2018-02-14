package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL_INDEX;

/**
 * Set the entity body with JSON content.
 */
@BallerinaFunction(packageName = "ballerina.mime",
        functionName = "setJson",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity",
                structPackage = "ballerina.mime"),
        args = {@Argument(name = "jsonContent", type = TypeKind.JSON)},
        isPublic = true)
public class SetJson extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct entityStruct = (BStruct) this.getRefArgument(context, 0);
        BJSON jsonContent = (BJSON) this.getRefArgument(context, 1);

        EntityBodyChannel byteChannel = new EntityBodyChannel(new ByteArrayInputStream(
                jsonContent.getMessageAsString().getBytes(StandardCharsets.UTF_8)));
        BStruct byteChannelStruct = createByteChannelStruct(context, byteChannel);
        entityStruct.setRefField(ENTITY_BYTE_CHANNEL_INDEX, byteChannelStruct);
        return AbstractNativeFunction.VOID_RETURN;
    }

    private BStruct createByteChannelStruct(Context context, EntityBodyChannel byteChannel) {
        BStruct byteChannelStruct = ConnectorUtils.createAndGetStruct(context
                , org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_IO
                , org.ballerinalang.mime.util.Constants.BYTE_CHANNEL_STRUCT);
        byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, byteChannel);
        return byteChannelStruct;
    }
}
