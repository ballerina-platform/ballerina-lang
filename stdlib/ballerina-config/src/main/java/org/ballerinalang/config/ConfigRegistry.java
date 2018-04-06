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

import org.ballerinalang.bcl.parser.BConfig;
import org.ballerinalang.config.cipher.AESCipherTool;
import org.ballerinalang.config.cipher.AESCipherToolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConfigRegistry caches configuration properties and provide API.
 *
 * @since 0.95
 */
public class ConfigRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConfigRegistry.class);
    private static final ConfigRegistry configRegistry = new ConfigRegistry();
    private static final Pattern encryptedFieldPattern = Pattern.compile("@encrypted:\\{(.*)\\}");

    private Map<String, String> configEntries = new HashMap<>();
    private AESCipherTool cipherTool;
    private PrintStream stderr = System.err;

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
        BConfig resolvedConfigs = ConfigProcessor.processConfiguration(runtimeParams, configFilePath,
                                                                       ballerinaConfPath);
        configEntries = resolvedConfigs.getConfigurations();

        if (resolvedConfigs.hasEncryptedValues()) {
            String customSecretFilePath = runtimeParams != null ? runtimeParams.get("ballerina.config.secret") : null;
            Path userSecretFile = getUserSecretFile(customSecretFilePath);

            try {
                if (Files.exists(userSecretFile)) {
                    cipherTool = new AESCipherTool(userSecretFile);
                } else {
                    // Prompting should not happen when secret file is explicitly set by the user
                    stderr.println("ballerina: enter secret for config value decryption:");
                    cipherTool = new AESCipherTool(new String(System.console().readPassword()));
                }
            } catch (AESCipherToolException e) {
                String msg = "failed to initialize the cipher tool: " + e.getMessage();
                throw new RuntimeException(msg, e);
            }
        }

        addConfiguration("ballerina.source.root", System.getProperty("ballerina.source.root"));
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
        addConfiguration(getConfigKey(tableHeader, tableField), value);
    }

    /**
     * Encrypts the value before adding the specified key/value pair as a configuration entry.
     *
     * @param key   The configuration key
     * @param value The configuration value
     */
    public void addEncryptedConfiguration(String key, String value) {
        if (cipherTool == null) {
            throw new RuntimeException("cipher tool is not initialized.");
        }

        addConfiguration(key, String.format("@encrypted:{%s}", value));
    }

    /**
     * Encrypts the given value before adding the specified key/value pair as a configuration entry. Here, the key will
     * be derived using the tableHeader and tableField parameters.
     *
     * @param tableHeader The name of the TOML table to which the config will be added
     * @param tableField  The config key under which the config value will be mapped in the table
     * @param value       The configuration value
     */
    public void addEncryptedConfiguration(String tableHeader, String tableField, String value) {
        addEncryptedConfiguration(getConfigKey(tableHeader, tableField), value);
    }

    /**
     * Checks whether the given key is present in the Config Registry.
     *
     * @param key The key to look-up
     * @return Returns true if the key is in the registry; returns false otherwise
     */
    public boolean contains(String key) {
        return configEntries.containsKey(key);
    }

    /**
     * Checks whether the given header/field combination is present in the Config Registry.
     *
     * @param tableHeader The name of the TOML table to look-up
     * @param tableField  The config key under which the config value will be mapped in the table
     * @return Returns true if the header/field pair is in the registry; returns false otherwise
     */
    public boolean contains(String tableHeader, String tableField) {
        return contains(getConfigKey(tableHeader, tableField));
    }

    /**
     * Retrieve the configuration value mapped by the specified key.
     *
     * @param key The key of the configuration value
     * @return The configuration value as a string
     */
    public String getConfiguration(String key) {
        String config = configEntries.get(key);
        Matcher base64Matcher = null;

        try {
            if (config != null) {
                base64Matcher = encryptedFieldPattern.matcher(config);

                if (base64Matcher.find()) {
                    return cipherTool.decrypt(base64Matcher.group(1));
                }
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("invalid base 64 value: " + base64Matcher.group(1));
        } catch (AESCipherToolException e) {
            throw new RuntimeException("failed to retrieve encrypted value: " + e.getMessage(), e);
        }

        return config;
    }

    /**
     * Retrieve the configuration value mapped by the specified table header and table field.
     *
     * @param tableHeader The name of the TOML table which contains the configuration
     * @param tableField  The config key under which the config value is mapped in the table
     * @return The configuration value as a string
     */
    public String getConfiguration(String tableHeader, String tableField) {
        return getConfiguration(getConfigKey(tableHeader, tableField));
    }

    /**
     * Retrieve the configuration value mapped by the specified key as a char array.
     *
     * @param key The key of the configuration value
     * @return The configuration value as a char array
     */
    public char[] getConfigAsCharArray(String key) {
        String configValue = getConfiguration(key);
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
        return getConfigAsCharArray(getConfigKey(tableHeader, tableField));
    }

    /**
     * Retrieve the configuration value mapped by the specified key.
     *
     * @param key          The key of the configuration value
     * @param defaultValue The value to be used if the key is not in the registry
     * @return The configuration value as a string
     */
    public String getConfigOrDefault(String key, String defaultValue) {
        String value;
        return ((value = getConfiguration(key)) != null) ? value : defaultValue;
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
        return getConfigOrDefault(getConfigKey(tableHeader, tableField), defaultValue);
    }

    /**
     * Retrieve the configuration value mapped by the specified key as a char array.
     *
     * @param key          The key of the configuration value
     * @param defaultValue The value to be used if the key is not in the registry
     * @return The configuration value as a char array
     */
    public char[] getConfigOrDefaultAsCharArray(String key, String defaultValue) {
        String configValue = getConfigOrDefault(key, defaultValue);
        return configValue != null ? configValue.toCharArray() : null;
    }

    /**
     * Retrieve the configuration value mapped by the specified key as a char array.
     *
     * @param tableHeader  The key of the configuration value
     * @param defaultValue The value to be used if the key is not in the registry
     * @return The configuration value as a char array
     */
    public char[] getConfigOrDefaultAsCharArray(String tableHeader, String tableField, String defaultValue) {
        return getConfigAsCharArray(getConfigKey(tableHeader, tableField), defaultValue);
    }

    /**
     * Retrieve the table of configurations specified by the table header.
     *
     * @param tableHeader The table name to retrieve
     * @return The config entries in the specified table
     */
    public Map<String, String> getConfigTable(String tableHeader) {
        Map<String, String> table = new HashMap<>();
        int subStringIndex = tableHeader.length() + 1;

        // TODO: handle tables properly at the config parsing level
        configEntries.entrySet().forEach(entry -> {
            if (entry.getKey().startsWith(tableHeader)) {
                table.put(entry.getKey().substring(subStringIndex), entry.getValue());
            }
        });

        return table;
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

    private Path getUserSecretFile(String path) {
        if (path == null) {
            return Paths.get(System.getProperty("ballerina.source.root"), "secret.txt");
        }

        Path userConfiguredPath = Paths.get(path);

        if (Files.notExists(userConfiguredPath)) {
            throw new RuntimeException("file not found: " + path);
        }

        return userConfiguredPath;
    }
}
