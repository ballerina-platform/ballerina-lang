package io.ballerina.toml.validator;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SchemaDeserializer implements JsonDeserializer<Schema> {

    @Override
    public Schema deserialize(JsonElement jsonElement, Type refType,
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
                return getNumericSchema(jsonObj, TypeEnum.INTEGER);
            case "number":
                return getNumericSchema(jsonObj, TypeEnum.NUMBER);
            case "string":
                return getStringSchema(jsonObj);
            case "boolean":
                return new BooleanSchema(TypeEnum.BOOLEAN);
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
        return new ObjectSchema(TypeEnum.OBJECT, description, additionalProperties, propertiesList);
    }

    private Schema getArraySchema(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObj) {
        JsonElement items = jsonObj.get("items").getAsJsonObject();
        Schema schema = jsonDeserializationContext.deserialize(items, Schema.class);
        return new ArraySchema(TypeEnum.ARRAY, schema);
    }

    private StringSchema getStringSchema(JsonObject jsonObj) {
        JsonElement patternProperty = jsonObj.get("pattern");
        String pattern = patternProperty != null ? patternProperty.getAsString() : null;
        return new StringSchema(TypeEnum.STRING, pattern);
    }

    private NumericSchema getNumericSchema(JsonObject jsonObj, TypeEnum typeEnum) {
        JsonElement minimumProperty = jsonObj.get("minimum");
        Double minimum = minimumProperty != null ? minimumProperty.getAsDouble() : null;
        JsonElement maximumProperty = jsonObj.get("maximum");
        Double maximum = maximumProperty != null ? maximumProperty.getAsDouble() : null;
        return new NumericSchema(typeEnum, minimum, maximum);
    }

}
