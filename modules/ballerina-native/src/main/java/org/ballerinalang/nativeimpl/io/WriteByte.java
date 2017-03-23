package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BByteArrayOutputStream;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function to perform byte-by-byte writing to a given byte array output stream.
 * A call to this function will the given byte to the given byte array output stream.
 * The byte value has to be provided as an int, in the range of 0 to 255.
 * ballerina.io:readByte
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "writeByte",
        args = {@Argument(name = "i", type = TypeEnum.INT),
                @Argument(name = "baos", type = TypeEnum.BYTEARRAYOUTPUTSTREAM)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Writes the given byte into the given byte array output stream. " +
                "The byte value has to be provided as an int, in the range of 0 to 255.")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "int",
        value = "The int value to be written into the byte array output stream")})
public class WriteByte extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        try {
            BInteger bInt = (BInteger) getArgument(context, 0);
            BByteArrayOutputStream baos = (BByteArrayOutputStream) getArgument(context, 1);
            baos.write(bInt.intValue());
        } catch (Throwable e) {
            throw new BallerinaException("Error while trying to write a byte into the byte array output " +
                    "stream. Reason: " + e.getMessage());
        }
        return VOID_RETURN;
    }
}
