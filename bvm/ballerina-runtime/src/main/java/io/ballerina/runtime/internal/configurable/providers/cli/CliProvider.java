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
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.runtime.internal.configurable.providers.cli.CliConstants.CLI_ARG_REGEX;
import static io.ballerina.runtime.internal.configurable.providers.cli.CliConstants.CLI_PREFIX;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_CLI_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_INCOMPATIBLE_TYPE;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_INVALID_BYTE_RANGE;

/**
 * This class implements @{@link ConfigProvider} tp provide values for configurable variables through cli args.
 *
 * @since 2.0.0
 */
public class CliProvider implements ConfigProvider {

    String[] cliConfigArgs;

    private Map<String, String> cliVarKeyValueMap;

    private Module rootModule;

    public CliProvider(Module rootModule, String... cliConfigArgs) {
        this.rootModule = rootModule;
        this.cliConfigArgs = cliConfigArgs;
        this.cliVarKeyValueMap = new HashMap<>();
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
    public Optional<Long> getAsIntAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(TypeConverter.stringToInt(cliArg.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type, cliArg.value);
        }
    }

    @Override
    public Optional<Integer> getAsByteAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(TypeConverter.stringToByte(cliArg.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type,
                                      cliArg.value);
        } catch (BError e) {
            throw new ConfigException(CONFIG_INVALID_BYTE_RANGE, cliArg, key.variable, cliArg.value);
        }
    }

    @Override
    public Optional<Boolean> getAsBooleanAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(TypeConverter.stringToBoolean(cliArg.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type, cliArg.value);
        }
    }

    @Override
    public Optional<Double> getAsFloatAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(TypeConverter.stringToFloat(cliArg.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type, cliArg.value);
        }
    }

    @Override
    public Optional<BDecimal> getAsDecimalAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(TypeConverter.stringToDecimal(cliArg.value));
        } catch (NumberFormatException e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, key.type, cliArg.value);
        }
    }

    @Override
    public Optional<BString> getAsStringAndMark(Module module, VariableKey key) {
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        return Optional.of(StringUtils.fromString(cliArg.value));
    }

    @Override
    public Optional<BArray> getAsArrayAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_CLI_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<BMap<BString, Object>> getAsRecordAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_CLI_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<BTable<BString, Object>> getAsTableAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        throw new ConfigException(CONFIG_CLI_TYPE_NOT_SUPPORTED, key.variable, effectiveType);
    }

    @Override
    public Optional<BXml> getAsXmlAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        CliArg cliArg = getCliArg(module, key);
        if (cliArg.value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(TypeConverter.stringToXml(cliArg.value));
        } catch (BError e) {
            throw new ConfigException(CONFIG_INCOMPATIBLE_TYPE, cliArg, key.variable, effectiveType, cliArg.value);
        }
    }

    private CliArg getCliArg(Module module, VariableKey variableKey) {
        String key = variableKey.variable;
        String value;
        if (rootModule.getOrg().equals(module.getOrg())) {
            if (rootModule.getName().equals(module.getName())) {
                value = cliVarKeyValueMap.get(key);
                if (value != null) {
                    return new CliArg(key, value);
                }
            }
            key = module.getName() + "." + key;
            value = cliVarKeyValueMap.get(key);
            if (value != null) {
                return new CliArg(key, value);
            }
            key = module.getOrg() + "." + key;
        } else {
            key = module.getOrg() + "." + module.getName() + "." + key;
        }
        return new CliArg(key, cliVarKeyValueMap.get(key));
    }
}
