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

import com.google.gson.JsonArray;
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
import java.util.ArrayList;

/**
 * This implements @{@link JsonSerializer} to serialize and deserialize @{@link ArrayList} objects.
 *
 * @since 0.976.0
 */
public class ArrayListAdapter implements JsonSerializer<ArrayList<Object>>, JsonDeserializer<ArrayList<Object>> {

    public JsonElement serialize(ArrayList<Object> list, Type type, JsonSerializationContext context) {
        JsonArray result = new JsonArray();
        for (Object o : list) {
            JsonObject wrapper = new JsonObject();
            if (o != null) {
                wrapper.add(Constants.TYPE, new JsonPrimitive(o.getClass().getName()));
                wrapper.add(Constants.DATA, context.serialize(o, o.getClass()));
            } else {
                wrapper.add(Constants.TYPE, new JsonPrimitive(Constants.NULL));
            }
            result.add(wrapper);
        }
        return result;
    }

    public ArrayList<Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonArray jsonObject = json.getAsJsonArray();
        ArrayList<Object> list = new ArrayList<>();
        try {
            for (int i = 0; i < jsonObject.size(); i++) {
                JsonObject wrapper = jsonObject.get(i).getAsJsonObject();
                String className = wrapper.get(Constants.TYPE).getAsString();
                if (className.equals(Constants.NULL)) {
                    list.add(i, null);
                } else {
                    JsonElement data = wrapper.get(Constants.DATA);
                    Object o = context.deserialize(data, Class.forName(className));
                    list.add(i, o);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unknown element type found after deserialize the element.", e);
        }
        return list;
    }
}
