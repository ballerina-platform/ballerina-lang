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

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.IncrementalTimeConverterUtil;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Executor class for finding whether a given time is within given range in incremental processing.
 * Condition evaluation logic is implemented within executor.
 */
public class IncrementalWithinTimeExpressionExecutor implements ExpressionExecutor {
    private ExpressionExecutor singleWithinExecutor = null;
    private ExpressionExecutor leftWithinExecutor;
    private ExpressionExecutor rightWithinExecutor;
    private ExpressionExecutor timeExpressionExecutor;
    private Map<Integer, List<Pattern>> supportedGmtNonGmtRegexPatterns = new HashMap<>();

    public IncrementalWithinTimeExpressionExecutor(ExpressionExecutor singleWithinExecutor,
            ExpressionExecutor timeExpressionExecutor) {
        if (singleWithinExecutor.getReturnType() != Attribute.Type.STRING) {
            throw new OperationNotSupportedException("Only string values are supported for single within clause "
                    + "but found, " + singleWithinExecutor.getReturnType());
        }
        if (timeExpressionExecutor.getReturnType() != Attribute.Type.LONG) {
            throw new OperationNotSupportedException("Only supports checking whether a long value is within the "
                    + "given range, but found " + timeExpressionExecutor.getReturnType());
        }
        this.singleWithinExecutor = singleWithinExecutor;
        this.timeExpressionExecutor = timeExpressionExecutor;

        setSupportedRegexPatterns();
    }

    public IncrementalWithinTimeExpressionExecutor(ExpressionExecutor leftWithinExecutor,
            ExpressionExecutor rightWithinExecutor, ExpressionExecutor timeExpressionExecutor) {
        if (timeExpressionExecutor.getReturnType() != Attribute.Type.LONG) {
            throw new OperationNotSupportedException("Only supports checking whether a long value is within the "
                    + "given range, but found " + timeExpressionExecutor.getReturnType());
        }
        this.timeExpressionExecutor = timeExpressionExecutor;
        if (leftWithinExecutor.getReturnType() == Attribute.Type.LONG
                || rightWithinExecutor.getReturnType() == Attribute.Type.STRING) {
            this.leftWithinExecutor = leftWithinExecutor;
        } else {
            throw new OperationNotSupportedException(
                    "Only string and long types are supported as " + "first value of within clause");
        }
        if (rightWithinExecutor.getReturnType() == Attribute.Type.LONG
                || rightWithinExecutor.getReturnType() == Attribute.Type.STRING) {
            this.rightWithinExecutor = rightWithinExecutor;
        } else {
            throw new OperationNotSupportedException(
                    "Only string and long types are supported as " + "second value of within clause");
        }
        setSupportedRegexPatterns();
    }

    @Override
    public Boolean execute(ComplexEvent event) {
        long startTime;
        long endTime;
        long timeStamp = (long) timeExpressionExecutor.execute(event);
        if (singleWithinExecutor != null) {
            String singleWithinTimeAsString = singleWithinExecutor.execute(event).toString().trim();
            Long[] startTimeEndTime = getStartTimeEndTime(singleWithinTimeAsString);
            startTime = startTimeEndTime[0];
            endTime = startTimeEndTime[1];
        } else {
            if (leftWithinExecutor.getReturnType() == Attribute.Type.LONG) {
                startTime = (long) leftWithinExecutor.execute(event);
            } else {
                startTime = IncrementalUnixTimeExpressionExecutor
                        .getUnixTimeStamp(leftWithinExecutor.execute(event).toString());
            }
            if (rightWithinExecutor.getReturnType() == Attribute.Type.LONG) {
                endTime = (long) rightWithinExecutor.execute(event);
            } else {
                endTime = IncrementalUnixTimeExpressionExecutor
                        .getUnixTimeStamp(rightWithinExecutor.execute(event).toString());
            }
        }
        return startTime <= timeStamp && timeStamp < endTime;
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.BOOL;
    }

    @Override
    public ExpressionExecutor cloneExecutor(String key) {
        if (singleWithinExecutor != null) {
            return new IncrementalWithinTimeExpressionExecutor(singleWithinExecutor.cloneExecutor(key),
                    timeExpressionExecutor.cloneExecutor(key));
        } else {
            return new IncrementalWithinTimeExpressionExecutor(leftWithinExecutor.cloneExecutor(key),
                    rightWithinExecutor.cloneExecutor(key), timeExpressionExecutor.cloneExecutor(key));
        }
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
                    startTime = IncrementalUnixTimeExpressionExecutor.getUnixTimeStamp(singleWithinTimeAsString);
                    endTime = startTime + 1000;
                    return new Long[] { startTime, endTime };
                case 1:
                    startTime = IncrementalUnixTimeExpressionExecutor
                            .getUnixTimeStamp(singleWithinTimeAsString.replaceAll("\\*", "0"));
                    endTime = startTime + 60000;
                    return new Long[] { startTime, endTime };
                case 2:
                    startTime = IncrementalUnixTimeExpressionExecutor
                            .getUnixTimeStamp(singleWithinTimeAsString.replaceAll("\\*", "0"));
                    endTime = startTime + 3600000;
                    return new Long[] { startTime, endTime };
                case 3:
                    startTime = IncrementalUnixTimeExpressionExecutor
                            .getUnixTimeStamp(singleWithinTimeAsString.replaceAll("\\*", "0"));
                    endTime = startTime + 86400000;
                    return new Long[] { startTime, endTime };
                case 4:
                    startTime = IncrementalUnixTimeExpressionExecutor.getUnixTimeStamp(
                            singleWithinTimeAsString.replaceFirst("\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*", "01 00:00:00"));
                    endTime = IncrementalTimeConverterUtil.getNextEmitTime(startTime, TimePeriod.Duration.MONTHS);
                    return new Long[] { startTime, endTime };
                case 5:
                    startTime = IncrementalUnixTimeExpressionExecutor.getUnixTimeStamp(singleWithinTimeAsString
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
