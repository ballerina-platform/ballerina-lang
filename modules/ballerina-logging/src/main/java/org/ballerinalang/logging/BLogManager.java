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
import org.ballerinalang.logging.formatters.jul.HTTPTraceLogFormatter;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.logging.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.logging.util.Constants.BALLERINA_RUNTIME_LOG;
import static org.ballerinalang.logging.util.Constants.BALLERINA_USER_LOG;
import static org.ballerinalang.logging.util.Constants.DEFAULT_BALLERINA_USER_LOG_FORMAT;
import static org.ballerinalang.logging.util.Constants.EMPTY_CONFIG;
import static org.ballerinalang.logging.util.Constants.HTTP_TRACELOG;
import static org.ballerinalang.logging.util.Constants.LOG_FORMAT;
import static org.ballerinalang.logging.util.Constants.LOG_LEVEL;

/**
 * Java util logging manager for ballerina which overrides the readConfiguration method to replace placeholders
 * having system or environment variables.
 *
 * @since 0.8.0
 */
public class BLogManager extends LogManager {

    public static PrintStream stdOut = System.out;
    public static PrintStream stdErr = System.err;

    private static final String logConfigFile = "logging.properties";
    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");

    private Logger httpTraceLogger;
    private BLogLevel ballerinaRootLogLevel;
    private Map<String, Map<String, String>> loggerConfigs = new HashMap<>();

    @Override
    public void readConfiguration() throws IOException {
        Properties properties = setDefaultConfiguration();

        properties.forEach((k, v) -> {
            String val = substituteVariables((String) v);
            properties.setProperty((String) k, val);
        });

        super.readConfiguration(propertiesToInputStream(properties));
    }

    public void readUserLevelLogConfiguration() throws IOException {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();

        String instancesVal = configRegistry.getGlobalConfigValue("ballerina.log.instances");
        if (instancesVal != EMPTY_CONFIG) {
            String[] loggerInstances = instancesVal.split(",");

            for (String instanceId : loggerInstances) {
                Map<String, String> map = new HashMap<>();
                map.put(LOG_LEVEL, configRegistry.getInstanceConfigValue(instanceId, LOG_LEVEL));
                map.put("format", configRegistry.getInstanceConfigValue(instanceId, "format"));
                loggerConfigs.put(instanceId, map);
            }
        }

        addDefaultLogConfigs(configRegistry);

        if (loggerConfigs.containsKey("ballerina.log") && loggerConfigs.get("ballerina.log").containsKey(LOG_LEVEL)) {
            ballerinaRootLogLevel = BLogLevel.valueOf(loggerConfigs.get("ballerina.log").get(LOG_LEVEL));
        } else {
            ballerinaRootLogLevel = BLogLevel.INFO;
        }

        String traceLogLevel = loggerConfigs.get("tracelog.http").get(LOG_LEVEL);
        if (traceLogLevel != null && (BLogLevel.valueOf(traceLogLevel) == BLogLevel.DEBUG)) {
            setHttpTraceLogHandler();
        }
    }

    // TODO: need to do this in a cleaner way
    private void addDefaultLogConfigs(ConfigRegistry configRegistry) {
        loggerConfigs.put(BALLERINA_USER_LOG, new HashMap<>());
        loggerConfigs.put(HTTP_TRACELOG, new HashMap<>());
        loggerConfigs.put(BALLERINA_RUNTIME_LOG, new HashMap<>());

        String level;
        if (!EMPTY_CONFIG.equals(level = configRegistry.getInstanceConfigValue(BALLERINA_USER_LOG, LOG_LEVEL))) {
            loggerConfigs.get(BALLERINA_USER_LOG).put(LOG_LEVEL, level);
        } else {
            loggerConfigs.get(BALLERINA_USER_LOG).put(LOG_LEVEL, BLogLevel.INFO.name());
        }

        if (!EMPTY_CONFIG.equals(level = configRegistry.getInstanceConfigValue(HTTP_TRACELOG, LOG_LEVEL))) {
            loggerConfigs.get(HTTP_TRACELOG).put(LOG_LEVEL, level);
        } else {
            loggerConfigs.get(HTTP_TRACELOG).put(LOG_LEVEL, BLogLevel.OFF.name());
        }

        if (!EMPTY_CONFIG.equals(level = configRegistry.getInstanceConfigValue(BALLERINA_RUNTIME_LOG, LOG_LEVEL))) {
            loggerConfigs.get(BALLERINA_RUNTIME_LOG).put(LOG_LEVEL, level);
        } else {
            loggerConfigs.get(BALLERINA_RUNTIME_LOG).put(LOG_LEVEL, BLogLevel.WARN.name());
        }

        String format;
        if (!EMPTY_CONFIG.equals(format = configRegistry.getInstanceConfigValue(BALLERINA_USER_LOG, LOG_FORMAT))) {
            loggerConfigs.get(BALLERINA_USER_LOG).put(LOG_FORMAT, format);
        } else {
            loggerConfigs.get(BALLERINA_USER_LOG).put(LOG_FORMAT, DEFAULT_BALLERINA_USER_LOG_FORMAT);
        }
    }

    public BLogLevel getPackageLogLevel(String pkg) {
        String level = ConfigRegistry.getInstance().getInstanceConfigValue(pkg, LOG_LEVEL);
        return !level.equals(EMPTY_CONFIG) ? BLogLevel.valueOf(level) : ballerinaRootLogLevel;
    }

    public String getLoggerConfiguration(String logger, String config) {
        return loggerConfigs.get(logger).get(config);
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

    private Properties setDefaultConfiguration() throws IOException {
        Properties properties = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream(logConfigFile);
        properties.load(in);
        return properties;
    }
}
