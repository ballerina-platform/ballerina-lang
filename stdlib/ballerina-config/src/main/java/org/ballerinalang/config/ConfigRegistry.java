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

import java.io.IOException;
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

    private Map<String, String> configEntries = new HashMap<>();

    private ConfigRegistry() {
    }

    public static ConfigRegistry getInstance() {
        return configRegistry;
    }

    /**
     * Prepares for parsing and loading the configurations by initializing the config processor.
     * @param runtimeParams     The Ballerina runtime parameters (-B params)
     * @param configFilePath    The path to the user provided Ballerina config file
     * @param ballerinaConfPath Path to the default ballerina.conf file
     */
    public void initRegistry(Map<String, String> runtimeParams, String configFilePath, Path ballerinaConfPath)
            throws IOException {
        configEntries = ConfigProcessor.processConfiguration(runtimeParams, configFilePath, ballerinaConfPath);
    }

    /**
     * Add the specified key/value pair as a configuration entry.
     *
     * @param key   The configuration key
     * @param value The configuration value
     */
    public void addConfiguration(String key, String value) {
        configEntries.put(key, value);
    }

    /**
     * Add the specified key/value pair as a configuration entry. Here, the key will be derived using the tableHeader
     * and tableField parameters.
     *
     * @param tableHeader The name of the TOML table to which the config will be added
     * @param tableField  The config key under which the config value will be mapped in the table
     * @param value       The configuration value
     */
    public void addConfiguration(String tableHeader, String tableField, String value) {
        configEntries.put(getConfigKey(tableHeader, tableField), value);
    }

    /**
     * Retrieve the configuration value mapped by the specified key.
     *
     * @param key          The key of the configuration value
     * @param defaultValue The value to be used if the key is not in the registry
     * @return The configuration value as a string
     */
    public String getConfigOrDefault(String key, String defaultValue) {
        return configEntries.getOrDefault(key, defaultValue);
    }

    /**
     * Retrieve the configuration value mapped by the specified table header and table field.
     *
     * @param tableHeader  The name of the TOML table which contains the configuration
     * @param tableField   The config key under which the config value is mapped in the table
     * @param defaultValue The value to be used if the key is not in the registry
     * @return The configuration value as a string
     */
    public String getConfigOrDefault(String tableHeader, String tableField, String defaultValue) {
        return configEntries.getOrDefault(getConfigKey(tableHeader, tableField), defaultValue);
    }

    /**
     * Retrieve the configuration value mapped by the specified key as a char array.
     *
     * @param key          The key of the configuration value
     * @param defaultValue The value to be used if the key is not in the registry
     * @return The configuration value as a char array
     */
    public char[] getConfigOrDefaultAsCharArray(String key, String defaultValue) {
        String configValue = configEntries.getOrDefault(key, defaultValue);
        return configValue != null ? configValue.toCharArray() : null;
    }

    /**
     * Retrieve the configuration value mapped by the specified key.
     *
     * @param key The key of the configuration value
     * @return The configuration value as a string
     */
    public String getConfiguration(String key) {
        return configEntries.get(key);
    }

    /**
     * Retrieve the configuration value mapped by the specified table header and table field.
     *
     * @param tableHeader The name of the TOML table which contains the configuration
     * @param tableField  The config key under which the config value is mapped in the table
     * @return The configuration value as a string
     */
    public String getConfiguration(String tableHeader, String tableField) {
        return configEntries.get(getConfigKey(tableHeader, tableField));
    }

    /**
     * Retrieve the configuration value mapped by the specified key as a char array.
     *
     * @param key The key of the configuration value
     * @return The configuration value as a char array
     */
    public char[] getConfigAsCharArray(String key) {
        String configValue = configEntries.get(key);
        return configValue != null ? configValue.toCharArray() : null;
    }

    /**
     * Retrieve the configuration value mapped by the specified table header and table field as a char array.
     *
     * @param tableHeader The name of the TOML table which contains the configuration
     * @param tableField  The config key under which the config value is mapped in the table
     * @return The configuration value as a char array
     */
    public char[] getConfigAsCharArray(String tableHeader, String tableField) {
        String configValue = configEntries.get(getConfigKey(tableHeader, tableField));
        return configValue != null ? configValue.toCharArray() : null;
    }

    /**
     * Removes the specified key from the Config Registry.
     *
     * @param key The key for the configuration value to be removed
     * @return The removed configuration value
     */
    public String removeConfiguration(String key) {
        return configEntries.remove(key);
    }

    /**
     * Removes all the entries in the Config Registry.
     */
    public void resetRegistry() {
        configEntries.clear();
    }

    private String getConfigKey(String tableHeader, String tableField) {
        return tableHeader + "." + tableField;
    }
}
