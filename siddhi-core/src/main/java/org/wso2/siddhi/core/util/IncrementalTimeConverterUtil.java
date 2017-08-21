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

package org.wso2.siddhi.core.util;

import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Performs time conversions related to incremental aggregation
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
            return currentTime - currentTime % 1000;
        case MINUTES:
            return currentTime - currentTime % 60000;
        case HOURS:
            return currentTime - currentTime % 3600000;
        case DAYS:
            return currentTime - currentTime % 86400000;
        case MONTHS:
            return getStartTimeOfAggregatesForMonth(currentTime, timeZone);
        case YEARS:
            return getStartTimeOfAggregatesForYear(currentTime, timeZone);
        default:
            throw new SiddhiAppRuntimeException("Undefined duration " + duration.toString());
        }
    }

    public static long getEmitTimeOfLastEventToRemove(long currentTime, TimePeriod.Duration duration,
            int bufferCount, String timeZone) {
        switch (duration) {
        case SECONDS:
            return currentTime - bufferCount * 1000L;
        case MINUTES:
            return currentTime - bufferCount * 60000L;
        case HOURS:
            return currentTime - bufferCount % 3600000L;
        case DAYS:
            return currentTime - bufferCount % 86400000L;
        case MONTHS:
            return getEmitTimeOfLastEventToRemoveForMonth(currentTime, bufferCount, timeZone);
        case YEARS:
            return getEmitTimeOfLastEventToRemoveForYear(currentTime, bufferCount, timeZone);
        default:
            throw new SiddhiAppRuntimeException("Undefined duration " + duration.toString());
        }
    }

    private static long getNextEmitTimeForMonth(long currentTime, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        if (zonedDateTime.getMonthValue() == 12) {
            // For a time in December, emit time should be beginning of January next year
            return ZonedDateTime.of(zonedDateTime.getYear() + 1, 1, 1, 0, 0, 0, 0,
                    ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone))).toEpochSecond() * 1000;
        } else {
            // For any other month, the 1st day of next month must be considered
            return ZonedDateTime.of(zonedDateTime.getYear(), zonedDateTime.getMonthValue() + 1, 1, 0, 0, 0, 0,
                    ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone))).toEpochSecond() * 1000;
        }
    }

    private static long getNextEmitTimeForYear(long currentTime, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        return ZonedDateTime.of(zonedDateTime.getYear() + 1, 1, 1, 0, 0, 0, 0,
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone))).toEpochSecond() * 1000;
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
        return ZonedDateTime.of(zonedDateTime.getYear(), 1, 1, 0, 0, 0, 0,
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone))).toEpochSecond() * 1000;
    }

    private static long getEmitTimeOfLastEventToRemoveForMonth(long currentEmitTime, int bufferCount, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentEmitTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        int givenMonth = zonedDateTime.getMonthValue();
        int noOfYearsToReduce = bufferCount / 12;
        int noOfMonthsToReduce = bufferCount % 12;
        int yearOfLastEventToRemove;
        int monthOfLastEventToRemove;
        if (givenMonth <= noOfMonthsToReduce) {
            // Example: If given month is 2 (Feb) and noOfMonthsToReduce is 4, reduce one more year.
            // Result must return 10th month (Oct) of that calculated year.
            yearOfLastEventToRemove = zonedDateTime.getYear() - ++noOfYearsToReduce;
            monthOfLastEventToRemove = givenMonth - bufferCount + 12;
        } else {
            yearOfLastEventToRemove = zonedDateTime.getYear() - noOfYearsToReduce;
            monthOfLastEventToRemove = givenMonth - bufferCount;
        }
        return ZonedDateTime.of(yearOfLastEventToRemove, monthOfLastEventToRemove, 1, 0, 0, 0, 0,
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone))).toEpochSecond() * 1000;

    }

    private static long getEmitTimeOfLastEventToRemoveForYear(long currentEmitTime, int bufferCount, String timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentEmitTime),
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone)));
        return ZonedDateTime.of(zonedDateTime.getYear() - bufferCount, 1, 1, 0, 0, 0, 0,
                ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone))).toEpochSecond() * 1000;
    }
}
