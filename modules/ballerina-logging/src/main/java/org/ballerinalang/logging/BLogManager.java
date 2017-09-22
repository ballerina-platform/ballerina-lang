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

import org.ballerinalang.logging.formatters.jul.HTTPTraceLogFormatter;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.logging.util.BLogLevelMapper;
import org.ballerinalang.logging.util.ConfigMapper;
import org.ballerinalang.logging.util.Constants;
import org.ballerinalang.logging.util.FormatStringMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Java util logging manager for ballerina which overrides the readConfiguration method to replace placeholders
 * having system or environment variables.
 *
 * @since 0.8.0
 */
public class BLogManager extends LogManager {

    public static final String BALLERINA_ROOT_LOGGER_NAME = "ballerina";
//    public static final int LOGGER_PREFIX_LENGTH = BALLERINA_ROOT_LOGGER_NAME.length() + 1; // +1 to account for the .
    public static final PrintStream STD_OUT = System.out;
    public static final PrintStream STD_ERR = System.err;

//    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");

    private Logger httpTraceLogger;
    private BLogLevel ballerinaRootLogLevel;
    private Properties logConfigs = new Properties();

    @Override
    public void readConfiguration() throws IOException {
        logConfigs = getDefaultConfiguration();
        Properties sysProps = System.getProperties();
        FormatStringMapper mapper = FormatStringMapper.getInstance();

        sysProps.forEach((k, v) -> {
            String key = (String) k;
            String val = (String) v;

            if (key.startsWith("log.")) {
                String property = ConfigMapper.mapConfiguration(key);

                if (property.endsWith(".level")) {
                    logConfigs.setProperty(property, BLogLevelMapper.getJDKLogLevel(val));

                    if (key.contains(Constants.BALLERINA_RUNTIME)) {
                        logConfigs.setProperty(Constants.LEVEL, logConfigs.getProperty(property));
                    }
                } else if (property.endsWith(".format")) {
                    logConfigs.setProperty(property, mapper.buildJDKLogFormat(key, val));
                }
            }
        });

        try {
            ballerinaRootLogLevel =
                    BLogLevelMapper.getBallerinaLogLevel(logConfigs.getProperty(Constants.BALLERINA_LEVEL));
        } catch (IllegalArgumentException e) {
            STD_ERR.println("Invalid log level value given for 'log.level'");
            STD_ERR.println("Setting 'log.level=INFO'");
            logConfigs.setProperty(Constants.BALLERINA_LEVEL, BLogLevel.INFO.name());
            ballerinaRootLogLevel = BLogLevel.INFO;
        }

        String traceLogLevel = logConfigs.getProperty(Constants.HTTP_TRACELOG_LEVEL);
        if (traceLogLevel != null &&
                (BLogLevelMapper.getBallerinaLogLevel(traceLogLevel) == BLogLevel.DEBUG)) {
            System.setProperty(Constants.HTTP_TRACELOG, Constants.LOG_DEST_CONSOLE);
            setHttpTraceLogHandler();
        }

        super.readConfiguration(propertiesToInputStream(logConfigs));
    }

    public BLogLevel getPackageLogLevel(String pkg) {
        String level = this.getProperty("log." + pkg + ".level");
        return level != null ? BLogLevelMapper.getBallerinaLogLevel(level) : ballerinaRootLogLevel;
    }

    public void setHttpTraceLogHandler() throws IOException {
        Handler handler = new ConsoleHandler();
        handler.setFormatter(new HTTPTraceLogFormatter());
        handler.setLevel(Level.FINE);

        if (httpTraceLogger == null) {
            // keep a reference to prevent this logger from being garbage collected
            httpTraceLogger = Logger.getLogger(Constants.HTTP_TRACELOG);
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

//    private String substituteVariables(String value) {
//        Matcher matcher = varPattern.matcher(value);
//        boolean found = matcher.find();
//        if (!found) {
//            return value;
//        }
//        StringBuffer buffer = new StringBuffer();
//        do {
//            String sysPropertyKey = matcher.group(1);
//            String sysPropertyValue = getSystemVariableValue(sysPropertyKey);
//            if (sysPropertyValue != null && !sysPropertyValue.isEmpty()) {
//                sysPropertyValue = sysPropertyValue.replace("\\", "\\\\");
//                matcher.appendReplacement(buffer, sysPropertyValue);
//            }
//        } while (matcher.find());
//        matcher.appendTail(buffer);
//        return buffer.toString();
//    }

//    private String getSystemVariableValue(String variableName) {
//        String value;
//        if (System.getProperty(variableName) != null) {
//            value = System.getProperty(variableName);
//        } else if (System.getenv(variableName) != null) {
//            value = System.getenv(variableName);
//        } else {
//            value = variableName;
//        }
//        return value;
//    }

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

    private Properties getDefaultConfiguration() {
        // Configurations for BRE log
        logConfigs.setProperty(Constants.BRE_LOG_FILE_HANDLER_LEVEL, Level.WARNING.getName());
        logConfigs.setProperty(Constants.BRE_LOG_FILE_HANDLER_PATTERN,
                               System.getProperty(Constants.BALLERINA_RUNTIME_LOG_FILE));
        logConfigs.setProperty(Constants.BRE_LOG_FILE_HANDLER_LIMIT, String.valueOf(1000000));
        logConfigs.setProperty(Constants.BRE_LOG_FILE_HANDLER_APPEND, "true");
        logConfigs.setProperty(Constants.BRE_LOG_FILE_HANDLER_FORMATTER, Constants.BRE_LOG_FORMATTER);

        // Configurations for HTTP trace log
        logConfigs.setProperty(Constants.HTTP_TRACELOG_USE_PARENT_HANDLERS, "false");

        // Root logger configurations
        logConfigs.setProperty(Constants.HANDLERS, Constants.BRE_LOG_FILE_HANDLER);
        logConfigs.setProperty(Constants.LEVEL, Level.WARNING.getName());

        return logConfigs;
    }
}
