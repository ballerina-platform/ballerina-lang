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

import com.moandjiezana.toml.Toml;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.internal.configurable.exceptions.TomlException;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import io.ballerina.runtime.internal.values.BmpStringValue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.CONFIG_FILE_NAME;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_TOML_FILE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_VARIABLE_TYPE;

/**
 * Toml parser for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlParser {

    static final Path CONFIG_FILE_PATH = Paths.get(RuntimeUtils.USER_DIR).resolve(CONFIG_FILE_NAME);

    private static Toml getConfigurationData() throws TomlException {
        if (!Files.exists(CONFIG_FILE_PATH)) {
            throw new TomlException("Configuration toml file `" + CONFIG_FILE_NAME + "` is not found");
        }
        return new Toml().read(CONFIG_FILE_PATH.toFile());
    }

    public static void populateConfigMap(String module, VariableKey[] configurationData) throws TomlException {
        Map<VariableKey, Object> configurableMap = ConfigurableMapHolder.getConfigurationMap();
        if (isConfigurationDataEmpty(configurationData)) {
            return;
        }
        Toml toml = getConfigurationData();
        if (toml != null && !toml.isEmpty()) {
            String orgName = configurationData[0].module.getOrg();
            validateOrganizationName(toml, orgName);
            for (Map.Entry<String, Object> organizaion : toml.entrySet()) {
                Toml moduleToml = extractModuleTable((Toml) organizaion.getValue(), module);
                for (VariableKey key : configurationData) {
                    Object value = validateAndExtractValue(key, moduleToml);
                    if (value == null) {
                        //This indicates the value not provided at toml file
                        break;
                    }
                    configurableMap.put(key, value);
                }
            }
        }
    }

    private static Object validateAndExtractValue(VariableKey key, Toml moduleToml) throws TomlException {
        String variableName = key.variable;
        if (!moduleToml.contains(variableName)) {
            return null;
        }
        Object value;
        try {
            switch (key.type.getTag()) {
                case TypeTags.INT_TAG:
                    value = moduleToml.getLong(variableName);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    value = moduleToml.getBoolean(variableName);
                    break;
                case TypeTags.FLOAT_TAG:
                    value = moduleToml.getDouble(variableName);
                    break;
                case TypeTags.STRING_TAG:
                    value = new BmpStringValue(moduleToml.getString(variableName));
                    break;
                default:
                    throw new TomlException(String.format("Configurable feature is yet to be supported for type '%s'",
                            key.type.toString()));
            }
        } catch (ClassCastException e) {
            throw new TomlException(String.format(INVALID_VARIABLE_TYPE, key.variable));
        }
        return value;
    }

    private static Toml extractModuleTable(Toml modules, String module) {

        Toml moduleToml = modules;
        if (!module.contains(".")) {
            moduleToml = modules.getTable(module);
        } else {
            String parent = module.substring(0, module.indexOf('.'));
            String submodule = module.substring(module.indexOf('.') + 1);
            moduleToml = extractModuleTable(moduleToml.getTable(parent), submodule);
        }
        return moduleToml;
    }

    private static void validateVariableType(VariableKey tomlKey, VariableKey[] configurationData)
            throws TomlException {

        List<VariableKey> dataList = Arrays.asList(configurationData);
        boolean moduleFound = false;
        for (VariableKey variableKey : configurationData) {
            Module confModule = variableKey.module;
            moduleFound = confModule.equals(tomlKey.module);
        }

        if (!moduleFound) {
            throw new TomlException(INVALID_TOML_FILE + "Module name not found.");

        }

    }

    private static void validateOrganizationName(Toml toml, String orgName) throws TomlException {
        if (toml.entrySet().size() != 1) {
            throw new TomlException(INVALID_TOML_FILE + "Multiple organization names found.");
        }
        if (!toml.contains(orgName)) {
            throw new TomlException(INVALID_TOML_FILE + "Organization name '" + orgName + "' not found.");
        }
    }

    private static boolean isConfigurationDataEmpty(VariableKey[] configurationData) {
        boolean empty = true;
        for (VariableKey variableKey : configurationData) {
            if (variableKey != null) {
                empty = false;
                break;
            }
        }
        return empty;
    }
}
