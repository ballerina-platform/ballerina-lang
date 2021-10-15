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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    public static final String TITLE = "title";
    public static final String SCHEMA = "$schema";
    public static final String DESCRIPTION = "description";
    public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String PROPERTIES = "properties";
    public static final String REQUIRED = "required";
    public static final String ITEMS = "items";
    public static final String PATTERN = "pattern";
    public static final String MIN_LENGTH = "minLength";
    public static final String MAX_LENGTH = "maxLength";
    public static final String MINIMUM = "minimum";
    public static final String MAXIMUM = "maximum";
    public static final String MESSAGE = "message";
    public static final String DEFAULT_VALUE = "default";

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
                Map<String, String> customMessages = parseOptionalMapFromMessageJson(jsonObj);
                Boolean defaultValue = parseOptionalBooleanFromJson(jsonObj, DEFAULT_VALUE);
                return new BooleanSchema(Type.BOOLEAN, customMessages, defaultValue);
            default:
                throw new JsonSchemaException(type + " is not supported type in json schema");
        }
    }

    private Schema getObjectSchema(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObj) {
        JsonElement titleProp = jsonObj.get(TITLE);
        String title = titleProp != null ? titleProp.getAsString() : null;
        JsonElement schemaProp = jsonObj.get(SCHEMA);
        String schema = schemaProp != null ? schemaProp.getAsString() : null;
        JsonElement descriptionProperty = jsonObj.get(DESCRIPTION);
        String description = descriptionProperty != null ? descriptionProperty.getAsString() : null;
        boolean additionalProperties = parseOptionalBooleanFromJson(jsonObj, ADDITIONAL_PROPERTIES);
        JsonObject properties = jsonObj.get(PROPERTIES).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = properties.entrySet();
        Map<String, AbstractSchema> propertiesList = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : entries) {
            String key = entry.getKey();
            AbstractSchema
                    abstractSchema = jsonDeserializationContext.deserialize(entry.getValue(), AbstractSchema.class);
            propertiesList.put(key, abstractSchema);
        }
        List<String> requiredList = parseOptionalListFromJson(jsonObj, REQUIRED);
        Map<String, String> customMessages = parseOptionalMapFromMessageJson(jsonObj);
        return new Schema(schema, title, Type.OBJECT, customMessages, description, additionalProperties,
                propertiesList, requiredList);
    }

    private AbstractSchema getArraySchema(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObj) {
        JsonElement items = jsonObj.get(ITEMS).getAsJsonObject();
        AbstractSchema abstractSchema = jsonDeserializationContext.deserialize(items, AbstractSchema.class);
        Map<String, String> customMessages = parseOptionalMapFromMessageJson(jsonObj);
        return new ArraySchema(Type.ARRAY, customMessages, abstractSchema);
    }

    private StringSchema getStringSchema(JsonObject jsonObj) {
        String pattern = parseOptionalStringFromJson(jsonObj, PATTERN);
        String defaultValue = parseOptionalStringFromJson(jsonObj, DEFAULT_VALUE);
        Integer minLength = parseOptionalIntFromJson(jsonObj, MIN_LENGTH);
        Integer maxLength = parseOptionalIntFromJson(jsonObj, MAX_LENGTH);
        Map<String, String> customMessages = parseOptionalMapFromMessageJson(jsonObj);
        return new StringSchema(Type.STRING, customMessages, pattern, defaultValue, minLength, maxLength);
    }

    private NumericSchema getNumericSchema(JsonObject jsonObj, Type type) {
        Double minimum = parseOptionalDoubleFromJson(jsonObj, MINIMUM);
        Double maximum = parseOptionalDoubleFromJson(jsonObj, MAXIMUM);
        Double defaultValue = parseOptionalDoubleFromJson(jsonObj, DEFAULT_VALUE);
        Map<String, String> customMessages = parseOptionalMapFromMessageJson(jsonObj);
        return new NumericSchema(type, customMessages, minimum, maximum, defaultValue);
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

    private List<String> parseOptionalListFromJson(JsonObject jsonObject, String key) {
        JsonArray jsonArray = jsonObject.getAsJsonArray(key);
        if (jsonArray == null || jsonArray.isJsonNull()) {
            return new ArrayList<>();
        }
        if (!jsonArray.isJsonArray()) {
            throw new JsonSchemaException(key + " should always be an array");
        }
        List<String> list = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                list.add(jsonElement.getAsString());
            }
        }
        return list;
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

    private Integer parseOptionalIntFromJson(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsInt();
        }
        throw new JsonSchemaException(key + " should always be a int");
    }

    private Map<String, String> parseOptionalMapFromMessageJson(JsonObject jsonObject) {
        Map<String, String> customMessages = new LinkedHashMap<>();
        JsonObject customMessageJson = jsonObject.getAsJsonObject(SchemaDeserializer.MESSAGE);
        if (customMessageJson == null) {
            return new LinkedHashMap<>();
        }

        addFieldToCustomMessagesMap(customMessages, customMessageJson, TYPE);
        addFieldToCustomMessagesMap(customMessages, customMessageJson, PATTERN);
        addFieldToCustomMessagesMap(customMessages, customMessageJson, REQUIRED);
        addFieldToCustomMessagesMap(customMessages, customMessageJson, ADDITIONAL_PROPERTIES);
        addFieldToCustomMessagesMap(customMessages, customMessageJson, MINIMUM);
        addFieldToCustomMessagesMap(customMessages, customMessageJson, MAXIMUM);
        addFieldToCustomMessagesMap(customMessages, customMessageJson, MAX_LENGTH);
        addFieldToCustomMessagesMap(customMessages, customMessageJson, MIN_LENGTH);
        
        return customMessages;
    }

    private void addFieldToCustomMessagesMap(Map<String, String> customMessages, JsonObject customMessageJson,
                                             String field) {
        JsonElement type = customMessageJson.get(field);
        if (!(type == null || type.isJsonNull())) {
            String customTypeMessage = type.getAsString();
            customMessages.put(field, customTypeMessage);
        }
    }
}


