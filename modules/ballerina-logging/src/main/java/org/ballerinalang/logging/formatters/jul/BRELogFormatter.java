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

package org.ballerinalang.logging.formatters.jul;

import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.logging.util.BLogLevelMapper;
import org.ballerinalang.logging.util.Constants;
import org.ballerinalang.logging.util.FormatStringMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A custom log formatter for formatting the BRE log
 *
 * @since 0.94
 */
public class BRELogFormatter extends Formatter {

    public static final Map<String, Integer> PLACEHOLDERS_MAP;

    private final String format;

    private SimpleDateFormat dateFormat;

    static {
        PLACEHOLDERS_MAP = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>(Constants.FMT_TIMESTAMP, 1),
                new AbstractMap.SimpleEntry<>(Constants.FMT_LEVEL, 2),
                new AbstractMap.SimpleEntry<>(Constants.FMT_LOGGER, 3),
                new AbstractMap.SimpleEntry<>(Constants.FMT_CLASS, 4),
                new AbstractMap.SimpleEntry<>(Constants.FMT_UNIT, 5),
                new AbstractMap.SimpleEntry<>(Constants.FMT_WORKER, 6),
                new AbstractMap.SimpleEntry<>(Constants.FMT_MESSAGE, 7),
                new AbstractMap.SimpleEntry<>(Constants.FMT_ERROR, 8)
        ).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

    public BRELogFormatter() {
        format = BLogManager.getLogManager().getProperty(BRELogFormatter.class.getCanonicalName() + ".format");
        dateFormat = FormatStringMapper.getInstance().getDateFormat(Constants.LOG_BRE_LOG_FORMAT);
    }

    @Override
    public String format(LogRecord record) {
        String ex = "";

        if (record.getThrown() != null) {
            StringWriter stringWriter = new StringWriter();
            stringWriter.append('\n');
            record.getThrown().printStackTrace(new PrintWriter(stringWriter));
            ex = stringWriter.toString();
        }

        return String.format(format,
                             dateFormat.format(new Date(record.getMillis())),
                             BLogLevelMapper.getBallerinaLogLevel(record.getLevel()),
                             record.getLoggerName(),
                             record.getSourceClassName(),
                             record.getSourceMethodName(),
                             record.getThreadID(),
                             record.getMessage(),
                             ex);
    }
}
