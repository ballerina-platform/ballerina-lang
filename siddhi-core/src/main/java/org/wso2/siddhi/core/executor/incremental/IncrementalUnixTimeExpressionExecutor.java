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
import org.wso2.siddhi.query.api.definition.Attribute;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

public class IncrementalUnixTimeExpressionExecutor implements ExpressionExecutor {
    private ExpressionExecutor expressionExecutor;
    private static Pattern nonGmtRegexPattern = Pattern
            .compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:][0-9]{2}\\s[+-][0-9]{2}[:][0-9]{2}");
    private static Pattern gmtRegexPattern = Pattern
            .compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:][0-9]{2}");

    public IncrementalUnixTimeExpressionExecutor(ExpressionExecutor expressionExecutor) {
        if (expressionExecutor.getReturnType() != Attribute.Type.STRING) {
            throw new OperationNotSupportedException("Only string values can be converted to unix time, but found " +
            expressionExecutor.getReturnType());
        }
        this.expressionExecutor = expressionExecutor;
    }

    @Override
    public Long execute(ComplexEvent event) {
        return getUnixTimeStamp(expressionExecutor.execute(event).toString());
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.LONG;
    }

    @Override
    public ExpressionExecutor cloneExecutor(String key) {
        return new IncrementalUnixTimeExpressionExecutor(expressionExecutor.cloneExecutor(key));
    }

    public static long getUnixTimeStamp(String stringTimeStamp) {
        stringTimeStamp = stringTimeStamp.trim();
        // stringTimeStamp must be of format "2017-06-01 04:05:50 +05:00 (not GMT) or 2017-06-01 04:05:50 (if in GMT)"
        if (gmtRegexPattern.matcher(stringTimeStamp).matches()) {
            String[] dateTime = stringTimeStamp.split(" ");
            return Instant.parse(dateTime[0].concat("T").concat(dateTime[1]).concat("Z")).toEpochMilli();
        } else if (nonGmtRegexPattern.matcher(stringTimeStamp).matches()) {
            String[] dateTimeZone = stringTimeStamp.split(" ");
            String[] splitDate = dateTimeZone[0].split("-");
            String[] splitTime = dateTimeZone[1].split(":");
            return ZonedDateTime
                    .of(Integer.parseInt(splitDate[0]), Integer.parseInt(splitDate[1]), Integer.parseInt(splitDate[2]),
                            Integer.parseInt(splitTime[0]), Integer.parseInt(splitTime[1]),
                            Integer.parseInt(splitTime[2]), 0, ZoneId.ofOffset("GMT", ZoneOffset.of(dateTimeZone[2])))
                    .toEpochSecond() * 1000;
        }
        throw new SiddhiAppRuntimeException("Timestamp " + stringTimeStamp + "doesn't match "
                + "the supported formats <yyyy>-<MM>-<dd> <HH>:<mm>:<ss> (for GMT time zone) or " +
                "<yyyy>-<MM>-<dd> <HH>:<mm>:<ss> <Z> (for non GMT time zone). The ISO 8601 UTC offset must be "
                + "provided for <Z> (ex. +05:30, -11:00");
    }
}
