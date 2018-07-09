package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.utils.IOConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.ballerinalang.mime.util.MimeConstants.BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;

/**
 * 'getBodyPartsAsChannel' native function converts a set of body parts into a byte channel.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getBodyPartsAsChannel",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.ARRAY), @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetBodyPartsAsChannel extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BMap<String, BValue> byteChannelStruct;
        BMap<String, BValue> entityStruct = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        MultipartDataSource multipartDataSource = new MultipartDataSource(entityStruct, multipartDataBoundary);
        multipartDataSource.serialize(outputStream);
        EntityBodyChannel entityBodyChannel = new EntityBodyChannel(new ByteArrayInputStream(
                outputStream.toByteArray()));
        byteChannelStruct = BLangConnectorSPIUtil.createBStruct(context, IOConstants.IO_PACKAGE, BYTE_CHANNEL_STRUCT);
        byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new EntityWrapper(entityBodyChannel));
        context.setReturnValues(byteChannelStruct);
    }
}
