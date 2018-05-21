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

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ballerinalang.bcl.parser.BConfig;
import org.ballerinalang.bcl.parser.BConfigLangListener;
import org.ballerinalang.toml.antlr4.TomlLexer;
import org.ballerinalang.toml.antlr4.TomlParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * ConfigProcessor processes runtime, environment and config file configurations.
 *
 * @since 0.95
 */
public class ConfigProcessor {

    private static final String ENV_VAR_FORMAT = "[a-zA-Z_]+[a-zA-Z0-9_]*";

    /**
     * Processes runtime, environment and config file properties.This populates configRegistry with configs based on
     * the following precedence order. 1. Ballerina runtime properties, 2. External config
     * (environment vars, etcd or something similar), 3. ballerina.conf file
     *
     * @param runtimeParams             The -B params passed to the BVM as CLI parameters
     * @param userProvidedConfigFile    The config file provided through the --config CLI parameter
     * @param ballerinaConfDefaultPath  The default config file (ballerina.conf) located at the source root
     * @return The parsed and resolved set of configurations
     * @throws IOException  Thrown if there was an error while attempting to process the config file
     */
    public static BConfig processConfiguration(Map<String, String> runtimeParams,
                                                           String userProvidedConfigFile,
                                                           Path ballerinaConfDefaultPath) throws IOException {
        String configFilePath = getConfigFile(userProvidedConfigFile, ballerinaConfDefaultPath);

        BConfig configEntries = new BConfig();
        if (configFilePath != null) {
            // Parse the config file
            configEntries = parseConfigFile(configFilePath);

            // If there are environment variables which map to any config keys, replace their values with the value
            // of the environment variable
            lookUpVariables(configEntries.getConfigurations());
        }

        if (runtimeParams != null && !runtimeParams.isEmpty()) {
            BConfig parsedRuntimeParams = parseRuntimeParams(runtimeParams);
            configEntries.addConfigurations(parsedRuntimeParams.getConfigurations());
            configEntries.setHasEncryptedValues(
                    configEntries.hasEncryptedValues() | parsedRuntimeParams.hasEncryptedValues());
        }
        return configEntries;
    }

    private static void lookUpVariables(Map<String, Object> configFileEntries) {
        configFileEntries.entrySet().forEach(entry -> {
            Object val = entry.getValue();

            if (val instanceof Map) {
                lookUpVariables((Map<String, Object>) val);
            } else {
                String envVarVal = getEnvVarValue(entry.getKey());

                if (envVarVal != null) {
                    if (val instanceof String) {
                        configFileEntries.put(entry.getKey(), envVarVal);
                    } else if (val instanceof Long) {
                        try {
                            configFileEntries.put(entry.getKey(), Long.parseLong(envVarVal));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "invalid int value received from environment: " + envVarVal);
                        }
                    } else if (val instanceof Double) {
                        try {
                            configFileEntries.put(entry.getKey(), Double.parseDouble(envVarVal));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "invalid float value received from environment: " + envVarVal);
                        }
                    } else if (val instanceof Boolean) {
                        configFileEntries.put(entry.getKey(), Boolean.parseBoolean(envVarVal));
                    }
                }
            }

        });
    }

    private static String getEnvVarValue(String key) {
        String envVarName = convertToEnvVarFormat(key);

        if (envVarName.matches(ENV_VAR_FORMAT)) { // Not all config keys are valid environment variable names
            return System.getenv(envVarName);
        }

        return null;
    }

    private static String convertToEnvVarFormat(String var) {
        return var.replace('.', '_');
    }

    private static String getConfigFile(String fileLocation, Path defaultLocation) {
        Path userProvidedPath = fileLocation != null ? Paths.get(fileLocation) : null;

        if (userProvidedPath != null) {
            if (!Files.exists(userProvidedPath)) {
                throw new RuntimeException("configuration file not found: " + fileLocation);
            }

            // If there is an explicitly specified config file, use it.
            return userProvidedPath.toString();
        }

        if (defaultLocation == null || !Files.exists(defaultLocation)) {
            // There isn't a default config file
            return null;
        }

        // A default config file was found. Returning its path.
        return defaultLocation.toString();
    }

    private static BConfig parseConfigFile(String path) throws IOException {
        ANTLRFileStream configFileStream = new ANTLRFileStream(path);
        BConfig configEntries = new BConfig();
        ParseTreeWalker treeWalker = new ParseTreeWalker();
        treeWalker.walk(new BConfigLangListener(configEntries), buildParseTree(configFileStream));
        return configEntries;
    }

    private static BConfig parseRuntimeParams(Map<String, String> runtimeParams) {
        StringBuilder stringBuilder = new StringBuilder();
        runtimeParams.forEach(
                (key, val) -> stringBuilder.append(key)
                                            .append('=')
                                            // TODO: need to handle this in a better way
                                            .append('\"').append(val).append('\"')
                                            .append('\n'));
        ANTLRInputStream runtimeConfigsStream = new ANTLRInputStream(stringBuilder.toString());
        BConfig runtimeConfigEntries = new BConfig();
        ParseTreeWalker treeWalker = new ParseTreeWalker();
        treeWalker.walk(new BConfigLangListener(runtimeConfigEntries), buildParseTree(runtimeConfigsStream));
        return runtimeConfigEntries;
    }

    private static ParseTree buildParseTree(CharStream configEntriesStream) {
        TomlLexer lexer = new TomlLexer(configEntriesStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        TomlParser parser = new TomlParser(tokenStream);
        return parser.toml();
    }
}
