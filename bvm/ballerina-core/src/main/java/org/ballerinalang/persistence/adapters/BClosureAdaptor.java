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
import org.ballerinalang.model.values.BClosure;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.Constants;

import java.lang.reflect.Type;

/**
 * This implements @{@link JsonSerializer} to serialize and deserialize @{@link BClosure} objects.
 *
 * @since 0.982.0
 */
public class BClosureAdaptor implements JsonSerializer<BClosure>, JsonDeserializer<BClosure> {
    @Override
    public JsonElement serialize(BClosure src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add(Constants.TYPE, new JsonPrimitive(src.value().getClass().getName()));
        result.add(Constants.DATA, context.serialize(src.value(), src.value().getClass()));
        return result;
    }

    @Override
    public BClosure deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get(Constants.TYPE).getAsString();
        JsonElement element = jsonObject.get(Constants.DATA);
        try {
            BValue bValue = context.deserialize(element, Class.forName(type));
            return new BClosure(bValue, bValue.getType());
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unknown element type found after deserialize the element : " + type, e);
        }
    }
}
