/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.converters.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ballerina.converters.exception.JsonToRecordConverterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Infers the json schema for a given json object.
 *
 * Only handles features supported by OpenAPI schema.
 *
 * @since 2.0.0
 */
public class SchemaGenerator {

    private SchemaGenerator() {
        // not called
    }
    public static Map<String, Object> generate(JsonNode json) throws JsonToRecordConverterException {
        Map<String, Object> schema = new HashMap<>();

        if (json.getNodeType() == JsonNodeType.MISSING
                || json.getNodeType() == JsonNodeType.POJO || json.getNodeType() == JsonNodeType.BINARY) {
            throw new JsonToRecordConverterException(ErrorMessages.unsupportedType());
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
        if (json.getNodeType() == JsonNodeType.ARRAY && json instanceof ArrayNode array) {
            schema.put("type", "array");

            if (array.isEmpty()) {
                schema.put("items", new HashMap<String, Object>());
            } else {
                schema.put("items", generate(array.get(0)));
            }

            return schema;
        }

        if (json.getNodeType() == JsonNodeType.NULL) {
            schema.put("type", "anydata");
            return schema;
        }

        if (!(json instanceof ObjectNode object)) {
            throw new JsonToRecordConverterException(ErrorMessages.parserException(json.toString()));
        }
        // handle object type
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
