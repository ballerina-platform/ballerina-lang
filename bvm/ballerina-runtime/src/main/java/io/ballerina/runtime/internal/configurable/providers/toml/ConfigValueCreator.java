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

package io.ballerina.runtime.internal.configurable.providers.toml;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBasicValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.isPrimitiveType;

/**
 * Value creator to create values for structured configurable values from TOML nodes.
 *
 * @since 2.0.0
 */
public class ConfigValueCreator {

    public Object createValue(TomlNode tomlValue, String variableName, Type type) {
        if (isPrimitiveType(type.getTag())) {
            return createPrimitiveValue(tomlValue, type);
        }
        return createStructuredValue(tomlValue, variableName, type);
    }

    private Object createPrimitiveValue(TomlNode tomlValue, Type type) {
        TomlValueNode value = ((TomlKeyValueNode) tomlValue).value();
        return getBalValue(type, value);
    }

    private Object createStructuredValue(TomlNode tomlValue, String variableName, Type type) {
        switch (type.getTag()) {
            case TypeTags.ARRAY_TAG:
                return createArrayValue(tomlValue, variableName, (ArrayType) type);
            case TypeTags.RECORD_TYPE_TAG:
                return createRecordValue(tomlValue, variableName, type);
            case TypeTags.MAP_TAG:
                return createMapValue(tomlValue, variableName, (MapType) type);
            case TypeTags.TABLE_TAG:
                return createTableValue(tomlValue, variableName, type);
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((IntersectionType) type).getEffectiveType();
                if (effectiveType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    return createRecordValue(tomlValue, variableName, type);
                }
                if (effectiveType.getTag() == TypeTags.TABLE_TAG) {
                    return createTableValue(tomlValue, variableName, type);
                }
                return createValue(tomlValue, variableName, effectiveType);
            case TypeTags.ANYDATA_TAG:
            case TypeTags.UNION_TAG:
                return createUnionValue(tomlValue, variableName, (BUnionType) type);
            default:
                //this branch will not be executed
                return null;
        }
    }

    private BArray createArrayValue(TomlNode tomlValue, String variableName, ArrayType arrayType) {
        Type elementType = arrayType.getElementType();
        if (isPrimitiveType(elementType.getTag())) {
            tomlValue = ((TomlKeyValueNode) tomlValue).value();
            return getPrimitiveArray(tomlValue, variableName, arrayType);
        } else {
            return getNonPrimitiveArray(tomlValue, variableName, arrayType, elementType);
        }
    }

    private BArray getNonPrimitiveArray(TomlNode tomlValue, String variableName, ArrayType arrayType,
                                        Type elementType) {
        switch (elementType.getTag()) {
            case TypeTags.ARRAY_TAG:
                tomlValue = ((TomlKeyValueNode) tomlValue).value();
                return getPrimitiveArray(tomlValue, variableName, arrayType);
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return getMapValueArray(tomlValue, variableName, arrayType, elementType);
            case TypeTags.ANYDATA_TAG:
            case TypeTags.UNION_TAG:
                return getUnionValueArray(tomlValue, variableName, arrayType, elementType);
            default:
                Type effectiveType = ((IntersectionType) elementType).getEffectiveType();
                switch(effectiveType.getTag()) {
                    case TypeTags.ARRAY_TAG:
                        tomlValue = ((TomlKeyValueNode) tomlValue).value();
                        return getPrimitiveArray(tomlValue, variableName, arrayType);
                    case TypeTags.RECORD_TYPE_TAG:
                    case TypeTags.MAP_TAG:
                        return getMapValueArray(tomlValue, variableName, arrayType, effectiveType);
                    case TypeTags.ANYDATA_TAG:
                    case TypeTags.UNION_TAG:
                        return getUnionValueArray(tomlValue, variableName, arrayType, effectiveType);
                    default:
                        //this branch will not be executed
                        return null;
                }
        }
    }

    private BArray getMapValueArray(TomlNode tomlValue, String variableName, ArrayType arrayType, Type elementType) {
        List<TomlTableNode> tableNodeList = ((TomlTableArrayNode) tomlValue).children();
        int arraySize = tableNodeList.size();
        ListInitialValueEntry.ExpressionEntry[] entries = new ListInitialValueEntry.ExpressionEntry[arraySize];
        for (int i = 0; i < arraySize; i++) {
            Object value = createValue(tableNodeList.get(i), variableName, elementType);
            entries[i] = new ListInitialValueEntry.ExpressionEntry(value);
        }
        return new ArrayValueImpl(arrayType, entries.length, entries);
    }

    private BArray getUnionValueArray(TomlNode tomlValue, String variableName, ArrayType arrayType, Type elementType) {
        TomlValueNode valueNode = ((TomlKeyValueNode) tomlValue).value();
        ListInitialValueEntry.ExpressionEntry[] expressionEntries;
        List<TomlValueNode> arrayList = ((TomlArrayValueNode) valueNode).elements();
        expressionEntries = createArray(variableName, arrayList, elementType);
        return new ArrayValueImpl(arrayType, expressionEntries.length, expressionEntries);
    }

    private BArray getPrimitiveArray(TomlNode tomlValue, String variableName, ArrayType arrayType) {
        ListInitialValueEntry.ExpressionEntry[] expressionEntries;
        List<TomlValueNode> arrayList = ((TomlArrayValueNode) tomlValue).elements();
        expressionEntries = createArray(variableName, arrayList, arrayType.getElementType());
        return new ArrayValueImpl(arrayType, expressionEntries.length, expressionEntries);
    }

    private ListInitialValueEntry.ExpressionEntry[] createArray(String variableName, List<TomlValueNode> arrayList,
                                                                Type elementType) {
        int arraySize = arrayList.size();
        ListInitialValueEntry.ExpressionEntry[] arrayEntries =
                new ListInitialValueEntry.ExpressionEntry[arraySize];
        for (int i = 0; i < arraySize; i++) {
            Object balValue;
            TomlNode tomlValueNode = arrayList.get(i);

            switch (elementType.getTag()) {
                case TypeTags.INTERSECTION_TAG:
                    ArrayType arrayType = (ArrayType) ((BIntersectionType) elementType).getEffectiveType();
                    balValue = getPrimitiveArray(tomlValueNode, variableName, arrayType);
                    break;
                case TypeTags.ANYDATA_TAG:
                case TypeTags.UNION_TAG:
                    balValue = createUnionValue(tomlValueNode, variableName, (BUnionType) elementType);
                    break;
                default:
                    balValue = getBalValue(elementType, arrayList.get(i));
            }
            arrayEntries[i] = new ListInitialValueEntry.ExpressionEntry(balValue);
        }
        return arrayEntries;
    }

    private BMap<BString, Object> createRecordValue(TomlNode tomlNode, String variableName, Type type) {
        RecordType recordType;
        String recordName;
        if (type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            recordName = type.getName();
            recordType = (RecordType) type;
        } else {
            recordType = (RecordType) ((BIntersectionType) type).getConstituentTypes().get(0);
            recordName = recordType.getName();
        }
        TomlTableNode tomlValue = (TomlTableNode) tomlNode;
        Map<String, Object> initialValueEntries = new HashMap<>();
        for (Map.Entry<String, TopLevelNode> tomlField : tomlValue.entries().entrySet()) {
            String fieldName = tomlField.getKey();
            Field field = recordType.getFields().get(fieldName);
            TomlNode value = tomlField.getValue();
            if (field == null) {
                field = Utils.createAdditionalField(recordType, fieldName, value);
            }
            Object objectValue = createValue(value, variableName + "." + fieldName, field.getFieldType());
            initialValueEntries.put(fieldName, objectValue);
        }
        return ValueCreator.createReadonlyRecordValue(recordType.getPackage(), recordName, initialValueEntries);
    }

    private BTable<BString, Object> createTableValue(TomlNode tomlValue, String variableName, Type type) {
        TableType tableType;
        Type constraintType;
        if (type.getTag() == TypeTags.INTERSECTION_TAG) {
            tableType = (TableType) ((BIntersectionType) type).getEffectiveType();
            constraintType = tableType.getConstrainedType();
            if (constraintType.getTag() == TypeTags.INTERSECTION_TAG) {
                constraintType = ((IntersectionType) constraintType).getEffectiveType();
            }
            if (constraintType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                tableType = (TableType) ((BIntersectionType) type).getConstituentTypes().get(0);
            }
        } else {
            tableType = (TableType) type;
        }
        constraintType = tableType.getConstrainedType();
        List<TomlTableNode> tableNodeList = ((TomlTableArrayNode) tomlValue).children();
        int tableSize = tableNodeList.size();
        ListInitialValueEntry.ExpressionEntry[] tableEntries = new ListInitialValueEntry.ExpressionEntry[tableSize];
        String[] keys = tableType.getFieldNames();
        for (int i = 0; i < tableSize; i++) {
            Object value = createValue(tableNodeList.get(i), variableName, constraintType);
            tableEntries[i] = new ListInitialValueEntry.ExpressionEntry(value);
        }
        ArrayValue tableData =
                new ArrayValueImpl(TypeCreator.createArrayType(constraintType), tableSize, tableEntries);
        ArrayValue keyNames = keys == null ? (ArrayValue) ValueCreator.createArrayValue(new BString[]{}) :
                (ArrayValue) StringUtils.fromStringArray(keys);
        if (constraintType.getTag() == TypeTags.INTERSECTION_TAG) {
            constraintType = ((IntersectionType) constraintType).getEffectiveType();
        }
        return new TableValueImpl<>(TypeCreator.createTableType(constraintType, keys, true), tableData, keyNames);
    }

    private Object getBalValue(Type type, TomlValueNode tomlValueNode) {
        Object tomlValue = ((TomlBasicValueNode<?>) tomlValueNode).getValue();
        switch (type.getTag()) {
            case TypeTags.BYTE_TAG:
                return ((Long) tomlValue).intValue();
            case TypeTags.DECIMAL_TAG:
                return ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) tomlValue));
            case TypeTags.STRING_TAG:
                return StringUtils.fromString((String) tomlValue);
            default:
                return tomlValue;
        }
    }

    private BMap<BString, Object> createMapValue(TomlNode tomlValue, String variableName, MapType mapType) {
        TomlTableNode tomlTableValue = (TomlTableNode) tomlValue;
        MappingInitialValueEntry.KeyValueEntry[] keyValueEntries =
                new MappingInitialValueEntry.KeyValueEntry[tomlTableValue.entries().size()];
        int count = 0;
        for (Map.Entry<String, TopLevelNode> field : tomlTableValue.entries().entrySet()) {
            String fieldName = field.getKey();
            Object value =
                    createValue(field.getValue(), variableName + "." + fieldName, mapType.getConstrainedType());
            keyValueEntries[count] =
                    new MappingInitialValueEntry.KeyValueEntry(StringUtils.fromString(fieldName), value);
            count++;

        }
        return ValueCreator.createMapValue(mapType, keyValueEntries);
    }


    private Object createUnionValue(TomlNode tomlValue, String variableName, BUnionType unionType) {
        Object balValue = Utils.getBalValueFromToml(tomlValue, new HashSet<>());
        List<Type> convertibleTypes = new ArrayList<>();
        for (Type type : unionType.getMemberTypes()) {
            if (TypeChecker.checkIsLikeType(balValue, type, false)) {
                convertibleTypes.add(type);
            }
        }
        Type type = convertibleTypes.get(0);
        if (isPrimitiveType(type.getTag()) || type.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return balValue;
        }
        return createStructuredValue(tomlValue, variableName, type);
    }
}
