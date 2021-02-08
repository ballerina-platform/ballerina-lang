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
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.exceptions.TomlException;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBasicValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.CONFIGURATION_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.DEFAULT_MODULE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_TOML_FILE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_VARIABLE_TYPE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.SUBMODULE_DELIMITER;

/**
 * Toml parser for configurable implementation.
 *
 * @since 2.0.0
 */
public class ConfigTomlParser {

    private ConfigTomlParser() {
    }

    private static TomlTableNode getConfigurationData(Path configFilePath) throws TomlException {
        if (!Files.exists(configFilePath)) {
            return null;
        }
        ConfigToml configToml = new ConfigToml(configFilePath);
        return configToml.tomlAstNode();
    }

    public static void populateConfigMap(Path filePath, Map<Module, VariableKey[]> configurationData)
            throws TomlException {
        if (configurationData.isEmpty()) {
            return;
        }
        TomlTableNode tomlNode = getConfigurationData(filePath);
        if (tomlNode == null || tomlNode.entries().isEmpty()) {
            //No values provided at toml file
            return;
        }
        for (Map.Entry<Module, VariableKey[]> moduleEntry : configurationData.entrySet()) {
            TomlTableNode moduleNode = retrieveModuleNode(tomlNode, moduleEntry.getKey());
            if (moduleNode == null) {
                //Module could contain optional configurable variable
                continue;
            }
            for (VariableKey key : moduleEntry.getValue()) {
                if (!moduleNode.entries().containsKey(key.variable)) {
                    //It is an optional configurable variable
                    continue;
                }
                Object value = validateNodeAndExtractValue(key, moduleNode.entries());
                ConfigurableMap.put(key, value);
            }
        }
    }

    private static TomlTableNode retrieveModuleNode(TomlTableNode tomlNode, Module module) {
        String orgName = module.getOrg();
        String moduleName = module.getName();
        if (tomlNode.entries().containsKey(orgName)) {
            tomlNode =  (TomlTableNode) tomlNode.entries().get(orgName);
        }
        TomlTableNode moduleNode = moduleName.equals(DEFAULT_MODULE) ? tomlNode : extractModuleNode(tomlNode,
                moduleName);
        return moduleNode;
    }

    private static Object validateNodeAndExtractValue(VariableKey key, Map<String, TopLevelNode> valueMap) {
        String variableName = key.variable;
        TomlValueNode tomlValue = ((TomlKeyValueNode) valueMap.get(variableName)).value();
        Type type = key.type;
        Object value;
        try {
            if (type.getTag() == TypeTags.INTERSECTION_TAG) {
                Type effectiveType = ((BIntersectionType) type).getEffectiveType();
                if (effectiveType.getTag() != TypeTags.ARRAY_TAG) {
                    throw new TomlException(String.format(CONFIGURATION_NOT_SUPPORTED, effectiveType.toString()));
                }
                checkTypeAndThrowError(tomlValue.kind(), variableName, effectiveType);
                value = retrieveArrayValues((TomlArrayValueNode) tomlValue, variableName, (ArrayType) effectiveType);
            } else {
                checkTypeAndThrowError(tomlValue.kind(), variableName, type);
                value = getBalValue(type.getTag(), ((TomlBasicValueNode<?>) tomlValue).getValue());
            }
        } catch (ClassCastException e) {
            throw new TomlException(INVALID_TOML_FILE, e);
        }
        return value;
    }

    private static Object retrieveArrayValues(TomlArrayValueNode arrayNode, String variableName,
                                              ArrayType effectiveType) {
        Type elementType = effectiveType.getElementType();
        List<TomlValueNode> arrayList = arrayNode.elements();
        checkTypeAndThrowError(arrayList.get(0).kind(), variableName, elementType);
        return new ArrayValueImpl(effectiveType, arrayList.size(), createArray(arrayList, elementType.getTag()));
    }

    private static ListInitialValueEntry.ExpressionEntry[] createArray(List<TomlValueNode> arrayList, int typeTag) {
        int arraySize = arrayList.size();
        ListInitialValueEntry.ExpressionEntry[] arrayEntries =
                new ListInitialValueEntry.ExpressionEntry[arraySize];
        for (int i = 0; i < arraySize; i++) {
            Object value = ((TomlBasicValueNode<?>) arrayList.get(i)).getValue();
            arrayEntries[i] = new ListInitialValueEntry.ExpressionEntry(getBalValue(typeTag, value));
        }
        return arrayEntries;
    }

    private static Object getBalValue(int typeTag, Object tomlValue) {
        if (typeTag == TypeTags.DECIMAL_TAG) {
            return ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) tomlValue));
        }
        if (typeTag == TypeTags.STRING_TAG) {
            return  StringUtils.fromString((String) tomlValue);
        }
        return tomlValue;
    }

    private static void checkTypeAndThrowError(TomlType actualType, String variableName, Type expectedType) {
        TomlType tomlType;
        switch (expectedType.getTag()) {
            case TypeTags.INT_TAG:
                tomlType = TomlType.INTEGER;
                break;
            case TypeTags.BOOLEAN_TAG:
                tomlType = TomlType.BOOLEAN;
                break;
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
                tomlType = TomlType.DOUBLE;
                break;
            case TypeTags.STRING_TAG:
                tomlType = TomlType.STRING;
                break;
            case TypeTags.ARRAY_TAG:
                tomlType = TomlType.ARRAY;
                break;
            default:
                throw new TomlException(String.format(CONFIGURATION_NOT_SUPPORTED, expectedType.toString()));
        }
        if (actualType == tomlType) {
            return;
        }
        throw new TomlException(INVALID_TOML_FILE + String.format(INVALID_VARIABLE_TYPE, variableName,
                expectedType.toString(), actualType.name()));
    }

    private static TomlTableNode extractModuleNode(TomlTableNode orgNode, String moduleName) {
        if (orgNode == null) {
            return orgNode;
        }
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
