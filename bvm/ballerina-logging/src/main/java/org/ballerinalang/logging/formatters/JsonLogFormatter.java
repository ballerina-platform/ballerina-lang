/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * JSON log formatter for formatting HTTP trace log files.
 *
 * @since 0.970.0
 */
public class JsonLogFormatter extends Formatter {

    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    @Override
    public String format(LogRecord record) {
        StringBuffer strbuff = new StringBuffer();
        long millis = record.getMillis();
        String date = getISO8601Date(new Date(millis), formatter);
        long seq = record.getSequenceNumber();
        String logger = record.getLoggerName();
        Level level = record.getLevel();
        String sourceClass = record.getSourceClassName();
        String sourceMethod = record.getSourceMethodName();
        int threadNum = record.getThreadID();
        String message = record.getMessage();
        Throwable sourceThrown = record.getThrown();

        strbuff.append("{ \"record\": {");

        writeJSON(strbuff, "logDate", date);

        strbuff.append(",");
        writeJSON(strbuff, "millis", String.valueOf(millis));

        strbuff.append(",");
        writeJSON(strbuff, "sequence", String.valueOf(seq));

        if (logger != null) {
            strbuff.append(",");
            writeJSON(strbuff, "logger", logger);
        }

        strbuff.append(",");
        writeJSON(strbuff, "level", level.getName());

        if (sourceClass != null) {
            strbuff.append(",");
            writeJSON(strbuff, "sourceClass", sourceClass);
        }

        if (sourceMethod != null) {
            strbuff.append(",");
            writeJSON(strbuff, "sourceMethod", sourceMethod);
        }

        strbuff.append(",");
        writeJSON(strbuff, "thread", String.valueOf(threadNum));

        if (message != null) {
            message = message.replaceAll("\\n", "\\\\n");
            message = message.replaceAll("\"", "\\\\\"");
            strbuff.append(",");
            writeJSON(strbuff, "message", message);
        }

        if (sourceThrown != null) {
            strbuff.append(",");
            strbuff.append("\"exception\": {");
            writeJSON(strbuff, "message", sourceThrown.getMessage());
            StackTraceElement[] frames = sourceThrown.getStackTrace();
            strbuff.append(",");
            strbuff.append("\"frames\": [");

            for (int i = 0; i < frames.length; i++) {
                strbuff.append(
                        "{ \"sourceClass\": \"" + frames[i].getClassName() + "\", \"sourceMethod\": \"" + frames[i]
                                .getMethodName() + "\"," + " \"line\": \"" + frames[i].getLineNumber() + "\" }");
                if (i < frames.length - 1) {
                    strbuff.append(",");
                }
            }
            strbuff.append("]");
            strbuff.append("}");
        }
        strbuff.append("} }\n");

        return strbuff.toString();
    }

    private void writeJSON(StringBuffer stringBuffer, String name, String value) {
        stringBuffer.append("\"" + name + "\": " + "\"" + value + "\"");
    }

    private String getISO8601Date(Date date, DateFormat formatter) {
        TimeZone timeZone = TimeZone.getDefault();
        formatter.setTimeZone(timeZone);
        return formatter.format(date);
    }
}
