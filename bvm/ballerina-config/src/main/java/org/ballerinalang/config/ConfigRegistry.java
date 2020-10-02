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

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConfigRegistry caches configuration properties and provide API.
 *
 * @since 0.95
 */
public class ConfigRegistry {

    private static final ConfigRegistry configRegistry = new ConfigRegistry();
    private static final Pattern encryptedFieldPattern = Pattern.compile("@encrypted:\\{(.*)\\}");
    private static final String ENV_VAR_FORMAT = "[a-zA-Z_]+[a-zA-Z0-9_]*";

    private Map<String, Object> configEntries = new ConcurrentHashMap<>();
    private AESCipherTool cipherTool;
    private PrintStream stderr = System.err;

    private boolean isInitialized;

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
     * @throws IOException Thrown if there was an error while attempting to process the config file
     */
    public void initRegistry(Map<String, String> runtimeParams, String configFilePath, Path ballerinaConfPath)
            throws IOException {
        BConfig resolvedConfigs = ConfigProcessor.processConfiguration(runtimeParams, configFilePath,
                                                                       ballerinaConfPath);
        configEntries = resolvedConfigs.getConfigurations();

        if (resolvedConfigs.hasEncryptedValues()) {
            String customSecretFilePath = runtimeParams != null ? runtimeParams.get("b7a.config.secret") : null;
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
        isInitialized = true;
    }

    /**
     * Add the specified key/value pair as a configuration entry.
     *
     * @param key   The configuration key
     * @param value The configuration value
     */
    public void addConfiguration(String key, Object value) {
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
    public void addConfiguration(String tableHeader, String tableField, Object value) {
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
     * Retrieves the config value specified by the key.
     *
     * @param key The key of the config entry
     * @return The associated config value if it exists
     */
    public Object getConfiguration(String key) {
        if (contains(key)) {
            Object value = configEntries.get(key);

            if (value instanceof String) {
                return resolveStringValue((String) value);
            }

            return value;
        }

        return null;
    }

    /**
     * Retrieves the config value specified by the section header and the field.
     *
     * @param sectionHeader The header name
     * @param field         The field in the section
     * @return The associated config value if it exists
     */
    public Object getConfiguration(String sectionHeader, String field) {
        return getConfiguration(getConfigKey(sectionHeader, field));
    }

    /**
     * Retrieves the config value specified by the key as a boolean.
     *
     * @param key The key of the config entry
     * @return The associated config value if it exists
     */
    public boolean getAsBoolean(String key) {
        if (contains(key)) {
            Object value;
            try {
                value = configEntries.get(key);
                if (value instanceof String) {
                    return Boolean.parseBoolean(resolveStringValue((String) value));
                }
                return (Boolean) value;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("config key '" + key + "' does not map to a valid 'boolean'");
            }
        }

        return Boolean.parseBoolean(lookupEnvVars(key));
    }

    /**
     * Retrieves the config value specified by the section header and the field.
     *
     * @param sectionHeader The header name
     * @param field         The field in the section
     * @return The associated config value if it exists
     */
    public boolean getAsBoolean(String sectionHeader, String field) {
        return getAsBoolean(getConfigKey(sectionHeader, field));
    }

    /**
     * Retrieves the config value specified by the key as an int.
     *
     * @param key The key of the config entry
     * @return The associated config val
     */
    public long getAsInt(String key) {
        if (contains(key)) {
            Object value;
            try {
                value = configEntries.get(key);
                if (value instanceof String) {
                    return Long.parseLong(resolveStringValue((String) value));
                }
                return (Long) value;
            } catch (ClassCastException | NumberFormatException e) {
                throw new IllegalArgumentException("config key '" + key + "' does not map to a valid 'int'");
            }
        }

        return Long.parseLong(lookupEnvVars(key));
    }

    /**
     * Retrieves the config value specified by the section header and the field.
     *
     * @param sectionHeader The header name
     * @param field The field in the section
     * @return The associated config value if it exists
     */
    public long getAsInt(String sectionHeader, String field) {
        return getAsInt(getConfigKey(sectionHeader, field));
    }

    /**
     * Retrieves the config value specified by the key as a float.
     *
     * @param key The key of the config entry
     * @return The associated config val
     */
    public double getAsFloat(String key) {
        if (contains(key)) {
            Object value;
            try {
                value = configEntries.get(key);
                if (value instanceof String) {
                    return Double.parseDouble(resolveStringValue((String) value));
                } else if (value instanceof Long) {
                    return (Long) value;
                }
                return (Double) value;
            } catch (ClassCastException | NumberFormatException e) {
                throw new IllegalArgumentException("config key '" + key + "' does not map to a valid 'float'");
            }
        }

        return Double.parseDouble(lookupEnvVars(key));
    }

    /**
     * Retrieves the config value specified by the section header and the field.
     *
     * @param sectionHeader The header name
     * @param field The field in the section
     * @return The associated config value if it exists
     */
    public double getAsFloat(String sectionHeader, String field) {
        return getAsFloat(getConfigKey(sectionHeader, field));
    }

    /**
     * Retrieves the config value specified by the key as a Map.
     *
     * @param key The key of the config entry
     * @return The associated config val
     */
    public Map<String, Object> getAsMap(String key) {
        Map<String, Object> section = new HashMap<>();
        int subStringIndex = key.length() + 1;

        // TODO: handle tables properly at the config parsing level
        configEntries.entrySet().forEach(entry -> {
            if (entry.getKey().startsWith(key)) {
                section.put(entry.getKey().substring(subStringIndex), entry.getValue());
            }
        });

        return section;
    }

    /**
     * Retrieves the config value specified by the section header and the field.
     *
     * @param sectionHeader The header name
     * @param field The field in the section
     * @return The associated config value if it exists
     */
    public Map<String, Object> getAsMap(String sectionHeader, String field) {
        return getAsMap(getConfigKey(sectionHeader, field));
    }

    /**
     * Retrieves the config value specified by the key as a List.
     *
     * @param key The key of the config entry
     * @return The associated config value
     */
    public List getAsArray(String key) {
        if (key == null) {
            return null;
        }
        Object value = configEntries.get(key);
        if (value instanceof List) {
            return (List) value;
        }
        return new ArrayList();
    }

    /**
     * Retrieves the config value specified by the section header and the field.
     *
     * @param sectionHeader The header name
     * @param field         The field in the section
     * @return The associated config value if it exists
     */
    public List getAsArray(String sectionHeader, String field) {
        return getAsArray(getConfigKey(sectionHeader, field));
    }

    /**
     * Retrieve the configuration value mapped by the specified key.
     *
     * @param key The key of the configuration value
     * @return The configuration value as a string
     */
    public String getAsString(String key) {
        if (key == null) {
            return null;
        }

        if (contains(key)) {
            String value = String.valueOf(configEntries.get(key));
            return resolveStringValue(value);
        }

        return lookupEnvVars(key);
    }

    /**
     * Retrieve the configuration value mapped by the specified table header and table field.
     *
     * @param tableHeader The name of the TOML table which contains the configuration
     * @param tableField  The config key under which the config value is mapped in the table
     * @return The configuration value as a string
     */
    public String getAsString(String tableHeader, String tableField) {
        return getAsString(getConfigKey(tableHeader, tableField));
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
        return ((value = getAsString(key)) != null) ? value : defaultValue;
    }

    /**
     * Returns an iterator for the key set of the config registry.
     *
     * @return An iterator for the key set
     */
    public Iterator<String> keySetIterator() {
        return configEntries.keySet().iterator();
    }

    /**
     * Removes the specified key from the Config Registry.
     *
     * @param key The key for the configuration value to be removed
     * @return The removed configuration value
     */
    public Object removeConfiguration(String key) {
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

    private String resolveStringValue(String value) {
        Matcher base64Matcher = null;

        try {
            if (value != null) {
                base64Matcher = encryptedFieldPattern.matcher(value);

                if (base64Matcher.find()) {
                    return cipherTool.decrypt(base64Matcher.group(1));
                }
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("invalid base 64 value: " + base64Matcher.group(1));
        } catch (AESCipherToolException e) {
            throw new RuntimeException("failed to retrieve encrypted value: " + e.getMessage(), e);
        }

        return value;
    }

    private String lookupEnvVars(String key) {
        String convertedKey = getEnvVarKey(key);
        return convertedKey.matches(ENV_VAR_FORMAT) ? System.getenv(convertedKey) : null;
    }

    private String getEnvVarKey(String configKey) {
        return configKey.replace('.', '_');
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }
}
