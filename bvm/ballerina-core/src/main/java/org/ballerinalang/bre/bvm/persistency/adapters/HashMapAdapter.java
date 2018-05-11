/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.bvm.persistency.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMapAdapter implements JsonSerializer<HashMap<String, Object>>, JsonDeserializer<HashMap<String, Object>> {

    public JsonElement serialize(HashMap<String, Object> map, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        for (String key : map.keySet()) {
            Object v = map.get(key);
            JsonObject wrapper = new JsonObject();
            if (v != null) {
                wrapper.add("objectClass", new JsonPrimitive(v.getClass().getName()));
                wrapper.add("data", context.serialize(v, v.getClass()));
            } else {
                wrapper.add("objectClass", new JsonPrimitive("NULL"));
            }
            result.add(key, wrapper);
        }

        return result;
    }

    public HashMap<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        HashMap<String, Object> map = new HashMap<String, Object>();
        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
        try {
            for (Map.Entry<String, JsonElement> entry : entries) {
                String key = entry.getKey();
                JsonObject wrapper = entry.getValue().getAsJsonObject();
                String className = wrapper.get("objectClass").getAsString();
                if ("NULL".equals(className)) {
                    map.put(key, null);
                } else {
                    JsonElement data = wrapper.get("data");
                    Object o = context.deserialize(data, Class.forName(className));
                    map.put(key, o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
