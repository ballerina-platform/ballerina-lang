/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "parse",
        args = {@Argument(name = "timestamp", type = TypeKind.STRING),
                @Argument(name = "format", type = TypeKind.UNION)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Time", structPackage = "ballerina.time")},
        isPublic = true
)
public class Parse extends AbstractTimeFunction {

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
