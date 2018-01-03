package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.mime.util.Constants;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Construct MediaType struct from Content-Type string.
 */
@BallerinaFunction(packageName = "ballerina.mime",
                   functionName = "getMediaType",
                   args = {
                           @Argument(name = "contentType",
                                     type = TypeKind.STRING)
                   },
                   returnType = { @ReturnType(type = TypeKind.STRUCT) },
                   isPublic = true)
public class GetMediaType extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String contentType = this.getStringArgument(context, 0);
        BStruct mediaType = ConnectorUtils
                .createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_MIME,
                        Constants.MEDIA_TYPE);
        mediaType = MimeUtil.parseMediaType(mediaType, contentType);
        return this.getBValues(mediaType);
    }
}
