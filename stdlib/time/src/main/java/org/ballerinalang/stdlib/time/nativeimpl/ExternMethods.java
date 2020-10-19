/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.stdlib.time.util.TimeUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;

import static org.ballerinalang.stdlib.time.util.Constants.MULTIPLIER_TO_NANO;
import static org.ballerinalang.stdlib.time.util.TimeUtils.changeTimezone;
import static org.ballerinalang.stdlib.time.util.TimeUtils.createDateTime;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getDefaultString;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getFormattedString;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeRecord;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeZoneRecord;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getZoneId;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getZonedDateTime;
import static org.ballerinalang.stdlib.time.util.TimeUtils.parseTime;

/**
 * Extern methods used in Ballerina Time library.
 *
 * @since 1.1.0
 */
public class ExternMethods {
    private ExternMethods() {}

    private static final TupleType getDateTupleType = TypeCreator.createTupleType(
            Arrays.asList(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_INT));
    private static final TupleType getTimeTupleType = TypeCreator.createTupleType(
            Arrays.asList(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_INT,
                          PredefinedTypes.TYPE_INT));

    public static BString toString(BMap<BString, Object> timeRecord) {
        return getDefaultString(timeRecord);
    }

    public static Object format(BMap<BString, Object> timeRecord, BString pattern) {
        try {
            if ("RFC_1123".equals(pattern.getValue())) {
                ZonedDateTime zonedDateTime = getZonedDateTime(timeRecord);
                return StringUtils.fromString(zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
            } else {
                return getFormattedString(timeRecord, pattern);
            }
        } catch (IllegalArgumentException e) {
            return TimeUtils.getTimeError("Invalid Pattern: " + pattern.getValue());
        }
    }

    public static long getYear(BMap<BString, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getYear();
    }

    public static long getMonth(BMap<BString, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getMonthValue();
    }

    public static long getDay(BMap<BString, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getDayOfMonth();
    }

    public static BString getWeekday(BMap<BString, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return StringUtils.fromString(dateTime.getDayOfWeek().toString());
    }

    public static long getHour(BMap<BString, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getHour();
    }

    public static long getMinute(BMap<BString, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getMinute();
    }

    public static long getSecond(BMap<BString, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getSecond();
    }

    public static long getMilliSecond(BMap<BString, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getNano() / 1000000;
    }

    public static BArray getDate(BMap<BString, Object> timeRecord) {
        BArray date = ValueCreator.createTupleValue(getDateTupleType);
        date.add(0, Long.valueOf(getYear(timeRecord)));
        date.add(1, Long.valueOf(getMonth(timeRecord)));
        date.add(2, Long.valueOf(getDay(timeRecord)));
        return date;
    }

    public static BArray getTime(BMap<BString, Object> timeRecord) {
        BArray time = ValueCreator.createTupleValue(getTimeTupleType);
        time.add(0, Long.valueOf(getHour(timeRecord)));
        time.add(1, Long.valueOf(getMinute(timeRecord)));
        time.add(2, Long.valueOf(getSecond(timeRecord)));
        time.add(3, Long.valueOf(getMilliSecond(timeRecord)));
        return time;
    }

    public static BMap<BString, Object> addDuration(BMap<BString, Object> timeRecord, long years, long months,
                                                    long days, long hours, long minutes, long seconds,
                                                    long milliSeconds) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        long nanoSeconds = milliSeconds * MULTIPLIER_TO_NANO;
        dateTime = dateTime.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes)
                .plusSeconds(seconds).plusNanos(nanoSeconds);
        long mSec = dateTime.toInstant().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), mSec, getZoneId(timeRecord));
    }

    public static BMap<BString, Object> subtractDuration(BMap<BString, Object> timeRecord,
                                                             long years, long months, long days, long hours,
                                                             long minutes, long seconds, long milliSeconds) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        long nanoSeconds = milliSeconds * MULTIPLIER_TO_NANO;
        dateTime = dateTime.minusYears(years).minusMonths(months).minusDays(days).minusHours(hours)
                .minusMinutes(minutes).minusSeconds(seconds).minusNanos(nanoSeconds);
        long mSec = dateTime.toInstant().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), mSec, getZoneId(timeRecord));
    }

    public static Object toTimeZone(BMap<BString, Object> timeRecord, BString zoneId) {
        try {
            return changeTimezone(timeRecord, zoneId);
        } catch (BError e) {
            return TimeUtils.getTimeError(e.getMessage());
        }
    }

    public static BMap<BString, Object> currentTime() {
        long currentTime = Instant.now().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), currentTime,
                                          StringUtils.fromString(ZoneId.systemDefault().toString()));
    }

    public static Object createTime(long years, long months, long dates, long hours, long minutes,
                                    long seconds, long milliSeconds, BString zoneId) {
        try {
            return createDateTime((int) years, (int) months, (int) dates, (int) hours, (int) minutes, (int) seconds,
                                  (int) milliSeconds, zoneId);
        } catch (BError e) {
            return TimeUtils.getTimeError(e.getMessage());
        }
    }

    public static Object parse(BString dateString, BString pattern) {
        try {
            TemporalAccessor parsedDateTime;
            if ("RFC_1123".equals(pattern.getValue())) {
                parsedDateTime = DateTimeFormatter.RFC_1123_DATE_TIME.parse(dateString.getValue());
                return getTimeRecord(parsedDateTime, dateString, pattern);
            }
            return parseTime(dateString, pattern);
        } catch (BError e) {
            return TimeUtils.getTimeError(e.getMessage());
        }
    }
}
