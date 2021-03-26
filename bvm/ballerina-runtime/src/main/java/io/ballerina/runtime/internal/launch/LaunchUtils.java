/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal.launch;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.launch.LaunchListener;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.ConfigMap;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlContentProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlDetails;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlFileProvider;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import io.ballerina.runtime.internal.values.ErrorValue;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.logging.BLogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.LogManager;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_ARGS_INIT_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_ARGS_INIT_PREFIX_LENGTH;
import static io.ballerina.runtime.api.constants.RuntimeConstants.CONFIG_FILE_PROPERTY;
import static io.ballerina.runtime.api.constants.RuntimeConstants.CONFIG_SEPARATOR;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UTIL_LOGGING_CONFIG_CLASS_PROPERTY;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UTIL_LOGGING_CONFIG_CLASS_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UTIL_LOGGING_MANAGER_CLASS_PROPERTY;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UTIL_LOGGING_MANAGER_CLASS_VALUE;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.CONFIG_DATA_ENV_VARIABLE;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.CONFIG_FILES_ENV_VARIABLE;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.CONFIG_FILE_NOT_FOUND;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.CONFIG_SECRET_FILE_NOT_FOUND;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.DEFAULT_CONFIG_PATH;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.DEFAULT_SECRET_PATH;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.EMPTY_CONFIG_STRING;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.SECRET_DATA_ENV_VARIABLE;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.SECRET_FILE_ENV_VARIABLE;

/**
 * Util methods to be used during starting and ending a ballerina program.
 *
 * @since 1.0
 */
public class LaunchUtils {

    private LaunchUtils() {
    }

    static {
        System.setProperty(UTIL_LOGGING_CONFIG_CLASS_PROPERTY, UTIL_LOGGING_CONFIG_CLASS_VALUE);
        System.setProperty(UTIL_LOGGING_MANAGER_CLASS_PROPERTY, UTIL_LOGGING_MANAGER_CLASS_VALUE);
    }


    public static String[] initConfigurations(String[] args) {

        if (ConfigRegistry.getInstance().isInitialized()) {
            return args;
        }
        Map<String, String> configArgs = new HashMap<>();
        String[] userProgramArgs = getUserArgs(args, configArgs);

        // load configurations
        loadConfigurations(configArgs, configArgs.get(CONFIG_FILE_PROPERTY));
        return userProgramArgs;
    }

    public static String[] getUserArgs(String[] args, Map<String, String> configArgs) {
        List<String> userProgramArgs = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(BALLERINA_ARGS_INIT_PREFIX)) {
                userProgramArgs.addAll(Arrays.asList(Arrays.copyOfRange(args, i + 1, args.length)));
                break;
            }
            if (args[i].toLowerCase().startsWith(BALLERINA_ARGS_INIT_PREFIX)) {
                String configString = args[i].substring(BALLERINA_ARGS_INIT_PREFIX_LENGTH);
                String[] keyValuePair = configString.split(CONFIG_SEPARATOR);
                if (keyValuePair.length >= 2) {
                    configArgs.put(keyValuePair[0], configString.substring(keyValuePair[0].length() + 1));
                    continue;
                }
            }
            userProgramArgs.add(args[i]);
        }
        return userProgramArgs.toArray(new String[0]);
    }

    public static void startListeners(boolean isService) {
        ServiceLoader<LaunchListener> listeners = ServiceLoader.load(LaunchListener.class);
        listeners.forEach(listener -> listener.beforeRunProgram(isService));
    }

    public static void stopListeners(boolean isService) {
        ServiceLoader<LaunchListener> listeners = ServiceLoader.load(LaunchListener.class);
        listeners.forEach(listener -> listener.afterRunProgram(isService));
    }

    /**
     * Initializes the {@link ConfigRegistry} and loads {@link LogManager} configs.
     */
    private static void loadConfigurations(Map<String, String> configArgs, String configFilePath) {
        Path ballerinaConfPath = Paths.get(System.getProperty("user.dir")).resolve("ballerina.conf");
        try {
            ConfigRegistry configRegistry = ConfigRegistry.getInstance();
            configRegistry.initRegistry(configArgs, configFilePath, ballerinaConfPath);
            LogManager logManager = LogManager.getLogManager();
            if (logManager instanceof BLogManager) {
                ((BLogManager) logManager).loadUserProvidedLogConfiguration();
            }
        } catch (IOException e) {
            RuntimeUtils.handleUsageError("failed to read the specified configuration file: " +
                                                 configFilePath);
        } catch (RuntimeException e) {
            RuntimeUtils.handleUsageError(e.getMessage());
        }
    }

    public static void initConfigurableVariables(Path[] configFilePaths, String secretContent, String configContent,
                                                 Map<Module, VariableKey[]> configurationData) {
        try {
            List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
            if (configContent != null) {
                supportedConfigProviders.add(new TomlContentProvider(configContent));
            }
            for (int i = configFilePaths.length - 1; i >= 0; i--) {
                supportedConfigProviders.add(new TomlFileProvider(configFilePaths[i]));
            }
            if (secretContent != null) {
                supportedConfigProviders.add(new TomlContentProvider(secretContent));
            }
            ConfigResolver configResolver = new ConfigResolver(configurationData, supportedConfigProviders);
            ConfigMap.setConfigurableMap(configResolver.resolveConfigs());
        } catch (ConfigException exception) {
            // TODO : Need to collect all the errors from each providers separately. Issue #29055
            throw new ErrorValue(StringUtils.fromString(exception.getMessage()));
        }
    }

    public static TomlDetails getConfigurationDetails() {
        List<Path> paths = new ArrayList<>();
        Map<String, String> envVars = System.getenv();
        String secret = populateSecretConfigDetails(paths, envVars);
        String configContent = populateConfigDetails(paths, envVars);
        return new TomlDetails(paths.toArray(new Path[0]), secret, configContent);
    }

    private static String populateConfigDetails(List<Path> paths, Map<String, String> envVars) {
        String configContent = null;
        if (envVars.containsKey(CONFIG_FILES_ENV_VARIABLE)) {
            String[] configPathList = envVars.get(CONFIG_FILES_ENV_VARIABLE).split(File.pathSeparator);
            for (String pathString: configPathList) {
                Path path = Paths.get(pathString);
                if (!Files.exists(path)) {
                    throw new ErrorValue(StringUtils.fromString(String.format(CONFIG_FILE_NOT_FOUND, path)));
                }
                paths.add(path);
            }
        } else if (envVars.containsKey(CONFIG_DATA_ENV_VARIABLE)) {
            String configString = envVars.get(CONFIG_DATA_ENV_VARIABLE);
            if (configString.isEmpty()) {
                throw new ErrorValue(
                        StringUtils.fromString(String.format(EMPTY_CONFIG_STRING, CONFIG_DATA_ENV_VARIABLE)));
            }
            configContent = configString;
        } else {
            if (Files.exists(DEFAULT_CONFIG_PATH)) {
                paths.add(DEFAULT_CONFIG_PATH);
            }
        }
        return configContent;
    }

    private static String populateSecretConfigDetails(List<Path> paths, Map<String, String> envVars) {
        String secret = null;
        if (envVars.containsKey(SECRET_FILE_ENV_VARIABLE)) {
            Path secretPath = Paths.get(envVars.get(SECRET_FILE_ENV_VARIABLE));
            if (!Files.exists(secretPath)) {
                throw new ErrorValue(StringUtils.fromString(String.format(CONFIG_SECRET_FILE_NOT_FOUND, secretPath)));
            }
            paths.add(secretPath);
        } else if (envVars.containsKey(SECRET_DATA_ENV_VARIABLE)) {
            String secretString = envVars.get(SECRET_DATA_ENV_VARIABLE);
            if (secretString.isEmpty()) {
                throw new ErrorValue(
                        StringUtils.fromString(String.format(EMPTY_CONFIG_STRING, SECRET_DATA_ENV_VARIABLE)));
            }
            secret = secretString;
        } else {
            if (Files.exists(DEFAULT_SECRET_PATH)) {
                paths.add(DEFAULT_SECRET_PATH);
            }
        }
        return secret;
    }
}
