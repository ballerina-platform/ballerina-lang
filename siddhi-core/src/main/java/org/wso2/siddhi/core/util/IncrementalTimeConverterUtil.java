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

/**
 * Performs time conversions related to incremental aggregation
 */
public class IncrementalTimeConverterUtil {

    public static long getNextEmitTime(long currentTime, TimePeriod.Duration duration) {
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
            return getNextEmitTimeForMonth(currentTime);
        case YEARS:
            return getNextEmitTimeForYear(currentTime);
        default:
            throw new SiddhiAppRuntimeException("Undefined duration " + duration.toString());
        }
    }

    public static long getStartTimeOfAggregates(long currentTime, TimePeriod.Duration duration) {
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
            return getStartTimeOfAggregatesForMonth(currentTime);
        case YEARS:
            return getStartTimeOfAggregatesForYear(currentTime);
        default:
            throw new SiddhiAppRuntimeException("Undefined duration " + duration.toString());
        }
    }

    public static long getEmitTimeOfLastEventToRemove(long currentTime, TimePeriod.Duration duration,
            int bufferCount) {
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
            return getEmitTimeOfLastEventToRemoveForMonth(currentTime, bufferCount);
        case YEARS:
            return getEmitTimeOfLastEventToRemoveForYear(currentTime, bufferCount);
        default:
            throw new SiddhiAppRuntimeException("Undefined duration " + duration.toString());
        }
    }

    private static long getNextEmitTimeForMonth(long currentTime) {
        Instant timeFromEpochMillis = Instant.ofEpochMilli(currentTime);
        // timeFromEpochMillis would be of "2010-01-01T12:00:00Z" format
        String[] timeAsString = timeFromEpochMillis.toString().split("(-|T|:|Z)");
        String startTimeOfNextMonth;
        if (timeAsString[1].equals("12")) {
            // For a time in December, emit time should be beginning of January next year
            Integer nextYear = Integer.parseInt(timeAsString[0]) + 1;
            startTimeOfNextMonth = nextYear.toString() + "-01-01T00:00:00Z";
            return Instant.parse(startTimeOfNextMonth).toEpochMilli();
        } else {
            // For any other month, the 1st day of next month must be considered
            Integer nextMonth = Integer.parseInt(timeAsString[1]) + 1;
            String nextMonthAsString = nextMonth.toString();
            if (nextMonthAsString.length() == 1) {
                nextMonthAsString = "0".concat(nextMonthAsString);
            }
            startTimeOfNextMonth = timeAsString[0] + "-" + nextMonthAsString + "-01T00:00:00Z";
            return Instant.parse(startTimeOfNextMonth).toEpochMilli();
        }
    }

    private static long getNextEmitTimeForYear(long currentTime) {
        Instant timeFromEpochMillis = Instant.ofEpochMilli(currentTime);
        String[] timeAsString = timeFromEpochMillis.toString().split("(-|T|:|Z)");
        Integer nextYear = Integer.parseInt(timeAsString[0]) + 1;
        String startTimeOfNextYear = nextYear.toString() + "-01-01T00:00:00Z";
        return Instant.parse(startTimeOfNextYear).toEpochMilli();
    }

    private static long getStartTimeOfAggregatesForMonth(long currentTime) {
        Instant timeFromEpochMillis = Instant.ofEpochMilli(currentTime);
        String[] timeAsString = timeFromEpochMillis.toString().split("-");
        String startTimeOfThisMonth = timeAsString[0] + "-" + timeAsString[1] + "-01T00:00:00Z";
        return Instant.parse(startTimeOfThisMonth).toEpochMilli();
    }

    private static long getStartTimeOfAggregatesForYear(long currentTime) {
        Instant timeFromEpochMillis = Instant.ofEpochMilli(currentTime);
        String[] timeAsString = timeFromEpochMillis.toString().split("-");
        String startTimeOfThisYear = timeAsString[0] + "-01-01T00:00:00Z";
        return Instant.parse(startTimeOfThisYear).toEpochMilli();
    }

    private static long getEmitTimeOfLastEventToRemoveForMonth(long currentEmitTime, int bufferCount) {
        Instant timeFromEpochMillis = Instant.ofEpochMilli(currentEmitTime);
        String[] timeAsString = timeFromEpochMillis.toString().split("-");
        Integer givenMonth = Integer.parseInt(timeAsString[1]);
        int noOfYearsToReduce = bufferCount / 12;
        int noOfMonthsToReduce = bufferCount % 12;
        Integer yearOfLastEventToRemove;
        Integer monthOfLastEventToRemove;
        if (givenMonth <= noOfMonthsToReduce) {
            // Example: If given month is 2 (Feb) and noOfMonthsToReduce is 4, reduce one more year.
            // Result must return 10th month (Oct) of that calculated year.
            yearOfLastEventToRemove = Integer.parseInt(timeAsString[0]) - ++noOfYearsToReduce;
            monthOfLastEventToRemove = givenMonth - bufferCount + 12;
        } else {
            yearOfLastEventToRemove = Integer.parseInt(timeAsString[0]) - noOfYearsToReduce;
            monthOfLastEventToRemove = givenMonth - bufferCount;
        }
        String monthOfLastEventToRemoveAsString = monthOfLastEventToRemove.toString();
        if (monthOfLastEventToRemoveAsString.length() == 1) {
            monthOfLastEventToRemoveAsString = "0".concat(monthOfLastEventToRemoveAsString);
        }
        return Instant
                .parse(yearOfLastEventToRemove.toString() + "-" + monthOfLastEventToRemoveAsString + "-01T00:00:00Z")
                .toEpochMilli();

    }

    private static long getEmitTimeOfLastEventToRemoveForYear(long currentEmitTime, int bufferCount) {
        Instant timeFromEpochMillis = Instant.ofEpochMilli(currentEmitTime);
        String[] timeAsString = timeFromEpochMillis.toString().split("-");
        Integer yearOfLastEventToRemove = Integer.parseInt(timeAsString[0]) - bufferCount;
        return Instant.parse(yearOfLastEventToRemove.toString() + "-01-01T00:00:00Z").toEpochMilli();
    }
}
