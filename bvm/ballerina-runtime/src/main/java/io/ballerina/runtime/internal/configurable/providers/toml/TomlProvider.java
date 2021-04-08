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
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.internal.configurable.ConfigConstants.CONFIGURATION_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.ConfigConstants.INCOMPATIBLE_TYPE_ERROR_MESSAGE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONSTRAINT_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.DEFAULT_FIELD_UNSUPPORTED;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.DEFAULT_MODULE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.FIELD_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.INVALID_ADDITIONAL_FIELD_IN_RECORD;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.INVALID_BYTE_RANGE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.INVALID_MODULE_STRUCTURE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.REQUIRED_FIELD_NOT_PROVIDED;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.SUBMODULE_DELIMITER;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.TABLE_KEY_NOT_PROVIDED;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getEffectiveTomlType;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getTomlTypeString;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.isPrimitiveType;
import static io.ballerina.runtime.internal.util.RuntimeUtils.isByteLiteral;

/**
 * Toml value provider for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlProvider implements ConfigProvider {

    Map<Module, TomlTableNode> moduleTomlNodeMap = new HashMap<>();

    Set<String> invalidRequiredModuleSet = new HashSet<>();

    TomlTableNode tomlNode;

    @Override
    public void initialize() {
        // Implemented in extended classes
    }

    @Override
    public boolean hasConfigs() {
        return (tomlNode != null && !tomlNode.entries().isEmpty());
    }

    @Override
    public Optional<Long> getAsIntAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of((Long) value);
    }

    @Override
    public Optional<Integer> getAsByteAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        int byteValue = validateAndGetByteValue(value, key.module.getName() + ":" + key.variable);
        return Optional.of(byteValue);
    }

    @Override
    public Optional<BString> getAsStringAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        String stringVal = (String) value;
        return Optional.of(StringUtils.fromString(stringVal));
    }

    @Override
    public Optional<Double> getAsFloatAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of((double) value);
    }

    @Override
    public Optional<Boolean> getAsBooleanAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of((boolean) value);
    }

    @Override
    public Optional<BDecimal> getAsDecimalAndMark(Module module, VariableKey key) {
        Object value = getPrimitiveTomlValue(module, key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) value)));
    }

    @Override
    public Optional<BArray> getAsArrayAndMark(Module module, VariableKey key) {
        Type effectiveType = ((IntersectionType) key.type).getEffectiveType();
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        if (moduleNode != null && moduleNode.entries().containsKey(key.variable)) {
            TomlNode tomlValue = moduleNode.entries().get(key.variable);
            return Optional.of(retrieveArrayValues(tomlValue, key.module.getName() + ":" + key.variable,
                                                   (ArrayType) effectiveType));
        }
        return Optional.empty();
    }

    @Override
    public Optional<BMap<BString, Object>> getAsRecordAndMark(Module module, VariableKey key) {
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        if (moduleNode != null && moduleNode.entries().containsKey(key.variable)) {
            TomlNode tomlValue = moduleNode.entries().get(key.variable);
            return Optional.of(retrieveRecordValues(tomlValue, key.module.getName() + ":" + key.variable, key.type));
        }
        return Optional.empty();
    }

    @Override
    public Optional<BTable<BString, Object>> getAsTableAndMark(Module module, VariableKey key) {
        Type effectiveType = ((BIntersectionType) key.type).getConstituentTypes().get(0);
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        if (moduleNode != null && moduleNode.entries().containsKey(key.variable)) {
            TomlNode tomlValue = moduleNode.entries().get(key.variable);
            return Optional.of(retrieveTableValues(tomlValue, key.module.getName() + ":" + key.variable,
                                                   (TableType) effectiveType));
        }
        return Optional.empty();
    }

    @Override
    public Optional<BXml> getAsXmlAndMark(Module module, VariableKey key) {
        // This will throw error if user has configured xml variable in the toml
        getPrimitiveTomlValue(module, key);
        return Optional.empty();
    }

    private Object getPrimitiveTomlValue(Module module, VariableKey key) {
        String variableName = key.variable;
        TomlTableNode moduleNode = getModuleTomlNode(module, key);
        if (moduleNode != null && moduleNode.entries().containsKey(variableName)) {
            TomlNode tomlValue = moduleNode.entries().get(variableName);
            tomlValue = getTomlNode(tomlValue, key.module.getName() + ":" + key.variable, key.type);
            return ((TomlBasicValueNode<?>) tomlValue).getValue();
        }
        return null;
    }

    private int validateAndGetByteValue(Object tomlValue, String variableName) {
        int byteValue = ((Long) tomlValue).intValue();
        if (!isByteLiteral(byteValue)) {
            throw new TomlConfigException(String.format(INVALID_BYTE_RANGE, variableName, tomlValue));
        }
        return byteValue;
    }

    private TomlNode getTomlNode(TomlNode tomlValue, String variableName, Type type) {
        TomlType tomlType = tomlValue.kind();
        if (tomlType != TomlType.KEY_VALUE) {
            throw new TomlConfigException(String.format(INCOMPATIBLE_TYPE_ERROR_MESSAGE, variableName, type,
                                                        getTomlTypeString(tomlValue)), tomlValue);
        }
        tomlValue = ((TomlKeyValueNode) tomlValue).value();
        tomlType = tomlValue.kind();
        if (tomlType != getEffectiveTomlType(type, variableName)) {
            throw new TomlConfigException(String.format(INCOMPATIBLE_TYPE_ERROR_MESSAGE, variableName, type,
                                                        getTomlTypeString(tomlValue)), tomlValue);
        }
        return tomlValue;
    }

    private TomlTableNode retrieveModuleNode(TomlTableNode tomlNode, Module module, boolean hasRequired) {
        String orgName = module.getOrg();
        String moduleName = module.getName();
        if (moduleName.equals(DEFAULT_MODULE)) {
            return tomlNode;
        }
        if (tomlNode.entries().containsKey(orgName)) {
            tomlNode = validateAndGetModuleStructure(tomlNode, orgName, orgName + SUBMODULE_DELIMITER + moduleName);
        }
        return extractModuleNode(tomlNode, moduleName, moduleName, hasRequired);
    }

    private TomlTableNode validateAndGetModuleStructure(TomlTableNode tomlNode, String key, String moduleName) {
        TomlNode retrievedNode = tomlNode.entries().get(key);
        if (retrievedNode != null && retrievedNode.kind() != TomlType.TABLE) {
            throw new TomlConfigException(String.format(INVALID_MODULE_STRUCTURE, moduleName, moduleName),
                                          retrievedNode);
        }
        return (TomlTableNode) retrievedNode;
    }

    private TomlTableNode extractModuleNode(TomlTableNode orgNode, String moduleName, String fullModuleName,
                                                   boolean hasRequired) {
        if (orgNode == null) {
            return null;
        }
        TomlTableNode moduleNode = orgNode;
        int subModuleIndex = moduleName.indexOf(SUBMODULE_DELIMITER);
        if (subModuleIndex == -1) {
            moduleNode = validateAndGetModuleStructure(orgNode, moduleName, fullModuleName);
            if (moduleNode == null && hasRequired && !invalidRequiredModuleSet.contains(fullModuleName)) {
                invalidRequiredModuleSet.add(fullModuleName);
                throw new TomlConfigException(String.format(INVALID_MODULE_STRUCTURE, fullModuleName,
                        fullModuleName), orgNode);
            }
        } else if (subModuleIndex != moduleName.length()) {
            String parent = moduleName.substring(0, subModuleIndex);
            String submodule = moduleName.substring(subModuleIndex + 1);
            moduleNode = extractModuleNode(validateAndGetModuleStructure(moduleNode, parent, fullModuleName), submodule,
                                           fullModuleName, hasRequired);
        }
        return moduleNode;
    }

    private Object retrievePrimitiveValue(TomlNode tomlValue, String variableName, Type type, String errorPrefix) {
        TomlType tomlType = tomlValue.kind();
        if (tomlType != TomlType.KEY_VALUE) {
            throw new TomlConfigException(errorPrefix + String.format(INCOMPATIBLE_TYPE_ERROR_MESSAGE, variableName,
                                                                      type, getTomlTypeString(tomlValue)), tomlValue);
        }
        TomlValueNode value = ((TomlKeyValueNode) tomlValue).value();
        tomlType = value.kind();
        if (tomlType != getEffectiveTomlType(type, variableName)) {
            throw new TomlConfigException(errorPrefix + String.format(INCOMPATIBLE_TYPE_ERROR_MESSAGE, variableName,
                                                                      type, getTomlTypeString(tomlValue)), value);
        }
        return getBalValue(variableName, type.getTag(), value);
    }

    private BArray retrieveArrayValues(TomlNode tomlValue, String variableName, ArrayType effectiveType) {
        if (tomlValue.kind() != TomlType.KEY_VALUE) {
            throw new TomlConfigException(String.format(INCOMPATIBLE_TYPE_ERROR_MESSAGE, variableName, effectiveType,
                                                        getTomlTypeString(tomlValue)), tomlValue);
        }
        tomlValue = ((TomlKeyValueNode) tomlValue).value();
        if (tomlValue.kind() != getEffectiveTomlType(effectiveType, variableName)) {
            throw new TomlConfigException(String.format(INCOMPATIBLE_TYPE_ERROR_MESSAGE, variableName, effectiveType,
                                                        getTomlTypeString(tomlValue)), tomlValue);
        }
        Type elementType = effectiveType.getElementType();
        List<TomlValueNode> arrayList = ((TomlArrayValueNode) tomlValue).elements();
        if (!isPrimitiveType(elementType.getTag())) {
            //Remove after supporting all arrays
            throw new TomlConfigException(String.format(CONFIGURATION_NOT_SUPPORTED, variableName,
                                                        effectiveType.toString()), tomlValue);
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
            TomlNode tomlValueNode = arrayList.get(i);
            if (tomlValueNode.kind() != getEffectiveTomlType(elementType, elementName)) {
                throw new TomlConfigException(String.format(INCOMPATIBLE_TYPE_ERROR_MESSAGE, elementName, elementType ,
                                                            getTomlTypeString(tomlValueNode)), tomlValueNode);
            }
            arrayEntries[i] = new ListInitialValueEntry.ExpressionEntry(getBalValue(variableName, elementType.getTag(),
                            arrayList.get(i)));
        }
        return arrayEntries;
    }

    private BMap<BString, Object> retrieveRecordValues(TomlNode tomlNode, String variableName, Type type) {
        RecordType recordType;
        String recordName;
        Module recordPkg;
        if (type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            recordName = type.getName();
            recordType = (RecordType) type;
            recordPkg = recordType.getPackage();;
        } else {
            Type originalType = ((BIntersectionType) type).getConstituentTypes().get(0);
            recordName = originalType.getName();
            recordType = (RecordType) ((BIntersectionType) type).getEffectiveType();
            recordPkg = originalType.getPackage();
        }
        if (tomlNode.kind() != getEffectiveTomlType(recordType, variableName)) {
            throw new TomlConfigException(String.format(INCOMPATIBLE_TYPE_ERROR_MESSAGE, variableName, recordType ,
                                                        getTomlTypeString(tomlNode)), tomlNode);
        }

        TomlTableNode tomlValue = (TomlTableNode) tomlNode;
        Map<String, Object> initialValueEntries = new HashMap<>();
        for (Map.Entry<String, TopLevelNode> tomlField : tomlValue.entries().entrySet()) {
            String fieldName = tomlField.getKey();
            Field field = recordType.getFields().get(fieldName);
            TomlNode value = tomlField.getValue();
            if (field == null) {
                throw new TomlConfigException(String.format(INVALID_ADDITIONAL_FIELD_IN_RECORD, fieldName, variableName,
                                                            recordType.toString()), value);
            }
            Type fieldType = field.getFieldType();
            if (!isSupportedType(fieldType)) {
                throw new TomlConfigException(String.format(FIELD_TYPE_NOT_SUPPORTED, fieldType, variableName), value);
            }
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
        validateRequiredField(initialValueEntries, recordType, variableName, tomlValue);
        if (type.getTag() == TypeTags.RECORD_TYPE_TAG && type.isReadOnly()) {
            return createReadOnlyFieldRecord(initialValueEntries, recordType, variableName, tomlValue);
        }
        return ValueCreator.createRecordValue(recordPkg, recordName, initialValueEntries);
    }

    private BMap<BString, Object> createReadOnlyFieldRecord(Map<String, Object> initialValueEntries,
                                                                   RecordType recordType, String variableName,
                                                                   TomlTableNode tomlValue) {
        for (Map.Entry<String, Field> field : recordType.getFields().entrySet()) {
            String fieldName = field.getKey();
            long flag = field.getValue().getFlags();
            // remove after fixing #28966
            if (!SymbolFlags.isFlagOn(flag, SymbolFlags.OPTIONAL) && !SymbolFlags.isFlagOn(flag,
                    SymbolFlags.REQUIRED) && initialValueEntries.get(fieldName) == null) {
                String error = String.format(DEFAULT_FIELD_UNSUPPORTED, fieldName, variableName);
                throw new TomlConfigException(error, tomlValue);
            }
        }

        MappingInitialValueEntry.KeyValueEntry[] initialValues =
                new MappingInitialValueEntry.KeyValueEntry[initialValueEntries.size()];
        int count = 0;
        for (Map.Entry<String, Object> value : initialValueEntries.entrySet()) {
            initialValues[count] = new MappingInitialValueEntry.KeyValueEntry(StringUtils.fromString(value.getKey())
                    , value.getValue());
            count++;
        }
        return ValueCreator.createMapValue(recordType, initialValues);
    }

    private static void validateRequiredField(Map<String, Object> initialValueEntries, RecordType recordType,
                                              String variableName, TomlNode tomlNode) {
        for (Map.Entry<String, Field> field : recordType.getFields().entrySet()) {
            String fieldName = field.getKey();
            if (SymbolFlags.isFlagOn(field.getValue().getFlags(), SymbolFlags.REQUIRED) &&
                    initialValueEntries.get(fieldName) == null) {
                throw new TomlConfigException(
                        String.format(REQUIRED_FIELD_NOT_PROVIDED, fieldName, recordType.toString(), variableName),
                        tomlNode);
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

    private BTable<BString, Object> retrieveTableValues(TomlNode tomlValue, String variableName,
                                                               TableType tableType) {
        Type constraintType = tableType.getConstrainedType();
        int tag = constraintType.getTag();
        if (tag != TypeTags.INTERSECTION_TAG && tag != TypeTags.RECORD_TYPE_TAG) {
            throw new TomlConfigException(String.format(CONSTRAINT_TYPE_NOT_SUPPORTED, constraintType, variableName),
                                          tomlValue);
        }
        if (tag == TypeTags.INTERSECTION_TAG &&
                ((BIntersectionType) constraintType).getEffectiveType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new TomlConfigException(String.format(CONSTRAINT_TYPE_NOT_SUPPORTED, constraintType, variableName),
                                          tomlValue);
        }
        if (tomlValue.kind() != getEffectiveTomlType(tableType, variableName)) {
            throw new TomlConfigException(String.format(INCOMPATIBLE_TYPE_ERROR_MESSAGE, variableName, tableType ,
                                                        getTomlTypeString(tomlValue)), tomlValue);
        }
        List<TomlTableNode> tableNodeList = ((TomlTableArrayNode) tomlValue).children();
        int tableSize = tableNodeList.size();
        ListInitialValueEntry.ExpressionEntry[] tableEntries = new ListInitialValueEntry.ExpressionEntry[tableSize];
        String[] keys = tableType.getFieldNames();
        for (int i = 0; i < tableSize; i++) {
            if (keys != null) {
                validateKeyField(tableNodeList.get(i), keys, tableType, variableName);
            }
            BMap<BString, Object> value = retrieveRecordValues(tableNodeList.get(i), variableName, constraintType);
            value.freezeDirect();
            tableEntries[i] = new ListInitialValueEntry.ExpressionEntry(value);
        }
        ArrayValue tableData =
                new ArrayValueImpl(TypeCreator.createArrayType(constraintType), tableSize, tableEntries);
        ArrayValue keyNames = keys == null ? (ArrayValue) ValueCreator.createArrayValue(new BString[]{}) :
                (ArrayValue) StringUtils.fromStringArray(keys);
        if (constraintType.getTag() == TypeTags.INTERSECTION_TAG) {
            constraintType = ((IntersectionType) constraintType).getEffectiveType();
        }
        TableType type = TypeCreator.createTableType(constraintType, keys, true);
        return new TableValueImpl<>((BTableType) type, tableData, keyNames);
    }

    private static void validateKeyField(TomlTableNode recordTable, String[] fieldNames, Type tableType,
                                         String variableName) {
        for (String key : fieldNames) {
            if (recordTable.entries().get(key) == null) {
                throw new TomlConfigException(String.format(TABLE_KEY_NOT_PROVIDED, key, tableType.toString(),
                                                            variableName), recordTable);
            }
        }
    }

    private static Object getBalValue(String variableName, int typeTag, TomlValueNode tomlValueNode) {
        Object tomlValue = ((TomlBasicValueNode<?>) tomlValueNode).getValue();
        if (typeTag == TypeTags.BYTE_TAG) {
            int value = ((Long) tomlValue).intValue();
            if (!isByteLiteral(value)) {
                throw new TomlConfigException(String.format(INVALID_BYTE_RANGE, variableName, value), tomlValueNode);
            }
            return value;
        }
        if (typeTag == TypeTags.DECIMAL_TAG) {
            return ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) tomlValue));
        }
        if (typeTag == TypeTags.STRING_TAG) {
            String stringVal = (String) tomlValue;
            return StringUtils.fromString(stringVal);
        }
        return tomlValue;
    }

    private TomlTableNode getModuleTomlNode(Module module, VariableKey key) {
        if (moduleTomlNodeMap.containsKey(module)) {
            TomlTableNode tomlTableNode = moduleTomlNodeMap.get(module);
            if (tomlTableNode != null || !key.isRequired()) {
                return tomlTableNode;
            }
        }
        TomlTableNode tomlTableNode = null;
        try {
            tomlTableNode = retrieveModuleNode(tomlNode, module, key.isRequired());
        } finally {
            moduleTomlNodeMap.put(module, tomlTableNode);
        }
        return tomlTableNode;
    }
}
