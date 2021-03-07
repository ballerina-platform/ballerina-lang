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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents Object schema in JSON schema.
 *
 * @since 2.0.0
 */
public class Schema extends AbstractSchema {
    private final String schema;
    private final String title;
    private final String description;
    private final boolean hasAdditionalProperties;
    private final Map<String, AbstractSchema> properties;
    private final List<String> required;

    public Schema(String schema, String title, Type type, Map<String, String> message, String description,
                  boolean hasAdditionalProperties,
                  Map<String, AbstractSchema> properties, List<String> required) {
        super(type, message);
        this.schema = schema;
        this.title = title;
        this.description = description;
        this.hasAdditionalProperties = hasAdditionalProperties;
        this.properties = properties;
        this.required = required;
    }

    /**
     * Builds a Json schema from external file.
     *
     * @param jsonPath path of the json schema file.
     * @return Parsed json schema object.
     * @throws IOException if the input is not resolved
     */
    public static Schema from(Path jsonPath) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(AbstractSchema.class, new SchemaDeserializer()).create();
        BufferedReader reader = Files.newBufferedReader(jsonPath);
        return (Schema) gson.fromJson(reader, AbstractSchema.class);
    }

    /**
     * Builds a Json schema from json string.
     *
     * @param jsonContent string content of the json schema.
     * @return Parsed json schema object.
     */
    public static Schema from(String jsonContent) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(AbstractSchema.class, new SchemaDeserializer()).create();
        return (Schema) gson.fromJson(jsonContent, AbstractSchema.class);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public boolean hasAdditionalProperties() {
        return hasAdditionalProperties;
    }

    public Map<String, AbstractSchema> properties() {
        return properties;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    public List<String> required() {
        return required;
    }

    public String title() {
        return title;
    }

    public String schema() {
        return schema;
    }
}
