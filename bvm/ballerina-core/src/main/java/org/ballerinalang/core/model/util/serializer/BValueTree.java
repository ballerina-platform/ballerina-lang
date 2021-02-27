/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model.util.serializer;

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.ENUM_SEPERATOR;

/**
 * Convert Java object graph into a tree of BValue objects.
 * <p>
 * Object reference sharing is tracked using an {@link IdentityHashMap},
 * repeated occurrences are marked by a link to previous occurrence.
 *
 * @since 0.982.0
 */
public class BValueTree implements BValueSerializer {
    private static final BValueProvider bValueProvider = BValueProvider.getInstance();
    private final BValueArrays bValueArrays;
    private final ObjectUID objectUID = new ObjectUID();

    BValueTree() {
        bValueArrays = new BValueArrays(this);
    }

    /**
     * Convert given Java object and it's references to {@link BValue} tree representation.
     *
     * @param src Java object to be converted
     * @return Converted {@link BValue} tree
     */
    BRefType toBValueTree(Object src) {
        BRefType tree = toBValue(src, src.getClass());
        BTreeHelper.trimTree(tree, objectUID.getRepeatedReferences());
        return tree;
    }

    public BRefType toBValue(Object src, Class<?> leftSideType) {
        if (src == null) {
            return null;
        }
        if (src instanceof String) {
            return BTreeHelper.createBString((String) src);
        }
        if (src.getClass().isArray()) {
            BRefType array = arrayFrom(src);
            if (array != null) {
                return array;
            }
        }
        if (src instanceof Character) {
            return new BInteger((long) (Character) src);
        }
        if (src instanceof Number) {
            BRefType num = numberToBValue(src);
            if (num != null) {
                return num;
            }
        }
        if (src instanceof Boolean) {
            return new BBoolean((Boolean) src);
        }
        if (src instanceof Enum) {
            return enumToBValue((Enum) src);
        }
        return convertReferenceSemanticObject(src, leftSideType);
    }

    private BMap mapToBValue(Map<Object, Object> source) {
        /*
         * Json dictionaries only allow strings to be keys, hence we have to transform original Map
         * so that we have some sort of Map<String, Value> representation.
         * Transformation:
         * Extract non-string typed key to a auxiliary dictionary as 'value'
         * and have an auto-generated *key* to represent the original complex key.
         * Finally add the auxiliary dictionary to target dictionary, using a special key.
         */
        BMap<String, BValue> target = new BMap<>();
        BMap<String, BValue> complexKeyMap = new BMap<>();
        for (Map.Entry<Object, Object> entry : source.entrySet()) {
            Object key = entry.getKey();
            BRefType serializedValue = toBValue(entry.getValue(), null);
            if (key instanceof String) {
                target.put((String) key, serializedValue);
            } else {
                BRefType serializedKey = toBValue(key, null);
                String complexKeyId = Long.toString(objectUID.findUID(key));
                target.put(complexKeyId, serializedValue);
                complexKeyMap.put(complexKeyId, serializedKey);
            }
        }
        if (!complexKeyMap.isEmpty()) {
            target.put(JsonSerializerConst.COMPLEX_KEY_MAP_TAG, complexKeyMap);
        }
        return BTreeHelper.wrapWithTypeMetadata(JsonSerializerConst.MAP_TAG, target);
    }

    private BMap<String, BValue> listToBValue(List list) {
        BValueArray array = new BValueArray(new BArrayType(BTypes.typeAny));
        for (Object item : list) {
            array.append(toBValue(item, null));
        }
        BMap<String, BValue> bMap = BTreeHelper.wrapWithTypeMetadata(JsonSerializerConst.LIST_TAG, array);
        bMap.put(JsonSerializerConst.LENGTH_TAG, new BInteger(list.size()));
        return bMap;
    }

    private BMap enumToBValue(Enum obj) {
        String fullEnumName = ObjectHelper.getTrimmedClassName(obj) + ENUM_SEPERATOR + obj.toString();
        BString name = BTreeHelper.createBString(fullEnumName);
        return BTreeHelper.wrapWithTypeMetadata(JsonSerializerConst.ENUM_TAG, name);
    }


    private BRefType arrayFrom(Object src) {
        Class<?> srcClass = src.getClass();
        if (srcClass == int[].class) {
            return bValueArrays.from((int[]) src);
        }
        if (srcClass == long[].class) {
            return bValueArrays.from((long[]) src);
        }
        if (srcClass == double[].class) {
            return bValueArrays.from((double[]) src);
        }
        if (srcClass == float[].class) {
            return bValueArrays.from((float[]) src);
        }
        if (srcClass == char[].class) {
            return bValueArrays.from((char[]) src);
        }
        if (srcClass == byte[].class) {
            return bValueArrays.from((byte[]) src);
        }
        if (srcClass == short[].class) {
            return bValueArrays.from((short[]) src);
        }
        if (srcClass == String[].class
                || srcClass == Integer[].class
                || srcClass == Long[].class
                || srcClass == Double[].class
                || srcClass == Float[].class
                || srcClass == Character[].class
                || srcClass == Byte[].class
                || srcClass == Short[].class) {
            return bValueArrays.from((Object[]) src);
        }
        return null;
    }

    private BRefType numberToBValue(Object src) {
        Class<?> srcClass = src.getClass();
        if (srcClass == Integer.class) {
            return new BInteger(((Integer) src).longValue());
        }
        if (srcClass == Long.class) {
            return new BInteger((Long) src);
        }
        if (srcClass == Float.class) {
            return new BFloat(((Float) src).doubleValue());
        }
        if (srcClass == Double.class) {
            return new BFloat((Double) src);
        }
        if (srcClass == Byte.class) {
            return new BInteger(((Byte) src).longValue());
        }
        if (srcClass == Short.class) {
            return new BInteger(((Short) src).intValue());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private BMap<String, BValue> convertReferenceSemanticObject(Object obj, Class<?> leftSideType) {
        if (objectUID.isTracked(obj)) {
            return createExistingReferenceNode(obj);
        }
        objectUID.track(obj);

        BMap<String, BValue> converted;
        String className = ObjectHelper.getTrimmedClassName(obj);
        SerializationBValueProvider provider = bValueProvider.find(className);
        if (provider != null) {
            converted = provider.toBValue(obj, this).toBMap();
        } else if (obj instanceof Map) {
            converted = mapToBValue((Map) obj);
        } else if (obj instanceof List) {
            converted = listToBValue((List) obj);
        } else if (obj.getClass().isArray()) {
            converted = arrayToBValue(obj);
        } else {
            converted = convertToBValueViaReflection(obj, leftSideType);
        }
        objectUID.addUID(obj, converted);
        return converted;
    }

    private BMap<String, BValue> arrayToBValue(Object array) {
        BValueArray bArray = new BValueArray(new BArrayType(BTypes.typeAny));
        int arrayLength = Array.getLength(array);
        for (int i = 0; i < arrayLength; i++) {
            bArray.append(toBValue(Array.get(array, i), null));
        }

        BMap<String, BValue> bMap = BTreeHelper.wrapWithTypeMetadata(JsonSerializerConst.ARRAY_TAG, bArray);
        bMap.put(JsonSerializerConst.LENGTH_TAG, new BInteger(arrayLength));
        Class<?> componentType = array.getClass().getComponentType();
        String trimmedName = ObjectHelper.getTrimmedClassName(componentType);
        bMap.put(JsonSerializerConst.COMPONENT_TYPE, BTreeHelper.createBString(trimmedName));
        return bMap;
    }

    private BMap<String, BValue> createExistingReferenceNode(Object obj) {
        BMap<String, BValue> map = new BMap<>();
        long objId = objectUID.findUID(obj);
        map.put(JsonSerializerConst.EXISTING_TAG, new BInteger(objId));
        objectUID.addRepeatedRef(objId);
        return map;
    }

    private BMap convertToBValueViaReflection(Object obj, Class<?> targetFieldType) {
        Class<?> objectType = obj.getClass();
        BMap<String, BValue> map = new BMap<>();
        HashMap<String, Field> allFields = ObjectHelper.getAllFields(objectType, 0);
        for (Map.Entry<String, Field> fieldEntry : allFields.entrySet()) {
            String fieldName = fieldEntry.getKey();
            Field field = fieldEntry.getValue();
            field.setAccessible(true);
            try {
                map.put(fieldName, toBValue(field.get(obj), field.getType()));
            } catch (IllegalAccessException e) {
                throw new BallerinaException(String.format("Error while reflective field access: %s.%s",
                        objectType.getName(), fieldName),
                        e);
            }
        }

        if (targetFieldType != objectType) {
            String className = ObjectHelper.getTrimmedClassName(obj);
            return BTreeHelper.wrapWithTypeMetadata(className, map);
        } else {
            return map;
        }
    }
}
