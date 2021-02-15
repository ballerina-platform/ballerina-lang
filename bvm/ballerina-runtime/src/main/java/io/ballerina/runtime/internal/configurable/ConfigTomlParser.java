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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.exceptions.TomlException;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBasicValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.CONFIGURATION_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.DEFAULT_MODULE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.FIELD_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_FIELD_IN_RECORD;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_TOML_FILE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_VARIABLE_TYPE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.REQUIRED_FIELD_NOT_PROVIDED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.SUBMODULE_DELIMITER;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.TABLE_KEY_NOT_PROVIDED;

/**
 * Toml parser for configurable implementation.
 *
 * @since 2.0.0
 */
public class ConfigTomlParser {

    private ConfigTomlParser() {
    }

    private static TomlTableNode getConfigurationData(Path configFilePath) {
        if (!Files.exists(configFilePath)) {
            return null;
        }
        ConfigToml configToml = new ConfigToml(configFilePath);
        return configToml.tomlAstNode();
    }

    public static void populateConfigMap(Path filePath, Map<Module, VariableKey[]> configurationData) {
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
            tomlNode = (TomlTableNode) tomlNode.entries().get(orgName);
        }
        return moduleName.equals(DEFAULT_MODULE) ? tomlNode : extractModuleNode(tomlNode, moduleName);
    }

    private static Object validateNodeAndExtractValue(VariableKey key, Map<String, TopLevelNode> valueMap) {
        String variableName = key.variable;
        Type type = key.type;
        Object value;
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
                value = retrievePrimitiveValue(((TomlKeyValueNode) valueMap.get(variableName)).value(), variableName,
                        type);
                break;
            case TypeTags.INTERSECTION_TAG:
                value = retrieveComplexValue((BIntersectionType) type, valueMap, variableName);
                break;
            default:
                throw new TomlException(String.format(CONFIGURATION_NOT_SUPPORTED, type.toString()));
        }
        return value;
    }

    private static Object retrieveComplexValue(BIntersectionType type, Map<String, TopLevelNode> valueMap,
                                               String variableName) {
        Type effectiveType = type.getEffectiveType();
        Object value;
        TomlNode tomlValue;
        switch (effectiveType.getTag()) {
            case TypeTags.ARRAY_TAG:
                tomlValue = ((TomlKeyValueNode) valueMap.get(variableName)).value();
                checkTypeAndThrowError(tomlValue.kind(), variableName, effectiveType);
                value = retrieveArrayValues((TomlArrayValueNode) tomlValue, variableName, (ArrayType) effectiveType);
                break;
            case TypeTags.RECORD_TYPE_TAG:
                tomlValue = valueMap.get(variableName);
                checkTypeAndThrowError(tomlValue.kind(), variableName, effectiveType);
                value = retrieveRecordValues((TomlTableNode) tomlValue, variableName, type);
                break;
            case TypeTags.TABLE_TAG:
                tomlValue = valueMap.get(variableName);
                checkTypeAndThrowError(tomlValue.kind(), variableName, effectiveType);
                value = retrieveTableValues((TomlTableArrayNode) tomlValue, variableName, (TableType) effectiveType);
                break;
            default:
                throw new TomlException(String.format(CONFIGURATION_NOT_SUPPORTED, effectiveType.toString()));
        }
        return value;
    }

    private static Object retrievePrimitiveValue(TomlNode tomlValue, String variableName, Type type) {
        checkTypeAndThrowError(tomlValue.kind(), variableName, type);
        return getBalValue(variableName, type.getTag(), ((TomlBasicValueNode<?>) tomlValue).getValue());
    }

    private static Object retrieveArrayValues(TomlArrayValueNode arrayNode, String variableName,
                                              ArrayType effectiveType) {
        Type elementType = effectiveType.getElementType();
        List<TomlValueNode> arrayList = arrayNode.elements();
        checkTypeAndThrowError(arrayList.get(0).kind(), variableName, elementType);
        return new ArrayValueImpl(effectiveType, arrayList.size(), createArray(variableName, arrayList,
                                                                               elementType.getTag()));
    }

    private static ListInitialValueEntry.ExpressionEntry[] createArray(String variableName,
                                                                       List<TomlValueNode> arrayList, int typeTag) {
        int arraySize = arrayList.size();
        ListInitialValueEntry.ExpressionEntry[] arrayEntries =
                new ListInitialValueEntry.ExpressionEntry[arraySize];
        for (int i = 0; i < arraySize; i++) {
            TomlBasicValueNode<?> tomlBasicValueNode = (TomlBasicValueNode<?>) arrayList.get(i);
            Object value = tomlBasicValueNode.getValue();
            arrayEntries[i] = new ListInitialValueEntry.ExpressionEntry(getBalValue(variableName, typeTag, value));
        }
        return arrayEntries;
    }

    private static Object retrieveRecordValues(TomlTableNode tomlValue, String variableName,
                                               BIntersectionType intersectionType) {
        RecordType effectiveType = (RecordType) intersectionType.getConstituentTypes().get(0);
        Map<String, Object> initialValueEntries = new HashMap<>();
        for (Map.Entry<String, TopLevelNode> tomlField : tomlValue.entries().entrySet()) {
            String fieldName = tomlField.getKey();
            Field field = effectiveType.getFields().get(fieldName);
            if (field == null) {
                throw new TomlException(String.format(INVALID_FIELD_IN_RECORD, fieldName, variableName,
                        effectiveType.toString()));
            }
            Type fieldType = field.getFieldType();
            if (!isSupportedType(fieldType)) {
                throw new TomlException(
                        String.format(FIELD_TYPE_NOT_SUPPORTED, fieldType.toString(), variableName, effectiveType));
            }
            TomlNode value = ((TomlKeyValueNode) tomlField.getValue()).value();
            Object objectValue;
            switch (fieldType.getTag()) {
                case TypeTags.ARRAY_TAG:
                    checkTypeAndThrowError(value.kind(), variableName + "." + fieldName, fieldType);
                    objectValue = retrieveArrayValues((TomlArrayValueNode) value, variableName, (ArrayType) fieldType);
                    break;
                case TypeTags.INTERSECTION_TAG:
                    ArrayType arrayType = (ArrayType) ((IntersectionType) fieldType).getEffectiveType();
                    checkTypeAndThrowError(value.kind(), variableName + "." + fieldName, arrayType);
                    objectValue = retrieveArrayValues((TomlArrayValueNode) value, variableName, arrayType);
                    break;
                default:
                    objectValue = retrievePrimitiveValue(value, variableName + "." + fieldName, fieldType);
            }
            initialValueEntries.put(fieldName, objectValue);
        }
        validateRequiredField(initialValueEntries, effectiveType, variableName);
        return ValueCreator
                .createReadonlyRecordValue(effectiveType.getPackage(), effectiveType.getName(), initialValueEntries);
    }

    private static void validateRequiredField(Map<String, Object> initialValueEntries, RecordType recordType,
                                              String variableName) {
        for (Map.Entry<String, Field> field : recordType.getFields().entrySet()) {
            String fieldName = field.getKey();
            if (SymbolFlags.isFlagOn(field.getValue().getFlags(), SymbolFlags.REQUIRED) &&
                    initialValueEntries.get(fieldName) == null) {
                throw new TomlException(
                        String.format(REQUIRED_FIELD_NOT_PROVIDED, fieldName, recordType.toString(), variableName));
            }
        }
    }

    private static boolean isSupportedType(Type type) {
        int typeTag = type.getTag();
        if (typeTag == TypeTags.INTERSECTION_TAG) {
            Type effectiveType = ((IntersectionType) type).getEffectiveType();
            if (effectiveType.getTag() != TypeTags.ARRAY_TAG) {
                return false;
            }
            typeTag = ((ArrayType) ((IntersectionType) type).getEffectiveType()).getElementType().getTag();
        } else if (typeTag == TypeTags.ARRAY_TAG) {
            typeTag = ((ArrayType) type).getElementType().getTag();
        }
        return (typeTag == TypeTags.INT_TAG || typeTag == TypeTags.FLOAT_TAG || typeTag == TypeTags.BOOLEAN_TAG ||
                typeTag == TypeTags.STRING_TAG || typeTag == TypeTags.DECIMAL_TAG);
    }

    private static Object retrieveTableValues(TomlTableArrayNode tomlValue, String variableName,
                                              TableType tableType) {
        List<TomlTableNode> tableNodeList = tomlValue.children();
        Type constraintType = tableType.getConstrainedType();
        int tableSize = tableNodeList.size();
        ListInitialValueEntry.ExpressionEntry[] tableEntries = new ListInitialValueEntry.ExpressionEntry[tableSize];
        String[] keys = tableType.getFieldNames();
        for (int i = 0; i < tableSize; i++) {
            if (keys != null) {
                validateKeyField(tableNodeList.get(i), keys, tableType, variableName);
            }
            Object value = retrieveRecordValues(tableNodeList.get(i), variableName, (BIntersectionType) constraintType);
            tableEntries[i] = new ListInitialValueEntry.ExpressionEntry(value);
        }
        ArrayValue tableData =
                new ArrayValueImpl(TypeCreator.createArrayType(constraintType), tableSize, tableEntries);
        ArrayValue keyNames = keys == null ? (ArrayValue) ValueCreator.createArrayValue(new BString[]{}) :
                (ArrayValue) StringUtils.fromStringArray(keys);
        return new TableValueImpl<>((BTableType) tableType, tableData, keyNames);
    }

    private static void validateKeyField(TomlTableNode recordTable, String[] fieldNames, Type tableType,
                                         String variableName) {
        for (String key : fieldNames) {
            if (recordTable.entries().get(key) == null) {
                throw new TomlException(
                        String.format(TABLE_KEY_NOT_PROVIDED, key, tableType.toString(), variableName));
            }
        }
    }

    private static Object getBalValue(String variableName, int typeTag, Object tomlValue) {
        if (typeTag == TypeTags.DECIMAL_TAG) {
            return ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) tomlValue));
        }
        if (typeTag == TypeTags.STRING_TAG) {
            String stringVal = (String) tomlValue;
            ConfigSecurityUtils.handleEncryptedValues(variableName, stringVal);
            return StringUtils.fromString(stringVal);
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
            case TypeTags.RECORD_TYPE_TAG:
                tomlType = TomlType.TABLE;
                break;
            case TypeTags.TABLE_TAG:
                tomlType = TomlType.TABLE_ARRAY;
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
