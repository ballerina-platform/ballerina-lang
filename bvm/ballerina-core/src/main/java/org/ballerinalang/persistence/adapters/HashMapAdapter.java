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
package org.ballerinalang.persistence.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.ballerinalang.runtime.Constants;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * This implements @{@link JsonSerializer} to serialize and deserialize @{@link HashMap} objects.
 *
 * @since 0.976.0
 */
public class HashMapAdapter implements JsonSerializer<HashMap<String, Object>>,
        JsonDeserializer<HashMap<String, Object>> {

    public JsonElement serialize(HashMap<String, Object> map, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        map.forEach((s, v) -> {
            JsonObject wrapper = new JsonObject();
            if (v != null) {
                wrapper.add(Constants.TYPE, new JsonPrimitive(v.getClass().getName()));
                wrapper.add(Constants.DATA, context.serialize(v, v.getClass()));
            } else {
                wrapper.add(Constants.TYPE, new JsonPrimitive(Constants.NULL));
            }
            result.add(s, wrapper);
        });
        return result;
    }

    public HashMap<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        HashMap<String, Object> map = new HashMap<>();
        jsonObject.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonObject wrapper = entry.getValue().getAsJsonObject();
            String className = wrapper.get(Constants.TYPE).getAsString();
            if (Constants.NULL.equals(className)) {
                map.put(key, null);
            } else {
                JsonElement data = wrapper.get(Constants.DATA);
                try {
                    map.put(key, context.deserialize(data, Class.forName(className)));
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("Unknown element type found after deserialize the element : " +
                                                         className, e);
                }
            }
        });
        return map;
    }
}
