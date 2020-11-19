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
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ANON_ORG;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.CONFIG_FILE_NAME;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.DEFAULT_MODULE;
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

    public static void populateConfigMap(Map<Module, VariableKey[]> configurationData) throws TomlException {
        Map<VariableKey, Object> configurableMap = ConfigurableMapHolder.getConfigurationMap();
        if (configurationData.isEmpty()) {
            return;
        }
        Toml toml = getConfigurationData();
        if (toml.isEmpty()) {
            //No values provided at toml file
            return;
        }
        Toml orgToml = extractOrganizationTable(toml, configurationData.keySet());
        for (Map.Entry<Module, VariableKey[]> moduleEntry : configurationData.entrySet()) {
            String moduleName = moduleEntry.getKey().getName();
            Toml moduleToml = moduleName.equals(DEFAULT_MODULE) ? orgToml : extractModuleTable(orgToml, moduleName);
            for (VariableKey key : moduleEntry.getValue()) {
                if (!moduleToml.contains(key.variable)) {
                    //It is an optional configurable variable
                    break;
                }
                Object value = validateAndExtractValue(key, moduleToml);
                configurableMap.put(key, value);
            }
        }
    }

    private static Object validateAndExtractValue(VariableKey key, Toml moduleToml) throws TomlException {
        String variableName = key.variable;
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
            throw new TomlException(String.format(INVALID_VARIABLE_TYPE, variableName));
        }
        return value;
    }

    private static Toml extractModuleTable(Toml modules, String module) {
        Toml moduleToml = modules;
        if (!module.contains(DEFAULT_MODULE)) {
            moduleToml = modules.getTable(module);
        } else {
            String parent = module.substring(0, module.indexOf(DEFAULT_MODULE));
            String submodule = module.substring(module.indexOf(DEFAULT_MODULE) + 1);
            moduleToml = extractModuleTable(moduleToml.getTable(parent), submodule);
        }
        return moduleToml;
    }

    private static Toml extractOrganizationTable(Toml toml, Set<Module> moduleSet) throws TomlException {
        String orgName = moduleSet.iterator().next().getOrg();
        if (orgName.equals(ANON_ORG)) {
            return toml;
        }
        if (toml.entrySet().size() != 1) {
            throw new TomlException(INVALID_TOML_FILE + "Multiple organization names found.");
        }
        if (!toml.contains(orgName)) {
            throw new TomlException(INVALID_TOML_FILE + "Organization name '" + orgName + "' not found.");
        }
        return (Toml) toml.entrySet().iterator().next().getValue();
    }
}
