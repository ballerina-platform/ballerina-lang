/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.providers.ConfigProvider;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.CONFIGURATION_NOT_SUPPORTED;

/**
 * Class that resolve the configurations on given providers.
 *
 * @since 2.0.0
 */
public class ConfigResolver {

    private Map<Module, VariableKey[]> configVarMap;

    private List<ConfigProvider> supportedConfigProviders;

    public ConfigResolver(Map<Module, VariableKey[]> configVarMap,
                          List<ConfigProvider> supportedConfigProviders) {
        this.configVarMap = configVarMap;
        this.supportedConfigProviders = supportedConfigProviders;
    }

    public Map<VariableKey, Object>  resolveConfigs() {
        Map<VariableKey, Object> configValueMap = new HashMap<>();
        if (configVarMap.isEmpty()) {
            return configValueMap;
        }
        List<ConfigProvider> runtimeConfigProviders = new LinkedList<>();
        for (ConfigProvider provider : supportedConfigProviders) {
            provider.initialize(configVarMap);
            if (provider.hasConfigs()) {
                runtimeConfigProviders.add(provider);
            }
        }
        for (Map.Entry<Module, VariableKey[]> entry : configVarMap.entrySet()) {
            Module module = entry.getKey();
            VariableKey[] variableKeys = entry.getValue();
            for (VariableKey varKey : variableKeys) {
                Object configValue = getConfigValue(module, varKey, runtimeConfigProviders);
                configValueMap.put(varKey, configValue);
            }
        }
        return configValueMap;
    }

    private Object getConfigValue(Module module, VariableKey varKey, List<ConfigProvider> configProviders) {
        Type type = varKey.type;
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsIntAndMark(module, varKey));
            case TypeTags.BYTE_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsByteAndMark(module, varKey));
            case TypeTags.BOOLEAN_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsBooleanAndMark(module, varKey));
            case TypeTags.FLOAT_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsFloatAndMark(module, varKey));
            case TypeTags.DECIMAL_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsDecimalAndMark(module, varKey));
            case TypeTags.STRING_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsStringAndMark(module, varKey));
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((IntersectionType) type).getEffectiveType();
                switch (effectiveType.getTag()) {
                    case TypeTags.ARRAY_TAG:
                        return getConfigValue(configProviders, configProvider -> configProvider
                                .getAsArrayAndMark(module, varKey));
                    case TypeTags.RECORD_TYPE_TAG:
                        return getConfigValue(configProviders, configProvider -> configProvider
                                .getAsRecordAndMark(module, varKey));
                    case TypeTags.TABLE_TAG:
                        return getConfigValue(configProviders, configProvider -> configProvider
                                .getAsTableAndMark(module, varKey));
                }
            default:
                throw ErrorCreator.createError(StringUtils.fromString(String.format(CONFIGURATION_NOT_SUPPORTED,
                                                                                    varKey.variable,
                                                                                    type.toString())));
        }
    }

    private Object getConfigValue(List<ConfigProvider> configProviders,
                                  Function<ConfigProvider, ConfigValue> getConfigFunc) {
        for (ConfigProvider configProvider : configProviders) {
            ConfigValue configValue = getConfigFunc.apply(configProvider);
            if (configValue != null) {
                return configValue.value;
            }
        }
        return null;
    }
}
