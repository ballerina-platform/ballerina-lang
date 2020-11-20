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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for Deserializing abstract Schema nodes accordingly.
 *
 * @since 2.0.0
 */
public class SchemaDeserializer implements JsonDeserializer<AbstractSchema> {

    public static final String TYPE = "type";
    public static final String OBJECT = "object";
    public static final String ARRAY = "array";
    public static final String INTEGER = "integer";
    public static final String NUMBER = "number";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String DESCRIPTION = "description";
    public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String PROPERTIES = "properties";
    public static final String ITEMS = "items";
    public static final String PATTERN = "pattern";
    public static final String MINIMUM = "minimum";
    public static final String MAXIMUM = "maximum";

    @Override
    public AbstractSchema deserialize(JsonElement jsonElement, java.lang.reflect.Type refType,
                                      JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObj = jsonElement.getAsJsonObject();
        String type = jsonObj.get(TYPE).getAsString();
        switch (type) {
            case OBJECT:
                return getObjectSchema(jsonDeserializationContext, jsonObj);
            case ARRAY:
                return getArraySchema(jsonDeserializationContext, jsonObj);
            case INTEGER:
                return getNumericSchema(jsonObj, Type.INTEGER);
            case NUMBER:
                return getNumericSchema(jsonObj, Type.NUMBER);
            case STRING:
                return getStringSchema(jsonObj);
            case BOOLEAN:
                return new BooleanSchema(Type.BOOLEAN);
            default:
                throw new JsonSchemaException(type + " is not supported type in json schema");
        }
    }

    private ObjectSchema getObjectSchema(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObj) {
        JsonElement descriptionProperty = jsonObj.get(DESCRIPTION);
        String description = descriptionProperty != null ? descriptionProperty.getAsString() : null;
        boolean additionalProperties = parseOptionalBooleanFromJson(jsonObj, ADDITIONAL_PROPERTIES);
        JsonObject properties = jsonObj.get(PROPERTIES).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = properties.entrySet();
        Map<String, AbstractSchema> propertiesList = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : entries) {
            String key = entry.getKey();
            AbstractSchema
                    abstractSchema = jsonDeserializationContext.deserialize(entry.getValue(), AbstractSchema.class);
            propertiesList.put(key, abstractSchema);
        }
        return new ObjectSchema(Type.OBJECT, description, additionalProperties, propertiesList);
    }

    private AbstractSchema getArraySchema(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObj) {
        JsonElement items = jsonObj.get(ITEMS).getAsJsonObject();
        AbstractSchema abstractSchema = jsonDeserializationContext.deserialize(items, AbstractSchema.class);
        return new ArraySchema(Type.ARRAY, abstractSchema);
    }

    private StringSchema getStringSchema(JsonObject jsonObj) {
        String pattern = parseOptionalStringFromJson(jsonObj, PATTERN);
        return new StringSchema(Type.STRING, pattern);
    }

    private NumericSchema getNumericSchema(JsonObject jsonObj, Type type) {
        Double minimum = parseOptionalDoubleFromJson(jsonObj, MINIMUM);
        Double maximum = parseOptionalDoubleFromJson(jsonObj, MAXIMUM);
        return new NumericSchema(type, minimum, maximum);
    }

    private Double parseOptionalDoubleFromJson(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsDouble();
        }
        throw new JsonSchemaException(key + " should always be a number");
    }

    private boolean parseOptionalBooleanFromJson(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return false;
        }

        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean()) {
            return jsonElement.getAsBoolean();
        }
        throw new JsonSchemaException(key + " should always be a boolean");
    }

    private String parseOptionalStringFromJson(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
            return jsonElement.getAsString();
        }
        throw new JsonSchemaException(key + " should always be a string");
    }
}


