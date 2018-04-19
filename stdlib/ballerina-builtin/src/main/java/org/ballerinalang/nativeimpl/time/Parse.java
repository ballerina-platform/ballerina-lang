package org.ballerinalang.nativeimpl.time;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.Utils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Get the Time for the given string.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "parse",
        args = {@Argument(name = "timestamp", type = TypeKind.STRING),
                @Argument(name = "format", type = TypeKind.UNION)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Time", structPackage = "ballerina.time")},
        isPublic = true
)
public class ParseTo extends AbstractTimeFunction {

    @Override
    public void execute(Context context) {
        String dateString = context.getStringArgument(0);
        BString pattern = (BString) context.getNullableRefArgument(0);

        TemporalAccessor parsedDateTime;
        switch (pattern.stringValue()) {
            case "RFC_1123":
                parsedDateTime = DateTimeFormatter.RFC_1123_DATE_TIME.parse(dateString);
                context.setReturnValues(getTimeStruct(parsedDateTime, context, dateString, pattern.stringValue()));
                break;
            default:
                context.setReturnValues(parseTime(context, dateString, pattern.stringValue()));
        }
    }

    private BStruct getTimeStruct(TemporalAccessor dateTime, Context context, String dateString, String pattern) {
        StructInfo timeZoneStructInfo = Utils.getTimeZoneStructInfo(context);
        StructInfo timeStructInfo = Utils.getTimeStructInfo(context);
        long epochTime = -1;
        String zoneId;
        try {
            epochTime = Instant.from(dateTime).toEpochMilli();
            zoneId = String.valueOf(ZoneId.from(dateTime));
        } catch (DateTimeException e) {
            if (epochTime < 0) {
                throw new BallerinaException(
                        "failed to parse \"" + dateString + "\" to the " + pattern + " format");
            }
            zoneId = ZoneId.systemDefault().toString();
        }
        return Utils.createTimeStruct(timeZoneStructInfo, timeStructInfo, epochTime, zoneId);
    }
}
