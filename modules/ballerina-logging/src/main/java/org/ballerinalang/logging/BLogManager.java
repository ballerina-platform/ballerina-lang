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
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.logging.util.BLogLevelMapper;
import org.ballerinalang.logging.util.ConfigMapper;
import org.ballerinalang.logging.util.Constants;
import org.ballerinalang.logging.util.LogConfiguration;
import org.ballerinalang.logging.util.LoggerConfig;
import org.ballerinalang.logging.util.PackageConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
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

    private HashMap<String, BLogLevel> logLevelMap = new HashMap<>();

    @Override
    public void readConfiguration(InputStream ins) throws IOException, SecurityException {
        Properties defaultConfigs = getDefaultConfiguration();
        readConfigFile(ins, defaultConfigs);

        super.readConfiguration(propertiesToInputStream(defaultConfigs));
    }

    private void readConfigFile(InputStream in, Properties properties) {
        Yaml configFile = new Yaml();
        LogConfiguration configuration = configFile.loadAs(in, LogConfiguration.class);

        List<LoggerConfig> loggers = configuration.getLoggers();
        if (loggers != null) {
            loggers.forEach(l -> {
                String name = ConfigMapper.mapConfiguration(l.getName());
                properties.setProperty(name + ".level", BLogLevelMapper.getJDKLogLevel(l.getLogLevel()));
                properties.setProperty(name + ".format", BLogLevelMapper.getJDKLogLevel(l.getLogFormat()));
            });
        }

        List<PackageConfig> packages = configuration.getPackages();
        if (packages != null) {
            packages.forEach(p -> logLevelMap.put(p.getName(), BLogLevel.valueOf(p.getLogLevel())));
        }
    }

    public BLogLevel getPackageLogLevel(String pkg) {
        BLogLevel level = logLevelMap.get(pkg);
        return level != null ? level : BLogLevel.valueOf(this.getProperty(Constants.BALLERINA_LEVEL));
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

    private Properties getDefaultConfiguration() {
        Properties properties = new Properties();

        // Configurations for BRE log
        properties.setProperty(Constants.BRELOG_FILE_HANDLER_LEVEL, Level.WARNING.getName());
        properties.setProperty(Constants.BRELOG_FILE_HANDLER_PATTERN,
                               System.getProperty("ballerina.home") + File.separator + "logs" + File.separator +
                                       "bre.log");
        properties.setProperty(Constants.BRELOG_FILE_HANDLER_LIMIT, String.valueOf(1000000));
        properties.setProperty(Constants.BRELOG_FILE_HANDLER_APPEND, "true");
        properties.setProperty(Constants.BRELOG_FILE_HANDLER_FORMATTER, Constants.DEFAULT_LOG_FORMATTER);
        properties.setProperty(Constants.DEFAULT_LOG_FORMATTER_FORMAT, Constants.DEFAULT_LOG_FORMAT);

        // Configurations for HTTP trace log
        properties.setProperty(Constants.HTTP_TRACELOG_FORMATTER_FORMAT, Constants.HTTP_TRACELOG_FORMAT);

        // Root logger configurations
        properties.setProperty(Constants.HANDLERS, Constants.BRELOG_FILE_HANDLER);
        properties.setProperty(Constants.LEVEL, Level.WARNING.getName());

        return properties;
    }
}
