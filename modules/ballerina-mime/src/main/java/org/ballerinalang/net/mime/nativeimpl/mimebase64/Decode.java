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
 * Mime Base64 Decoder.
 */
@BallerinaFunction(packageName = "ballerina.net.mime",
                   functionName = "decode",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "MimeBase64Decoder",
                                        structPackage = "ballerina.net.mime"),
                   args = {
                           @Argument(name = "content",
                                     type = TypeKind.BLOB)
                   },
                   returnType = { @ReturnType(type = TypeKind.BLOB) },
                   isPublic = true)
public class Decode extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        byte[] encodedContent = this.getBlobArgument(context, 0);
        Base64.Decoder decoder = Base64.getMimeDecoder();
        byte[] decodedContent = decoder.decode(encodedContent);
        return getBValues(new BBlob(decodedContent));
    }
}
