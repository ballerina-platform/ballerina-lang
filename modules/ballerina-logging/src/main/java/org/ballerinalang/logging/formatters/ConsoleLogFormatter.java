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
import org.ballerinalang.logging.BLogRecord;

import java.util.Date;

/**
 * A formatter for formatting the messages logged to the console
 *
 * @since 0.94
 */
public class ConsoleLogFormatter extends BLogFormatter {

    private final String LOG_FORMAT;
    private final String JDK_LOG_FORMAT;

    public ConsoleLogFormatter() {
        LOG_FORMAT = BLogManager.getLogManager().getProperty("ballerina.format");
        JDK_LOG_FORMAT = buildJDKLogFormat(LOG_FORMAT);
    }

    @Override
    public String format(BLogRecord logRecord) {
        String error = logRecord.getError();
        return String.format(JDK_LOG_FORMAT,
                             dateFormat.format(new Date(logRecord.getTimestamp())),
                             logRecord.getLevel().name(),
                             logRecord.getPackageName(), logRecord.getCallableUnitName(),
                             logRecord.getFileName(), logRecord.getLineNumber(),
                             logRecord.getWorkerName(),
                             logRecord.getMessage(),
                             error != null ? error : "");
    }
}
