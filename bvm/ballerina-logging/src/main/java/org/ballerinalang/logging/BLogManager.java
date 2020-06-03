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

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.logging.formatters.HttpAccessLogFormatter;
import org.ballerinalang.logging.formatters.HttpTraceLogFormatter;
import org.ballerinalang.logging.formatters.JsonLogFormatter;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.logging.util.BLogLevelMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SocketHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.logging.util.Constants.BALLERINA_USER_LOG_LEVEL;
import static org.ballerinalang.logging.util.Constants.CONSOLE_LOGGER;
import static org.ballerinalang.logging.util.Constants.HTTP_ACCESS_LOG;
import static org.ballerinalang.logging.util.Constants.HTTP_ACCESS_LOG_CONSOLE;
import static org.ballerinalang.logging.util.Constants.HTTP_ACCESS_LOG_FILE;
import static org.ballerinalang.logging.util.Constants.HTTP_TRACE_LOG;
import static org.ballerinalang.logging.util.Constants.HTTP_TRACE_LOG_CONSOLE;
import static org.ballerinalang.logging.util.Constants.HTTP_TRACE_LOG_FILE;
import static org.ballerinalang.logging.util.Constants.HTTP_TRACE_LOG_HOST;
import static org.ballerinalang.logging.util.Constants.HTTP_TRACE_LOG_PORT;
import static org.ballerinalang.logging.util.Constants.LOG_LEVEL;

/**
 * Java util logging manager for ballerina which overrides the readConfiguration method to replace placeholders
 * having system or environment variables.
 *
 * @since 0.8.0
 */
public class BLogManager extends LogManager {

    public static final String BALLERINA_ROOT_LOGGER_NAME = "ballerina";
    public static final int LOGGER_PREFIX_LENGTH = BALLERINA_ROOT_LOGGER_NAME.length() + 1; // +1 to account for the .
    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");

    private Map<String, BLogLevel> loggerLevels = new HashMap<>();
    private BLogLevel ballerinaUserLogLevel = BLogLevel.INFO; // default to INFO
    private Logger httpTraceLogger;
    private Logger httpAccessLogger;

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

    public void loadUserProvidedLogConfiguration() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();

        Iterator<String> keys = configRegistry.keySetIterator();
        keys.forEachRemaining(key -> {
            if (key.endsWith(LOG_LEVEL)) {
                loggerLevels.put(key.substring(0, key.length() - LOG_LEVEL.length()),
                                 BLogLevel.toBLogLevel(configRegistry.getAsString(key)));
            }
        });

        // setup Ballerina user-level log level configuration
        String userLogLevel = configRegistry.getAsString(BALLERINA_USER_LOG_LEVEL);
        if (userLogLevel != null) {
            ballerinaUserLogLevel = BLogLevel.toBLogLevel(userLogLevel);
        }
        loggerLevels.put(BALLERINA_USER_LOG_LEVEL, ballerinaUserLogLevel);

        setHttpTraceLogHandler();
        setHttpAccessLogHandler();

        // have to set default console logger level here since ballerina config is not initialized at the time of the
        // logger initialization
        if (loggerLevels.get(CONSOLE_LOGGER) != null) {
            LogManager.getLogManager().getLogger("").setLevel(BLogLevelMapper.getLoggerLevel(
                    loggerLevels.get(CONSOLE_LOGGER)));
        }

    }

    public BLogLevel getPackageLogLevel(String pkg) {
        return loggerLevels.containsKey(pkg) ? loggerLevels.get(pkg) : ballerinaUserLogLevel;
    }

    /**
     * Get the global log level.
     *
     * @return ballerinaUserLogLevel
     */
    public BLogLevel getGlobalLogLevel() {
        return ballerinaUserLogLevel;
    }

    /**
     * Checks if module log level has been enabled.
     *
     * @return true if module log level has been enabled, false if not.
     */
    public boolean isModuleLogLevelEnabled() {
        return loggerLevels.size() > 1;
    }

    /**
     * Initializes the HTTP trace logger.
     */
    public void setHttpTraceLogHandler() {
        if (httpTraceLogger == null) {
            // keep a reference to prevent this logger from being garbage collected
            httpTraceLogger = Logger.getLogger(HTTP_TRACE_LOG);
        }
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        PrintStream stdErr = System.err;
        boolean tracelogsEnabled = false;

        String consoleLogEnabled = configRegistry.getAsString(HTTP_TRACE_LOG_CONSOLE);
        if (Boolean.parseBoolean(consoleLogEnabled)) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new HttpTraceLogFormatter());
            consoleHandler.setLevel(Level.FINEST);
            httpTraceLogger.addHandler(consoleHandler);
            tracelogsEnabled = true;
        }

        String logFilePath = configRegistry.getAsString(HTTP_TRACE_LOG_FILE);
        if (logFilePath != null && !logFilePath.trim().isEmpty()) {
            try {
                FileHandler fileHandler = new FileHandler(logFilePath, true);
                fileHandler.setFormatter(new HttpTraceLogFormatter());
                fileHandler.setLevel(Level.FINEST);
                httpTraceLogger.addHandler(fileHandler);
                tracelogsEnabled = true;
            } catch (IOException e) {
                throw new RuntimeException("failed to setup HTTP trace log file: " + logFilePath, e);
            }
        }

        String host = configRegistry.getAsString(HTTP_TRACE_LOG_HOST);
        String port = configRegistry.getAsString(HTTP_TRACE_LOG_PORT);
        if ((host != null && !host.trim().isEmpty()) && (port != null && !port.trim().isEmpty())) {
            try {
                SocketHandler socketHandler = new SocketHandler(host, Integer.parseInt(port));
                socketHandler.setFormatter(new JsonLogFormatter());
                socketHandler.setLevel(Level.FINEST);
                httpTraceLogger.addHandler(socketHandler);
                tracelogsEnabled = true;
            } catch (IOException e) {
                throw new RuntimeException("failed to connect to " + host + ":" + port, e);
            }
        }

        if (tracelogsEnabled) {
            httpTraceLogger.setLevel(Level.FINEST);
            System.setProperty("http.tracelog.enabled", "true");
            stdErr.println("ballerina: HTTP trace log enabled");
        }
    }

    /**
     * Initializes the HTTP access logger.
     */
    public void setHttpAccessLogHandler() {
        if (httpAccessLogger == null) {
            // keep a reference to prevent this logger from being garbage collected
            httpAccessLogger = Logger.getLogger(HTTP_ACCESS_LOG);
        }
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        PrintStream stdErr = System.err;
        boolean accesslogsEnabled = false;

        String consoleLogEnabled = configRegistry.getAsString(HTTP_ACCESS_LOG_CONSOLE);
        if (Boolean.parseBoolean(consoleLogEnabled)) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new HttpAccessLogFormatter());
            consoleHandler.setLevel(Level.INFO);
            httpAccessLogger.addHandler(consoleHandler);
            httpAccessLogger.setLevel(Level.INFO);
            accesslogsEnabled = true;
        }

        String filePath = configRegistry.getAsString(HTTP_ACCESS_LOG_FILE);
        if (filePath != null && !filePath.trim().isEmpty()) {
            try {
                FileHandler fileHandler = new FileHandler(filePath, true);
                fileHandler.setFormatter(new HttpAccessLogFormatter());
                fileHandler.setLevel(Level.INFO);
                httpAccessLogger.addHandler(fileHandler);
                httpAccessLogger.setLevel(Level.INFO);
                accesslogsEnabled = true;
            } catch (IOException e) {
                throw new RuntimeException("failed to setup HTTP access log file: " + filePath, e);
            }
        }

        if (accesslogsEnabled) {
            System.setProperty("http.accesslog.enabled", "true");
            stdErr.println("ballerina: HTTP access log enabled");
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
