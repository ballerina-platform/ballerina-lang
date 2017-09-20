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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    private static final String logFormatRegex =
            "\\{\\{timestamp\\}\\}(\\[[a-zA-Z0-9_+\\-.\\ \\t:,!@#$%^&*();\\\\/|<>\"']+\\])?|\\{\\{\\w+\\}\\}";
    private static final Pattern logPattern = Pattern.compile(logFormatRegex);
    private static final Set<String> validPlaceholders;

    private SimpleDateFormat dateFormat;
    private String[] tokens;

    static {
        validPlaceholders = Collections.unmodifiableSet(Stream.of(
                Constants.FMT_TIMESTAMP, Constants.FMT_LEVEL, Constants.FMT_LOGGER, Constants.FMT_PACKAGE,
                Constants.FMT_UNIT, Constants.FMT_FILE, Constants.FMT_LINE, Constants.FMT_WORKER, Constants.FMT_MESSAGE,
                Constants.FMT_ERROR).collect(Collectors.toSet()));
    }

    public String buildJDKLogFormat(String logFormat) {
        tokens = parseFormatString(logFormat);
        StringBuilder formatBuilder = new StringBuilder();

        for (int i = 0, j = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith(Constants.FMT_TIMESTAMP)) {
                dateFormat =
                        new SimpleDateFormat(
                                tokens[i].substring(Constants.FMT_TIMESTAMP.length() + 1, tokens[i].length() - 1));
                formatBuilder.append("%" + (j + 1) + "$s");
                j++;
            } else if (isAPlaceholderToken(tokens[i])) {
                formatBuilder.append("%" + (j + 1) + "$s");
                j++;
            } else {
                formatBuilder.append(tokens[i]);
            }
        }
        formatBuilder.append('\n');

        return formatBuilder.toString();
    }

    public SimpleDateFormat getDateFormat() {
        if (dateFormat != null) {
            return dateFormat;
        }

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS"); // default timestamp format if it is not set
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

        // TODO: Validate the tokens
        return tokens.toArray(new String[tokens.size()]);
    }

    private boolean isAPlaceholderToken(String token) {
        return validPlaceholders.contains(token);
    }
}
