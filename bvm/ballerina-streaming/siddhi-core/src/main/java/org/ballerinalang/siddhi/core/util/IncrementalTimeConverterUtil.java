/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.util;

import org.ballerinalang.siddhi.core.exception.SiddhiAppRuntimeException;
import org.ballerinalang.siddhi.query.api.aggregation.TimePeriod;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Performs time conversions related to incremental aggregation.
 */
public class IncrementalTimeConverterUtil {

    public static long getNextEmitTime(long currentTime, TimePeriod.Duration duration, String timeZone) {
        switch (duration) {
            case SECONDS:
                return currentTime - currentTime % 1000 + 1000;
            case MINUTES:
                return currentTime - currentTime % 60000 + 60000;
            case HOURS:
                return currentTime - currentTime % 3600000 + 3600000;
            case DAYS:
                return currentTime - currentTime % 86400000 + 86400000;
            case MONTHS:
                return getNextEmitTimeForMonth(currentTime, timeZone);
            case YEARS:
                return getNextEmitTimeForYear(currentTime, timeZone);
            default:
                throw new SiddhiAppRuntimeException("Undefined duration " + duration.toString());
        }
    }

    public static long getStartTimeOfAggregates(long currentTime, TimePeriod.Duration duration, String timeZone) {
        switch (duration) {
            case SECONDS:
                return currentTime - currentTime % getMillisecondsPerDuration(duration);
            case MINUTES:
                return currentTime - currentTime % getMillisecondsPerDuration(duration);
            case HOURS:
                return currentTime - currentTime % getMillisecondsPerDuration(duration);
            case DAYS:
                return currentTime - currentTime % getMillisecondsPerDuration(duration);
            case MONTHS:
                return getStartTimeOfAggregatesForMonth(currentTime, timeZone);
            case YEARS:
                return getStartTimeOfAggregatesForYear(currentTime, timeZone);
            default:
                throw new SiddhiAppRuntimeException("Undefined duration " + duration.toString());
        }
    }

    public static long getPreviousStartTime(long currentStartTime, TimePeriod.Duration duration, String timeZone) {
        switch (duration) {
            case SECONDS:
                return currentStartTime - getMillisecondsPerDuration(duration);
            case MINUTES:
                return currentStartTime - getMillisecondsPerDuration(duration);
            case HOURS:
                return currentStartTime - getMillisecondsPerDuration(duration);
            case DAYS:
                return currentStartTime - getMillisecondsPerDuration(duration);
            case MONTHS:
                return getStartTimeOfPreviousMonth(currentStartTime, timeZone);
            case YEARS:
                return getStartTimeOfPreviousYear(currentStartTime, timeZone);
            default:
                throw new SiddhiAppRuntimeException("Undefined duration " + duration.toString());
        }
    }

    private static long getNextEmitTimeForMonth(long currentTime, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        if (zonedDateTime.getMonthValue() == 12) {
            // For a time in December, emit time should be beginning of January next year
            return ZonedDateTime
                    .of(zonedDateTime.getYear() + 1, 1, 1, 0, 0, 0, 0, ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)))
                    .toEpochSecond() * 1000;
        } else {
            // For any other month, the 1st day of next month must be considered
            return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue() + 1, 1, 0, 0, 0, 0,
                    ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone))).toEpochSecond() * 1000;
        }
    }

    private static long getNextEmitTimeForYear(long currentTime, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        return ZonedDateTime
                .of(zonedDateTime.getYear() + 1, 1, 1, 0, 0, 0, 0, ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)))
                .toEpochSecond() * 1000;
    }

    private static long getStartTimeOfAggregatesForMonth(long currentTime, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), 1, 0, 0, 0, 0,
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone))).toEpochSecond() * 1000;
    }

    private static long getStartTimeOfAggregatesForYear(long currentTime, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        return ZonedDateTime
                .of(zonedDateTime.getYear(), 1, 1, 0, 0, 0, 0, ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)))
                .toEpochSecond() * 1000;
    }

    private static long getStartTimeOfPreviousMonth(long currentEmitTime, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentEmitTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        int givenMonth = zonedDateTime.getMonthValue();
        int givenYear = zonedDateTime.getYear();

        if (givenMonth == 1) {
            return ZonedDateTime.of(--givenYear, 12, 1, 0, 0, 0, 0, ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)))
                    .toEpochSecond() * 1000;
        } else {
            return ZonedDateTime
                    .of(givenYear, --givenMonth, 1, 0, 0, 0, 0, ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)))
                    .toEpochSecond() * 1000;
        }
    }

    private static long getStartTimeOfPreviousYear(long currentEmitTime, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentEmitTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        int givenYear = zonedDateTime.getYear();
        return ZonedDateTime.of(--givenYear, 1, 1, 0, 0, 0, 0, ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)))
                .toEpochSecond() * 1000;
    }

    public static int getMillisecondsPerDuration(TimePeriod.Duration duration) {
        switch (duration) {
            case SECONDS:
                return 1000;
            case MINUTES:
                return 60000;
            case HOURS:
                return 3600000;
            case DAYS:
                return 86400000;
            default:
                throw new SiddhiAppRuntimeException("Cannot provide number of milliseconds per duration " + duration
                        + ".Number of milliseconds are only define for SECONDS, MINUTES, HOURS and DAYS");
        }
    }
}
