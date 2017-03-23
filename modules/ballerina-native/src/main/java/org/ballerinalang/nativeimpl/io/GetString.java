package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BByteArrayOutputStream;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.charset.Charset;

/**
 * Native function to convert a given byte array of an output stream, into a string.
 * ballerina.io:getString
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "getString",
        args = {@Argument(name = "baos", type = TypeEnum.BYTEARRAYOUTPUTSTREAM),
                @Argument(name = "charSet", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Converts a given byte array of an output stream, into a string")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "bytearrayoutputstream",
        value = "A byte array of an output stream")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "string",
        value = "The character set to be used when converting the bytes into a string")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The string representation of the given byte array")})
public class GetString extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BString result;
        try {
            BByteArrayOutputStream baos = (BByteArrayOutputStream) getArgument(context, 0);
            BString charSet = (BString) getArgument(context, 1);
            result = new BString(baos.toString(Charset.forName(charSet.stringValue()).name()));
        } catch (Throwable e) {
            throw new BallerinaException("Error while converting byte array into a string. Reason: " + e.getMessage());
        }
        return getBValues(result);
    }
}
