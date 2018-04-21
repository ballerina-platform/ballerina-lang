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
package org.ballerinalang.nativeimpl.time;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.Utils;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

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

    public static final String KEY_ZONED_DATETIME = "ZonedDateTime";

    private StructInfo timeStructInfo;
    private StructInfo zoneStructInfo;

    BStruct createCurrentTime(Context context) {
        long currentTime = Instant.now().toEpochMilli();
        return Utils.createTimeStruct(getTimeZoneStructInfo(context), getTimeStructInfo(context), currentTime,
                ZoneId.systemDefault().toString());
    }

    BStruct createDateTime(Context context, int year, int month, int day, int hour, int minute, int second,
            int milliSecond, String zoneIDStr) {
        int nanoSecond = milliSecond * 1000000;
        ZoneId zoneId;
        if (zoneIDStr.isEmpty()) {
            zoneId = ZoneId.systemDefault();
            zoneIDStr = zoneId.toString();
        } else {
            zoneId = ZoneId.of(zoneIDStr);
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, nanoSecond, zoneId);
        long timeValue = zonedDateTime.toInstant().toEpochMilli();
        return Utils.createTimeStruct(getTimeZoneStructInfo(context), getTimeStructInfo(context), timeValue, zoneIDStr);
    }

    BStruct parseTime(Context context, String dateValue, String pattern) {
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

            ZoneId zoneId = null;
            try {
                zoneId = ZoneId.from(temporalAccessor);
            } catch (DateTimeException e) {
                zoneId = ZoneId.systemDefault(); // Initialize to the default system timezone
            }

            ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, nanoSecond, zoneId);
            long timeValue = zonedDateTime.toInstant().toEpochMilli();
            return Utils.createTimeStruct(getTimeZoneStructInfo(context), getTimeStructInfo(context), timeValue,
                    zoneId.toString());

        } catch (DateTimeParseException e) {
            throw new BallerinaException("parse date \"" + dateValue + "\" for the format \"" + pattern  + "\" failed");
        } catch (IllegalArgumentException e) {
            throw new BallerinaException("invalid pattern for parsing " + pattern);
        }
    }

    String getFormattedtString(BStruct timeStruct, String pattern) {
        String formattedString;
        try {
            ZonedDateTime dateTime = getZonedDateTime(timeStruct);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
            formattedString =  dateTime.format(dateTimeFormatter);
        } catch (IllegalArgumentException e) {
            throw new BallerinaException("invalid pattern for formatting: " + pattern);
        }
        return formattedString;
    }

    String getDefaultString(BStruct timeStruct) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    BStruct addDuration(Context context, BStruct timeStruct, long years, long months, long days, long hours,
            long minutes, long seconds, long milliSeconds) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        long nanoSeconds = milliSeconds * 1000000;
        dateTime = dateTime.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes)
                .plusSeconds(seconds).plusNanos(nanoSeconds);
        BStruct zoneData = (BStruct) timeStruct.getRefField(0);
        String zoneIdName = zoneData.getStringField(0);
        long mSec = dateTime.toInstant().toEpochMilli();
        return Utils.createTimeStruct(getTimeZoneStructInfo(context), getTimeStructInfo(context), mSec, zoneIdName);
    }

    BStruct subtractDuration(Context context, BStruct timeStruct, long years, long months, long days, long hours,
            long minutes, long seconds, long milliSeconds) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        long nanoSeconds = milliSeconds * 1000000;
        dateTime = dateTime.minusYears(years).minusMonths(months).minusDays(days).minusHours(hours)
                .minusMinutes(minutes).minusSeconds(seconds).minusNanos(nanoSeconds);
        BStruct zoneData = (BStruct) timeStruct.getRefField(0);
        String zoneIdName = zoneData.getStringField(0);
        long mSec = dateTime.toInstant().toEpochMilli();
        return Utils.createTimeStruct(getTimeZoneStructInfo(context), getTimeStructInfo(context), mSec, zoneIdName);
    }

    BStruct changeTimezone(Context context, BStruct timeStruct, String zoneId) {
        BStruct timezone = Utils.createTimeZone(Utils.getTimeZoneStructInfo(context), zoneId);
        timeStruct.setRefField(0, timezone);
        clearStructCache(timeStruct);
        return timeStruct;
    }


    int getYear(BStruct timeStruct) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        return dateTime.getYear();
    }

    int getMonth(BStruct timeStruct) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        return dateTime.getMonthValue();
    }

    int getDay(BStruct timeStruct) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        return dateTime.getDayOfMonth();
    }

    int getHour(BStruct timeStruct) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        return dateTime.getHour();
    }

    int getMinute(BStruct timeStruct) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        return dateTime.getMinute();
    }

    int getSecond(BStruct timeStruct) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        return dateTime.getSecond();
    }

    int getMilliSecond(BStruct timeStruct) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        return dateTime.getNano() / 1000000;
    }

    String getWeekDay(BStruct timeStruct) {
        ZonedDateTime dateTime = getZonedDateTime(timeStruct);
        return dateTime.getDayOfWeek().toString();
    }

    protected ZonedDateTime getZonedDateTime(BStruct timeStruct) {
        ZonedDateTime dateTime = (ZonedDateTime) timeStruct.getNativeData(KEY_ZONED_DATETIME);
        if (dateTime != null) {
            return dateTime;
        }
        long timeData = timeStruct.getIntField(0);
        BStruct zoneData = (BStruct) timeStruct.getRefField(0);
        ZoneId zoneId;
        if (zoneData != null) {
            String zoneIdName = zoneData.getStringField(0);
            if (zoneIdName.isEmpty()) {
                zoneId = ZoneId.systemDefault();
            } else {
                zoneId = ZoneId.of(zoneIdName);
            }
        } else {
            zoneId = ZoneId.systemDefault();
        }
        dateTime = Instant.ofEpochMilli(timeData).atZone(zoneId);
        timeStruct.addNativeData(KEY_ZONED_DATETIME, dateTime);
        return dateTime;
    }

    private void clearStructCache(BStruct timeStruct) {
        timeStruct.addNativeData(KEY_ZONED_DATETIME, null);
    }

    private StructInfo getTimeZoneStructInfo(Context context) {
        if (zoneStructInfo == null) {
            zoneStructInfo = Utils.getTimeZoneStructInfo(context);
        }
        return zoneStructInfo;
    }

    private StructInfo getTimeStructInfo(Context context) {
        if (timeStructInfo == null) {
            timeStructInfo = Utils.getTimeStructInfo(context);
        }
        return timeStructInfo;
    }
}
