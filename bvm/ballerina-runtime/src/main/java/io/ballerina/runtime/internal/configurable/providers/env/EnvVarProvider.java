/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.runtime.internal.configurable.providers.env;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.configurable.providers.ConfigUtils;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BUnionType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_ENV_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_ENV_VARIABLE_AMBIGUITY;
import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_ENV_VARS_AMBIGUITY;
import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_ENV_VAR_NAME_AMBIGUITY;
import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_INCOMPATIBLE_TYPE;
import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_INVALID_BYTE_RANGE;
import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_UNUSED_ENV_VARS;

/**
 * This class implements @{@link ConfigProvider} to provide values for configurable variables through environment
 * variables.
 *
 * @since 2201.9.0
 */
public class EnvVarProvider implements ConfigProvider {

    private final Module rootModule;
    private final Map<String, String> envVariables;
    private final Map<String, VariableKey> visitedEnvVariableMap = new HashMap<>();
    private static final String ENV_VAR_PREFIX = "BAL_CONFIG_VAR_";

    public EnvVarProvider(Module rootModule, Map<String, String> envVariables) {
        this.rootModule = rootModule;
        this.envVariables = filterConfigEnvVariables(envVariables);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void complete(RuntimeDiagnosticLog diagnosticLog) {
        Set<String> varKeySet = envVariables.keySet();
        varKeySet.removeAll(visitedEnvVariableMap.keySet());
        if (varKeySet.isEmpty()) {
            return;
        }
        for (String key : varKeySet) {
            diagnosticLog.error(CONFIG_UNUSED_ENV_VARS, null, key);
        }
    }

    @Override
    public boolean hasConfigs() {
        return !this.envVariables.isEmpty();
    }

    @Override
    public Optional<ConfigValue> getAsIntAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        try {
            return getEnvConfigValue(TypeConverter.stringToInt(envVar.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, envVar, key.variable, key.type, envVar.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsByteAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        try {
            return getEnvConfigValue(TypeConverter.stringToByte(envVar.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, envVar, key.variable, key.type,
                    envVar.value);
        } catch (BError e) {
            throw new ConfigException(CONFIG_INVALID_BYTE_RANGE, envVar, key.variable, envVar.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsBooleanAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        try {
            return getEnvConfigValue(TypeConverter.stringToBoolean(envVar.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, envVar, key.variable, key.type, envVar.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsFloatAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        try {
            return getEnvConfigValue(TypeConverter.stringToFloat(envVar.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, envVar, key.variable, key.type, envVar.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsDecimalAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        try {
            return getEnvConfigValue(TypeConverter.stringToDecimal(envVar.value));
        } catch (NumberFormatException | BError e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, envVar, key.variable, key.type, envVar.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsStringAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        return getEnvConfigValue(StringUtils.fromString(envVar.value));
    }

    @Override
    public Optional<ConfigValue> getAsArrayAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_ENV_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<ConfigValue> getAsRecordAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_ENV_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<ConfigValue> getAsMapAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_ENV_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<ConfigValue> getAsTableAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_ENV_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<ConfigValue> getAsUnionAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        BUnionType unionType = (BUnionType) ((BIntersectionType) key.type).getEffectiveType();
        boolean isEnum = SymbolFlags.isFlagOn(unionType.getFlags(), SymbolFlags.ENUM);
        if (!isEnum && ConfigUtils.containsUnsupportedMembers(unionType)) {
            throw new ConfigException(CONFIG_ENV_TYPE_NOT_SUPPORTED, key.variable, unionType);
        }
        if (envVar.value == null) {
            return Optional.empty();
        }
        if (isEnum) {
            return getEnvConfigValue(ConfigUtils.getFiniteValue(key, unionType, envVar.value, envVar.toString()));
        }
        return getEnvConfigValue(ConfigUtils.getUnionValue(key, unionType, envVar.value, envVar.toString()));
    }

    @Override
    public Optional<ConfigValue> getAsFiniteAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        BFiniteType type;
        if (key.type.getTag() == TypeTags.INTERSECTION_TAG) {
            type = (BFiniteType) ((IntersectionType) key.type).getEffectiveType();
        } else {
            type = (BFiniteType) key.type;
        }
        Object value = ConfigUtils.getFiniteBalValue(envVar.value, type, key, envVar.toString());
        return getEnvConfigValue(value);
    }

    @Override
    public Optional<ConfigValue> getAsXmlAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        try {
            return getEnvConfigValue(TypeConverter.stringToXml(envVar.value));
        } catch (BError e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, envVar, key.variable, effectiveType, envVar.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsTupleAndMark(Module module, VariableKey key) {
        EnvVar envVar = getEnvVar(module, key);
        if (envVar.value == null) {
            return Optional.empty();
        }
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_ENV_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    private EnvVar getEnvVar(Module module, VariableKey variableKey) {
        String key = getRefinedEnvVarKey(module.getOrg() + "_" + module.getName() + "_" + variableKey.variable);
        String value = envVariables.get(key);
        if (value != null || !rootModule.getOrg().equals(module.getOrg())) {
            return markAndGetEnvVar(key, variableKey, value);
        }
        // Handle special case for root module and root org modules.
        String moduleKey;
        String rootOrgValue = null;
        String rootModuleValue;
        if (rootModule.getName().equals(module.getName())) {
            rootOrgValue = envVariables.get(getRefinedEnvVarKey(variableKey.variable));
        }
        moduleKey = getRefinedEnvVarKey(module.getName() + "_" + variableKey.variable);
        rootModuleValue = envVariables.get(moduleKey);

        // Handle Cli args ambiguities.
        return checkAmbiguitiesAndGetEnvVar(variableKey, key, moduleKey, rootOrgValue, rootModuleValue);
    }

    private static String getRefinedEnvVarKey(String key) {
        return ENV_VAR_PREFIX + key.replace(".", "_").toUpperCase(Locale.ROOT);
    }

    private EnvVar checkAmbiguitiesAndGetEnvVar(VariableKey variableKey, String key, String moduleKey,
                                                String rootOrgValue, String rootModuleValue) {
        if (rootOrgValue == null && rootModuleValue == null) {
            return markAndGetEnvVar(key, variableKey, null);
        }
        String refinedEnvVarKey = getRefinedEnvVarKey(variableKey.variable);
        if (rootOrgValue != null && rootModuleValue == null) {
            return markAndGetEnvVar(refinedEnvVarKey, variableKey, rootOrgValue);

        }
        if (rootOrgValue == null) {
            return markAndGetEnvVar(moduleKey, variableKey, rootModuleValue);
        }

        // This means multiple env variable values are matched for same variable, hence exception.
        StringBuilder errorString = new StringBuilder();
        errorString.append("[").append(refinedEnvVarKey).append("=").append(rootOrgValue);
        visitedEnvVariableMap.put(refinedEnvVarKey, variableKey);
        visitedEnvVariableMap.put(moduleKey, variableKey);
        errorString.append(", ").append(moduleKey).append("=").append(rootModuleValue);
        errorString.append("]");
        throw new ConfigException(CONFIG_ENV_VARS_AMBIGUITY, variableKey.variable, errorString);
    }

    private EnvVar markAndGetEnvVar(String key, VariableKey variableKey, String value) {
        // Handle env vars and module ambiguities
        VariableKey existingKey = visitedEnvVariableMap.get(key);
        if (existingKey != null) {
            Module currentModule = variableKey.module;
            Module existingModule = existingKey.module;
            String fullQualifiedKey1 =
                    getRefinedEnvVarKey(currentModule.getOrg() + "." + currentModule.getName() + "."
                                        + variableKey.variable);
            String fullQualifiedKey2 =
                    getRefinedEnvVarKey(existingModule.getOrg() + "." + existingModule.getName() + "."
                                        + variableKey.variable);
            if (fullQualifiedKey1.equalsIgnoreCase(fullQualifiedKey2)) {
                throw new ConfigException(CONFIG_ENV_VAR_NAME_AMBIGUITY, key, variableKey.toString(),
                        existingKey.toString());
            }
            throw new ConfigException(CONFIG_ENV_VARIABLE_AMBIGUITY, variableKey.toString(), existingKey.toString(),
                    fullQualifiedKey1 + "=" + "<value>");
        }
        visitedEnvVariableMap.put(key, variableKey);
        return new EnvVar(key, value);
    }

    public class EnvVar {

        public String key;
        public String value;

        public EnvVar(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    private Optional<ConfigValue> getEnvConfigValue(Object value) {
        return Optional.of(new EnvConfigValue(value));
    }


    private Map<String, String> filterConfigEnvVariables(Map<String, String> originalMap) {
        Map<String, String> filteredMap = new HashMap<>();
        for (Map.Entry<String, String> entry : originalMap.entrySet()) {
            if (entry.getKey().startsWith(ENV_VAR_PREFIX)) {
                filteredMap.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredMap;
    }
}
