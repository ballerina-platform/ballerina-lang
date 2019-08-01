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

package org.ballerinalang.stdlib.time.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.time.util.TimeUtils;

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
        functionName = "parse"
)
public class Parse extends AbstractTimeFunction {

    @Override
    public void execute(Context context) {
    }

    public static Object parse(Strand strand, String dateString, Object pattern) {
        try {
            TemporalAccessor parsedDateTime;
            if ("RFC_1123".equals(pattern.toString())) {
                parsedDateTime = DateTimeFormatter.RFC_1123_DATE_TIME.parse(dateString);
                return getTimeRecord(parsedDateTime, dateString, pattern.toString());
            }
            return parseTime(dateString, pattern.toString());
        } catch (ErrorValue e) {
            return e;
        }
    }

    private static MapValue<String, Object> getTimeRecord(TemporalAccessor dateTime, String dateString,
                                                          String pattern) {
        MapValue<String, Object> timeZoneRecord = TimeUtils.getTimeZoneRecord();
        MapValue<String, Object> timeRecord = TimeUtils.getTimeRecord();
        long epochTime = -1;
        String zoneId;
        try {
            epochTime = Instant.from(dateTime).toEpochMilli();
            zoneId = String.valueOf(ZoneId.from(dateTime));
        } catch (DateTimeException e) {
            if (epochTime < 0) {
                throw TimeUtils.getTimeError("failed to parse \"" + dateString + "\" to the " + pattern + " format");
            }
            zoneId = ZoneId.systemDefault().toString();
        }
        return TimeUtils.createTimeRecord(timeZoneRecord, timeRecord, epochTime, zoneId);
    }
}
