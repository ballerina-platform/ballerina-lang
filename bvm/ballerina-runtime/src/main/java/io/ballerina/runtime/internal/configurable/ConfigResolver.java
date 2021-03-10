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
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static io.ballerina.runtime.internal.configurable.ConfigConstants.CONFIGURATION_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.DEFAULT_MODULE;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.VALUE_NOT_PROVIDED;

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

    public Map<VariableKey, Object> resolveConfigs() {
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
            String moduleName = module.getName();
            VariableKey[] variableKeys = entry.getValue();
            for (VariableKey varKey : variableKeys) {
                Optional<?> configValue = getConfigValue(module, varKey, runtimeConfigProviders);
                if (varKey.isRequired && configValue.isEmpty()) {
                    String configVarName =
                            (moduleName.equals(DEFAULT_MODULE)) ? varKey.variable : moduleName + ":" + varKey.variable;
                    throw new ConfigException(String.format(VALUE_NOT_PROVIDED, configVarName));
                }
                configValue.ifPresent(o -> configValueMap.put(varKey, o));
            }
        }
        return configValueMap;
    }

    private Optional<?> getConfigValue(Module module, VariableKey key, List<ConfigProvider> configProviders) {
        Type type = key.type;
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsIntAndMark(module, key));
            case TypeTags.BYTE_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsByteAndMark(module, key));
            case TypeTags.BOOLEAN_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsBooleanAndMark(module, key));
            case TypeTags.FLOAT_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsFloatAndMark(module, key));
            case TypeTags.DECIMAL_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsDecimalAndMark(module, key));
            case TypeTags.STRING_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsStringAndMark(module, key));
            case TypeTags.RECORD_TYPE_TAG:
                return getConfigValue(configProviders, configProvider -> configProvider
                        .getAsRecordAndMark(module, key));
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((IntersectionType) type).getEffectiveType();
                switch (effectiveType.getTag()) {
                    case TypeTags.ARRAY_TAG:
                        return getConfigValue(configProviders, configProvider -> configProvider
                                .getAsArrayAndMark(module, key));
                    case TypeTags.RECORD_TYPE_TAG:
                        return getConfigValue(configProviders, configProvider -> configProvider
                                .getAsRecordAndMark(module, key));
                    case TypeTags.TABLE_TAG:
                        return getConfigValue(configProviders, configProvider -> configProvider
                                .getAsTableAndMark(module, key));
                    default:
                        throw new ErrorValue(StringUtils.fromString(
                                String.format(CONFIGURATION_NOT_SUPPORTED, key.module.getName() + ":" + key.variable,
                                              effectiveType.toString())));
                }
            default:
                throw new ErrorValue(StringUtils.fromString(
                        String.format(CONFIGURATION_NOT_SUPPORTED, key.module.getName() + ":" + key.variable,
                                      type.toString())));
        }
    }

    private Optional<?> getConfigValue(List<ConfigProvider> configProviders,
                                          Function<ConfigProvider, Optional<?>> getConfigFunc) {
        Optional<?> configValue = Optional.empty();
        for (ConfigProvider configProvider : configProviders) {
            Optional<?> value = getConfigFunc.apply(configProvider);
            if (value.isPresent()) {
                 configValue = value;
            }
        }
        return configValue;
    }
}
