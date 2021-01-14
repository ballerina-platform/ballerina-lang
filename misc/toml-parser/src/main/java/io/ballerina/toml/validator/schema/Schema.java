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
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Represents Root schema in JSON schema.
 *
 * @since 2.0.0
 */
public class Schema extends ObjectSchema {

    @SerializedName("$schema")
    private String schema;
    private String title;

    public Schema(String description, boolean additionalProperties,
                  Map<String, AbstractSchema> properties, String schema, String title, List<String> required) {
        super(Type.OBJECT, description, additionalProperties, properties, required);
        this.schema = schema;
        this.title = title;
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
        Schema rootSchema = gson.fromJson(reader, Schema.class);
        rootSchema.setType(Type.OBJECT);
        return rootSchema;
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
        Schema rootSchema = gson.fromJson(jsonContent, Schema.class);
        rootSchema.setType(Type.OBJECT);
        return rootSchema;
    }
}
