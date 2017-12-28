package org.ballerinalang.net.mime.nativeimpl.mimebase64;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Base64;

/**
 * Mime base64 encoder.
 */
@BallerinaFunction(packageName = "ballerina.net.mime",
                   functionName = "encode",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "MimeBase64Encoder",
                                        structPackage = "ballerina.net.mime"),
                   args = {
                           @Argument(name = "content",
                                     type = TypeKind.BLOB)
                   },
                   returnType = { @ReturnType(type = TypeKind.BLOB) },
                   isPublic = true)
public class Encode extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        byte[] originalContent = this.getBlobArgument(context, 0);
        Base64.Encoder encoder = Base64.getMimeEncoder();
        byte[] encoded = encoder.encode(originalContent);
        return getBValues(new BBlob(encoded));
    }
}
