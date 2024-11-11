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
import io.ballerina.runtime.internal.configurable.ConfigMap;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.ConfigDetails;
import io.ballerina.runtime.internal.configurable.providers.cli.CliProvider;
import io.ballerina.runtime.internal.configurable.providers.env.EnvVarProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlContentProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlFileProvider;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.troubleshoot.StrandDump;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import sun.misc.Signal;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.api.constants.RuntimeConstants.DOT;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_DATA_ENV_VARIABLE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_FILES_ENV_VARIABLE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_FILE_NAME;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.DEFAULT_CONFIG_PATH;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.MODULES_ROOT;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.TEST_DIR_NAME;

/**
 * Util methods to be used during starting and ending a ballerina program.
 * 
 * @since 1.0
 */
public final class LaunchUtils {

    private static final PrintStream outStream = System.out;

    private LaunchUtils() {
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen. This will handle trap signals for strand dump.
     */
    public static void startTrapSignalHandler() {
        try {
            Signal.handle(new Signal("TRAP"), signal -> outStream.println(StrandDump.getStrandDump()));
        } catch (IllegalArgumentException ignored) {
            // In some Operating Systems like Windows, "TRAP" POSIX signal is not supported.
            // There getting the strand dump using kill signals is not expected, hence this exception is ignored.
        }
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen adding module configurable data.
     */
    public static void addModuleConfigData(Map<Module, VariableKey[]> configurationData, Module m,
                                           VariableKey[] variableKeys) {
        VariableKey[] currKeys = configurationData.get(m);
        VariableKey[] mergedKeyArray;
        if (currKeys == null) {
            mergedKeyArray = variableKeys;
        } else {
            mergedKeyArray = new VariableKey[currKeys.length + variableKeys.length];
            System.arraycopy(currKeys, 0, mergedKeyArray, 0, currKeys.length);
            System.arraycopy(variableKeys, 0, mergedKeyArray, currKeys.length, variableKeys.length);
        }
        configurationData.put(m, mergedKeyArray);
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen initialize configurable variables.
     */
    public static void initConfigurableVariables(Module rootModule, Map<Module, VariableKey[]> configurationData,
                                                 String[] args, Path[] configFilePaths, String configContent) {

        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        CliProvider cliConfigProvider = new CliProvider(rootModule, args);
        List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
        Set<Module> moduleSet = configurationData.keySet();
        if (configContent != null) {
            supportedConfigProviders.add(new TomlContentProvider(rootModule, configContent, moduleSet));
        }
        for (int i = configFilePaths.length - 1; i >= 0; i--) {
            supportedConfigProviders.add(new TomlFileProvider(rootModule, configFilePaths[i], moduleSet));
        }
        supportedConfigProviders.add(cliConfigProvider);
        Map<String, String> envVariables = System.getenv();
        if (!envVariables.isEmpty()) {
            EnvVarProvider envVarProvider = new EnvVarProvider(rootModule, envVariables);
            if (envVarProvider.hasConfigs()) {
                supportedConfigProviders.add(envVarProvider);
            }
        }
        ConfigResolver configResolver = new ConfigResolver(configurationData,
                                                           diagnosticLog, supportedConfigProviders);
        ConfigMap.setConfigurableMap(configResolver.resolveConfigs());
        if (!diagnosticLog.getDiagnosticList().isEmpty()) {
            RuntimeUtils.handleDiagnosticErrors(diagnosticLog);
        }
    }

    public static ConfigDetails getConfigurationDetails() {
        List<Path> paths = new ArrayList<>();
        Map<String, String> envVars = System.getenv();
        String configContent = populateConfigDetails(paths, envVars);
        return new ConfigDetails(paths.toArray(new Path[0]), configContent);
    }

    private static String populateConfigDetails(List<Path> paths, Map<String, String> envVars) {
        if (envVars.containsKey(CONFIG_FILES_ENV_VARIABLE)) {
            String[] configPathList = envVars.get(CONFIG_FILES_ENV_VARIABLE).split(File.pathSeparator);
            for (String pathString : configPathList) {
                paths.add(Path.of(pathString));
            }
        } else if (envVars.containsKey(CONFIG_DATA_ENV_VARIABLE)) {
            return envVars.get(CONFIG_DATA_ENV_VARIABLE);
        } else {
            if (Files.exists(DEFAULT_CONFIG_PATH)) {
                paths.add(DEFAULT_CONFIG_PATH);
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen to get configurable input paths.
     */
    public static ConfigDetails getTestConfigPaths(Module module, String pkgName, String sourceRoot) {
        String moduleName = module.getName();
        Path testConfigPath = Path.of(sourceRoot);
        if (!Files.exists(testConfigPath)) {
            testConfigPath = getSourceRootInContainer();
        }
        if (!moduleName.equals(pkgName)) {
            testConfigPath = testConfigPath.resolve(MODULES_ROOT)
                    .resolve(moduleName.substring(moduleName.indexOf(DOT) + 1));
        }
        testConfigPath = testConfigPath.resolve(TEST_DIR_NAME).resolve(CONFIG_FILE_NAME);
        if (!Files.exists(testConfigPath)) {
            return new ConfigDetails(new Path[]{}, null);
        } else {
            return new ConfigDetails(new Path[]{testConfigPath}, null);
        }
    }

    private static Path getSourceRootInContainer() {
        // Since we are inside a docker container, it's current working directory is the source root.
        return Path.of(RuntimeUtils.USER_DIR);
    }
}
