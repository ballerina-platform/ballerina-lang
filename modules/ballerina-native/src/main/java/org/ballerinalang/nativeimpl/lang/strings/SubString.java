package org.ballerinalang.nativeimpl.lang.strings;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

/**
 * Native function ballerina.model.arrays:subString(string, int, int).
 */
@BallerinaFunction(
        packageName = "ballerina.lang.strings",
        functionName = "subString",
        args = {@Argument(name = "mainString", type = TypeEnum.STRING),
                @Argument(name = "from", type = TypeEnum.INT),
                @Argument(name = "to", type = TypeEnum.INT)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns a new string that is the substring of the specified string") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "mainString",
        value = "The original string argument") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "from",
        value = "The starting index") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "to",
        value = "The ending index") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The derived sub string") })
public class SubString extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String initialString = getStringArgument(context, 0);

        long fromLong = getIntArgument(context, 0);
        long toLong = getIntArgument(context, 1);

        if (toLong != (int) toLong) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.INDEX_NUMBER_TOO_LARGE, toLong);
        }
        if (fromLong != (int) fromLong) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.INDEX_NUMBER_TOO_LARGE, fromLong);
        }

        int from = (int) fromLong;
        int to = (int) toLong;

        if (from < 0 || to > initialString.length()) {
            throw new BallerinaException("String index out of range. Actual:" + initialString.length() +
                    " requested: " + from + " to " + to);
        }
        BString subString = new BString(initialString.substring(from, to));
        return getBValues(subString);
    }
}
