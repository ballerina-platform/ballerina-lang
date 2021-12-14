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

package io.ballerina.runtime.internal.configurable.providers.cli;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BUnionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.internal.configurable.providers.cli.CliConstants.CLI_ARG_REGEX;
import static io.ballerina.runtime.internal.configurable.providers.cli.CliConstants.CLI_PREFIX;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_CLI_ARGS_AMBIGUITY;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_CLI_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_CLI_UNUSED_CLI_ARGS;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_CLI_VARIABLE_AMBIGUITY;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_INCOMPATIBLE_TYPE;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_INVALID_BYTE_RANGE;

/**
 * This class implements @{@link ConfigProvider} tp provide values for configurable variables through cli args.
 *
 * @since 2.0.0
 */
public class CliProvider implements ConfigProvider {

    private String[] cliConfigArgs;

    private final Map<String, String> cliVarKeyValueMap;

    private final Map<String, VariableKey> markedCliKeyVariableMap;

    private final Module rootModule;

    public CliProvider(Module rootModule, String... cliConfigArgs) {
        this.rootModule = rootModule;
        this.cliConfigArgs = cliConfigArgs;
        this.cliVarKeyValueMap = new HashMap<>();
        this.markedCliKeyVariableMap = new HashMap<>();
    }

    @Override
    public void initialize() {
        for (String cliConfigArg : cliConfigArgs) {
            if (cliConfigArg.startsWith(CLI_PREFIX)) {
                String configKeyValue = cliConfigArg.substring(2);
                String[] keyValuePair = configKeyValue.split(CLI_ARG_REGEX, 2);
                if (keyValuePair.length == 2) {
                    String key = keyValuePair[0].trim();
                    if (key.endsWith("\\")) {
                        key = key + " ";
                    }
                    cliVarKeyValueMap.put(key, keyValuePair[1]);
                }
            }
        }
    }

    @Override
    public boolean hasConfigs() {
        return !cliVarKeyValueMap.isEmpty();
    }

    @Override
    public Optional<ConfigValue> getAsIntAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return getCliConfigValue(TypeConverter.stringToInt(cliArg.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type, cliArg.value);
        }
    }

    private Optional<ConfigValue> getCliConfigValue(Object value) {
        return Optional.of(new CliConfigValue(value));
    }

    @Override
    public Optional<ConfigValue> getAsByteAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return getCliConfigValue(TypeConverter.stringToByte(cliArg.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type,
                                      cliArg.value);
        } catch (BError e) {
            throw new ConfigException(CONFIG_INVALID_BYTE_RANGE, cliArg, key.variable, cliArg.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsBooleanAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return getCliConfigValue(TypeConverter.stringToBoolean(cliArg.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type, cliArg.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsFloatAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return getCliConfigValue(TypeConverter.stringToFloat(cliArg.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type, cliArg.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsDecimalAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return getCliConfigValue(TypeConverter.stringToDecimal(cliArg.value));
        } catch (NumberFormatException | BError e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type, cliArg.value);
        }
    }

    @Override
    public Optional<ConfigValue> getAsStringAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        return getCliConfigValue(StringUtils.fromString(cliArg.value));
    }

    @Override
    public Optional<ConfigValue> getAsArrayAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_CLI_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<ConfigValue> getAsRecordAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_CLI_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<ConfigValue> getAsMapAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_CLI_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<ConfigValue> getAsTableAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_CLI_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<ConfigValue> getAsUnionAndMark(Module module, VariableKey key) {
        BUnionType unionType = (BUnionType) ((BIntersectionType) key.type).getEffectiveType();
        if (!SymbolFlags.isFlagOn(unionType.getFlags(), SymbolFlags.ENUM)) {
            throw new ConfigException(CONFIG_CLI_TYPE_NOT_SUPPORTED, key.variable, unionType);
        }
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        BString stringVal = StringUtils.fromString(cliArg.value);
        List<Type> memberTypes = unionType.getMemberTypes();
        for (Type type : memberTypes) {
            if (((BFiniteType) type).valueSpace.contains(stringVal)) {
                return getCliConfigValue(stringVal);
            }
        }
        throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable,
                                  Utils.decodeIdentifier(unionType.toString()), cliArg.value);
    }

    @Override
    public Optional<ConfigValue> getAsXmlAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return getCliConfigValue(TypeConverter.stringToXml(cliArg.value));
        } catch (BError e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, effectiveType, cliArg.value);
        }
    }

    @Override
    public void complete(RuntimeDiagnosticLog diagnosticLog) {
        Set<String> varKeySet = cliVarKeyValueMap.keySet();
        varKeySet.removeAll(markedCliKeyVariableMap.keySet());
        if (varKeySet.isEmpty()) {
            return;
        }
        for (String key : varKeySet) {
            diagnosticLog.error(CONFIG_CLI_UNUSED_CLI_ARGS, null, key + "=" + cliVarKeyValueMap.get(key));
        }
    }

    private CliArg getCliArg(Module module, VariableKey variableKey) {

        String key = module.getOrg() + "." + module.getName() + "." + variableKey.variable;
        String value = cliVarKeyValueMap.get(key);
        if (value != null || !rootModule.getOrg().equals(module.getOrg())) {
            return markAndGetCliArg(key, variableKey, value);
        }
        // Handle special case for root module and root org modules.
        String moduleKey;
        String rootOrgValue = null;
        String rootModuleValue;
        if (rootModule.getName().equals(module.getName())) {
            rootOrgValue = cliVarKeyValueMap.get(variableKey.variable);
        }
        moduleKey = module.getName() + "." + variableKey.variable;
        rootModuleValue = cliVarKeyValueMap.get(moduleKey);

        // Handle Cli args ambiguities.
        return checkAmbiguitiesAndGetCliArg(variableKey, key, moduleKey, rootOrgValue, rootModuleValue);
    }

    private CliArg checkAmbiguitiesAndGetCliArg(VariableKey variableKey, String key, String moduleKey,
                                                String rootOrgValue, String rootModuleValue) {
        if (rootOrgValue == null && rootModuleValue == null) {
            return markAndGetCliArg(key, variableKey, null);
        }
        if (rootOrgValue != null && rootModuleValue == null) {
           return markAndGetCliArg(variableKey.variable, variableKey, rootOrgValue);

        }
        if (rootOrgValue == null) {
            return markAndGetCliArg(moduleKey, variableKey, rootModuleValue);
        }

        // This means multiple command line values are matched for same variable, hence exception.
        StringBuilder errorString = new StringBuilder();
        errorString.append("[").append(variableKey.variable).append("=").append(rootOrgValue);
        markedCliKeyVariableMap.put(variableKey.variable, variableKey);
        markedCliKeyVariableMap.put(moduleKey, variableKey);
        errorString.append(", ").append(moduleKey).append("=").append(rootModuleValue);
        errorString.append("]");
        throw new ConfigException(CONFIG_CLI_ARGS_AMBIGUITY, variableKey.variable, errorString);

    }

    private CliArg markAndGetCliArg(String key, VariableKey variableKey, String value) {
        // Handle cli args and module ambiguities
        VariableKey existingKey = markedCliKeyVariableMap.get(key);
        if (existingKey != null) {
            Module module = variableKey.module;
            String fullQualifiedKey = module.getOrg() + "." + module.getName() + "." + variableKey.variable;
            throw new ConfigException(CONFIG_CLI_VARIABLE_AMBIGUITY, variableKey.toString(), existingKey.toString(),
                                      "-C" + fullQualifiedKey + "=" + "<value>");
        }
        markedCliKeyVariableMap.put(key, variableKey);
        return new CliArg(key, value);
    }
}
