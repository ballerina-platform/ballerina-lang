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
package org.ballerinalang.logging;

import org.ballerinalang.logging.formatters.HTTPTraceLogFormatter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java util logging manager for ballerina which overrides the readConfiguration method to replace placeholders
 * having system or environment variables.
 *
 * @since 0.8.0
 */
public class BLogManager extends LogManager {
    public static final String BALLERINA_ROOT_LOGGER_NAME = "ballerina";
    public static final int LOGGER_PREFIX_LENGTH = BALLERINA_ROOT_LOGGER_NAME.length() + 1; // +1 to account for the .
    public static final PrintStream STD_OUT = System.out;

    // Trace log related constants
    public static final String HTTP_TRACE_LOGGER = "tracelog.http";
    public static final String LOG_DEST_CONSOLE = "__console";

    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");

    private Logger httpTraceLogger;

    @Override
    public void readConfiguration(InputStream ins) throws IOException, SecurityException {
        Properties properties = new Properties();
        properties.load(ins);
        properties.forEach((k, v) -> {
            String val = substituteVariables((String) v);
            properties.setProperty((String) k, val);
        });

        super.readConfiguration(propertiesToInputStream(properties));
    }

    public void setHttpTraceLogHandler() throws IOException {
        Handler handler = new ConsoleHandler();
        handler.setFormatter(new HTTPTraceLogFormatter());
        handler.setLevel(Level.FINE);

        if (httpTraceLogger == null) {
            // keep a reference to prevent this logger from being garbage collected
            httpTraceLogger = Logger.getLogger(HTTP_TRACE_LOGGER);
        }

        removeHandlers(httpTraceLogger);
        httpTraceLogger.addHandler(handler);
        httpTraceLogger.setLevel(Level.FINE);
    }

    private static void removeHandlers(Logger logger) {
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            logger.removeHandler(handler);
        }
    }

    private String substituteVariables(String value) {
        Matcher matcher = varPattern.matcher(value);
        boolean found = matcher.find();
        if (!found) {
            return value;
        }
        StringBuffer buffer = new StringBuffer();
        do {
            String sysPropertyKey = matcher.group(1);
            String sysPropertyValue = getSystemVariableValue(sysPropertyKey);
            if (sysPropertyValue != null && !sysPropertyValue.isEmpty()) {
                sysPropertyValue = sysPropertyValue.replace("\\", "\\\\");
                matcher.appendReplacement(buffer, sysPropertyValue);
            }
        } while (matcher.find());
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String getSystemVariableValue(String variableName) {
        String value;
        if (System.getProperty(variableName) != null) {
            value = System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            value = System.getenv(variableName);
        } else {
            value = variableName;
        }
        return value;
    }

    private InputStream propertiesToInputStream(Properties properties) throws IOException {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            properties.store(outputStream, "Java util logging configuration properties");
            return new ByteArrayInputStream(outputStream.toByteArray());
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
