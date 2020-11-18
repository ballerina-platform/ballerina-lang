package io.ballerina.toml.validator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class RootSchema extends ObjectSchema {
    @SerializedName("$schema")
    private String schema;
    private String title;

    public RootSchema(TypeEnum type, String description, boolean additionalProperties,
                      Map<String, Schema> properties, String schema, String title) {
        super(type, description, additionalProperties, properties);
        this.schema = schema;
        this.title = title;
    }

    /**
     * Builds a Json schema from external file.
     * @param jsonPath path of the json schema file.
     * @return Parsed json schema object.
     * @throws IOException if the input is not resolved
     */
    public static RootSchema from(Path jsonPath) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Schema.class, new SchemaDeserializer()).create();
        BufferedReader reader = Files.newBufferedReader(jsonPath);
        return gson.fromJson(reader, RootSchema.class);
    }

    /**
     * Builds a Json schema from json string.
     * @param jsonContent string content of the json schema.
     * @return Parsed json schema object.
     */
    public static RootSchema from(String jsonContent) {
        Gson gson = new Gson();
        return gson.fromJson(jsonContent, RootSchema.class);
    }
}
