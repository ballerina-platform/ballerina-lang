/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.persistence.serializable.serializer;

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.BBooleanBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.BIntegerBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.BMapBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.BRefValueArrayBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.BStringBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.BallerinaBrokerByteBufBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.ClassBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.ConcurrentHashMapBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.NumericBValueProviders;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.persistence.serializable.serializer.BValueHelper.addHashValue;
import static org.ballerinalang.persistence.serializable.serializer.BValueHelper.createBString;
import static org.ballerinalang.persistence.serializable.serializer.BValueHelper.getHashCode;
import static org.ballerinalang.persistence.serializable.serializer.BValueHelper.wrapObject;

/**
 * Serialize @{@link SerializableState} into JSON and back.
 */
public class JsonSerializer implements ObjectToJsonSerializer, BValueSerializer {
    private final IdentityHashMap<Object, Object> identityMap = new IdentityHashMap<>();
    private final HashSet<String> repeatedReferenceSet = new HashSet<>();
    private final BValueProvider bValueProvider = BValueProvider.getInstance();
    private static final String BVALUE_PACKAGE_PATH = getBValuePackagePath();

    public JsonSerializer() {
        bValueProvider.register(new NumericBValueProviders.BigIntegerBValueProvider());
        bValueProvider.register(new NumericBValueProviders.BigDecimalBValueProvider());
        bValueProvider.register(new BStringBValueProvider());
        bValueProvider.register(new BRefValueArrayBValueProvider());
        bValueProvider.register(new BMapBValueProvider());
        bValueProvider.register(new ClassBValueProvider());
        bValueProvider.register(new BallerinaBrokerByteBufBValueProvider());
        bValueProvider.register(new ConcurrentHashMapBValueProvider());
        bValueProvider.register(new BIntegerBValueProvider());
        bValueProvider.register(new BBooleanBValueProvider());
    }

    private static String getBValuePackagePath() {
        return BValue.class.getPackage().getName();
    }

    /**
     * Get the BValueProvider associated with this JsonSerializer instance.
     * Use this instance to add SerializationBValueProvider implementations for this {@link JsonSerializer} instance.
     *
     * @return
     */
    public BValueProvider getBValueProviderRegistry() {
        return this.bValueProvider;
    }

    /**
     * Generate JSON serialized output from the given Java object instance.
     * @param object instance to be serialized
     * @return
     */
    @Override
    public String serialize(Object object) {
        try {
            if (object == null) {
                return null;
            }
            BRefType<?> jsonObj = toBValue(object, object.getClass());
            trimTree(jsonObj);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            jsonObj.serialize(outputStream);
            return outputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException(e);
        } finally {
            identityMap.clear();
            repeatedReferenceSet.clear();
        }
    }

    /**
     * Walk the object graph and remove the HASH code from nodes that does not repeat.
     *
     * @param jsonObj
     */
    @SuppressWarnings("unchecked")
    private void trimTree(BValue jsonObj) {
        if (jsonObj == null) {
            return;
        }
        if (jsonObj instanceof BMap) {
            BMap map = (BMap) jsonObj;
            BString bHashCode = (BString) map.get(JsonSerializerConst.HASH_TAG);
            if (bHashCode != null) {
                String hashCode = bHashCode.stringValue();
                if (!repeatedReferenceSet.contains(hashCode)) {
                    map.remove(JsonSerializerConst.HASH_TAG);
                }
            }
            trimTree(map.get(JsonSerializerConst.PAYLOAD_TAG));
        }
        if (jsonObj instanceof BRefValueArray) {
            BRefValueArray array = (BRefValueArray) jsonObj;
            for (int i = 0; i < array.size(); i++) {
                trimTree(array.get(i));
            }
        }
    }

    private BMap toBValue(Map<Object, Object> source) {
        if (source == null) {
            return null;
        }
        // Json dictionaries only allow strings to be keys, hence we have to transform original Map
        // so that we have some sort of Map<String, Value>.
        // Transformation:
        // extract non-string typed key[1] to a auxiliary dictionary
        // as 'value' and have an auto-generated key[2] to represent the original complex key[1].
        // Finally add the auxiliary dictionary to target dictionary, using a special key.
        BMap<String, BValue> target = new BMap<>();
        BMap<String, BValue> complexKeyMap = new BMap<>();
        for (Map.Entry<Object, Object> entry : source.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof String) {
                target.put((String) key, toBValue(entry.getValue(), Object.class));
            } else {
                String complexKeyHash = getHashCode(key, JsonSerializerConst.COMPLEX_KEY_TAG, null);
                target.put(complexKeyHash, toBValue(entry.getValue(), Object.class));
                complexKeyMap.put(complexKeyHash, toBValue(key, null));
            }
        }
        if (!complexKeyMap.isEmpty()) {
            target.put(JsonSerializerConst.COMPLEX_KEY_MAP_TAG, complexKeyMap);
        }
        return wrapObject(JsonSerializerConst.MAP_TAG, target);
    }

    private BIntArray toBValue(int[] array) {
        if (array == null) {
            return null;
        }
        BIntArray intArray = new BIntArray(array.length);
        for (int i = 0; i < array.length; i++) {
            intArray.add(i, array[i]);
        }
        return intArray;
    }

    private BIntArray toBValue(long[] array) {
        if (array == null) {
            return null;
        }
        return new BIntArray(array);
    }

    private BFloatArray toBValue(double[] array) {
        if (array == null) {
            return null;
        }
        return new BFloatArray(array);
    }

    private BStringArray toBValue(String[] array) {
        if (array == null) {
            return null;
        }
        return new BStringArray(array);
    }

    private BRefValueArray toBValue(Byte[][] array) {
        if (array == null) {
            return null;
        }
        BByteArray[] byteArrays = new BByteArray[array.length];
        for (int i = 0; i < array.length; i++) {
            byteArrays[i] = toBValue(array[i]);
        }
        return new BRefValueArray(byteArrays, new BArrayType(BTypes.typeByte));
    }

    private BByteArray toBValue(Byte[] array) {
        if (array == null) {
            return null;
        }
        BByteArray byteArray = new BByteArray(array.length);
        for (int i = 0; i < array.length; i++) {
            byteArray.add(i, array[i]);
        }
        return byteArray;
    }

    private BMap<String, BValue> toBValue(List list) {
        BRefValueArray array = new BRefValueArray(BTypes.typeAny);
        for (Object item : list) {
            array.append(toBValue(item, Object.class));
        }
        return wrapObject(JsonSerializerConst.LIST_TAG, array);
    }

    private BMap toBValue(Enum obj) {
        String fullEnumName = getTrimmedClassName(obj) + "." + obj.toString();
        BString name = createBString(fullEnumName);
        return wrapObject(JsonSerializerConst.ENUM_TAG, name);
    }

    public BRefType toBValue(Object obj, Class<?> leftSideType) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof int[]) {
            return toBValue((int[]) obj);
        }
        if (obj instanceof long[]) {
            return toBValue((long[]) obj);
        }
        if (obj instanceof double[]) {
            return toBValue((double[]) obj);
        }
        if (obj instanceof String[]) {
            return toBValue((String[]) obj);
        }
        if (obj instanceof Byte[][]) {
            return toBValue((Byte[][]) obj);
        }
        if (obj instanceof Byte[]) {
            return toBValue((Byte[]) obj);
        }
        if (obj instanceof Byte) {
            return new BInteger(((Byte) obj).longValue());
        }
        if (obj instanceof Character) {
            return new BInteger((long) ((Character) obj).charValue());
        }
        if (obj instanceof Integer) {
            return new BInteger(((Integer) obj).longValue());
        }
        if (obj instanceof Long) {
            return new BInteger((Long) obj);
        }
        if (obj instanceof Float) {
            return new BFloat(((Float) obj).doubleValue());
        }
        if (obj instanceof Double) {
            return new BFloat((Double) obj);
        }
        if (obj instanceof Boolean) {
            return new BBoolean((Boolean) obj);
        }
        if (obj instanceof String) {
            return createBString((String) obj);
        }
        if (obj instanceof Enum) {
            return toBValue((Enum) obj);
        }
        return convertReferenceSemanticObject(obj, leftSideType);
    }

    @SuppressWarnings("unchecked")
    private BMap convertReferenceSemanticObject(Object obj, Class<?> leftSideType) {
        if (identityMap.containsKey(obj)) {
            return createExistingReferenceNode(obj);
        }
        identityMap.put(obj, obj);

        String className = getTrimmedClassName(obj);
        SerializationBValueProvider provider = bValueProvider.find(className);
        if (provider != null) {
            BMap<String, BValue> converted = (BMap<String, BValue>) provider.toBValue(obj, this);
            addHashValue(obj, converted);
            return converted;
        }
        if (obj instanceof Map) {
            BMap map = toBValue((Map) obj);
            addHashValue(obj, map);
            return map;
        }
        if (obj instanceof List) {
            BMap<String, BValue> map = toBValue((List) obj);
            addHashValue(obj, map);
            return map;
        }
        if (obj.getClass().isArray()) {
            BMap<String, BValue> map = arrayToBValue(obj);
            addHashValue(obj, map);
            return map;
        }

        BMap map = convertToBValueViaReflection(obj, leftSideType);
        addHashValue(obj, map);
        return map;
    }

    private BMap<String, BValue> arrayToBValue(Object array) {
        BRefValueArray bArray = new BRefValueArray(BTypes.typeAny);
        int arrayLength = Array.getLength(array);
        for (int i = 0; i < arrayLength; i++) {
            bArray.append(toBValue(Array.get(array, i), null));
        }

        BMap<String, BValue> bMap = wrapObject(JsonSerializerConst.ARRAY_TAG, bArray);
        Class<?> componentType = array.getClass().getComponentType();
        String trimmedName = getTrimmedClassName(componentType);
        bMap.put(JsonSerializerConst.COMPONENT_TYPE, createBString(trimmedName));
        return bMap;
    }

    static String getTrimmedClassName(Object obj) {
        Class<?> clazz = obj.getClass();
        return getTrimmedClassName(clazz);
    }

    static String getTrimmedClassName(Class<?> clazz) {
        String className = clazz.getName();
        // if obj is a BValue, trim fully qualified name to class name.
        if (BValue.class.isAssignableFrom(clazz) && className.startsWith(BVALUE_PACKAGE_PATH)) {
            className = className.substring(BVALUE_PACKAGE_PATH.length() + 1);
        }
        return className;
    }

    private BMap<String, BValue> createExistingReferenceNode(Object obj) {
        BMap<String, BValue> map = new BMap<>();
        BString hashCode = getHashCode(obj);
        map.put(JsonSerializerConst.EXISTING_TAG, hashCode);
        repeatedReferenceSet.add(hashCode.stringValue());
        return map;
    }

    private BMap convertToBValueViaReflection(Object obj, Class<?> leftSideType) {
        Class<?> objClass = obj.getClass();
        BMap<String, BValue> map = new BMap<>();
        HashMap<String, Field> allFields = ObjectHelper.getAllFields(objClass, 0);
        for (Map.Entry<String, Field> fieldEntry : allFields.entrySet()) {
            String fieldName = fieldEntry.getKey();
            Field field = fieldEntry.getValue();
            field.setAccessible(true);
            try {
                map.put(fieldName, toBValue(field.get(obj), field.getType()));
            } catch (IllegalAccessException e) {
                // field is set to be accessible
            }
        }

        if (leftSideType != objClass) {
            String className = getTrimmedClassName(obj);
            return wrapObject(className, map);
        } else {
            return map;
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> destinationType) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8);
        BRefType<?> objTree = JsonParser.parse(inputStreamReader);
        JsonDeserializer jsonDeserializer = new JsonDeserializer(objTree);
        return destinationType.cast(jsonDeserializer.deserialize(destinationType));
    }
}
