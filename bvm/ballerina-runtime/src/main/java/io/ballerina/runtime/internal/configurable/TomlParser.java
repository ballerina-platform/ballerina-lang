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
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.exceptions.TomlException;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlBooleanValueNode;
import io.ballerina.toml.semantic.ast.TomlDoubleValueNodeNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ANON_ORG;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.CONFIG_FILE_NAME;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.DEFAULT_MODULE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_TOML_FILE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_VARIABLE_TYPE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.SUBMODULE_DELIMITER;

/**
 * Toml parser for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlParser {

    static final Path CONFIG_FILE_PATH = Paths.get(RuntimeUtils.USER_DIR).resolve(CONFIG_FILE_NAME);

    private static TomlTableNode getConfigurationData() throws TomlException {
        if (!Files.exists(CONFIG_FILE_PATH)) {
            throw new TomlException("Configuration toml file `" + CONFIG_FILE_NAME + "` is not found");
        }
        ConfigToml configToml = new ConfigToml(CONFIG_FILE_PATH);
        return configToml.tomlAstNode();
    }

    public static void populateConfigMap(Map<Module, VariableKey[]> configurationData) throws TomlException {
        if (configurationData.isEmpty()) {
            return;
        }
        TomlTableNode tomlNode = getConfigurationData();
        if (tomlNode.entries().isEmpty()) {
            //No values provided at toml file
            return;
        }
        for (Map.Entry<Module, VariableKey[]> moduleEntry : configurationData.entrySet()) {
            String orgName = moduleEntry.getKey().getOrg();
            String moduleName = moduleEntry.getKey().getName();
            TomlTableNode orgNode = orgName.equals(ANON_ORG) ? tomlNode : extractOrganizationNode(tomlNode, orgName);
            TomlTableNode moduleNode = moduleName.equals(DEFAULT_MODULE) ? orgNode : extractModuleNode(orgNode,
                    moduleName);
            for (VariableKey key : moduleEntry.getValue()) {
                if (!moduleNode.entries().containsKey(key.variable)) {
                    //It is an optional configurable variable
                    break;
                }
                Object value = validateNodeAndExtractValue(key, moduleNode.entries());
                ConfigurableMap.put(key, value);
            }
        }
    }

    private static Object validateNodeAndExtractValue(VariableKey key, Map<String, TopLevelNode> valueMap) {
        String variableName = key.variable;
        TomlValueNode tomlValue = ((TomlKeyValueNode) valueMap.get(variableName)).value();
        Type type = key.type;
        Object value;
        try {
            switch (type.getTag()) {
                case TypeTags.INT_TAG:
                    checkTypeAndthrowError(TomlType.INTEGER,  tomlValue.kind(), variableName, type);
                    value = ((TomlLongValueNode) tomlValue).getValue();
                    break;
                case TypeTags.BOOLEAN_TAG:
                    checkTypeAndthrowError(TomlType.BOOLEAN,  tomlValue.kind(), variableName, type);
                    value = ((TomlBooleanValueNode) tomlValue).getValue();
                    break;
                case TypeTags.FLOAT_TAG:
                    checkTypeAndthrowError(TomlType.DOUBLE,  tomlValue.kind(), variableName, type);
                    value = ((TomlDoubleValueNodeNode) tomlValue).getValue();
                    break;
                case TypeTags.STRING_TAG:
                    checkTypeAndthrowError(TomlType.STRING,  tomlValue.kind(), variableName, type);
                    value = StringUtils.fromString(((TomlStringValueNode) tomlValue).getValue());
                    break;
                default:
                    throw new TomlException(String.format("Configurable feature is yet to be supported for type '%s'",
                            type.toString()));
            }
        } catch (ClassCastException e) {
            throw new TomlException(INVALID_TOML_FILE, e);
        }
        return value;
    }

    private static void checkTypeAndthrowError(TomlType tomlType, TomlType actualType, String variableName,
                                               Type expected) {
        if (actualType == tomlType) {
            return;
        }
        throw new TomlException(INVALID_TOML_FILE + String.format(INVALID_VARIABLE_TYPE, variableName,
                expected.toString(), actualType.name()));
    }

    private static TomlTableNode extractModuleNode(TomlTableNode orgNode, String moduleName) {
        TomlTableNode moduleNode = orgNode;
        int subModuleIndex = moduleName.indexOf(SUBMODULE_DELIMITER);
        if (subModuleIndex == -1) {
            moduleNode = (TomlTableNode) orgNode.entries().get(moduleName);
        } else if (subModuleIndex != moduleName.length()) {
            String parent = moduleName.substring(0, subModuleIndex);
            String submodule = moduleName.substring(subModuleIndex + 1);
            moduleNode = extractModuleNode((TomlTableNode) moduleNode.entries().get(parent), submodule);
        }
        return moduleNode;
    }

    private static TomlTableNode extractOrganizationNode(TomlTableNode tomlNode, String orgName) {
        if (!tomlNode.entries().containsKey(orgName)) {
            throw new TomlException(INVALID_TOML_FILE + "Organization name '" + orgName + "' not found.");
        }
        return (TomlTableNode) tomlNode.entries().get(orgName);
    }
}
