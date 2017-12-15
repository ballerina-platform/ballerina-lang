package org.ballerinalang.net.mime.nativeimpl.mimebase64;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

@BallerinaFunction(packageName = "ballerina.net.mime",
                   functionName = "encodeString",
                   receiver = @Receiver(type = TypeKind.STRUCT, structType = "MimeBase64Encoder",
                                        structPackage = "ballerina.net.mime"),
                   args = {
                           @Argument(name = "content",
                                     type = TypeKind.STRING), @Argument(name = "charset",
                                                                        type = TypeKind.STRING)
                   },
                   returnType = { @ReturnType(type = TypeKind.STRING) },
                   isPublic = true)
public class EncodeString extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        String content = this.getStringArgument(context, 0);
        String charset = this.getStringArgument(context, 1);
        byte[] mimeBytes;
        try {
            mimeBytes = content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("Error occured while encoding mime string: " + e.getMessage());
        }
        String mimeEncodedString = Base64.getMimeEncoder().encodeToString(mimeBytes);
        return getBValues(new BString(mimeEncodedString));
    }
}
