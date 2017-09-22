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

package org.ballerinalang.logging;

import org.ballerinalang.logging.formatters.ConsoleLogFormatter;
import org.ballerinalang.logging.handlers.BConsoleLogHandler;
import org.ballerinalang.logging.handlers.BLogHandler;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.logging.util.Constants;

/**
 * Logger for the Ballerina log API
 *
 * @since 0.94
 */
public class BLogger {

    private BLogLevel level; // system log level
    private String name;
    private BLogHandler logHandler;
    private BLogManager logManager;

    public BLogger(String name) {
        this.name = name;
        this.logHandler = new BConsoleLogHandler(
                new ConsoleLogFormatter(BLogManager.getLogManager().getProperty(Constants.BALLERINA_LOG_FORMAT)));
        this.logManager = (BLogManager) BLogManager.getLogManager();
    }

    public void log(BLogRecord logRecord) {
        if (!isLoggable(logRecord.getLevel(), logRecord.getPackageName())) {
            return;
        }

        logHandler.publish(logRecord);
    }

    public BLogLevel getLevel() {
        return level;
    }

    public void setLevel(BLogLevel level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BLogHandler getLogHandler() {
        return logHandler;
    }

    public void setLogHandler(BLogHandler logHandler) {
        this.logHandler = logHandler;
    }

    private boolean isLoggable(BLogLevel level, String pkg) {
        return pkg != null && logManager.getPackageLogLevel(pkg).value() <= level.value();
    }
}
