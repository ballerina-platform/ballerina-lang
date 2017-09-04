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

package org.ballerinalang.logging.handlers;

import org.ballerinalang.logging.BLogRecord;

import java.util.Date;

/**
 * A handler implementation for logging to the console
 *
 * @since 0.94
 */
public class BConsoleLogHandler extends BLogHandler {

    @Override
    public void publish(BLogRecord logRecord) {
        if (!isLoggable(logRecord.getLevel())) {
            return;
        }

        System.err.println(format(logRecord));
    }

    private static String format(BLogRecord logRecord) {
        StringBuilder logMsgBuilder = new StringBuilder();

        Date date = new Date(logRecord.getTimestamp());
        String level = logRecord.getLevel().name();
        String msg = logRecord.getMessage();
        String location = logRecord.getLocation();
        String lineNumber = logRecord.getLineNumber();
        String workerName = logRecord.getWorkerName();
        String error = logRecord.getError() != null ? logRecord.getError() : "";

        return logMsgBuilder
                .append(String.format("%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS,%1$tL", date)).append(' ')
                .append(level).append(" [").append(location).append("] [")
                .append(lineNumber).append("] [")
                .append(workerName).append("] - ")
                .append('\"').append(msg).append('\"')
                .append(' ').append(error)
                .toString();
    }
}
