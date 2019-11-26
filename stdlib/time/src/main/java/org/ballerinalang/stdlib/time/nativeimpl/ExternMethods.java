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

import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.TupleValueImpl;
import org.ballerinalang.stdlib.time.util.TimeUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;

import static org.ballerinalang.stdlib.time.nativeimpl.AbstractTimeFunction.ZONE_FIELD;
import static org.ballerinalang.stdlib.time.nativeimpl.AbstractTimeFunction.ZONE_ID_FIELD;
import static org.ballerinalang.stdlib.time.nativeimpl.AbstractTimeFunction.changeTimezone;
import static org.ballerinalang.stdlib.time.nativeimpl.AbstractTimeFunction.createDateTime;
import static org.ballerinalang.stdlib.time.nativeimpl.AbstractTimeFunction.getDefaultString;
import static org.ballerinalang.stdlib.time.nativeimpl.AbstractTimeFunction.getFormattedString;
import static org.ballerinalang.stdlib.time.nativeimpl.AbstractTimeFunction.getZonedDateTime;
import static org.ballerinalang.stdlib.time.nativeimpl.AbstractTimeFunction.parseTime;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeRecord;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeZoneRecord;

/**
 * Extern methods used in Ballerina Time library.
 *
 * @since 1.1.0
 */
public class ExternMethods {
    private ExternMethods() {}

    private static final BTupleType getDateTupleType = new BTupleType(
            Arrays.asList(BTypes.typeInt, BTypes.typeInt, BTypes.typeInt));
    private static final BTupleType getTimeTupleType = new BTupleType(
            Arrays.asList(BTypes.typeInt, BTypes.typeInt, BTypes.typeInt, BTypes.typeInt));

    public static MapValue<?, ?> addDuration(MapValue<String, Object> timeRecord, long years,
                                                   long months, long days, long hours, long minutes, long seconds,
                                                   long milliSeconds) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        long nanoSeconds = milliSeconds * 1000000;
        dateTime = dateTime.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes)
                .plusSeconds(seconds).plusNanos(nanoSeconds);
        MapValue<String, Object> zoneData = (MapValue<String, Object>) timeRecord.get(ZONE_FIELD);
        String zoneIdName = zoneData.get(ZONE_ID_FIELD).toString();
        long mSec = dateTime.toInstant().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), mSec, zoneIdName);
    }

    public static Object createTime(long years, long months, long dates, long hours, long minutes,
                                    long seconds, long milliSeconds, String zoneId) {
        try {
            return createDateTime((int) years, (int) months, (int) dates, (int) hours, (int) minutes, (int) seconds,
                                  (int) milliSeconds, zoneId);
        } catch (ErrorValue e) {
            return e;
        }
    }

    public static MapValue<?, ?> currentTime() {
        long currentTime = Instant.now().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), currentTime,
                                          ZoneId.systemDefault().toString());
    }

    public static long getDay(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getDayOfMonth();
    }

    public static Object format(MapValue<String, Object> timeRecord, Object pattern) {
        try {
            if ("RFC_1123".equals(pattern.toString())) {
                ZonedDateTime zonedDateTime = getZonedDateTime(timeRecord);
                return zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
            } else {
                return getFormattedString(timeRecord, pattern.toString());
            }
        } catch (IllegalArgumentException e) {
            return TimeUtils.getTimeError("Invalid Pattern: " + pattern.toString());
        }
    }

    public static ArrayValue getDate(MapValue<String, Object> timeRecord) {
        TupleValueImpl date = new TupleValueImpl(getDateTupleType);
        date.add(0, Long.valueOf(getYear(timeRecord)));
        date.add(1, Long.valueOf(getMonth(timeRecord)));
        date.add(2, Long.valueOf(getDay(timeRecord)));
        return date;
    }

    public static ArrayValue getTime(MapValue<String, Object> timeRecord) {
        TupleValueImpl time = new TupleValueImpl(getTimeTupleType);
        time.add(0, Long.valueOf(getHour(timeRecord)));
        time.add(1, Long.valueOf(getMinute(timeRecord)));
        time.add(2, Long.valueOf(getSecond(timeRecord)));
        time.add(3, Long.valueOf(getMilliSecond(timeRecord)));
        return time;
    }

    public static long getHour(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getHour();
    }

    public static long getMilliSecond(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getNano() / 1000000;
    }

    public static long getMinute(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getMinute();
    }

    public static long getMonth(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getMonthValue();
    }

    public static Object parse(String dateString, Object pattern) {
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

    public static long getSecond(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getSecond();
    }

    public static MapValue<String, Object> subtractDuration(MapValue<String, Object> timeRecord,
                                                            long years, long months, long days, long hours,
                                                            long minutes, long seconds, long milliSeconds) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        long nanoSeconds = milliSeconds * 1000000;
        dateTime = dateTime.minusYears(years).minusMonths(months).minusDays(days).minusHours(hours)
                .minusMinutes(minutes).minusSeconds(seconds).minusNanos(nanoSeconds);
        MapValue<String, Object> zoneData = (MapValue<String, Object>) timeRecord.get(ZONE_FIELD);
        String zoneIdName = zoneData.get(ZONE_ID_FIELD).toString();
        long mSec = dateTime.toInstant().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), mSec, zoneIdName);
    }

    public static String toString(MapValue<String, Object> timeRecord) {
        return getDefaultString(timeRecord);
    }

    public static Object toTimeZone(MapValue<String, Object> timeRecord, String zoneId) {
        try {
            return changeTimezone(timeRecord, zoneId);
        } catch (ErrorValue e) {
            return e;
        }
    }

    public static String getWeekday(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getDayOfWeek().toString();
    }

    public static long getYear(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getYear();
    }
}
