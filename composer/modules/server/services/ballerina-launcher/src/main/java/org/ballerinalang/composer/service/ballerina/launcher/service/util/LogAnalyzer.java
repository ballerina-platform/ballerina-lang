/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.ballerina.launcher.service.util;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LogAnalyzer.
 */
public class LogAnalyzer {

    final static Pattern STREAM_PATTERN = Pattern.compile("\\{(tracelog.http.[a-z]*)}");
    final static Pattern TIMESTAMP_PATTERN = Pattern.compile("^\\[([0-9- :,]*)]*");
    final static Pattern ID_PATTERN = Pattern.compile("id: ([a-z0-9]*)");
    final static Pattern DIRECTION = Pattern.compile("(INBOUND|OUTBOUND)");
    final static Pattern HEADER = Pattern.compile("INBOUND|OUTBOUND: ([\\s\\S]*)");

    private static String getTimestamp(String logLine) {
        Matcher matcher = TIMESTAMP_PATTERN.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private static String getStream(String logLine) {
        Matcher matcher = STREAM_PATTERN.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private static String getId(String logLine) {
        Matcher matcher = ID_PATTERN.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private static String getDirection(String logLine) {
        Matcher matcher = DIRECTION.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private static String getHeader(String logLine) {
        Matcher matcher = HEADER.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    public static String parseLogLine(String logLine) {

        Gson gson = new Gson();
        LogDTO log = new LogDTO();
        log.setTimestamp(getTimestamp(logLine));
        log.setStream(getStream(logLine));
        log.setId(getId(logLine));
        log.setDirection(getDirection(logLine));
        log.setHeaders(getHeader(logLine));

        String json = gson.toJson(log);
        return json;
    }
}
