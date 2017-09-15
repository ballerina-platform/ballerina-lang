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

package org.ballerinalang.logging.formatters;

import org.ballerinalang.logging.BLogRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A base class for log formatters.
 *
 * @since 0.94
 */
public abstract class BLogFormatter {

    private static final String logFormatRegex =
            "\\{\\{\\w+\\}\\}(\\[[a-zA-Z0-9_+\\-.\\ \\t:,!@#$%^&*();\\\\/|<>\"']+\\])?";
    private static final Pattern logPattern = Pattern.compile(logFormatRegex);

    protected SimpleDateFormat dateFormat;

    /**
     * A method for formatting a log record.
     *
     * @param logRecord The log record containing the information to be logged.
     * @return Returns the formatted log record as a String.
     */
    public abstract String format(BLogRecord logRecord);

    protected String buildJDKLogFormat(String logFormat) {
        String[] tokens = parseFormatString(logFormat);
        StringBuilder formatBuilder = new StringBuilder();

        for (int i = 0, j = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("{{timestamp}}")) {
                dateFormat =
                        new SimpleDateFormat(tokens[i].substring("{{timestamp}}".length() + 3, tokens[i].length() - 1));
                formatBuilder.append("%" + (j + 1) + "$s");
                j++;
            } else if (tokens[i].startsWith("{{level}}")) {
                formatBuilder.append("%" + (j + 1) + "$-5s");
                j++;
            } else if (tokens[i].startsWith("{{")) {
                formatBuilder.append("%" + (j + 1) + "$s");
                j++;
            } else {
                formatBuilder.append(tokens[i]);
            }
        }
        formatBuilder.append('\n');

        return formatBuilder.toString();
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
}
