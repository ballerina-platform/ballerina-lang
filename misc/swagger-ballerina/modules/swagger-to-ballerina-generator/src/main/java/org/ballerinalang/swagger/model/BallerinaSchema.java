/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.swagger.model;

import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.Schema;

import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class wraps the {@link Schema} from swagger models inorder to overcome complications
 * while populating handlebar templates.
 */
public class BallerinaSchema {
    private static final String LIST_SUFFIX = "List";
    private static final String OAS_PATH_SEPARATOR = "/";
    private static final String UNSUPPORTED_PROPERTY_MSG = "// Unsupported Property Found.";

    private Schema oasSchema;
    private Set<Map.Entry<String, Schema>> properties;

    public BallerinaSchema buildFromSchema(Schema schema) {
        this.oasSchema = schema;

        // identify array type schema definitions
        if (schema instanceof ArraySchema) {
            properties = new LinkedHashSet<>();
            Schema propSchema = new Schema();

            String type = getReferenceType(((ArraySchema) schema).getItems().get$ref());
            String name = type.toLowerCase(Locale.ENGLISH) + LIST_SUFFIX;
            type = type.isEmpty() ? UNSUPPORTED_PROPERTY_MSG : type + "[]";

            propSchema.setType(type);
            AbstractMap.SimpleEntry entry = new AbstractMap.SimpleEntry<>(name, propSchema);
            properties.add(entry);

            return this;
        }

        Set<Map.Entry<String, Schema>> entries = schema.getProperties().entrySet();

        // change conflicting swagger data types to ballerina data types
        for (Map.Entry entry : entries) {
            Schema prop = (Schema) entry.getValue();

            // Extract reference type objects
            if (prop.get$ref() != null) {
                String type = getReferenceType(prop.get$ref());
                type = type.isEmpty() ? UNSUPPORTED_PROPERTY_MSG : type;
                prop.setType(type);
                continue;
            }

            switch (prop.getType()) {
                case "integer":
                    prop.setType("int");
                    break;
                case "number":
                    prop.setType("float");
                    break;
                case "object":
                    prop.setType("any");
                    break;
                default:
                    break;
            }
        }

        this.properties = entries;
        return this;
    }

    private String getReferenceType(String refPath) {
        // null check on refPath is not required since swagger parser always make this is not null
        return refPath.substring(refPath.lastIndexOf(OAS_PATH_SEPARATOR) + 1);
    }

    public Schema getOasSchema() {
        return oasSchema;
    }

    public Set<Map.Entry<String, Schema>> getProperties() {
        return properties;
    }
}
