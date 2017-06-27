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

package org.ballerinalang.nativeimpl.utils.logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class BvmLogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        StringBuilder sBuilder = new StringBuilder();

        // Add timestamp
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS O").withZone(
                ZoneId.systemDefault());
        sBuilder.append(dtf.format(Instant.ofEpochMilli(record.getMillis())));

        // Add log level
        sBuilder.append(' ');
        sBuilder.append(record.getLevel().getLocalizedName().toLowerCase());

        // Add logger name
        sBuilder.append(' ');
        sBuilder.append('[');

        String loggerName = record.getLoggerName();

        if(loggerName.length() <= "ballerina.".length()) {
            sBuilder.append(".");
        } else {
            sBuilder.append(loggerName.substring("ballerina.".length()));
        }

        sBuilder.append(']');

        // Add log message
        sBuilder.append(" - ");
        sBuilder.append(record.getMessage());

        sBuilder.append(System.lineSeparator());

        //TODO: read the format string from System property
        return sBuilder.toString();
    }
}
