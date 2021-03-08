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
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlProvider;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Singleton class that holds runtime configurable values.
 *
 * @since 2.0.0
 */
public class ConfigMap {
    private static Map<VariableKey, Object> configurableMap = new HashMap<>();

    private ConfigMap(){}

    public static void initialize(Path configFilePath, Map<Module, VariableKey[]> configVarMap) {
        try {
            List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
            supportedConfigProviders.add(new ConfigTomlProvider(configFilePath));
            ConfigResolver configResolver = new ConfigResolver(configVarMap, supportedConfigProviders);
            configurableMap = configResolver.resolveConfigs();
        } catch (ConfigException exception) {
            // TODO : Need to collect all the errors from each providers separately. Issue #29055
            throw new ErrorValue(StringUtils.fromString(exception.getMessage()));
        }
    }

    public static Object get(VariableKey key) {
        return configurableMap.get(key);
    }

    public static boolean containsKey(VariableKey key) {
        return configurableMap.containsKey(key);
    }

    static void put(VariableKey key, Object value) {
        configurableMap.put(key, value);
    }
}
