package org.wso2.ballerina.nativeimpl.lang.string;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Native function ballerina.lang.array:subString(string, int, int).
 */
@BallerinaFunction(
        packageName = "ballerina.lang.string",
        functionName = "subString",
        args = {@Argument(name = "mainString", type = TypeEnum.STRING),
                @Argument(name = "from", type = TypeEnum.INT),
                @Argument(name = "to", type = TypeEnum.INT)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
public class SubString extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String initialString = getArgument(context, 0).stringValue();
        BInteger argFrom = (BInteger) getArgument(context, 1);
        BInteger argTo = (BInteger) getArgument(context, 2);

        int from = argFrom.intValue();
        int to = argTo.intValue();
        if (from < 0 || to > initialString.length()) {
            throw new BallerinaException("String index out of range. Actual:" + initialString.length() +
                    " requested: " + from + " to " + to);
        }
        BString subString = new BString(initialString.substring(from, to));
        return getBValues(subString);
    }
}
