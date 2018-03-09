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

package org.ballerinalang.siddhi.core.executor.incremental;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.exception.SiddhiAppRuntimeException;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.function.FunctionExecutor;
import org.ballerinalang.siddhi.core.util.IncrementalTimeConverterUtil;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.query.api.aggregation.TimePeriod;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Executor class for finding the start time and end time of the within clause in incremental processing.
 * This is important when retrieving incremental aggregate values by specifying a time range with 'within' clause.
 */
public class IncrementalStartTimeEndTimeFunctionExecutor extends FunctionExecutor {

    private static List<Pattern> getSupportedRegexPatterns(int withinStringLength) {
        List<Pattern> gmtRegexPatterns = new ArrayList<>();
        List<Pattern> nonGmtRegexPatterns = new ArrayList<>();

        gmtRegexPatterns.add(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:][0-9]{2}"));
        gmtRegexPatterns.add(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:]\\*\\*"));
        gmtRegexPatterns.add(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:]\\*\\*[:]\\*\\*"));
        gmtRegexPatterns.add(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s\\*\\*[:]\\*\\*[:]\\*\\*"));
        gmtRegexPatterns.add(Pattern.compile("[0-9]{4}-[0-9]{2}-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*"));
        gmtRegexPatterns.add(Pattern.compile("[0-9]{4}-\\*\\*-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*"));

        nonGmtRegexPatterns.add(Pattern
                .compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:][0-9]{2}\\s[+-][0-9]{2}[:][0-9]{2}"));
        nonGmtRegexPatterns.add(
                Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:]\\*\\*\\s[+-][0-9]{2}[:][0-9]{2}"));
        nonGmtRegexPatterns.add(
                Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:]\\*\\*[:]\\*\\*\\s[+-][0-9]{2}[:][0-9]{2}"));
        nonGmtRegexPatterns.add(
                Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s\\*\\*[:]\\*\\*[:]\\*\\*\\s[+-][0-9]{2}[:][0-9]{2}"));
        nonGmtRegexPatterns
                .add(Pattern.compile("[0-9]{4}-[0-9]{2}-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*\\s[+-][0-9]{2}[:][0-9]{2}"));
        nonGmtRegexPatterns
                .add(Pattern.compile("[0-9]{4}-\\*\\*-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*\\s[+-][0-9]{2}[:][0-9]{2}"));

        if (withinStringLength == 19) {
            return gmtRegexPatterns;
        } else if (withinStringLength == 26) {
            return nonGmtRegexPatterns;
        } else {
            return null;
        }
    }

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length == 1) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new SiddhiAppValidationException("Only string values are supported for single within clause "
                        + "but found, " + attributeExpressionExecutors[0].getReturnType());
            }
        } else if (attributeExpressionExecutors.length == 2) {
            if (!(attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG
                    || attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING)) {
                throw new SiddhiAppValidationException(
                        "Only string and long types are supported as first value of within clause");
            }
            if (!(attributeExpressionExecutors[1].getReturnType() == Attribute.Type.LONG
                    || attributeExpressionExecutors[1].getReturnType() == Attribute.Type.STRING)) {
                throw new SiddhiAppValidationException(
                        "Only string and long types are supported as second value of within clause");
            }
        } else {
            throw new SiddhiAppValidationException("incrementalAggregator:startTimeEndTime() function accepts " +
                    "only one or two arguments, but found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        long startTime;
        long endTime;

        if (data[0] instanceof Long) {
            startTime = (long) data[0];
        } else {
            startTime = IncrementalUnixTimeFunctionExecutor.getUnixTimeStamp(data[0].toString());
        }
        if (data[1] instanceof Long) {
            endTime = (long) data[1];
        } else {
            endTime = IncrementalUnixTimeFunctionExecutor.getUnixTimeStamp(data[1].toString());
        }

        if (!(startTime < endTime)) {
            throw new SiddhiAppRuntimeException("The start time must be less than the end time in the within clause. "
                    + "However, the given start time is " + startTime + " and given end time is " + endTime
                    + ", in unix time. Hence, start time is not less than end time.");
        }
        return new Long[]{startTime, endTime};
    }

    @Override
    protected Object execute(Object data) {
        return getStartTimeEndTime(data.toString().trim());
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.OBJECT;
    }

    @Override
    public Map<String, Object> currentState() {
        return null; // No states
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        // Nothing to be done
    }

    private Long[] getStartTimeEndTime(String singleWithinTimeAsString) {
        long startTime;
        long endTime;
        String timeZone;
        List<Pattern> supportedPatterns = getSupportedRegexPatterns(singleWithinTimeAsString.length());
        if (supportedPatterns == null) {
            throw new SiddhiAppRuntimeException("Incorrect format provided for within clause. "
                    + "Supported formats for non GMT timezones are <yyyy>-**-** **:**:** <ZZ>, "
                    + "<yyyy>-<MM>-** **:**:** <ZZ>, " + "<yyyy>-<MM>-<dd> **:**:** <ZZ>, "
                    + "<yyyy>-<MM>-<dd> <HH>:**:** <ZZ>, " + "<yyyy>-<MM>-<dd> <HH>:<mm>:** <ZZ>, and "
                    + "<yyyy>-<MM>-<dd> <HH>:<mm>:<ss> <ZZ>. The ISO 8601 UTC offset must be provided for "
                    + "<ZZ> (ex. +05:30, -11:00). For GMT timezone the same formats must be adhered to, without <ZZ>. "
                    + "However the given format is " + singleWithinTimeAsString);
        }
        for (int i = 0; i < TimePeriod.Duration.values().length; i++) {
            if (supportedPatterns.get(i).matcher(singleWithinTimeAsString).matches()) {
                switch (i) {
                    case 0:
                        startTime = IncrementalUnixTimeFunctionExecutor.getUnixTimeStamp(singleWithinTimeAsString);
                        endTime = startTime + 1000;
                        return new Long[]{startTime, endTime};
                    case 1:
                        startTime = IncrementalUnixTimeFunctionExecutor
                                .getUnixTimeStamp(singleWithinTimeAsString.replaceAll("\\*", "0"));
                        endTime = startTime + 60000;
                        return new Long[]{startTime, endTime};
                    case 2:
                        startTime = IncrementalUnixTimeFunctionExecutor
                                .getUnixTimeStamp(singleWithinTimeAsString.replaceAll("\\*", "0"));
                        endTime = startTime + 3600000;
                        return new Long[]{startTime, endTime};
                    case 3:
                        startTime = IncrementalUnixTimeFunctionExecutor
                                .getUnixTimeStamp(singleWithinTimeAsString.replaceAll("\\*", "0"));
                        endTime = startTime + 86400000;
                        return new Long[]{startTime, endTime};
                    case 4:
                        startTime = IncrementalUnixTimeFunctionExecutor.getUnixTimeStamp(
                                singleWithinTimeAsString.
                                        replaceFirst("\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*", "01 00:00:00"));
                        timeZone = IncrementalTimeGetTimeZone.getTimeZone(
                                singleWithinTimeAsString.
                                        replaceFirst("\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*", "01 00:00:00"));
                        endTime = IncrementalTimeConverterUtil.getNextEmitTime(startTime, TimePeriod.Duration.MONTHS,
                                timeZone);
                        return new Long[]{startTime, endTime};
                    case 5:
                        startTime = IncrementalUnixTimeFunctionExecutor.getUnixTimeStamp(singleWithinTimeAsString
                                .replaceFirst("\\*\\*-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*", "01-01 00:00:00"));
                        timeZone = IncrementalTimeGetTimeZone.getTimeZone(singleWithinTimeAsString
                                .replaceFirst("\\*\\*-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*", "01-01 00:00:00"));
                        endTime = IncrementalTimeConverterUtil.getNextEmitTime(startTime, TimePeriod.Duration.YEARS,
                                timeZone);
                        return new Long[]{startTime, endTime};
                    default:
                        // Won't occur since there are only 6 TimePeriod.Durations (sec, min, hour, day, month, year)
                }
            }
        }
        throw new SiddhiAppRuntimeException("Incorrect format provided for within clause. "
                + "Supported formats for non GMT timezones are <yyyy>-**-** **:**:** <ZZ>, "
                + "<yyyy>-<MM>-** **:**:** <ZZ>, " + "<yyyy>-<MM>-<dd> **:**:** <ZZ>, "
                + "<yyyy>-<MM>-<dd> <HH>:**:** <ZZ>, " + "<yyyy>-<MM>-<dd> <HH>:<mm>:** <ZZ>, and "
                + "<yyyy>-<MM>-<dd> <HH>:<mm>:<ss> <ZZ>. The ISO 8601 UTC offset must be provided for "
                + "<ZZ> (ex. +05:30, -11:00). For GMT timezone the same formats must be adhered to, without <ZZ>. "
                + "However the given format is " + singleWithinTimeAsString);
    }
}
