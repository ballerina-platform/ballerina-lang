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

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TYPE_NOT_SUPPORTED;

/**
 * Class that resolve the configurations on given providers.
 *
 * @since 2.0.0
 */
public class ConfigResolver {

    private final Map<Module, VariableKey[]> configVarMap;

    private final List<ConfigProvider> supportedConfigProviders;

    private final List<ConfigProvider> runtimeConfigProviders;

    private final RuntimeDiagnosticLog diagnosticLog;

    public ConfigResolver(Map<Module, VariableKey[]> configVarMap, RuntimeDiagnosticLog diagnosticLog,
                          List<ConfigProvider> supportedConfigProviders) {
        this.configVarMap = configVarMap;
        this.supportedConfigProviders = supportedConfigProviders;
        this.runtimeConfigProviders = new LinkedList<>();
        this.diagnosticLog = diagnosticLog;
    }

    public Map<VariableKey, ConfigValue> resolveConfigs() {
        Map<VariableKey, ConfigValue> configValueMap = new HashMap<>();
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
                diagnosticLog.warn(e.getErrorCode(), null, e.getArgs());
            }
        }
        for (Map.Entry<Module, VariableKey[]> entry : configVarMap.entrySet()) {
            Module module = entry.getKey();
            VariableKey[] variableKeys = entry.getValue();
            for (VariableKey varKey : variableKeys) {
                Optional<?> configValue = getConfigValue(module, varKey);
                configValue.ifPresent(o -> configValueMap.put(varKey, (ConfigValue) o));
            }
        }
        for (ConfigProvider provider : runtimeConfigProviders) {
            provider.complete(diagnosticLog);
        }
        return configValueMap;
    }

    private Optional<?> getConfigValue(Module module, VariableKey key) {
        Function<ConfigProvider, Optional<?>> function = getValueFunction(module, key, key.type);
        if (function != null) {
            return getConfigValue(key, function);
        }
        return Optional.empty();
    }

    private Function<ConfigProvider, Optional<?>> getValueFunction(Module module, VariableKey key, Type type) {
        switch (type.getTag()) {
            case TypeTags.NULL_TAG:
                return null;
            case TypeTags.INT_TAG:
                return configProvider -> configProvider.getAsIntAndMark(module, key);
            case TypeTags.BYTE_TAG:
                return configProvider -> configProvider.getAsByteAndMark(module, key);
            case TypeTags.BOOLEAN_TAG:
                return configProvider -> configProvider.getAsBooleanAndMark(module, key);
            case TypeTags.FLOAT_TAG:
                return configProvider -> configProvider.getAsFloatAndMark(module, key);
            case TypeTags.DECIMAL_TAG:
                return configProvider -> configProvider.getAsDecimalAndMark(module, key);
            case TypeTags.STRING_TAG:
                return configProvider -> configProvider.getAsStringAndMark(module, key);
            case TypeTags.RECORD_TYPE_TAG:
                return configProvider -> configProvider.getAsRecordAndMark(module, key);
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                return configProvider -> configProvider.getAsXmlAndMark(module, key);
            case TypeTags.FINITE_TYPE_TAG:
                return configProvider -> configProvider.getAsFiniteAndMark(module, key);
            case TypeTags.ARRAY_TAG:
                return configProvider -> configProvider.getAsArrayAndMark(module, key);
            case TypeTags.MAP_TAG:
                return configProvider -> configProvider.getAsMapAndMark(module, key);
            case TypeTags.TABLE_TAG:
                return configProvider -> configProvider.getAsTableAndMark(module, key);
            case TypeTags.ANYDATA_TAG:
            case TypeTags.UNION_TAG:
            case TypeTags.JSON_TAG:
                return configProvider -> configProvider.getAsUnionAndMark(module, key);
            case TypeTags.TUPLE_TAG:
                return configProvider -> configProvider.getAsTupleAndMark(module, key);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return getValueFunction(module, key, ((ReferenceType) type).getReferredType());
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((IntersectionType) type).getEffectiveType();
                return getValueFunction(module, key, effectiveType);
            default:
                diagnosticLog.error(CONFIG_TYPE_NOT_SUPPORTED, key.location, key.variable,
                        Utils.decodeIdentifier(type.toString()));
        }
        return null;
    }

    private Optional<?> getConfigValue(VariableKey key, Function<ConfigProvider, Optional<?>> getConfigFunc) {
        Optional<?> configValue = Optional.empty();
        List<ConfigException> exceptionList = new ArrayList<>(runtimeConfigProviders.size());
        for (ConfigProvider configProvider : runtimeConfigProviders) {
            try {
                Optional<?> value = getConfigFunc.apply(configProvider);
                if (value.isPresent()) {
                    configValue = value;
                }
            } catch (ConfigException e) {
                exceptionList.add(e);
            }
        }

        // Handle errors while getting config values.
        if (configValue.isPresent()) {
            exceptionList.forEach(e -> diagnosticLog.warn(e.getErrorCode(), key.location, e.getArgs()));
            return configValue;
        }
        if (exceptionList.isEmpty() && key.isRequired) {
            diagnosticLog.error(RuntimeErrors.CONFIG_VALUE_NOT_PROVIDED, key.location, key.variable);
            return configValue;
        }
        exceptionList.forEach(e -> diagnosticLog.error(e.getErrorCode(), key.location, e.getArgs()));
        return configValue;
    }
}
