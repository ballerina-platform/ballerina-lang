package org.ballerinalang.nativeimpl.time;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Convert a Time to string in the given format.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "Time.formatTo",
        args = {@Argument(name = "time", type = TypeKind.STRUCT, structType = "Time", structPackage = "ballerina.time"),
                @Argument(name = "pattern", type = TypeKind.ENUM)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class FormatTo extends AbstractTimeFunction {

    @Override
    public void execute(Context context) {
        BStruct timeStruct = ((BStruct) context.getRefArgument(0));
        String pattern = context.getStringArgument(0);

        switch (pattern) {
            case "RFC_1123":
                ZonedDateTime zonedDateTime = getZonedDateTime(timeStruct);
                String formattedDateTime = zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
                context.setReturnValues(new BString(formattedDateTime));
                break;
            default:
                throw new BallerinaException("failed to format date/time: unrecognized time format");
        }
    }
}
