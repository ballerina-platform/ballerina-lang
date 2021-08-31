/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.runtime.internal.values.TupleValueImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.ballerina.runtime.internal.TypeChecker.getType;

/**
 * Class @{@link SingletonUtils} provides utility methods to create Ballerina Singleton Values.
 *
 * @since 2.0.0
 */
public class SingletonUtils {

    private static List<BMap> mapValues = new ArrayList<>();
    private static List<BTable> tableValues = new ArrayList<>();

    private SingletonUtils() {
    }

    /**
     * Provide the singleton type depending on the type of a value.
     *
     * @param value value object
     * @return the suitable singleton type
     */
    public static Type createSingletonType(Object value, Type type) {
        Type returnType;
        int typeTag = type.getTag();
        int typeFlags = getTypeFlags(type);
        switch (typeTag) {
            case TypeTags.NULL_TAG:
                return PredefinedTypes.TYPE_NULL;
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
            case TypeTags.FINITE_TYPE_TAG:
                returnType = TypeCreator.createFiniteType(value.toString(), Set.of(value), typeFlags);
                break;
            case TypeTags.ARRAY_TAG:
                returnType = createArraySingletonType((ArrayValueImpl) value, type);
                break;
            case TypeTags.TUPLE_TAG:
                returnType = createTupleSingletonType((TupleValueImpl) value, type);
                break;
//  TODO: Uncomment these after fixing the failing tests
//            case TypeTags.MAP_TAG:
//            case TypeTags.RECORD_TYPE_TAG:
//                returnType = createMapSingletonType((MapValueImpl) value, type);
//                mapValues.clear();
//                break;
//            case TypeTags.TABLE_TAG:
//                returnType = createTableSingletonType((TableValueImpl) value, (TableType) type);
//                tableValues.clear();
//                break;
            default:
                returnType = type;
        }
        return returnType;
    }

    private static Type createTableSingletonType(TableValueImpl value, TableType type) {
        if (tableValues.contains(value)) {
            return TypeCreator.createFiniteType(type.getName(), Set.of(value), getTypeFlags(type));
        }
        tableValues.add(value);
        int size = value.size();
        ListInitialValueEntry.ExpressionEntry[] tableEntries = new ListInitialValueEntry.ExpressionEntry[size];
        int count = 0;
        for (Object entry : value.values()) {
            Object fieldValue = entry;
            Type newFieldType = createSingletonType(fieldValue, getType(fieldValue));
            if (newFieldType.getTag() != TypeTags.NULL_TAG) {
                fieldValue = ((BFiniteType) newFieldType).getValueSpace().iterator().next();
            }
            tableEntries[count++] = new ListInitialValueEntry.ExpressionEntry(fieldValue);
        }
        ArrayValue tableData =
                new ArrayValueImpl(TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY), size, tableEntries);
        String[] fieldNames = type.getFieldNames();
        TableType tableType = TypeCreator.createTableType(PredefinedTypes.TYPE_MAP, fieldNames, true);
        BTable tableValue =
                ValueCreator.createTableValue(tableType, tableData, StringUtils.fromStringArray(fieldNames));
        return TypeCreator.createFiniteType(type.getName(), Set.of(tableValue), getTypeFlags(type));
    }

    private static Type createMapSingletonType(MapValueImpl value, Type type) {
        if (mapValues.contains(value)) {
            return TypeCreator.createFiniteType(type.getName(), Set.of(value), getTypeFlags(type));
        }
        mapValues.add(value);
        MappingInitialValueEntry.KeyValueEntry[] keyValues =
                new MappingInitialValueEntry.KeyValueEntry[value.size()];
        int count = 0;
        for (Object field : value.getKeys()) {
            BString fieldName = (BString) field;
            Object fieldValue = value.get(fieldName);
            Type newFieldType = createSingletonType(fieldValue, getType(fieldValue));
            if (newFieldType.getTag() != TypeTags.NULL_TAG) {
                fieldValue = ((BFiniteType) newFieldType).getValueSpace().iterator().next();
            }
            keyValues[count++] =
                    (MappingInitialValueEntry.KeyValueEntry) ValueCreator.createKeyFieldEntry(fieldName, fieldValue);

        }
        String name = type.getName();
        MapType mapType = TypeCreator.createMapType(name, PredefinedTypes.TYPE_ANY, type.getPackage(), true);
        BMap<BString, Object> mapValue = new MapValueImpl<>(mapType, keyValues);
        return TypeCreator.createFiniteType(name, Set.of(mapValue), getTypeFlags(type));
    }

    private static Type createTupleSingletonType(TupleValueImpl value, Type type) {
        Object[] values = value.getValues();
        List<Type> typeList = new ArrayList<>();
        int typeFlags = getTypeFlags(type);
        for (int i = 0; i < value.size(); i++) {
            if (values[i] == null) {
                typeList.add(PredefinedTypes.TYPE_NULL);
            } else {
                typeList.add(createSingletonType(values[i], getType(values[i])));
            }
        }
        TupleType tupleType = TypeCreator.createTupleType(typeList, typeFlags);
        TupleValueImpl tupleValue = new TupleValueImpl(values, tupleType);
        return TypeCreator.createFiniteType(tupleValue.toString(), Set.of(tupleValue), typeFlags);
    }

    private static Type createArraySingletonType(ArrayValueImpl value, Type type) {
        List<Type> typeList = new ArrayList<>();
        int size = value.size();
        Object[] values = new Object[size];
        int typeFlags = getTypeFlags(type);
        switch (value.getElementType().getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                long[] intValues = value.getIntArray();
                for (int i = 0; i < size; i++) {
                    typeList.add(TypeCreator
                            .createFiniteType(String.valueOf(intValues[i]), Set.of(intValues[i]), 0));
                    values[i] = intValues[i];
                }
                break;
            case TypeTags.BOOLEAN_TAG:
                boolean[] booleanValues = value.getBooleanArray();
                for (int i = 0; i < size; i++) {
                    typeList.add(TypeCreator
                            .createFiniteType(String.valueOf(booleanValues[i]), Set.of(booleanValues[i]), 0));
                    values[i] = booleanValues[i];
                }
                break;
            case TypeTags.BYTE_TAG:
                byte[] byteValues = value.getByteArray();
                for (int i = 0; i < size; i++) {
                    typeList.add(TypeCreator.createFiniteType(String.valueOf(byteValues[i]), Set.of(byteValues[i]), 0));
                    values[i] = byteValues[i];
                }
                break;
            case TypeTags.FLOAT_TAG:
                double[] floatValues = value.getFloatArray();
                for (int i = 0; i < size; i++) {
                    typeList.add(
                            TypeCreator.createFiniteType(String.valueOf(floatValues[i]), Set.of(floatValues[i]), 0));
                    values[i] = floatValues[i];
                }
                break;
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
                for (int i = 0; i < size; i++) {
                    BString bString = value.getBString(i);
                    typeList.add(
                            TypeCreator.createFiniteType(bString.getValue(), Set.of(bString), 0));
                    values[i] = bString;
                }
                break;
            default:
                for (int i = 0; i < size; i++) {
                    Object[] refValues = value.getValues();
                    if (refValues[i] == null) {
                        typeList.add(PredefinedTypes.TYPE_NULL);
                    } else {
                        typeList.add(createSingletonType(refValues[i], getType(refValues[i])));
                    }
                    values[i] = refValues[i];
                }
                break;
        }
        TupleType tupleType = TypeCreator.createTupleType(typeList, typeFlags);
        TupleValueImpl tupleValue = new TupleValueImpl(values, tupleType);
        return TypeCreator.createFiniteType(tupleValue.toString(), Set.of(tupleValue), 0);
    }

    /**
     * Provide the similar flags as a parent type.
     *
     * @return generated flags
     */
    private static int getTypeFlags(Type type) {
        int flags = (int) type.getFlags();
        if (type.isNilable()) {
            flags = TypeFlags.addToMask(flags, TypeFlags.NILABLE);
        }
        if (type.isAnydata()) {
            flags = TypeFlags.addToMask(flags, TypeFlags.ANYDATA);
        }
        if (type.isPureType()) {
            flags = TypeFlags.addToMask(flags, TypeFlags.PURETYPE);
        }
        return flags;
    }
}
