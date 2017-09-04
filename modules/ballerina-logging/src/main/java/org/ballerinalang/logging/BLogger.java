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

import org.ballerinalang.logging.handlers.BConsoleLogHandler;
import org.ballerinalang.logging.handlers.BLogHandler;
import org.ballerinalang.logging.util.BLogLevel;

/**
 * Logger for the Ballerina log API
 *
 * @since 0.94
 */
public class BLogger {

    private BLogLevel level;
    private String name;
    private BLogHandler logHandler;

    public BLogger(BLogLevel level, String name) {
        this.level = level;
        this.name = name;
    }

    public BLogger(String name) {
        this.name = name;
        this.logHandler = new BConsoleLogHandler();

        String logLevel = System.getProperty("log.level");
        if(logLevel != null) {
            this.level = BLogLevel.valueOf(logLevel);
        } else {
            this.level = BLogLevel.INFO; // Default log level: INFO
        }
    }

    public void log(BLogLevel level, String msg, LogContext logCtx) {
        if (!isLoggable(level)) {
            return;
        }

        log(level, msg, null, logCtx);
    }

    public void log(BLogLevel level, String msg, String error, LogContext logCtx) {
        if (!isLoggable(level)) {
            return;
        }

        BLogRecord logRecord = new BLogRecord(level, msg);
        logRecord.setTimestamp(logCtx.getTimestamp());
        logRecord.setLocation(logCtx.getPackageName() + ":" + logCtx.getLocation());
        logRecord.setLineNumber(logCtx.getLineNumber());
        logRecord.setWorkerName(logCtx.getWorkerName());
        logRecord.setError(error);

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

    private boolean isLoggable(BLogLevel level) {
        return this.level.getLevelValue() <= level.getLevelValue() ? true : false;
    }
}
