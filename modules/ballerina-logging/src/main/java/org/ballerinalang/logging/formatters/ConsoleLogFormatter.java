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
import org.ballerinalang.logging.util.Constants;
import org.ballerinalang.logging.util.FormatStringMapper;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A formatter for formatting the messages logged to the console
 *
 * @since 0.94
 */
public class ConsoleLogFormatter implements BLogFormatter {

    public static final Map<String, Integer> PLACEHOLDERS_MAP;

    private final String logFormat;

    private SimpleDateFormat dateFormat;

    static {
        PLACEHOLDERS_MAP = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>(Constants.FMT_TIMESTAMP, 1),
                new AbstractMap.SimpleEntry<>(Constants.FMT_LEVEL, 2),
                new AbstractMap.SimpleEntry<>(Constants.FMT_LOGGER, 3),
                new AbstractMap.SimpleEntry<>(Constants.FMT_PACKAGE, 4),
                new AbstractMap.SimpleEntry<>(Constants.FMT_UNIT, 5),
                new AbstractMap.SimpleEntry<>(Constants.FMT_FILE, 6),
                new AbstractMap.SimpleEntry<>(Constants.FMT_LINE, 7),
                new AbstractMap.SimpleEntry<>(Constants.FMT_WORKER, 8),
                new AbstractMap.SimpleEntry<>(Constants.FMT_MESSAGE, 9),
                new AbstractMap.SimpleEntry<>(Constants.FMT_ERROR, 10)
        ).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

    public ConsoleLogFormatter(String logFormat) {
        this.logFormat = logFormat;
        dateFormat = FormatStringMapper.getInstance().getDateFormat(Constants.BALLERINA_LOG_FORMAT);
    }

    @Override
    public String format(BLogRecord logRecord) {
        String error = logRecord.getError();
        return String.format(logFormat,
                             dateFormat.format(new Date(logRecord.getTimestamp())),
                             logRecord.getLevel().name(),
                             logRecord.getLoggerName(),
                             logRecord.getPackageName(), logRecord.getCallableUnitName(),
                             logRecord.getFileName(), logRecord.getLineNumber(),
                             logRecord.getWorkerName(),
                             logRecord.getMessage(),
                             error != null ? error : "");
    }
}
