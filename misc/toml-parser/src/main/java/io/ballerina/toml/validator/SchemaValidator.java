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

package io.ballerina.toml.validator;

import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBooleanValueNode;
import io.ballerina.toml.semantic.ast.TomlDoubleValueNodeNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.ast.TomlNodeVisitor;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.validator.schema.AbstractSchema;
import io.ballerina.toml.validator.schema.ArraySchema;
import io.ballerina.toml.validator.schema.NumericSchema;
import io.ballerina.toml.validator.schema.ObjectSchema;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.toml.validator.schema.StringSchema;
import io.ballerina.toml.validator.schema.Type;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Visitor to validate toml object against rules in json schema.
 *
 * @since 2.0.0
 */
public class SchemaValidator extends TomlNodeVisitor {

    private AbstractSchema schema;
    private String key;

    public SchemaValidator(Schema schema) {
        this.schema = schema;
    }

    @Override
    public void visit(TomlTableNode tomlTableNode) {
        if (schema.type() != Type.OBJECT) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlTableNode.location(), "TVE0002", "error.invalid.type",
                    DiagnosticSeverity.ERROR, String.format("Key \"%s\" expects %s . Found object", this.key,
                            schema.type()));
            tomlTableNode.addDiagnostic(diagnostic);
            return;
        }
        ObjectSchema objectSchema = (ObjectSchema) schema;
        Map<String, AbstractSchema> properties = objectSchema.properties();
        List<String> requiredFields = getRequiredFields(objectSchema);
        Map<String, TopLevelNode> tableEntries = tomlTableNode.entries();
        for (Map.Entry<String, TopLevelNode> tableEntry : tableEntries.entrySet()) {
            String key = tableEntry.getKey();
            requiredFields.remove(key);
            TopLevelNode value = tableEntry.getValue();
            AbstractSchema schema = properties.get(key);
            if (schema != null) {
                visitNode(value, schema, key);
                continue;
            }
            if (!objectSchema.hasAdditionalProperties()) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0001", "error.unexpected.property",
                        DiagnosticSeverity.ERROR);
                TomlDiagnostic diagnostic = new TomlDiagnostic(value.location(), diagnosticInfo,
                        "Unexpected Property \"" + key + "\"");
                tomlTableNode.addDiagnostic(diagnostic);
            }
        }
        for (String field : requiredFields) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0006", "error.required.field.missing",
                    DiagnosticSeverity.ERROR);
            TomlDiagnostic diagnostic = new TomlDiagnostic(tomlTableNode.location(), diagnosticInfo,
                    "Missing required field \"" + field + "\"");
            tomlTableNode.addDiagnostic(diagnostic);
        }
    }

    @Override
    public void visit(TomlTableArrayNode tomlTableArrayNode) {
        if (schema.type() != Type.ARRAY) {
            TomlDiagnostic diagnostic =
                    getTomlDiagnostic(tomlTableArrayNode.location(), "TVE0002", "error.invalid.type",
                            DiagnosticSeverity.ERROR, String.format("Key \"%s\" expects %s . Found array", this.key,
                                    schema.type()));
            tomlTableArrayNode.addDiagnostic(diagnostic);
            return;
        }
        ArraySchema arraySchema = (ArraySchema) schema;
        AbstractSchema memberSchema = arraySchema.items();
        List<TomlTableNode> children = tomlTableArrayNode.children();
        for (TomlTableNode child : children) {
            visitNode(child, memberSchema);
        }
    }

    @Override
    public void visit(TomlKeyValueNode keyValue) {
        TomlValueNode value = keyValue.value();
        visitNode(value);
    }

    @Override
    public void visit(TomlValueNode tomlValue) {
        visitNode(tomlValue);
    }

    @Override
    public void visit(TomlStringValueNode tomlStringValueNode) {
        if (schema.type() != Type.STRING) {
            TomlDiagnostic diagnostic =
                    getTomlDiagnostic(tomlStringValueNode.location(), "TVE0002", "error.invalid.type",
                            DiagnosticSeverity.ERROR,
                            String.format("Key \"%s\" expects %s . Found string", this.key, schema.type()));
            tomlStringValueNode.addDiagnostic(diagnostic);
            return;
        }
        StringSchema stringSchema = (StringSchema) this.schema;
        if (stringSchema.pattern().isPresent()) {
            String pattern = stringSchema.pattern().get();
            if (!Pattern.compile(pattern).matcher(tomlStringValueNode.getValue()).matches()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlStringValueNode.location(), "TVE0003",
                        "error.regex.mismatch", DiagnosticSeverity.ERROR,
                        String.format("Key \"%s\" value does not match the Regex provided in Schema %s", this.key,
                                pattern));
                tomlStringValueNode.addDiagnostic(diagnostic);
            }
        }
    }

    @Override
    public void visit(TomlDoubleValueNodeNode tomlDoubleValueNodeNode) {
        if (schema.type() != Type.NUMBER) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlDoubleValueNodeNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR,
                    String.format("Key \"%s\" expects %s . Found number", this.key, schema.type()));
            tomlDoubleValueNodeNode.addDiagnostic(diagnostic);
            return;
        }
        List<Diagnostic> diagnostics =
                validateMinMaxValues((NumericSchema) schema, tomlDoubleValueNodeNode.getValue(),
                        tomlDoubleValueNodeNode.location());
        tomlDoubleValueNodeNode.addDiagnostics(diagnostics);
    }

    @Override
    public void visit(TomlLongValueNode tomlLongValueNode) {
        if (schema.type() != Type.INTEGER) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlLongValueNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR,
                    String.format("Key \"%s\" expects %s . Found integer", this.key, schema.type()));
            tomlLongValueNode.addDiagnostic(diagnostic);
            return;
        }
        List<Diagnostic> diagnostics =
                validateMinMaxValues((NumericSchema) schema, Double.valueOf(tomlLongValueNode.getValue()),
                        tomlLongValueNode.location());
        for (Diagnostic diagnostic : diagnostics) {
            tomlLongValueNode.addDiagnostic(diagnostic);
        }
    }

    @Override
    public void visit(TomlArrayValueNode tomlArrayValueNode) {
        if (schema.type() != Type.ARRAY) {
            TomlDiagnostic diagnostic =
                    getTomlDiagnostic(tomlArrayValueNode.location(), "TVE0002", "error.invalid.type",
                            DiagnosticSeverity.ERROR, String.format("Key \"%s\" expects %s . Found array", this.key,
                                    schema.type()));
            tomlArrayValueNode.addDiagnostic(diagnostic);
            return;
        }
        ArraySchema arraySchema = (ArraySchema) schema;
        AbstractSchema items = arraySchema.items();
        for (TomlValueNode valueNode: tomlArrayValueNode.elements()) {
            visitNode(valueNode, items);
        }
    }

    private List<Diagnostic> validateMinMaxValues(NumericSchema numericSchema, Double value,
                                                  TomlNodeLocation location) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (numericSchema.maximum().isPresent()) {
            Double max = numericSchema.maximum().get();
            if (value >= max) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(location, "TVE0005", "error" +
                                ".maximum.value.exceed", DiagnosticSeverity.ERROR,
                        String.format("Key \"%s\" value can't be higher than %f", this.key,
                                max));
                diagnostics.add(diagnostic);
            }
        }
        if (numericSchema.minimum().isPresent()) {
            Double min = numericSchema.minimum().get();
            if (value <= min) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(location, "TVE0004",
                        "error.minimum.value.deceed", DiagnosticSeverity.ERROR,
                        String.format("Key \"%s\" value can't be lower than %f", this.key,
                                min));
                diagnostics.add(diagnostic);
            }
        }
        return diagnostics;
    }

    @Override
    public void visit(TomlBooleanValueNode tomlBooleanValueNode) {
        if (schema.type() != Type.BOOLEAN) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlBooleanValueNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR,
                    String.format("Key \"%s\" expects %s . Found boolean", this.key, schema.type()));
            tomlBooleanValueNode.addDiagnostic(diagnostic);
        }
    }

    private void visitNode(TomlNode node) {
        AbstractSchema previousSchema = this.schema;
        String previousKey = this.key;
        node.accept(this);
        this.schema = previousSchema;
        this.key = previousKey;
    }

    private void visitNode(TomlNode node, AbstractSchema schema) {
        AbstractSchema previousSchema = this.schema;
        this.schema = schema;
        node.accept(this);
        this.schema = previousSchema;
    }

    private void visitNode(TomlNode node, AbstractSchema schema, String key) {
        AbstractSchema previousSchema = this.schema;
        String previousKey = this.key;
        this.schema = schema;
        this.key = key;
        node.accept(this);
        this.schema = previousSchema;
        this.key = previousKey;
    }

    private TomlDiagnostic getTomlDiagnostic(TomlNodeLocation location, String code, String template,
                                             DiagnosticSeverity severity, String message) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, template, severity);
        return new TomlDiagnostic(location, diagnosticInfo, message);
    }

    private List<String> getRequiredFields(ObjectSchema objectSchema) {
        if (objectSchema.required() == null) {
            return new ArrayList<>();
        }
        return objectSchema.required();
    }
}
