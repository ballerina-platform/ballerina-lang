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
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.persistence.serializable.SerializableState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serialize @{@link SerializableState} into JSON and back.
 */
public class JsonSerializer implements StateSerializer {

    @Override
    public byte[] serialize(SerializableState sState) {
        JsonNode jsonState = convertToJson(sState);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            jsonState.serialize(outputStream);
        } catch (IOException e) {
            // it's ByteArrayOutputStream, no IO here.
        }

        return outputStream.toByteArray();
    }

    private JsonNode convertToJson(Map<String, Object> map, String valueType) {
        if (map == null) {
            return new JsonNode(JsonNode.Type.NULL);
        }
        JsonNode jsonMap = new JsonNode(JsonNode.Type.OBJECT);
        for (String key : map.keySet()) {
            Object value = map.get(key);
            jsonMap.set(key, convertToJson(value));
        }

        return wrapMap("string", valueType, jsonMap);
    }

    private JsonNode convertToJson(int[] array) {
        if (array == null) {
            return new JsonNode(JsonNode.Type.NULL);
        }
        JsonNode node = new JsonNode(JsonNode.Type.ARRAY);
        for (int i : array) {
            node.add(i);
        }
        return node;
    }

    private JsonNode convertToJson(long[] array) {
        if (array == null) {
            return new JsonNode(JsonNode.Type.NULL);
        }
        JsonNode node = new JsonNode(JsonNode.Type.ARRAY);
        for (long i : array) {
            node.add(i);
        }
        return node;
    }

    private JsonNode convertToJson(double[] array) {
        if (array == null) {
            return new JsonNode(JsonNode.Type.NULL);
        }
        JsonNode node = new JsonNode(JsonNode.Type.ARRAY);
        for (double i : array) {
            node.add(i);
        }
        return node;
    }

    private JsonNode convertToJson(String[] array) {
        if (array == null) {
            return new JsonNode(JsonNode.Type.NULL);
        }
        JsonNode node = new JsonNode(JsonNode.Type.ARRAY);
        for (String i : array) {
            node.add(i);
        }
        return node;
    }

    private JsonNode convertToJson(Byte[][] array) {
        if (array == null) {
            return new JsonNode(JsonNode.Type.NULL);
        }
        JsonNode node = new JsonNode(JsonNode.Type.ARRAY);
        for (Byte[] i : array) {
            node.add(convertToJson(i));
        }
        return node;
    }

    private JsonNode convertToJson(Byte[] array) {
        if (array == null) {
            return new JsonNode(JsonNode.Type.NULL);
        }
        JsonNode node = new JsonNode(JsonNode.Type.ARRAY);
        for (Byte i : array) {
            node.add(i);
        }
        return node;
    }

    private JsonNode convertToJson(List list) {
        JsonNode jsonArray = new JsonNode(JsonNode.Type.ARRAY);
        for (Object item : list) {
            jsonArray.add(convertToJson((Object) item));
        }
        return wrapObject("list", jsonArray);
    }


    private JsonNode convertToJson(Object obj) {
        if (obj == null) {
            return new JsonNode(JsonNode.Type.NULL);
        }
        if (obj instanceof int[]) {
            return convertToJson((int[]) obj);
        }
        if (obj instanceof long[]) {
            return convertToJson((long[]) obj);
        }
        if (obj instanceof double[]) {
            return convertToJson((double[]) obj);
        }
        if (obj instanceof String[]) {
            return convertToJson((String[]) obj);
        }
        if (obj instanceof Byte[][]) {
            return convertToJson((Byte[][]) obj);
        }
        if (obj instanceof Byte[]) {
            return convertToJson((Byte[]) obj);
        }
        if (obj instanceof Integer) {
            return new JsonNode(((Integer) obj).longValue());
        }
        if (obj instanceof Long) {
            return new JsonNode((Long) obj);
        }
        if (obj instanceof Float) {
            return new JsonNode(((Float) obj).doubleValue());
        }
        if (obj instanceof Double) {
            return new JsonNode((Double) obj);
        }
        if (obj instanceof Boolean) {
            return new JsonNode((Boolean) obj);
        }
        if (obj instanceof String) {
            return new JsonNode((String) obj);
        }
        if (obj instanceof Enum) {
            return convertToJson((Enum) obj);
        }
        return convertObjToJson(obj);
    }

    private JsonNode convertToJson(Enum obj) {
        String fullEnumName = obj.getClass().getSimpleName() + "." + obj.toString();
        JsonNode jsonNode = new JsonNode(fullEnumName);
        return wrapObject("enum", jsonNode);
    }

    private JsonNode convertObjToJson(Object obj) {
        if (obj == null) {
            return new JsonNode(JsonNode.Type.NULL);
        }
        if (obj instanceof Map) {
            return convertToJson((Map) obj, Object.class.getSimpleName());
        }
        if (obj instanceof List) {
            return convertToJson((List) obj);
        }
        return convertToJsonViaReflection(obj);
    }

    private JsonNode convertToJsonViaReflection(Object obj) {
        Class objClass = obj.getClass();
        JsonNode jsonNode = new JsonNode(JsonNode.Type.OBJECT);

        for (Field field : getAllFields(objClass)) {
            field.setAccessible(true);
            try {
                jsonNode.set(field.getName(), convertToJson(field.get(obj)));
            } catch (IllegalAccessException e) {
                // field is set to be accessible
            }
        }
        return wrapObject(objClass.getSimpleName(), jsonNode);
    }

    private List<Field> getAllFields(Class clazz) {
        ArrayList<Field> fields = Lists.newArrayList(clazz.getDeclaredFields());

        for (Class parent = clazz.getSuperclass(); parent != Object.class; parent = parent.getSuperclass()) {
            fields.addAll(Lists.newArrayList(parent.getDeclaredFields()));
        }
        return fields;
    }

    private JsonNode wrapObject(String type, JsonNode payload) {
        JsonNode objInfo = new JsonNode(JsonNode.Type.OBJECT);
        objInfo.set("type", type);
        objInfo.set("payload", payload);
        return objInfo;
    }

    private JsonNode wrapMap(String keyType, String valType, JsonNode payload) {
        JsonNode mapInfo = new JsonNode(JsonNode.Type.OBJECT);
        mapInfo.set("type", "map");
        mapInfo.set("keyType", keyType);
        mapInfo.set("valType", valType);
        mapInfo.set("payload", payload);
        return mapInfo;
    }

    @Override
    public SerializableState deserialize(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8);
        JsonNode jsonNode = JsonParser.parse(inputStreamReader);
        JsonDeserializer jsonDeserializer = new JsonDeserializer(jsonNode);
        return jsonDeserializer.deserialize();
    }
}
