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

import org.ballerinalang.config.utils.ConfigFileParserException;
import org.ballerinalang.config.utils.parser.ConfigFileParser;
import org.ballerinalang.config.utils.parser.ConfigParamParser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * ConfigProcessor processes runtime, environment and config file configurations.
 *
 * @since 0.95
 */
public class ConfigProcessor {

    private static final String BALLERINA_CONF = "ballerina.conf";

    private Path ballerinaConfDefaultPath;
    private Map<String, String> runtimeParams = new HashMap<>();
    private Map<String, String> resolvedGlobalConfigs = new HashMap<>();
    private Map<String, Map<String, String>> resolvedInstanceConfigs = new HashMap<>();
    private ConfigRegistry configRegistry;

    public ConfigProcessor(ConfigRegistry configRegistry) {
        this.configRegistry = configRegistry;
    }

    /**
     * Sets runtime config properties gathered from user as a map.
     *
     * @param params The Ballerina runtime parameters (i.e: -B params)
     */
    public void setRuntimeConfiguration(Map<String, String> params) {
        this.runtimeParams = params;
    }

    /**
     * Sets the given path as the path of the default ballerina.conf file.
     *
     * @param path The default path for ballerina.conf - i.e: {SOURCE_ROOT}/ballerina.conf
     */
    public void setBallerinaConfDefaultPath(Path path) {
        this.ballerinaConfDefaultPath = path;
    }

    /**
     * Processes runtime, environment and config file properties.This populates configRegistry with configs based on
     * the following precedence order. 1. Ballerina runtime properties, 2. External config
     * (environment vars, etcd or something similar), 3. ballerina.conf file
     *
     * @throws ConfigFileParserException if an error occur while parsing the file
     */
    public void processConfiguration() throws ConfigFileParserException {
        ConfigParamParser paramParser = new ConfigParamParser(runtimeParams);
        Map<String, String> runtimeGlobalConfigs = paramParser.getGlobalConfigs();
        Map<String, Map<String, String>> runtimeInstanceConfigs = paramParser.getInstanceConfigs();

        File confFile = getConfigFile(runtimeParams.get(BALLERINA_CONF));

        if (confFile != null) {
            ConfigFileParser parser = new ConfigFileParser(confFile);
            Map<String, String> fileGlobalConfigs = parser.getGlobalConfigs();
            Map<String, Map<String, String>> fileInstanceConfigs = parser.getInstanceConfigs();

            // TODO: make this variable replacement a pluggable process
            // Give precedence to environment and system variables
            lookUpVariables(fileGlobalConfigs, fileInstanceConfigs);

            // Add the remaining global configs to the resolved pool
            resolvedGlobalConfigs.putAll(fileGlobalConfigs);

            // Add the remaining configs of each instance config to the resolved pool
            resolvedInstanceConfigs.forEach((key, val) -> {
                Map<String, String> map = fileInstanceConfigs.get(key);
                if (map != null) {
                    val.putAll(map);
                }
            });

            // Add the remaining instance configs to the resolved pool
            fileInstanceConfigs.forEach((key, val) -> resolvedInstanceConfigs.putIfAbsent(key, val));
        }

        // Merge the runtime configurations with the already resolved configs.
        // Any configs already resolved and present are overridden.
        resolvedGlobalConfigs.putAll(runtimeGlobalConfigs);
        resolvedInstanceConfigs.forEach((key, val) -> {
            if (runtimeInstanceConfigs.containsKey(key)) {
                val.putAll(runtimeInstanceConfigs.get(key));
            }
        });
        runtimeInstanceConfigs.forEach((key, val) -> resolvedInstanceConfigs.putIfAbsent(key, val));

        configRegistry.setGlobalConfigs(resolvedGlobalConfigs);
        configRegistry.setInstanceConfigs(resolvedInstanceConfigs);
    }

    private void lookUpVariables(Map<String, String> globalConfigs, Map<String, Map<String, String>> instanceConfigs) {
        globalConfigs.keySet().forEach(key -> {
            String value = System.getenv(convertToEnvVarFormat(key));
            value = (value == null) ? System.getProperty(key) : value;

            if (value != null) {
                // replace the config value if there is an environment variable of the same name
                resolvedGlobalConfigs.put(key, value);
                globalConfigs.remove(key);
            }
        });

        instanceConfigs.keySet().forEach(instanceId -> {
            Map<String, String> configInstance = instanceConfigs.get(instanceId);
            configInstance.keySet().forEach(key -> {
                String value = System.getenv(convertToEnvVarFormat(key, instanceId));
                value = (value == null) ? System.getProperty(key) : value;
                if (value != null) {
                    // replace the config value if there is an environment variable of the same name
                    if (resolvedInstanceConfigs.containsKey(instanceId)) {
                        resolvedInstanceConfigs.get(instanceId).put(key, value);
                    } else {
                        Map<String, String> map = new HashMap<>();
                        map.put(key, value);
                        resolvedInstanceConfigs.put(instanceId, map);
                    }
                    configInstance.remove(key);
                }
            });
        });
    }

    private String convertToEnvVarFormat(String var) {
        return var.toUpperCase(Locale.ROOT).replace('.', '_');
    }

    private String convertToEnvVarFormat(String var, String instanceId) {
        return instanceId.toUpperCase(Locale.ROOT) + "__" + convertToEnvVarFormat(var);
    }

    private File getConfigFile(String fileLocation) {
        File confFile;
        if (fileLocation != null) {
            confFile = new File(fileLocation);
            if (!confFile.exists()) {
                throw new RuntimeException("failed to start ballerina runtime: file not found: " + fileLocation);
            }
        } else {
            if (ballerinaConfDefaultPath == null || !Files.exists(ballerinaConfDefaultPath)) {
                return null;
            }
            confFile = ballerinaConfDefaultPath.toFile();
        }
        return confFile;
    }
}
