/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.stdlib.time.nativeimpl;

import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.stdlib.time.util.TimeUtils;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Contains utility methods for time related functions.
 *
 * @since 0.89
 */
public abstract class AbstractTimeFunction extends BlockingNativeCallableUnit {

    private static final String KEY_ZONED_DATETIME = "ZonedDateTime";
    private static final String TIME_FIELD = "time";
    private static final String ZONE_FIELD = "zone";
    private static final String ZONE_ID_FIELD = "id";

    private static MapValue<String, Object> zoneRecord;
    private static MapValue<String, Object> timeRecord;

    static MapValue<String, Object> createCurrentTime() {
        long currentTime = Instant.now().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), currentTime,
                                          ZoneId.systemDefault().toString());
    }

    static MapValue<String, Object> createDateTime(int year, int month, int day, int hour, int minute, int second,
                                                   int milliSecond, String zoneIDStr) {
        int nanoSecond = milliSecond * 1000000;
        ZoneId zoneId;
        if (zoneIDStr.isEmpty()) {
            zoneId = ZoneId.systemDefault();
            zoneIDStr = zoneId.toString();
        } else {
            zoneId = TimeUtils.getTimeZone(zoneIDStr);
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, nanoSecond, zoneId);
        long timeValue = zonedDateTime.toInstant().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), timeValue, zoneIDStr);
    }

    static MapValue<String, Object> parseTime(String dateValue, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            TemporalAccessor temporalAccessor = formatter.parse(dateValue);
            //Initialize with default values
            int year = 0;
            int month = 1;
            int day = 1;
            int hour = 0;
            int minute = 0;
            int second = 0;
            int nanoSecond = 0;
            if (temporalAccessor.isSupported(ChronoField.YEAR)) {
                year = temporalAccessor.get(ChronoField.YEAR);
            }
            if (temporalAccessor.isSupported(ChronoField.MONTH_OF_YEAR)) {
                month = temporalAccessor.get(ChronoField.MONTH_OF_YEAR);
            }
            if (temporalAccessor.isSupported(ChronoField.DAY_OF_MONTH)) {
                day = temporalAccessor.get(ChronoField.DAY_OF_MONTH);
            }
            if (temporalAccessor.isSupported(ChronoField.HOUR_OF_DAY)) {
                hour = temporalAccessor.get(ChronoField.HOUR_OF_DAY);
            }
            if (temporalAccessor.isSupported(ChronoField.MINUTE_OF_HOUR)) {
                minute = temporalAccessor.get(ChronoField.MINUTE_OF_HOUR);
            }
            if (temporalAccessor.isSupported(ChronoField.SECOND_OF_MINUTE)) {
                second = temporalAccessor.get(ChronoField.SECOND_OF_MINUTE);
            }
            if (temporalAccessor.isSupported(ChronoField.NANO_OF_SECOND)) {
                nanoSecond = temporalAccessor.get(ChronoField.NANO_OF_SECOND);
            }

            ZoneId zoneId;
            try {
                zoneId = ZoneId.from(temporalAccessor);
            } catch (DateTimeException e) {
                zoneId = ZoneId.systemDefault(); // Initialize to the default system timezone
            }

            ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, nanoSecond, zoneId);
            long timeValue = zonedDateTime.toInstant().toEpochMilli();
            return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), timeValue, zoneId.toString());
        } catch (DateTimeParseException e) {
            throw TimeUtils.getTimeError("parse date \"" + dateValue + "\" for the format \"" + pattern + "\" "
                                                 + "failed:" + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw TimeUtils.getTimeError("invalid pattern: " + pattern);
        }
    }

    static String getFormattedString(MapValue<String, Object> timeRecord, String pattern)
            throws IllegalArgumentException {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(dateTimeFormatter);
    }

    static String getDefaultString(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    static MapValue<String, Object> addDuration(MapValue<String, Object> timeRecord, long years, long months,
                                                long days, long hours, long minutes, long seconds, long milliSeconds) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        long nanoSeconds = milliSeconds * 1000000;
        dateTime = dateTime.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes)
                .plusSeconds(seconds).plusNanos(nanoSeconds);
        MapValue<String, Object> zoneData = (MapValue<String, Object>) timeRecord.get(ZONE_FIELD);
        String zoneIdName = zoneData.get(ZONE_ID_FIELD).toString();
        long mSec = dateTime.toInstant().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), mSec, zoneIdName);
    }

    static MapValue<String, Object> subtractDuration(MapValue<String, Object> timeRecord, long years, long months,
                                                     long days, long hours, long minutes, long seconds,
                                                     long milliSeconds) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        long nanoSeconds = milliSeconds * 1000000;
        dateTime = dateTime.minusYears(years).minusMonths(months).minusDays(days).minusHours(hours)
                .minusMinutes(minutes).minusSeconds(seconds).minusNanos(nanoSeconds);
        MapValue<String, Object> zoneData = (MapValue<String, Object>) timeRecord.get(ZONE_FIELD);
        String zoneIdName = zoneData.get(ZONE_ID_FIELD).toString();
        long mSec = dateTime.toInstant().toEpochMilli();
        return TimeUtils.createTimeRecord(getTimeZoneRecord(), getTimeRecord(), mSec, zoneIdName);
    }

    static MapValue<String, Object> changeTimezone(MapValue<String, Object> timeRecord, String zoneId) {
        MapValue<String, Object> timezone = TimeUtils.createTimeZone(TimeUtils.getTimeZoneRecord(), zoneId);
        timeRecord.put(ZONE_FIELD, timezone);
        clearRecordCache(timeRecord);
        return timeRecord;
    }

    static int getYear(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getYear();
    }

    static int getMonth(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getMonthValue();
    }

    static int getDay(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getDayOfMonth();
    }

    static int getHour(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getHour();
    }

    static int getMinute(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getMinute();
    }

    static int getSecond(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getSecond();
    }

    static int getMilliSecond(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getNano() / 1000000;
    }

    static String getWeekDay(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = getZonedDateTime(timeRecord);
        return dateTime.getDayOfWeek().toString();
    }

    static ZonedDateTime getZonedDateTime(MapValue<String, Object> timeRecord) {
        ZonedDateTime dateTime = (ZonedDateTime) timeRecord.getNativeData(KEY_ZONED_DATETIME);
        if (dateTime != null) {
            return dateTime;
        }
        long timeData = (Long) timeRecord.get(TIME_FIELD);
        MapValue<String, Object> zoneData = (MapValue<String, Object>) timeRecord.get(ZONE_FIELD);
        ZoneId zoneId;
        if (zoneData != null) {
            String zoneIdName = zoneData.get(ZONE_ID_FIELD).toString();
            if (zoneIdName.isEmpty()) {
                zoneId = ZoneId.systemDefault();
            } else {
                zoneId = TimeUtils.getTimeZone(zoneIdName);
            }
        } else {
            zoneId = ZoneId.systemDefault();
        }
        dateTime = Instant.ofEpochMilli(timeData).atZone(zoneId);
        timeRecord.addNativeData(KEY_ZONED_DATETIME, dateTime);
        return dateTime;
    }

    private static void clearRecordCache(MapValue<String, Object> timeRecord) {
        timeRecord.addNativeData(KEY_ZONED_DATETIME, null);
    }

    private static MapValue<String, Object> getTimeZoneRecord() {
        if (zoneRecord == null) {
            zoneRecord = TimeUtils.getTimeZoneRecord();
        }
        return zoneRecord;
    }

    private static MapValue<String, Object> getTimeRecord() {
        if (timeRecord == null) {
            timeRecord = TimeUtils.getTimeRecord();
        }
        return timeRecord;
    }
}
