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

package io.ballerina.runtime.internal.configurable.providers.toml;

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
import io.ballerina.runtime.internal.configurable.providers.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.utils.ConfigSecurityUtils;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ErrorValue;
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

import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.CONFIGURATION_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.CONSTRAINT_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.DEFAULT_MODULE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.FIELD_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.INVALID_ADDITIONAL_FIELD_IN_RECORD;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.INVALID_BYTE_RANGE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.INVALID_MODULE_STRUCTURE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.INVALID_TOML_TYPE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.REQUIRED_FIELD_NOT_PROVIDED;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.SUBMODULE_DELIMITER;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConfigConstants.TABLE_KEY_NOT_PROVIDED;
import static io.ballerina.runtime.internal.util.RuntimeUtils.isByteLiteral;

/**
 * Toml parser for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlConfigProvider implements ConfigProvider {

    Map<Module, TomlTableNode> moduleTomlNodeMap = new HashMap<>();

    TomlTableNode tomlNode;

    public TomlConfigProvider(Path configPath) {
        this.tomlNode = getConfigurationData(configPath);
    }

    @Override
    public void initialize(Map<Module, VariableKey[]> configVarMap) {
        //No values provided at toml file
        if (tomlNode == null || tomlNode.entries().isEmpty()) {
            //No values provided at toml file
            return;
        }
        for (Map.Entry<Module, VariableKey[]> moduleEntry : configVarMap.entrySet()) {
            Module module = moduleEntry.getKey();
            TomlTableNode moduleNode = retrieveModuleNode(tomlNode, module);
            if (moduleNode != null) {
                moduleTomlNodeMap.put(module, moduleNode);
            }
        }
    }

    @Override
    public boolean hasConfigs() {
        return (tomlNode != null && !tomlNode.entries().isEmpty() && !moduleTomlNodeMap.isEmpty());
    }

    @Override
    public ConfigValue getAsIntAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return null;
        }
        return new ConfigValue(((Long) value).intValue());
    }

    @Override
    public ConfigValue getAsByteAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return null;
        }
        int byteValue = validateAndGetByteValue(value, key.variable);
        return new ConfigValue(byteValue);
    }

    @Override
    public ConfigValue getAsStringAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return null;
        }
        String stringVal = (String) value;
        ConfigSecurityUtils.handleEncryptedValues(key.variable, stringVal);
        return new ConfigValue(StringUtils.fromString(stringVal));
    }

    @Override
    public ConfigValue getAsFloatAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return null;
        }
        return new ConfigValue(value);
    }

    @Override
    public ConfigValue getAsBooleanAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return null;
        }
        return new ConfigValue(value);
    }

    @Override
    public ConfigValue getAsDecimalAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return null;
        }
        return new ConfigValue(ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) value)));
    }

    @Override
    public ConfigValue getAsArrayAndMark(Module module, VariableKey key) {
        String variableName = key.variable;
        TomlTableNode moduleNode = moduleTomlNodeMap.get(module);
        if (moduleNode != null && moduleNode.entries().containsKey(variableName)) {
            TomlNode tomlValue = moduleNode.entries().get(variableName);
            return new ConfigValue(retrieveArrayValues(tomlValue, variableName,
                                                       (ArrayType) ((BIntersectionType) key.type).getEffectiveType()));
        }
        return null;
    }

    @Override
    public ConfigValue getAsRecordAndMark(Module module, VariableKey key){
        String variableName = key.variable;
        TomlTableNode moduleNode = moduleTomlNodeMap.get(module);
        if (moduleNode != null && moduleNode.entries().containsKey(variableName)) {
            TomlNode tomlValue = moduleNode.entries().get(variableName);
            return new ConfigValue(retrieveRecordValues(tomlValue, variableName, (BIntersectionType) key.type));
        }
        return null;
    }

    @Override
    public ConfigValue getAsTableAndMark(Module module, VariableKey key){
        String variableName = key.variable;
        TomlTableNode moduleNode = moduleTomlNodeMap.get(module);
        if (moduleNode != null && moduleNode.entries().containsKey(variableName)) {
            TomlNode tomlValue = moduleNode.entries().get(variableName);
            return new ConfigValue(retrieveTableValues(tomlValue, variableName, (TableType) key.type));
        }
        return null;
    }


    private TomlTableNode getConfigurationData(Path configFilePath) {
        if (!Files.exists(configFilePath)) {
            return null;
        }
        TomlConfig configToml = new TomlConfig(configFilePath);
        return configToml.tomlAstNode();
    }

    private TomlTableNode retrieveModuleNode(TomlTableNode tomlNode, Module module) {
        String orgName = module.getOrg();
        String moduleName = module.getName();
        if (tomlNode.entries().containsKey(orgName)) {
            tomlNode = validateAndGetModuleStructure(tomlNode, orgName, orgName + SUBMODULE_DELIMITER + moduleName);
        }
        return moduleName.equals(DEFAULT_MODULE) ? tomlNode : extractModuleNode(tomlNode, moduleName, moduleName);
    }

    private TomlTableNode validateAndGetModuleStructure(TomlTableNode tomlNode, String key, String moduleName) {
        TomlNode retrievedNode = tomlNode.entries().get(key);
        if (retrievedNode != null && retrievedNode.kind() != TomlType.TABLE) {
            throw new ErrorValue(StringUtils.fromString(String.format(INVALID_MODULE_STRUCTURE, moduleName,
                                                                      moduleName)));
        }
        return (TomlTableNode) retrievedNode;
    }

    private TomlTableNode extractModuleNode(TomlTableNode orgNode, String moduleName, String fullModuleName) {
        if (orgNode == null) {
            return null;
        }
        TomlTableNode moduleNode = orgNode;
        int subModuleIndex = moduleName.indexOf(SUBMODULE_DELIMITER);
        if (subModuleIndex == -1) {
            moduleNode = validateAndGetModuleStructure(orgNode, moduleName, fullModuleName);
        } else if (subModuleIndex != moduleName.length()) {
            String parent = moduleName.substring(0, subModuleIndex);
            String submodule = moduleName.substring(subModuleIndex + 1);
            moduleNode = extractModuleNode(validateAndGetModuleStructure(moduleNode, parent, fullModuleName), submodule,
                    fullModuleName);
        }
        return moduleNode;
    }

    private Object getPrimitiveTomlValue(Module module, VariableKey key) {
        String variableName = key.variable;
        TomlTableNode moduleNode = moduleTomlNodeMap.get(module);
        if (moduleNode != null && moduleNode.entries().containsKey(variableName)) {
            TomlNode tomlValue = moduleNode.entries().get(variableName);
            tomlValue = getTomlNode(tomlValue, variableName, key.type, "");
            return ((TomlBasicValueNode<?>) tomlValue).getValue();
        }
        return null;
    }

    private int validateAndGetByteValue(Object tomlValue, String variableName) {
        int byteValue = ((Long) tomlValue).intValue();
        if (!isByteLiteral(byteValue)) {
            throw new ErrorValue(StringUtils.fromString(String.format(INVALID_BYTE_RANGE, variableName, tomlValue)));
        }
        return byteValue;
    }

    private Object retrievePrimitiveValue(TomlNode tomlValue, String variableName, Type type,
                                                 String errorPrefix) {
        tomlValue = getTomlNode(tomlValue, variableName, type, errorPrefix);
        return getBalValue(variableName, type.getTag(), ((TomlBasicValueNode<?>) tomlValue).getValue());
    }

    private TomlNode getTomlNode(TomlNode tomlValue, String variableName, Type type, String errorPrefix) {
        TomlType tomlType = tomlValue.kind();
        if (tomlType != TomlType.KEY_VALUE) {
            throw new ErrorValue(StringUtils.fromString(errorPrefix + String.format(INVALID_TOML_TYPE, variableName, type,
                                                                                    getTomlTypeString(tomlValue))));
        }
        tomlValue = ((TomlKeyValueNode) tomlValue).value();
        tomlType = tomlValue.kind();
        if (tomlType != getEffectiveTomlType(type, variableName)) {
            throw new ErrorValue(StringUtils.fromString(errorPrefix + String.format(INVALID_TOML_TYPE, variableName, type,
                            getTomlTypeString(tomlValue))));
        }
        return tomlValue;
    }

    private Object retrieveArrayValues(TomlNode tomlValue, String variableName, ArrayType effectiveType) {
        if (tomlValue.kind() != TomlType.KEY_VALUE) {
            throw new ErrorValue(StringUtils.fromString(String.format(INVALID_TOML_TYPE, variableName,
                                                                              effectiveType,
                                                                              getTomlTypeString(tomlValue))));
        }
        tomlValue = ((TomlKeyValueNode) tomlValue).value();
        if (tomlValue.kind() != getEffectiveTomlType(effectiveType, variableName)) {
            throw new ErrorValue(StringUtils.fromString(String.format(INVALID_TOML_TYPE, variableName,
                                                                              effectiveType,
                                                                              getTomlTypeString(tomlValue))));
        }
        Type elementType = effectiveType.getElementType();
        List<TomlValueNode> arrayList = ((TomlArrayValueNode) tomlValue).elements();
        if (!isPrimitiveType(elementType.getTag())) {
            //Remove after supporting all arrays
            throw new ErrorValue(StringUtils.fromString(
                    String.format(CONFIGURATION_NOT_SUPPORTED, variableName, effectiveType.toString())));
        }
        return new ArrayValueImpl(effectiveType, arrayList.size(), createArray(variableName, arrayList, elementType));
    }

    private ListInitialValueEntry.ExpressionEntry[] createArray(String variableName,
                                                                       List<TomlValueNode> arrayList,
                                                                       Type elementType) {
        int arraySize = arrayList.size();
        ListInitialValueEntry.ExpressionEntry[] arrayEntries =
                new ListInitialValueEntry.ExpressionEntry[arraySize];
        for (int i = 0; i < arraySize; i++) {
            String elementName = variableName + "[" + i + "]";
            TomlNode tomlNode = arrayList.get(i);
            if (tomlNode.kind() != getEffectiveTomlType(elementType, elementName)) {
                throw new ErrorValue(StringUtils.fromString(String.format(INVALID_TOML_TYPE, elementName, elementType,
                                                                          getTomlTypeString(tomlNode))));
            }
            TomlBasicValueNode<?> tomlBasicValueNode = (TomlBasicValueNode<?>) arrayList.get(i);
            Object value = tomlBasicValueNode.getValue();
            arrayEntries[i] =
                    new ListInitialValueEntry.ExpressionEntry(getBalValue(variableName, elementType.getTag(), value));
        }
        return arrayEntries;
    }

    private Object retrieveRecordValues(TomlNode tomlNode, String variableName, BIntersectionType intersectionType) {
        Type effectiveType = intersectionType.getEffectiveType();
        if (tomlNode.kind() != getEffectiveTomlType(effectiveType, variableName)) {
            throw new ErrorValue(StringUtils.fromString(String.format(INVALID_TOML_TYPE, variableName, effectiveType ,
                    getTomlTypeString(tomlNode))));
        }
        RecordType recordType = (RecordType) intersectionType.getConstituentTypes().get(0);
        Map<String, Object> initialValueEntries = new HashMap<>();
        for (Map.Entry<String, TopLevelNode> tomlField : ((TomlTableNode) tomlNode).entries().entrySet()) {
            String fieldName = tomlField.getKey();
            Field field = recordType.getFields().get(fieldName);
            if (field == null) {
                throw new ErrorValue(StringUtils.fromString(String.format(INVALID_ADDITIONAL_FIELD_IN_RECORD,
                                                                          fieldName, variableName,
                                                                          recordType.toString())));
            }
            Type fieldType = field.getFieldType();
            if (!isSupportedType(fieldType)) {
                throw new ErrorValue(StringUtils.fromString(String.format(FIELD_TYPE_NOT_SUPPORTED, fieldType,
                                                                          variableName)));
            }
            TomlNode value = tomlField.getValue();
            Object objectValue;
            String errorPrefix = "field '" + fieldName + "' from ";
            switch (fieldType.getTag()) {
                case TypeTags.ARRAY_TAG:
                    objectValue = retrieveArrayValues(value, variableName, (ArrayType) fieldType);
                    break;
                case TypeTags.INTERSECTION_TAG:
                    ArrayType arrayType = (ArrayType) ((IntersectionType) fieldType).getEffectiveType();
                    objectValue = retrieveArrayValues(value, variableName, arrayType);
                    break;
                default:
                    objectValue = retrievePrimitiveValue(value, variableName, fieldType, errorPrefix);
            }
            initialValueEntries.put(fieldName, objectValue);
        }
        validateRequiredField(initialValueEntries, recordType, variableName);
        return ValueCreator
                .createReadonlyRecordValue(recordType.getPackage(), recordType.getName(), initialValueEntries);
    }

    private void validateRequiredField(Map<String, Object> initialValueEntries, RecordType recordType,
                                              String variableName) {
        for (Map.Entry<String, Field> field : recordType.getFields().entrySet()) {
            String fieldName = field.getKey();
            if (SymbolFlags.isFlagOn(field.getValue().getFlags(), SymbolFlags.REQUIRED) &&
                    initialValueEntries.get(fieldName) == null) {
                throw new ErrorValue(StringUtils.fromString(
                        String.format(REQUIRED_FIELD_NOT_PROVIDED, fieldName, recordType.toString(), variableName)));
            }
        }
    }

    private boolean isSupportedType(Type type) {
        //Remove this check when we support all field types
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
        return isPrimitiveType(typeTag);
    }

    private Object retrieveTableValues(TomlNode tomlValue, String variableName,
                                              TableType tableType) {
        Type constraintType = tableType.getConstrainedType();
        if (constraintType.getTag() != TypeTags.INTERSECTION_TAG) {
            throw new ErrorValue(StringUtils.fromString(
                    String.format(CONSTRAINT_TYPE_NOT_SUPPORTED, constraintType, variableName)));
        }
        if (((BIntersectionType) constraintType).getEffectiveType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new ErrorValue(StringUtils.fromString(
                    String.format(CONSTRAINT_TYPE_NOT_SUPPORTED, constraintType, variableName)));
        }
        if (tomlValue.kind() != getEffectiveTomlType(tableType, variableName)) {
            throw new ErrorValue(StringUtils.fromString(String.format(INVALID_TOML_TYPE, variableName, tableType,
                                                                      getTomlTypeString(tomlValue))));
        }
        List<TomlTableNode> tableNodeList = ((TomlTableArrayNode) tomlValue).children();
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

    private void validateKeyField(TomlTableNode recordTable, String[] fieldNames, Type tableType,
                                  String variableName) {
        for (String key : fieldNames) {
            if (recordTable.entries().get(key) == null) {
                throw new ErrorValue(StringUtils.fromString(String.format(TABLE_KEY_NOT_PROVIDED,
                                                                          key, tableType.toString(), variableName)));
            }
        }
    }

    private Object getBalValue(String variableName, int typeTag, Object tomlValue) {
        if (typeTag == TypeTags.BYTE_TAG) {
            return validateAndGetByteValue(tomlValue, variableName);
        }
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

    private Object getTomlTypeString(TomlNode tomlNode) {
        switch (tomlNode.kind()) {
            case STRING:
                return "string";
            case INTEGER:
                return "int";
            case DOUBLE:
                return "float";
            case BOOLEAN:
                return "boolean";
            case ARRAY:
                return "array";
            case TABLE:
                return "record";
            case TABLE_ARRAY:
                return "table";
            case KEY_VALUE:
                return getTomlTypeString(((TomlKeyValueNode) tomlNode).value());
            default:
                return "unsupported type";
        }
    }

    private TomlType getEffectiveTomlType(Type expectedType, String variableName) {
        TomlType tomlType;
        switch (expectedType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
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
                throw new ErrorValue(StringUtils.fromString(String.format(CONFIGURATION_NOT_SUPPORTED,
                                                                          variableName, expectedType.toString())));
        }
        return tomlType;
    }

    private boolean isPrimitiveType(int typeTag) {
        return typeTag <= TypeTags.BOOLEAN_TAG;
    }
}
