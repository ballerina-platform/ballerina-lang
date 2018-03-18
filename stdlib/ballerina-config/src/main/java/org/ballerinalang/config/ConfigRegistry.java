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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * ConfigRegistry caches configuration properties and provide API.
 *
 * @since 0.95
 */
public class ConfigRegistry {

    private static final ConfigRegistry configRegistry = new ConfigRegistry();

    private static final String STRING_NULL_VALUE = null;

    private Map<String, String> globalConfigs = new HashMap<>();
    private Map<String, Map<String, String>> instanceConfigs = new HashMap<>();

    private ConfigProcessor confProcessor;

    private ConfigRegistry() {
        confProcessor = new ConfigProcessor(this);
    }

    public static ConfigRegistry getInstance() {
        return configRegistry;
    }

    /**
     * Prepares for parsing and loading the configurations by initializing the config processor.
     *
     * @param runtimeParams The Ballerina runtime parameters (-B params)
     * @param ballerinaConfPath Path to the default ballerina.conf file
     */
    public void initRegistry(Map<String, String> runtimeParams, Path ballerinaConfPath) {
        confProcessor.setRuntimeConfiguration(runtimeParams);
        confProcessor.setBallerinaConfDefaultPath(ballerinaConfPath);
    }

    /**
     * Prompts the config processor to process and load the configurations to the config registry.
     *
     * @throws ConfigFileParserException Thrown if the config parsing failed
     */
    public void loadConfigurations() throws ConfigFileParserException {
        confProcessor.processConfiguration();
    }

    /**
     * Sets global config properties as a map.
     *
     * @param globalConfigMap Processed map of key value pairs.
     */
    protected void setGlobalConfigs(Map<String, String> globalConfigMap) {
        globalConfigs.putAll(globalConfigMap);
    }

    /**
     * Returns global config value based on config name.
     *
     * @param configKey The key used to look up a configuration
     * @return The corresponding config value
     */
    public String getGlobalConfigValue(String configKey) {
        if (globalConfigs.isEmpty() || (globalConfigs.get(configKey) == null)) {
            return STRING_NULL_VALUE;
        }
        return globalConfigs.get(configKey);
    }

    /**
     * Sets instance config properties as a map.
     *
     * @param instanceConfigMap The final, resolved map of key value pairs.
     */
    protected void setInstanceConfigs(Map<String, Map<String, String>> instanceConfigMap) {
        instanceConfigs.putAll(instanceConfigMap);
    }

    /**
     * Returns instance config value based on instance id and config name.
     *
     * @param instanceId The ID of the instance whose configurations which need to be looked up
     * @param configKey The key for the configuration to be looked up
     * @return The config value
     */
    public String getInstanceConfigValue(String instanceId, String configKey) {
        if (instanceConfigs.isEmpty() || (instanceConfigs.get(instanceId) == null)) {
            return STRING_NULL_VALUE;
        }

        Map<String, String> instanceConfig = instanceConfigs.get(instanceId);
        String configValue = instanceConfig.get(configKey);
        if (configValue == null) {
            return STRING_NULL_VALUE;
        }
        return configValue;
    }
}
