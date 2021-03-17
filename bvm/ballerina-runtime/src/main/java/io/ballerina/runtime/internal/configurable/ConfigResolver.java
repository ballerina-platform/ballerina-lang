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
import io.ballerina.runtime.internal.diagnostics.DiagnosticLog;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ORG_NAME_SEPARATOR;
import static io.ballerina.runtime.internal.configurable.ConfigConstants.CONFIGURATION_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.VALUE_NOT_PROVIDED;

/**
 * Class that resolve the configurations on given providers.
 *
 * @since 2.0.0
 */
public class ConfigResolver {

    private Map<Module, VariableKey[]> configVarMap;

    private ConfigProvider[] supportedConfigProviders;

    private List<ConfigProvider> runtimeConfigProviders;

    private Module rootModule;

    private DiagnosticLog diagnosticLog;

    public ConfigResolver(Module rootModule, Map<Module, VariableKey[]> configVarMap,
                          DiagnosticLog diagnosticLog, ConfigProvider... supportedConfigProviders) {
        this.rootModule = rootModule;
        this.configVarMap = configVarMap;
        this.supportedConfigProviders = supportedConfigProviders;
        this.runtimeConfigProviders = new LinkedList<>();
        this.diagnosticLog = diagnosticLog;
    }

    public Map<VariableKey, Object> resolveConfigs() {
        Map<VariableKey, Object> configValueMap = new HashMap<>();
        if (configVarMap.isEmpty()) {
            return configValueMap;
        }
        for (ConfigProvider provider : supportedConfigProviders) {
            try {
                provider.initialize();
                if (provider.hasConfigs()) {
                    runtimeConfigProviders.add(provider);
                }
            } catch (ConfigException e) {
                diagnosticLog.warn(e.getMessage());
            }
        }
        for (Map.Entry<Module, VariableKey[]> entry : configVarMap.entrySet()) {
            Module module = entry.getKey();
            VariableKey[] variableKeys = entry.getValue();
            for (VariableKey varKey : variableKeys) {
                Optional<?> configValue = getConfigValue(module, varKey);
                configValue.ifPresent(o -> configValueMap.put(varKey, o));
            }
        }
        return configValueMap;
    }

    private Optional<?> getConfigValue(Module module, VariableKey key) {
        Type type = key.type;
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                return getConfigValue(module, key, configProvider -> configProvider
                        .getAsIntAndMark(module, key));
            case TypeTags.BYTE_TAG:
                return getConfigValue(module, key, configProvider -> configProvider
                        .getAsByteAndMark(module, key));
            case TypeTags.BOOLEAN_TAG:
                return getConfigValue(module, key, configProvider -> configProvider
                        .getAsBooleanAndMark(module, key));
            case TypeTags.FLOAT_TAG:
                return getConfigValue(module, key, configProvider -> configProvider
                        .getAsFloatAndMark(module, key));
            case TypeTags.DECIMAL_TAG:
                return getConfigValue(module, key, configProvider -> configProvider
                        .getAsDecimalAndMark(module, key));
            case TypeTags.STRING_TAG:
                return getConfigValue(module, key, configProvider -> configProvider
                        .getAsStringAndMark(module, key));
            case TypeTags.RECORD_TYPE_TAG:
                return getConfigValue(module, key, configProvider -> configProvider
                        .getAsRecordAndMark(module, key));
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((IntersectionType) type).getEffectiveType();
                switch (effectiveType.getTag()) {
                    case TypeTags.ARRAY_TAG:
                        return getConfigValue(module, key, configProvider -> configProvider
                                .getAsArrayAndMark(module, key));
                    case TypeTags.RECORD_TYPE_TAG:
                        return getConfigValue(module, key, configProvider -> configProvider
                                .getAsRecordAndMark(module, key));
                    case TypeTags.TABLE_TAG:
                        return getConfigValue(module, key, configProvider -> configProvider
                                .getAsTableAndMark(module, key));
                    case TypeTags.XML_TAG:
                    case TypeTags.XML_ELEMENT_TAG:
                    case TypeTags.XML_COMMENT_TAG:
                    case TypeTags.XML_PI_TAG:
                    case TypeTags.XML_TEXT_TAG:
                        return getConfigValue(module, key, configProvider -> configProvider
                                .getAsXmlAndMark(module, key));
                    default:
                        throw new ErrorValue(StringUtils.fromString(
                                String.format(CONFIGURATION_NOT_SUPPORTED, key.module.getName() + ":" + key.variable,
                                              effectiveType.toString())));
                }
            default:
                diagnosticLog.error(String.format(CONFIGURATION_NOT_SUPPORTED,
                                                  key.module.getName() + ":" + key.variable, type.toString()));
        }
        return Optional.empty();
    }

    private Optional<?> getConfigValue(Module module, VariableKey key,
                                       Function<ConfigProvider, Optional<?>> getConfigFunc) {
        Optional<?> configValue = Optional.empty();
        List<ConfigException> exceptionList = new ArrayList<>(runtimeConfigProviders.size());
        for (ConfigProvider configProvider : runtimeConfigProviders) {
            try {
                Optional<?> value = getConfigFunc.apply(configProvider);
                if (configValue.isEmpty()) {
                    configValue = value;
                }
            } catch (ConfigException e) {
                exceptionList.add(e);
            }
        }

        // Handle errors while getting config values.
        if (configValue.isPresent()) {
            exceptionList.forEach(e -> diagnosticLog.warn(e.getMessage()));
            return configValue;
        }
        if (exceptionList.isEmpty() && key.isRequired) {
            String displayName;
            if (module.equals(rootModule)) {
                displayName = key.variable;
            } else if (module.getOrg().equals(rootModule.getOrg())) {
                displayName = module.getName() + ":" + key.variable;
            } else {
                displayName = module.getOrg() + ORG_NAME_SEPARATOR + module.getName() + ":" + key.variable;
            }
            diagnosticLog.error(String.format(VALUE_NOT_PROVIDED, displayName));
            return configValue;
        }
        exceptionList.forEach(e -> diagnosticLog.error(e.getMessage()));
        return configValue;
    }
}
