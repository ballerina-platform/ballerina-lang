/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package io.ballerina.toml.validator.schema;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for Deserializing abstract Schema nodes accordingly.
 *
 * @since 2.0.0
 */
public class SchemaDeserializer implements JsonDeserializer<Schema> {

    @Override
    public Schema deserialize(JsonElement jsonElement, java.lang.reflect.Type refType,
                              JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        JsonObject jsonObj = jsonElement.getAsJsonObject();
        String type = jsonObj.get("type").getAsString();
        switch (type) {
            case "object":
                return getObjectSchema(jsonDeserializationContext, jsonObj);
            case "array":
                return getArraySchema(jsonDeserializationContext, jsonObj);
            case "integer":
                return getNumericSchema(jsonObj, Type.INTEGER);
            case "number":
                return getNumericSchema(jsonObj, Type.NUMBER);
            case "string":
                return getStringSchema(jsonObj);
            case "boolean":
                return new BooleanSchema(Type.BOOLEAN);
            default:
                throw new JsonParseException("type " + type + " is not supported type");
        }
    }

    private ObjectSchema getObjectSchema(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObj) {
        JsonElement descriptionProperty = jsonObj.get("description");
        String description = descriptionProperty != null ? descriptionProperty.getAsString() : null;
        JsonElement additionalProperty = jsonObj.get("additionalProperties");
        boolean additionalProperties = additionalProperty == null || additionalProperty.getAsBoolean();
        JsonObject properties = jsonObj.get("properties").getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = properties.entrySet();
        Map<String, Schema> propertiesList = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : entries) {
            String key = entry.getKey();
            Schema schema = jsonDeserializationContext.deserialize(entry.getValue(), Schema.class);
            propertiesList.put(key, schema);
        }
        return new ObjectSchema(Type.OBJECT, description, additionalProperties, propertiesList);
    }

    private Schema getArraySchema(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObj) {
        JsonElement items = jsonObj.get("items").getAsJsonObject();
        Schema schema = jsonDeserializationContext.deserialize(items, Schema.class);
        return new ArraySchema(Type.ARRAY, schema);
    }

    private StringSchema getStringSchema(JsonObject jsonObj) {
        JsonElement patternProperty = jsonObj.get("pattern");
        String pattern = patternProperty != null ? patternProperty.getAsString() : null;
        return new StringSchema(Type.STRING, pattern);
    }

    private NumericSchema getNumericSchema(JsonObject jsonObj, Type type) {
        JsonElement minimumProperty = jsonObj.get("minimum");
        Double minimum = minimumProperty != null ? minimumProperty.getAsDouble() : null;
        JsonElement maximumProperty = jsonObj.get("maximum");
        Double maximum = maximumProperty != null ? maximumProperty.getAsDouble() : null;
        return new NumericSchema(type, minimum, maximum);
    }

}
