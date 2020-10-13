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

package io.ballerina.runtime.launch;

import io.ballerina.runtime.util.RuntimeUtils;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.logging.BLogManager;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.LogManager;

import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_METRICS_ENABLED;
import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_OBSERVABILITY_ENABLED;
import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static io.ballerina.runtime.util.BLangConstants.BALLERINA_ARGS_INIT_PREFIX;
import static io.ballerina.runtime.util.BLangConstants.BALLERINA_ARGS_INIT_PREFIX_LENGTH;
import static io.ballerina.runtime.util.BLangConstants.CONFIG_FILE_PROPERTY;
import static io.ballerina.runtime.util.BLangConstants.CONFIG_SEPARATOR;
import static io.ballerina.runtime.util.BLangConstants.UTIL_LOGGING_CONFIG_CLASS_PROPERTY;
import static io.ballerina.runtime.util.BLangConstants.UTIL_LOGGING_CONFIG_CLASS_VALUE;
import static io.ballerina.runtime.util.BLangConstants.UTIL_LOGGING_MANAGER_CLASS_PROPERTY;
import static io.ballerina.runtime.util.BLangConstants.UTIL_LOGGING_MANAGER_CLASS_VALUE;

/**
 * Util methods to be used during starting and ending a ballerina program.
 * 
 * @since 1.0
 */
public class LaunchUtils {

    static {
        System.setProperty(UTIL_LOGGING_CONFIG_CLASS_PROPERTY, UTIL_LOGGING_CONFIG_CLASS_VALUE);
        System.setProperty(UTIL_LOGGING_MANAGER_CLASS_PROPERTY, UTIL_LOGGING_MANAGER_CLASS_VALUE);
    }

    private static PrintStream errStream = System.err;

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
            ConfigRegistry.getInstance().initRegistry(configArgs, configFilePath, ballerinaConfPath);
            LogManager logManager = LogManager.getLogManager();
            if (logManager instanceof BLogManager) {
                ((BLogManager) logManager).loadUserProvidedLogConfiguration();
            }

            boolean observeFlag = ConfigRegistry.getInstance().getAsBoolean(CONFIG_OBSERVABILITY_ENABLED);
            if (observeFlag) {
                ConfigRegistry.getInstance().addConfiguration(CONFIG_METRICS_ENABLED, Boolean.TRUE);
                ConfigRegistry.getInstance().addConfiguration(CONFIG_TRACING_ENABLED, Boolean.TRUE);
            }

        } catch (IOException e) {
            RuntimeUtils.handleUsageError("failed to read the specified configuration file: " +
                                                 configFilePath);
        } catch (RuntimeException e) {
            RuntimeUtils.handleUsageError(e.getMessage());
        }
    }
}
