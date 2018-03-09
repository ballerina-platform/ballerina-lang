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
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Executor class for retrieving the timezone from a string timeStamp.
 */
public class IncrementalTimeGetTimeZone extends FunctionExecutor {
    private static Pattern nonGmtRegexPattern = Pattern
            .compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:][0-9]{2}\\s[+-][0-9]{2}[:][0-9]{2}");
    private static Pattern gmtRegexPattern = Pattern
            .compile("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:][0-9]{2}");

    public static String getTimeZone(String stringTimeStamp) {
        stringTimeStamp = stringTimeStamp.trim();
        // stringTimeStamp must be of format "2017-06-01 04:05:50 +05:00 (not GMT) or 2017-06-01 04:05:50 (if in GMT)"
        if (gmtRegexPattern.matcher(stringTimeStamp).matches()) {
            return "+00:00";
        } else if (nonGmtRegexPattern.matcher(stringTimeStamp).matches()) {
            String[] dateTimeZone = stringTimeStamp.split(" ");
            return dateTimeZone[2];
        }
        throw new SiddhiAppRuntimeException("Timestamp " + stringTimeStamp + "doesn't match "
                + "the supported formats <yyyy>-<MM>-<dd> <HH>:<mm>:<ss> (for GMT time zone) or " +
                "<yyyy>-<MM>-<dd> <HH>:<mm>:<ss> <Z> (for non GMT time zone). The ISO 8601 UTC offset must be "
                + "provided for <Z> (ex. +05:30, -11:00");
    }

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if (!(attributeExpressionExecutors.length == 0 || attributeExpressionExecutors.length == 1)) {
            throw new SiddhiAppValidationException("incrementalAggregator:getTimeZone() function " +
                    "accepts zero or one argument, but found " + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors.length == 1) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new SiddhiAppValidationException("Time zone can be retrieved, only from " +
                        "string values, but found " + attributeExpressionExecutors[0].getReturnType());
            }
        }
    }

    @Override
    protected Object execute(Object[] data) {
        return null; //Since the getTimeZone function takes in 1 parameter, this method does not get called.
        // Hence, not implemented.
    }

    @Override
    protected Object execute(Object data) {
        if (data == null) {
            return ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).getId();
        }
        return getTimeZone(data.toString());
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.STRING;
    }

    @Override
    public Map<String, Object> currentState() {
        return null;    //No states
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //Nothing to be done
    }
}
