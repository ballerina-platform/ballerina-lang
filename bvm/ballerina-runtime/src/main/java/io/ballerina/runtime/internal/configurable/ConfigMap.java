/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.configurable;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.internal.configurable.providers.ConfigProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigProvider;
import io.ballerina.runtime.internal.util.RuntimeUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.CONFIG_FILE_NAME;

/**
 * Singleton class that holds runtime configurable values.
 *
 * @since 2.0.0
 */
public class ConfigMap {
    private static Map<VariableKey, Object> configurableMap = new HashMap<>();

    private ConfigMap(){}

    public static void initialize(Map<Module, VariableKey[]> configVarMap) {
        List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
        supportedConfigProviders.add(new TomlConfigProvider(getConfigPath()));
        ConfigResolver configResolver = new ConfigResolver(configVarMap, supportedConfigProviders);
        configurableMap = configResolver.resolveConfigs();
    }

    public static Object get(VariableKey key) {
        return configurableMap.get(key);
    }

    public static boolean containsKey(VariableKey key) {
        return configurableMap.containsKey(key);
    }

    private static Path getConfigPath() {
        Map<String, String> envVariables = System.getenv();
        return Paths.get(envVariables.getOrDefault(TomlConfigConstants.CONFIG_ENV_VARIABLE,
                                                   Paths.get(RuntimeUtils.USER_DIR, CONFIG_FILE_NAME).toString()));
    }
}
