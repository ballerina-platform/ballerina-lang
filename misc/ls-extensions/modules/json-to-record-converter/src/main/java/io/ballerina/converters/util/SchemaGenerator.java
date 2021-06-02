package io.ballerina.converters.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ballerina.converters.exception.ConverterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Infers the json schema for a given json object.
 *
 * Only handles features supported by OpenAPI schema.
 */
public class SchemaGenerator {
    public static Map<String, Object> generate(JsonNode json) throws ConverterException {
        Map<String, Object> schema = new HashMap<>();

        if (json.getNodeType() == JsonNodeType.NULL || json.getNodeType() == JsonNodeType.MISSING
                || json.getNodeType() == JsonNodeType.POJO || json.getNodeType() == JsonNodeType.BINARY) {
            throw new ConverterException(ErrorMessages.unsupportedType());
        }

        // handle primitives
        if (json.getNodeType() == JsonNodeType.STRING) {
            schema.put("type", "string");
            return schema;
        }

        if (json.getNodeType() == JsonNodeType.BOOLEAN) {
            schema.put("type", "boolean");
            return schema;
        }

        if (json.getNodeType() == JsonNodeType.NUMBER) {
            if (json.isInt()) {
                schema.put("type", "integer");
            } else {
                schema.put("type", "number");
            }
            return schema;
        }

        // handle array type
        if (json.getNodeType() == JsonNodeType.ARRAY && json instanceof ArrayNode) {
            schema.put("type", "array");

            ArrayNode array = (ArrayNode) json;

            if (array.isEmpty()) {
                schema.put("items", new HashMap<String, Object>());
            } else {
                schema.put("items", generate(array.get(0)));
            }

            return schema;
        }

        if (!(json instanceof ObjectNode)) {
            throw new ConverterException(ErrorMessages.parserException(json.toString()));
        }
        // handle object type
        ObjectNode object = (ObjectNode) json;
        schema.put("type", "object");

        if (object.isEmpty()) {
            schema.put("properties", new HashMap<String, Object>());
        } else {
            Map<String, Object> properties = new HashMap<>();
            ArrayList<String> required = new ArrayList<>();
            for (Iterator<Map.Entry<String, JsonNode>> iterator = object.fields(); iterator.hasNext();) {
                Map.Entry<String, JsonNode> property = iterator.next();
                properties.put(property.getKey(), generate(property.getValue()));
                required.add(property.getKey());
            }
            schema.put("properties", properties);
            schema.put("required", required);
            schema.put("additionalProperties", false);
        }

        return schema;
    }

}
