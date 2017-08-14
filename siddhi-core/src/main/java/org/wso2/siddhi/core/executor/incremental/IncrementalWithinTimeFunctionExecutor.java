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

package org.wso2.siddhi.core.executor.incremental;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.util.IncrementalTimeConverterUtil;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Executor class for finding whether a given time is within given range in incremental processing.
 * Condition evaluation logic is implemented within executor.
 */
public class IncrementalWithinTimeFunctionExecutor extends FunctionExecutor {
    private boolean isSingleWithin = false;
    private Map<Integer, List<Pattern>> supportedGmtNonGmtRegexPatterns = new HashMap<>();

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length == 2) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new SiddhiAppValidationException("Only string values are supported for single within clause "
                        + "but found, " + attributeExpressionExecutors[0].getReturnType());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.LONG) {
                throw new SiddhiAppValidationException("Only supports checking whether a long value is within the "
                        + "given range, but found " + attributeExpressionExecutors[1].getReturnType());
            }
            isSingleWithin = true;
            setSupportedRegexPatterns();
        } else if (attributeExpressionExecutors.length == 3) {
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
            if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.LONG) {
                throw new SiddhiAppValidationException("Only supports checking whether a long value is within the "
                        + "given range, but found " + attributeExpressionExecutors[2].getReturnType());
            }
            setSupportedRegexPatterns();
        } else {
            throw new SiddhiAppValidationException("incrementalAggregator:within() function accepts only two or " +
                    "three arguments, but found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        long startTime;
        long endTime;
        long timeStamp;
        if (isSingleWithin) {
            Long[] startTimeEndTime = getStartTimeEndTime(data[0].toString().trim());
            startTime = startTimeEndTime[0];
            endTime = startTimeEndTime[1];
            timeStamp = (long) data[1];
        } else {
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
            timeStamp = (long) data[2];
        }
        if (!(startTime < endTime)) {
            throw new SiddhiAppRuntimeException("The start time must be less than the end time in the within clause. " +
                    "However, the given start time is " + startTime + " and given end time is " +
                    endTime + ", in unix time. Hence, start time is not less than end time.");
        }
        return startTime <= timeStamp && timeStamp < endTime;
    }

    @Override
    protected Object execute(Object data) {
        return null; //Since the within function takes in 2 or 3 parameter, this method does not get called.
        // Hence, not implemented.
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //nothing to stop
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.BOOL;
    }

    @Override
    public Map<String, Object> currentState() {
        return null;    //No states
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //Nothing to be done
    }

    private void setSupportedRegexPatterns() {
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

        supportedGmtNonGmtRegexPatterns.put(19, gmtRegexPatterns);
        supportedGmtNonGmtRegexPatterns.put(26, nonGmtRegexPatterns);
    }

    private Long[] getStartTimeEndTime(String singleWithinTimeAsString) {
        long startTime;
        long endTime;
        List<Pattern> supportedPatterns = supportedGmtNonGmtRegexPatterns.get(singleWithinTimeAsString.length());
        if (supportedPatterns == null) {
            throw new SiddhiAppRuntimeException("Incorrect format provided for within clause. "
                    + "Supported formats for non GMT timezones are <yyyy>-**-** **:**:** <ZZ>, "
                    + "<yyyy>-<MM>-** **:**:** <ZZ>, " + "<yyyy>-<MM>-<dd> **:**:** <ZZ>, "
                    + "<yyyy>-<MM>-<dd> <HH>:**:** <ZZ>, " + "<yyyy>-<MM>-<dd> <HH>:<mm>:** <ZZ>, and "
                    + "<yyyy>-<MM>-<dd> <HH>:<mm>:<ss> <ZZ>. The ISO 8601 UTC offset must be provided for "
                    + "<ZZ> (ex. +05:30, -11:00). For GMT timezone the same formats must be adhered to, without <ZZ>");
        }
        for (int i = 0; i < TimePeriod.Duration.values().length; i++) {
            if (supportedPatterns.get(i).matcher(singleWithinTimeAsString).matches()) {
                switch (i) {
                case 0:
                    startTime = IncrementalUnixTimeFunctionExecutor.getUnixTimeStamp(singleWithinTimeAsString);
                    endTime = startTime + 1000;
                    return new Long[] { startTime, endTime };
                case 1:
                    startTime = IncrementalUnixTimeFunctionExecutor
                            .getUnixTimeStamp(singleWithinTimeAsString.replaceAll("\\*", "0"));
                    endTime = startTime + 60000;
                    return new Long[] { startTime, endTime };
                case 2:
                    startTime = IncrementalUnixTimeFunctionExecutor
                            .getUnixTimeStamp(singleWithinTimeAsString.replaceAll("\\*", "0"));
                    endTime = startTime + 3600000;
                    return new Long[] { startTime, endTime };
                case 3:
                    startTime = IncrementalUnixTimeFunctionExecutor
                            .getUnixTimeStamp(singleWithinTimeAsString.replaceAll("\\*", "0"));
                    endTime = startTime + 86400000;
                    return new Long[] { startTime, endTime };
                case 4:
                    startTime = IncrementalUnixTimeFunctionExecutor.getUnixTimeStamp(
                            singleWithinTimeAsString.replaceFirst("\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*", "01 00:00:00"));
                    endTime = IncrementalTimeConverterUtil.getNextEmitTime(startTime, TimePeriod.Duration.MONTHS);
                    return new Long[] { startTime, endTime };
                case 5:
                    startTime = IncrementalUnixTimeFunctionExecutor.getUnixTimeStamp(singleWithinTimeAsString
                            .replaceFirst("\\*\\*-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*", "01-01 00:00:00"));
                    endTime = IncrementalTimeConverterUtil.getNextEmitTime(startTime, TimePeriod.Duration.YEARS);
                    return new Long[] { startTime, endTime };
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
                + "<ZZ> (ex. +05:30, -11:00). For GMT timezone the same formats must be adhered to, without <ZZ>");
    }
}
