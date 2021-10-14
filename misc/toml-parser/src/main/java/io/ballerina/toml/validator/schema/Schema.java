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
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlInlineTableValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.toml.validator.ValidationUtil.getTomlDiagnostic;
import static io.ballerina.toml.validator.ValidationUtil.getTypeErrorMessage;

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

    private static final String PROPERTY_HOLDER = "${property}";

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

    @Override
    public <T extends TomlNode> List<Diagnostic> validate(T givenValueNode, String key) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (givenValueNode.kind() == TomlType.INLINE_TABLE) {
            TomlInlineTableValueNode tomlInlineTableValueNode = (TomlInlineTableValueNode) givenValueNode;
            Map<String, AbstractSchema> properties = this.properties();
            List<String> requiredFields = getRequiredFields(this);
            for (TopLevelNode topLevelNode : tomlInlineTableValueNode.elements()) {
                String newKey = topLevelNode.key().name();
                AbstractSchema abstractSchema = properties.get(newKey);
                requiredFields.remove(newKey);
                if (abstractSchema != null) {
                    continue;
                }
                if (!this.hasAdditionalProperties()) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0001", "error.unexpected.property",
                            DiagnosticSeverity.ERROR);
                    TomlDiagnostic diagnostic = new TomlDiagnostic(topLevelNode.location(), diagnosticInfo,
                            getUnexpectedPropertyErrorMessage(newKey, key));
                    diagnostics.add(diagnostic);
                }
            }

            for (String field : requiredFields) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0006", "error.required.field.missing",
                        DiagnosticSeverity.ERROR);
                TomlDiagnostic diagnostic = new TomlDiagnostic(tomlInlineTableValueNode.location(), diagnosticInfo,
                        getRequiredErrorMessage(field));
                diagnostics.add(diagnostic);
            }
        } else if (givenValueNode.kind() == TomlType.TABLE) {
            TomlTableNode tomlTableNode = (TomlTableNode) givenValueNode;
            Map<String, AbstractSchema> properties = this.properties();
            List<String> requiredFields = getRequiredFields(this);
            Map<String, TopLevelNode> tableEntries = tomlTableNode.entries();
            for (Map.Entry<String, TopLevelNode> tableEntry : tableEntries.entrySet()) {
                String newKey = tableEntry.getKey();
                requiredFields.remove(newKey);
                TopLevelNode value = tableEntry.getValue();
                AbstractSchema schema = properties.get(newKey);
                if (schema != null) {
                    continue;
                }
                if (!this.hasAdditionalProperties()) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0001", "error.unexpected.property",
                            DiagnosticSeverity.ERROR);
                    TomlDiagnostic diagnostic = new TomlDiagnostic(value.location(), diagnosticInfo,
                            getUnexpectedPropertyErrorMessage(newKey, key));
                    diagnostics.add(diagnostic);
                }
            }
            for (String field : requiredFields) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0006", "error.required.field.missing",
                        DiagnosticSeverity.ERROR);
                TomlDiagnostic diagnostic = new TomlDiagnostic(tomlTableNode.location(), diagnosticInfo,
                        getRequiredErrorMessage(field));
                diagnostics.add(diagnostic);
            }
        } else {
            if (!givenValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(givenValueNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(this, givenValueNode.kind(),
                                key));
                return Collections.singletonList(diagnostic);
            }
            return Collections.emptyList();
        }

        return diagnostics;
    }

    private List<String> getRequiredFields(Schema objectSchema) {
        if (objectSchema.required() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(objectSchema.required());
    }

    private String getUnexpectedPropertyErrorMessage(String key, String parent) {
        Map<String, String> message = this.message();
        String typeCustomMessage = message.get(SchemaDeserializer.ADDITIONAL_PROPERTIES);
        if (parent == null) {
            parent = this.title();
        }
        if (typeCustomMessage == null) {
            return String.format("key '%s' not supported in schema '%s'", key, parent);
        }
        return typeCustomMessage.replace(PROPERTY_HOLDER, key);
    }

    private String getRequiredErrorMessage(String field) {
        Map<String, String> message = this.message();
        String typeCustomMessage = message.get(SchemaDeserializer.REQUIRED);
        if (typeCustomMessage == null) {
            return "missing required field '" + field + "'";
        }
        return typeCustomMessage.replace(PROPERTY_HOLDER, field);
    }
}
