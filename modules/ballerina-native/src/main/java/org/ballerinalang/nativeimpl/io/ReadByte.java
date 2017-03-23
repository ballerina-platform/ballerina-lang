package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBufferedInputstream;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function to perform byte-by-byte reading from a given buffered input stream.
 * A call to this function will return the next byte from the input stream.
 * The byte value is returned as an int in the range of 0 to 255.
 * ballerina.io:readByte
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "readByte",
        args = {@Argument(name = "bis", type = TypeEnum.BUFFEREDINPUTSTREAM)},
        returnType = {@ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Read the next byte of data from the given buffered input stream. " +
                "The byte value is returned as an int in the range of 0 to 255.")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "bufferedinputstream",
        value = "The buffered input stream to read bytes from")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int",
        value = "The next byte or -1 if the end of the stream has reached.")})
public class ReadByte extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BInteger result;
        try {
            BBufferedInputstream bis = (BBufferedInputstream) getArgument(context, 0);
            result = new BInteger(bis.read());
        } catch (Throwable e) {
            throw new BallerinaException("Error while trying to read a byte from the buffered input " +
                    "stream. Reason: " + e.getMessage());
        }
        return getBValues(result);
    }
}
