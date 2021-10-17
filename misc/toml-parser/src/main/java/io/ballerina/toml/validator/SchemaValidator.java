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
import io.ballerina.toml.semantic.ast.TomlInlineTableValueNode;
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
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.toml.validator.schema.SchemaDeserializer;
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

    private static final String PROPERTY_HOLDER = "${property}";

    private AbstractSchema schema;
    private String key;
    private String schemaTitle;

    public SchemaValidator(Schema schema) {
        this.schema = schema;
        this.schemaTitle = schema.title();
    }

    @Override
    public void visit(TomlTableNode tomlTableNode) {
        if (schema.type() != Type.OBJECT) {
            if (!tomlTableNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlTableNode.location(), "TVE0002", "error.invalid.type",
                        DiagnosticSeverity.ERROR, getTypeErrorMessage(Type.OBJECT));
                tomlTableNode.addDiagnostic(diagnostic);
            }
            return;
        }
        Schema objectSchema = (Schema) schema;
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
                        getUnexpectedPropertyErrorMessage(key));
                tomlTableNode.addDiagnostic(diagnostic);
            }
        }
        for (String field : requiredFields) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0006", "error.required.field.missing",
                    DiagnosticSeverity.ERROR);
            TomlDiagnostic diagnostic = new TomlDiagnostic(tomlTableNode.location(), diagnosticInfo,
                    getRequiredErrorMessage(field));
            tomlTableNode.addDiagnostic(diagnostic);
        }
    }

    private String getRequiredErrorMessage(String field) {
        Map<String, String> message = this.schema.message();
        String typeCustomMessage = message.get(SchemaDeserializer.REQUIRED);
        if (typeCustomMessage == null) {
            return "missing required field '" + field + "'";
        }
        return typeCustomMessage.replace(PROPERTY_HOLDER, field);
    }

    private String getUnexpectedPropertyErrorMessage(String key) {
        Map<String, String> message = this.schema.message();
        String typeCustomMessage = message.get(SchemaDeserializer.ADDITIONAL_PROPERTIES);
        if (typeCustomMessage == null) {
            return String.format("key '%s' not supported in schema '%s'", key, schemaTitle);
        }
        return typeCustomMessage.replace(PROPERTY_HOLDER, key);
    }

    @Override
    public void visit(TomlTableArrayNode tomlTableArrayNode) {
        if (schema.type() != Type.ARRAY) {
            if (!tomlTableArrayNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlTableArrayNode.location(), "TVE0002", "error" +
                        ".invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(Type.ARRAY));
                tomlTableArrayNode.addDiagnostic(diagnostic);
            }
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

    private String getTypeErrorMessage(Type found) {
        Map<String, String> message = this.schema.message();
        String typeCustomMessage = message.get(SchemaDeserializer.TYPE);
        if (typeCustomMessage == null) {
            return String.format("incompatible type for key '%s': expected '%s', found '%s'", this.key, schema.type(),
                    found);
        }
        return typeCustomMessage;
    }

    private String getPatternErrorMessage(String pattern) {
        Map<String, String> message = this.schema.message();
        String typeCustomMessage = message.get(SchemaDeserializer.PATTERN);
        if (typeCustomMessage == null) {
            return String.format("value for key '%s' expected to match the regex: %s", this.key, pattern);
        }
        return typeCustomMessage;
    }

    private String getMaxLengthErrorMessage(int maxLength) {
        Map<String, String> message = this.schema.message();
        String typeCustomMessage = message.get(SchemaDeserializer.MAX_LENGTH);
        if (typeCustomMessage == null) {
            return String.format("length of the value for key '%s' is greater than defined max length %s", this.key,
                    maxLength);
        }
        return typeCustomMessage;
    }

    private String getMinLengthErrorMessage(int maxLength) {
        Map<String, String> message = this.schema.message();
        String typeCustomMessage = message.get(SchemaDeserializer.MIN_LENGTH);
        if (typeCustomMessage == null) {
            return String.format("length of the value for key '%s' is lower than defined min length %s", this.key,
                    maxLength);
        }
        return typeCustomMessage;
    }

    @Override
    public void visit(TomlStringValueNode tomlStringValueNode) {
        if (schema.type() != Type.STRING) {
            if (!tomlStringValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlStringValueNode.location(), "TVE0002", "error" +
                        ".invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(Type.STRING));
                tomlStringValueNode.addDiagnostic(diagnostic);
            }
            return;
        }
        StringSchema stringSchema = (StringSchema) this.schema;
        if (stringSchema.pattern().isPresent()) {
            String pattern = stringSchema.pattern().get();
            if (!Pattern.compile(pattern).matcher(tomlStringValueNode.getValue()).matches()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlStringValueNode.location(), "TVE0003",
                        "error.regex.mismatch", DiagnosticSeverity.ERROR, getPatternErrorMessage(pattern));
                tomlStringValueNode.addDiagnostic(diagnostic);
            }
        }
        if (stringSchema.maxLength().isPresent()) {
            int maxLength = stringSchema.maxLength().get();
            String value = tomlStringValueNode.getValue();
            if (value.length() > maxLength) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlStringValueNode.location(), "TVE0007",
                        "error.maxlen.exceeded", DiagnosticSeverity.ERROR, getMaxLengthErrorMessage(maxLength));
                tomlStringValueNode.addDiagnostic(diagnostic);
            }
        }
        if (stringSchema.minLength().isPresent()) {
            int minLength = stringSchema.minLength().get();
            String value = tomlStringValueNode.getValue();
            if (value.length() < minLength) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlStringValueNode.location(), "TVE0008",
                        "error.minlen.deceed", DiagnosticSeverity.ERROR, getMinLengthErrorMessage(minLength));
                tomlStringValueNode.addDiagnostic(diagnostic);
            }
        }
    }

    @Override
    public void visit(TomlDoubleValueNodeNode tomlDoubleValueNodeNode) {
        if (schema.type() != Type.NUMBER) {
            if (!tomlDoubleValueNodeNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlDoubleValueNodeNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(Type.NUMBER));
                tomlDoubleValueNodeNode.addDiagnostic(diagnostic);
            }
            return;
        }
        List<Diagnostic> diagnostics = validateMinMaxValues((NumericSchema) schema, tomlDoubleValueNodeNode.getValue(),
                tomlDoubleValueNodeNode.location());
        tomlDoubleValueNodeNode.addDiagnostics(diagnostics);
    }

    @Override
    public void visit(TomlLongValueNode tomlLongValueNode) {
        if (schema.type() != Type.INTEGER) {
            if (!tomlLongValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlLongValueNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(Type.INTEGER));
                tomlLongValueNode.addDiagnostic(diagnostic);
            }
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
            if (!tomlArrayValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic =
                        getTomlDiagnostic(tomlArrayValueNode.location(), "TVE0002", "error.invalid.type",
                                DiagnosticSeverity.ERROR, getTypeErrorMessage(Type.ARRAY));
                tomlArrayValueNode.addDiagnostic(diagnostic);
            }
            return;
        }
        ArraySchema arraySchema = (ArraySchema) schema;
        AbstractSchema items = arraySchema.items();
        for (TomlValueNode valueNode : tomlArrayValueNode.elements()) {
            visitNode(valueNode, items);
        }
    }

    private String getMaxValueExceedErrorMessage(Double max) {
        Map<String, String> message = this.schema.message();
        String maxCustomMessage = message.get(SchemaDeserializer.MAXIMUM);
        if (maxCustomMessage == null) {
            return String.format("value for key '%s' can't be higher than %f", this.key,
                    max);
        }
        return maxCustomMessage;
    }

    private String getMinValueDeceedErrorMessage(Double min) {
        Map<String, String> message = this.schema.message();
        String minCustomMessage = message.get(SchemaDeserializer.MINIMUM);
        if (minCustomMessage == null) {
            return String.format("value for key '%s' can't be lower than %f", this.key,
                    min);
        }
        return minCustomMessage;
    }

    private List<Diagnostic> validateMinMaxValues(NumericSchema numericSchema, Double value,
                                                  TomlNodeLocation location) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (numericSchema.maximum().isPresent()) {
            Double max = numericSchema.maximum().get();
            if (value > max) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(location, "TVE0005", "error" +
                        ".maximum.value.exceed", DiagnosticSeverity.ERROR, getMaxValueExceedErrorMessage(max));
                diagnostics.add(diagnostic);
            }
        }
        if (numericSchema.minimum().isPresent()) {
            Double min = numericSchema.minimum().get();
            if (value < min) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(location, "TVE0004", "error.minimum.value.deceed",
                        DiagnosticSeverity.ERROR, getMinValueDeceedErrorMessage(min));
                diagnostics.add(diagnostic);
            }
        }
        return diagnostics;
    }

    @Override
    public void visit(TomlBooleanValueNode tomlBooleanValueNode) {
        if (schema.type() != Type.BOOLEAN) {
            if (!tomlBooleanValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlBooleanValueNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(Type.BOOLEAN));
                tomlBooleanValueNode.addDiagnostic(diagnostic);
            }
        }
    }

    @Override
    public void visit(TomlInlineTableValueNode tomlInlineTableValueNode) {
        if (schema.type() != Type.OBJECT) {
            if (!tomlInlineTableValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlInlineTableValueNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(Type.OBJECT));
                tomlInlineTableValueNode.addDiagnostic(diagnostic);
            }
            return;
        }
        Schema schema = (Schema) this.schema;
        Map<String, AbstractSchema> properties = schema.properties();
        List<String> requiredFields = getRequiredFields(schema);
        
        for (TopLevelNode topLevelNode : tomlInlineTableValueNode.elements()) {
            String key = topLevelNode.key().name();
            AbstractSchema abstractSchema = properties.get(key);
            requiredFields.remove(key);
            if (abstractSchema != null) {
                visitNode(topLevelNode, abstractSchema, key);
                continue;
            }
            if (!schema.hasAdditionalProperties()) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0001", "error.unexpected.property",
                        DiagnosticSeverity.ERROR);
                TomlDiagnostic diagnostic = new TomlDiagnostic(topLevelNode.location(), diagnosticInfo,
                        getUnexpectedPropertyErrorMessage(key));
                tomlInlineTableValueNode.addDiagnostic(diagnostic);
            }
        }

        for (String field : requiredFields) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0006", "error.required.field.missing",
                    DiagnosticSeverity.ERROR);
            TomlDiagnostic diagnostic = new TomlDiagnostic(tomlInlineTableValueNode.location(), diagnosticInfo,
                    getRequiredErrorMessage(field));
            tomlInlineTableValueNode.addDiagnostic(diagnostic);
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

    private List<String> getRequiredFields(Schema objectSchema) {
        if (objectSchema.required() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(objectSchema.required());
    }
}
