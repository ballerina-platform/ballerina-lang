package org.ballerinalang.nativeimpl.time;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.ballerinalang.nativeimpl.Utils.STRUCT_TYPE_TIME;

/**
 * Convert a Time to string in the given format.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "format",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = STRUCT_TYPE_TIME, structPackage = "ballerina.time"),
        args = {@Argument(name = "pattern", type = TypeKind.UNION)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class Format extends AbstractTimeFunction {

    @Override
    public void execute(Context context) {
        BStruct timeStruct = ((BStruct) context.getRefArgument(0));
        BString pattern = (BString) context.getNullableRefArgument(1);

        switch (pattern.stringValue()) {
            case "RFC_1123":
                ZonedDateTime zonedDateTime = getZonedDateTime(timeStruct);
                String formattedDateTime = zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
                context.setReturnValues(new BString(formattedDateTime));
                break;
            default:
                context.setReturnValues(new BString(getFormattedtString(timeStruct, pattern.stringValue())));
        }
    }
}
