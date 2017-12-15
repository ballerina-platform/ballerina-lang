package org.ballerinalang.net.mime.nativeimpl.qprintable;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@BallerinaFunction(packageName = "ballerina.net.mime",
                   functionName = "encode",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "QuotedPrintableEncoder",
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
      /*  byte[] originalContent = this.getBlobArgument(context, 0);
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            OutputStream encodedOut = MimeUtility.encode(baos, "quoted-printable");
            encodedOut.write(originalContent);
        } catch (MessagingException e) {
            throw new BallerinaException(
                    "Error occured while encoding byte array with quoted-printable: " + e.getMessage());
        } catch (IOException e) {
            throw new BallerinaException(
                    "IOException occured while encoding byte array with quoted-printable: " + e.getMessage());
        }
        return getBValues(new BBlob(baos.toByteArray()));*/
      return null;
    }
}
