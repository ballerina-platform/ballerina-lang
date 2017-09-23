/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.logging.util;

import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.logging.formatters.ConsoleLogFormatter;
import org.ballerinalang.logging.formatters.jul.BRELogFormatter;
import org.ballerinalang.logging.formatters.jul.HTTPTraceLogFormatter;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A parser for parsing the log format string. This class is responsible for mapping the Ballerina log format string to
 * the JDK log format string.
 *
 * @since 0.94
 */
public class FormatStringMapper {

    private static final FormatStringMapper formatStringMapper = new FormatStringMapper();
    private static final String defaultTimestampFmt = "yyyy-MM-dd HH:mm:ss,SSS";
    private static final String logFormatRegex =
            "\\{\\{timestamp\\}\\}(\\[[a-zA-Z0-9_+\\-.\\ \\t:,!@#$%^&*();\\\\/|<>\"']*\\])?|\\{\\{\\w+\\}\\}";
    private static final Pattern logPattern = Pattern.compile(logFormatRegex);

    private final Map<String, Map<String, Integer>> placeholderMap;
    private final Map<String, SimpleDateFormat> dateFormatMap = new HashMap<>();

    private FormatStringMapper() {
        placeholderMap = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>(Constants.BALLERINA_LOG_FORMAT, ConsoleLogFormatter.PLACEHOLDERS_MAP),
                new AbstractMap.SimpleEntry<>(Constants.LOG_BRE_LOG_FORMAT, BRELogFormatter.PLACEHOLDERS_MAP),
                new AbstractMap.SimpleEntry<>(Constants.LOG_TRACELOG_HTTP_FORMAT,
                                              HTTPTraceLogFormatter.PLACEHOLDERS_MAP)
        ).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

    public static FormatStringMapper getInstance() {
        return formatStringMapper;
    }

    /**
     * This method takes in a Ballerina log format string and converts it in to format strings used in String formatters
     * in Java. This also makes use of the placeholder map to determine the positions of the log record items within the
     * log message.
     *
     * @param logFormatKey The .format configuration key for a particular logger
     * @param logFormatVal Ballerina log format to be mapped to the Java format string
     * @return The Java format string equivalent of the provided Ballerina log format string
     */
    public String buildJDKLogFormat(String logFormatKey, String logFormatVal) {
        String[] tokens = parseFormatString(logFormatVal);
        StringBuilder formatBuilder = new StringBuilder();
        Map<String, Integer> placeholders = placeholderMap.get(logFormatKey);
        placeholders = placeholders != null ? placeholders : placeholderMap.get(Constants.BALLERINA_LOG_FORMAT);

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith(Constants.FMT_TIMESTAMP)) {
                String format;
                try {
                    format = tokens[i].substring(Constants.FMT_TIMESTAMP.length() + 1, tokens[i].length() - 1);
                } catch (Exception e) {
                    BLogManager.stdErr.println(
                            "Invalid timestamp format detected. Defaulting to: \"" + defaultTimestampFmt + "\"");
                    format = defaultTimestampFmt;
                }
                dateFormatMap.put(logFormatKey, new SimpleDateFormat(format));
                formatBuilder.append("%1$s");
            } else if (placeholders.containsKey(tokens[i])) {
                formatBuilder.append("%" + placeholderMap.get(logFormatKey).get(tokens[i]) + "$s");
            } else {
                formatBuilder.append(tokens[i]);
            }
        }
//        formatBuilder.append('\n');

        return formatBuilder.toString();
    }

    /**
     * This method can be used to retrieve the timestamp format to be used with a particular logger. The timestamp
     * formats are added in the buildJDKLogFormat() method, when mapping the Ballerina log format string to a Java
     * format string. Therefore, this relies on buildJDKLogFormat() being called beforehand to ensure the correct
     * timestamp format is returned for a particular logger. In the event that it fails to find a timestamp format for a
     * particular logger, the default timestamp format used in Ballerina is returned.
     *
     * @param key The .format configuration key for a particular logger
     * @return The timestamp format corresponding to the 'key'
     */
    public SimpleDateFormat getDateFormat(String key) {
        SimpleDateFormat sdf = dateFormatMap.get(key);
        return sdf != null ? sdf : new SimpleDateFormat(defaultTimestampFmt);
    }

    private String[] parseFormatString(String formatString) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = logPattern.matcher(formatString);

        int i = 0;
        while (matcher.find()) {
            if (matcher.start() != i) {
                tokens.add(formatString.substring(i, matcher.start()));
            }

            tokens.add(formatString.substring(matcher.start(), matcher.end()));
            i = matcher.end();
        }
        tokens.add(formatString.substring(i));

        return tokens.toArray(new String[tokens.size()]);
    }
}
