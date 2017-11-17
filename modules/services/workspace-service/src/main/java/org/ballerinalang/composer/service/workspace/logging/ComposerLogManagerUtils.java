/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.workspace.logging;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for modifying composer logs.
 */
public class ComposerLogManagerUtils {
    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([^}]*)}");
    private static final String LOG_CONFIG_FILE_PROPERTY = "java.util.logging.config.file";
    private static final String LOG_CONFIG_FILE = "composer-logging.properties";
    
    /**
     * Updating the default log manager.
     * @throws IOException When log config file cannot be found.
     */
    public void updateLogManager() throws IOException {
        // Modifying log manager property reading.
        LogManager logManager = LogManager.getLogManager();
        InputStream properties = this.readConfiguration();
        logManager.readConfiguration(properties);
    }
    
    /**
     * Read the logging configuration file and replacing the environment variables.
     * @return The updated properties as an input stream
     * @throws IOException When log config file cannot be found.
     */
    private InputStream readConfiguration() throws IOException {
        Properties properties = getDefaultLogConfiguration();
    
        properties.forEach((k, v) -> {
            String val = substituteVariables((String) v);
            properties.setProperty((String) k, val);
        });
    
        return propertiesToInputStream(properties);
    }
    
    /**
     * Substituting environment variables.
     * @param value The value to be replaced
     * @return The updated value.
     */
    private String substituteVariables(String value) {
        Matcher matcher = ENV_VAR_PATTERN.matcher(value);
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
    
    /**
     * Getting the system variable's value.
     * @param variableName The name of the variable.
     * @return The value.
     */
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
    
    /**
     * Converts properties to an input stream.
     * @param properties The properties.
     * @return Then input stream
     * @throws IOException When log config file cannot be found.
     */
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
    
    /**
     * Getting the default log config file's properties.
     * @return The log file properties.
     * @throws IOException When log config file cannot be found.
     */
    private Properties getDefaultLogConfiguration() throws IOException {
        Properties prop = new Properties();
        InputStream propertiesStream;
        try {
            propertiesStream = new FileInputStream(System.getProperty(LOG_CONFIG_FILE_PROPERTY));
            prop.load(propertiesStream);
        } catch (IOException e) {
            propertiesStream = getClass().getClassLoader().getResourceAsStream(LOG_CONFIG_FILE);
            prop.load(propertiesStream);
        }
        IOUtils.closeQuietly(propertiesStream);
        return prop;
    }
}
