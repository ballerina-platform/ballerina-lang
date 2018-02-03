/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.logging.formatters;

import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.logging.util.BLogLevelMapper;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A custom formatter for formatting the error log.
 *
 * @since 0.89
 */
public class ErrorLogFormatter extends Formatter {

    private static final String format = BLogManager.getLogManager().getProperty(
            ErrorLogFormatter.class.getCanonicalName() + ".format");

    @Override
    public String format(LogRecord record) {
        String source = "";
        if (record.getLoggerName().length() <= BLogManager.LOGGER_PREFIX_LENGTH) {
            source = record.getLoggerName().substring(BLogManager.LOGGER_PREFIX_LENGTH);
        }
        return String.format(format,
                             new Date(record.getMillis()),
                             BLogLevelMapper.getBallerinaLogLevel(record.getLevel()),
                             source,
                             record.getMessage());
    }
}
