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

import com.google.common.collect.Lists;
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
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serialize @{@link SerializableState} into JSON and back.
 */
public class JsonSerializer implements StateSerializer, ObjectToJsonSerializer {

    @Override
    public byte[] serialize(SerializableState sState) {
        BRefType<?> jsonState = toBValue(sState, SerializableState.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        jsonState.serialize(outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public String serialize(Object object) {
        if (object == null) {
            return null;
        }
        BRefType<?> jsonObj = toBValue(object, object.getClass());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        jsonObj.serialize(outputStream);
        try {
            return outputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException(e);
        }
    }

    private BMap toBValue(Map<String, Object> source) {
        if (source == null) {
            return null;
        }

        BMap<String, BValue> target = new BMap<>();
        for (Map.Entry<String, Object> key : source.entrySet()) {
            target.put(key.getKey(), toBValue(key.getValue(), Object.class));
        }
        // due to type erasure any map<K, V> at runtime is just map<Object, Object>
        return wrapObject("map", target);
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
        return wrapObject("list", array);
    }

    private BMap toBValue(Enum obj) {
        String fullEnumName = obj.getClass().getSimpleName() + "." + obj.toString();
        BString name = new BString(fullEnumName);
        return wrapObject("enum", name);
    }

    private BRefType toBValue(Object obj, Class<?> leftSideType) {
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
            return new BString((String) obj);
        }
        if (obj instanceof Enum) {
            return toBValue((Enum) obj);
        }
        if (obj instanceof Map) {
            return toBValue((Map) obj);
        }
        if (obj instanceof List) {
            return toBValue((List) obj);
        }
        return convertToBValueViaReflection(obj, leftSideType);
    }

    private BMap convertToBValueViaReflection(Object obj, Class<?> leftSideType) {
        Class objClass = obj.getClass();
        BMap<String, BValue> map = new BMap<>();

        for (Field field : getAllFields(objClass)) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), toBValue(field.get(obj), field.getType()));
            } catch (IllegalAccessException e) {
                // field is set to be accessible
            }
        }

        if (leftSideType != objClass) {
            return wrapObject(objClass.getSimpleName(), map);
        } else {
            return map;
        }
    }

    private List<Field> getAllFields(Class clazz) {
        ArrayList<Field> fields = Lists.newArrayList(clazz.getDeclaredFields());

        for (Class parent = clazz.getSuperclass(); parent != Object.class; parent = parent.getSuperclass()) {
            fields.addAll(Lists.newArrayList(parent.getDeclaredFields()));
        }
        return fields;
    }

    private BMap wrapObject(String type, BValue payload) {
        BMap<String, BValue> map = new BMap<>();
        map.put("type", new BString(type));
        map.put("payload", payload);
        return map;
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
