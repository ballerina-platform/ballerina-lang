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

import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.logging.util.BLogLevelMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A custom log formatter for formatting HTTP trace log files.
 *
 * @since 0.93
 */
public class HTTPTraceLogFormatter extends Formatter {

    private static String format = BLogManager.getLogManager().getProperty(
            HTTPTraceLogFormatter.class.getCanonicalName() + ".format");

    @Override
    public String format(LogRecord record) {
        String source = record.getLoggerName();
        String ex = "";

        if (record.getThrown() != null) {
            StringWriter stringWriter = new StringWriter();
            stringWriter.append('\n');
            record.getThrown().printStackTrace(new PrintWriter(stringWriter));
            ex = stringWriter.toString();
        }

        return String.format(format,
                             new Date(record.getMillis()),
                             BLogLevelMapper.getBallerinaLogLevel(record.getLevel()),
                             source,
                             record.getMessage(),
                             ex);
    }
}
