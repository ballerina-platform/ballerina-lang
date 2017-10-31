/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.config;

import org.ballerinalang.config.utils.parser.ConfigFileParser;
import org.ballerinalang.config.utils.parser.ConfigParamParser;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ConfigProcessor processes runtime, environment and config file configurations.
 *
 * @since 0.95
 */
public class ConfigProcessor {

    private static final String BALLERINA_CONF = "ballerina.conf";
    private static final String USER_DIR = "user.dir";
    private static final String BALLERINA_CONF_DEFAULT_PATH = System.getProperty(USER_DIR) + File.separator +
            BALLERINA_CONF;
    private static Map<String, String> runtimeParams = new HashMap<>();
    private static Map<String, String> prioritizedGlobalConfigs = new HashMap<>();
    private static Map<String, Map<String, String>> prioritizedInstanceConfigs = new HashMap<>();

    /**
     * Sets runtime config properties gathered from user as a map.
     *
     * @param params is a map of key value pairs
     */
    public static void setRuntimeConfiguration(Map<String, String> params) {
        runtimeParams = params;
    }

    /**
     * Processes runtime, environment and config file properties.This populates configRegistry with configs based on
     * the following precedence order. 1. Ballerina runtime properties, 2. External config
     * (environment vars, etcd or something similar), 3. ballerina.conf file
     */
    public static void processConfiguration() throws IOException {
        ConfigParamParser paramParser = new ConfigParamParser(runtimeParams);
        Map<String, String> runtimeGlobalConfigs = paramParser.getGlobalConfigs();
        Map<String, Map<String, String>> runtimeInstanceConfigs = paramParser.getInstanceConfigs();

        File confFile = getConfigFile(runtimeGlobalConfigs);
        if (confFile == null) {
            prioritizedGlobalConfigs.putAll(runtimeGlobalConfigs);
            prioritizedInstanceConfigs.putAll(runtimeInstanceConfigs);
        } else {
            ConfigFileParser parser = new ConfigFileParser(confFile);
            Map<String, String> fileGlobalConfigs = parser.getGlobalConfigs();
            Map<String, Map<String, String>> fileInstanceConfigs = parser.getInstanceConfigs();

            createPrioritizedConfigs
                    (runtimeGlobalConfigs, runtimeInstanceConfigs, fileGlobalConfigs, fileInstanceConfigs);
        }
        ConfigRegistry.setGlobalConfigs(prioritizedGlobalConfigs);
        ConfigRegistry.setInstanceConfigs(prioritizedInstanceConfigs);
    }

    private static void createPrioritizedConfigs(Map<String, String> runtimeGlobalConfigs
            , Map<String, Map<String, String>> runtimeInstanceConfigs, Map<String, String> fileGlobalConfigs
            , Map<String, Map<String, String>> fileInstanceConfigs) {

        prioritizedInstanceConfigs = new HashMap<>();
        prioritizedGlobalConfigs = new HashMap<>(fileGlobalConfigs);
        prioritizedGlobalConfigs.putAll(runtimeGlobalConfigs);

        Set<String> instances = new HashSet<>(runtimeInstanceConfigs.keySet());
        instances.addAll(fileInstanceConfigs.keySet());

        for (String instance : instances) {
            Map<String, String> runtimeConfigs = runtimeInstanceConfigs.get(instance);
            Map<String, String> fileConfigs = fileInstanceConfigs.get(instance);

            if (fileConfigs == null) {
                prioritizedInstanceConfigs.put(instance, runtimeConfigs);
            } else if (runtimeConfigs == null) {
                prioritizedInstanceConfigs.put(instance, fileConfigs);
            } else {
                fileConfigs.putAll(runtimeConfigs);
                prioritizedInstanceConfigs.put(instance, fileConfigs);
            }
        }
    }

    private static File getConfigFile(Map<String, String> runtimeGlobalConfigs) {
        String fileLocation = runtimeGlobalConfigs.get(BALLERINA_CONF);
        File confFile;
        if (fileLocation != null) {
            confFile = new File(fileLocation);
            if (!confFile.exists()) {
                throw new BallerinaException("failed to parse ballerina.conf file: " + fileLocation);
            }
        } else {
            confFile = new File(BALLERINA_CONF_DEFAULT_PATH);
            if (!confFile.exists()) {
                return null;
            }
        }
        return confFile;
    }
}
