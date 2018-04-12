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

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;
import org.ballerinalang.swagger.utils.GeneratorConstants;

import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class wraps the {@link Schema} from swagger models inorder to overcome complications
 * while populating handlebar templates.
 */
public class BallerinaSchema implements BallerinaSwaggerObject<BallerinaSchema, Schema> {
    private static final String LIST_SUFFIX = "List";
    private static final String OAS_PATH_SEPARATOR = "/";
    private static final String UNSUPPORTED_PROPERTY_MSG = "// Unsupported Property Found.";

    private Schema oasSchema;
    private Set<Map.Entry<String, Schema>> properties;

    @Override
    public BallerinaSchema buildContext(Schema schema) throws BallerinaOpenApiException {
        this.oasSchema = schema;

        // identify array type schema definitions
        if (schema instanceof ArraySchema) {
            this.properties = new LinkedHashSet<>();
            Schema propSchema = new Schema();

            String type = getReferenceType(((ArraySchema) schema).getItems().get$ref());
            String name = type.toLowerCase(Locale.ENGLISH) + LIST_SUFFIX;
            toPropertyName(name);
            type = type.isEmpty() ? UNSUPPORTED_PROPERTY_MSG : type + "[]";

            propSchema.setType(type);
            AbstractMap.SimpleEntry entry = new AbstractMap.SimpleEntry<>(name, propSchema);
            this.properties.add(entry);

            return this;
        }

        if (schema.getProperties() == null) {
            throw new BallerinaOpenApiException("Unsupported schema type in schema: " + schema.getName());
        }

        Set<Map.Entry<String, Schema>> entries = schema.getProperties().entrySet();
        Set<Map.Entry<String, Schema>> newEntries = new LinkedHashSet<>();

        // change conflicting swagger data types to ballerina data types
        for (Map.Entry<String, Schema> entry : entries) {
            Schema prop = entry.getValue();
            String name;

            // Extract reference type objects
            if (prop.get$ref() != null) {
                String type = getReferenceType(prop.get$ref());
                type = type.isEmpty() ? UNSUPPORTED_PROPERTY_MSG : type;
                name = toPropertyName(entry.getKey());
                prop.setType(type);
                newEntries.add(new AbstractMap.SimpleEntry<>(name, prop));
                continue;
            }

            name = toPropertyName(entry.getKey());
            prop.setType(getPropertyType(prop));
            newEntries.add(new AbstractMap.SimpleEntry<>(name, prop));
        }

        this.properties = newEntries;
        return this;
    }

    @Override
    public BallerinaSchema getDefaultValue() {
        return null;
    }

    private String getReferenceType(String refPath) {
        // null check on refPath is not required since swagger parser always make sure this is not null
        return refPath.substring(refPath.lastIndexOf(OAS_PATH_SEPARATOR) + 1);
    }

    private String getPropertyType(Schema prop) {
        String type;

        switch (prop.getType()) {
            case "integer":
                type = "int";
                break;
            case "number":
                type = "float";
                break;
            case "array":
                String ref = null;
                if (prop instanceof ArraySchema) {
                    ref = ((ArraySchema) prop).getItems().get$ref();
                }

                if (ref == null) {
                    type = getPropertyType(((ArraySchema) prop).getItems());
                } else {
                    type = getReferenceType(ref);
                }

                // define type with ballerina array syntax
                type += "[]";
                break;
            case "object":
                type = "any";
                break;
            default:
                type = prop.getType();
                break;
        }

        return type;
    }

    /**
     * Verify if {@code origName} is reserved ballerina keyword and prefix the {@code origName} with an '_'.
     * Ex: toPropertyName("type") will return "_type".
     *
     * @param origName original property name
     * @return keyword escaped property name
     */
    private String toPropertyName(String origName) {
        String escapedName = origName;
        boolean isKeyword = GeneratorConstants.RESERVED_KEYWORDS.stream().anyMatch(key -> key.equals(origName));
        if (isKeyword) {
            escapedName = '_' + origName;
        }

        return escapedName;
    }

    public Schema getOasSchema() {
        return oasSchema;
    }

    public Set<Map.Entry<String, Schema>> getProperties() {
        return properties;
    }

}
