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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.EmailSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.PasswordSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;
import org.ballerinalang.swagger.utils.GeneratorConstants;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
    private String type;
    private Set<Map.Entry<String, Schema>> properties;

    @Override
    public BallerinaSchema buildContext(Schema schema, OpenAPI openAPI) throws BallerinaOpenApiException {
        this.oasSchema = schema;

        // identify array type schema definitions
        if (schema instanceof ArraySchema) {
            extractArraySchema((ArraySchema) schema);
            return this;
        } else if (schema instanceof ComposedSchema) {
            extractComposedSchema((ComposedSchema) schema, openAPI);
            return this;
        } else if (isValueTypeSchema(schema)) {
            this.type = getPropertyType(schema);
            return this;
        } else if (schema.get$ref() != null) {
            String refType = getReferenceType(schema.get$ref());
            this.type = refType;
            schema = openAPI.getComponents().getSchemas().get(refType);
        } else if (schema.getProperties() == null) {
            if (schema.getType() == null) {
                throw new BallerinaOpenApiException("Unsupported schema type in schema: " + schema.getName());
            }

            this.type = getPropertyType(schema);
            return this;
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
    public BallerinaSchema buildContext(Schema schema) throws BallerinaOpenApiException {
        return buildContext(schema, null);
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

    private void extractArraySchema(ArraySchema schema) {
        this.properties = new LinkedHashSet<>();
        Schema propSchema = new Schema();
        String type;

        if (schema.getItems().get$ref() != null) {
            type = getReferenceType(schema.getItems().get$ref());
        } else {
            type = schema.getItems().getType();
        }
        String name = type.toLowerCase(Locale.ENGLISH) + LIST_SUFFIX;
        name = toPropertyName(name);
        type = type.isEmpty() ? UNSUPPORTED_PROPERTY_MSG : type + "[]";

        propSchema.setType(type);
        this.type = type; // this schema type is an array of some type
        AbstractMap.SimpleEntry entry = new AbstractMap.SimpleEntry<>(name, propSchema);
        this.properties.add(entry);
    }

    private void extractComposedSchema(ComposedSchema cSchema, OpenAPI openAPI) throws BallerinaOpenApiException {
        this.properties = new LinkedHashSet<>();
        List<Schema> allOf = cSchema.getAllOf();

        List<BallerinaSchema> childSchemas = new ArrayList<>();
        for (Schema schema: allOf) {
            childSchemas.add(new BallerinaSchema().buildContext(schema, openAPI));
        }

        childSchemas.forEach(schema -> this.properties.addAll(schema.getProperties()));
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

    private boolean isValueTypeSchema(Schema schema) {
        // its easier to check what it is not but its not possible here as there are
        // Schema type definitions as well
        return (schema instanceof BinarySchema || schema instanceof BooleanSchema || schema instanceof ByteArraySchema
                || schema instanceof DateSchema || schema instanceof DateTimeSchema || schema instanceof EmailSchema
                || schema instanceof FileSchema || schema instanceof IntegerSchema || schema instanceof BooleanSchema
                || schema instanceof MapSchema || schema instanceof NumberSchema || schema instanceof PasswordSchema
                || schema instanceof StringSchema);
    }

    public Schema getOasSchema() {
        return oasSchema;
    }

    public Set<Map.Entry<String, Schema>> getProperties() {
        return properties;
    }

    public String getType() {
        return type;
    }
}
