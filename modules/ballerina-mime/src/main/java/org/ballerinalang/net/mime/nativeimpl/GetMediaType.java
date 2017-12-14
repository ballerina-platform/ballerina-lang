package org.ballerinalang.net.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.net.mime.util.MimeUtil;
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
@BallerinaFunction(packageName = "ballerina.net.mime",
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
        BStruct mediaType = MimeUtil.parseMediaType(context, contentType);
        return this.getBValues(mediaType);
    }
}
