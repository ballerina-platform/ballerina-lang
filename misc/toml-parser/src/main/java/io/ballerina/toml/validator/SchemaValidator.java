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
import io.ballerina.toml.validator.schema.AbstractSchema;
import io.ballerina.toml.validator.schema.ArraySchema;
import io.ballerina.toml.validator.schema.BooleanSchema;
import io.ballerina.toml.validator.schema.CompositionSchema;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.toml.validator.schema.SchemaDeserializer;
import io.ballerina.toml.validator.schema.StringSchema;
import io.ballerina.toml.validator.schema.Type;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.ballerina.toml.validator.ValidationUtil.getTomlDiagnostic;

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
            if (!tomlTableNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlTableNode.location(), "TVE0002", "error.invalid.type",
                        DiagnosticSeverity.ERROR, getTypeErrorMessage(schema, Type.OBJECT, key));
                tomlTableNode.addDiagnostic(diagnostic);
            }
            return;
        }
        Schema objectSchema = (Schema) schema;
        tomlTableNode.addDiagnostics(objectSchema.validate(tomlTableNode, this.key));
        Map<String, AbstractSchema> properties = objectSchema.properties();
        Map<String, TopLevelNode> tableEntries = tomlTableNode.entries();
        for (Map.Entry<String, TopLevelNode> tableEntry : tableEntries.entrySet()) {
            String key = tableEntry.getKey();
            TopLevelNode value = tableEntry.getValue();
            AbstractSchema schema = properties.get(key);
            if (schema != null) {
                visitNode(value, schema, key);
            }
        }
    }

    @Override
    public void visit(TomlTableArrayNode tomlTableArrayNode) {
        if (schema.type() != Type.ARRAY) {
            if (!tomlTableArrayNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlTableArrayNode.location(), "TVE0002", "error" +
                        ".invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(schema, Type.ARRAY, key));
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
        if (isCompositeType()) {
            CompositionSchema schema = (CompositionSchema) this.schema;
            value.addDiagnostics(schema.validate(value, key));
            return;
        }
        visitNode(value);
    }

    private boolean isCompositeType() {
        return this.schema.type() == Type.ANY_OF || this.schema.type() == Type.ALL_OF ||
                this.schema.type() == Type.ONE_OF || this.schema.type() == Type.NOT;
    }

    @Override
    public void visit(TomlValueNode tomlValue) {
        visitNode(tomlValue);
    }

    public static String getTypeErrorMessage(AbstractSchema schema, Type found, String key) {
        Map<String, String> message = schema.message();
        String typeCustomMessage = message.get(SchemaDeserializer.TYPE);
        if (typeCustomMessage == null) {
            return String.format("incompatible type for key '%s': expected '%s', found '%s'", key, schema.type(),
                    found);
        }
        return typeCustomMessage;
    }

    @Override
    public void visit(TomlStringValueNode tomlStringValueNode) {
        List<Diagnostic> diagnosticForString = this.getDiagnosticForString(tomlStringValueNode);
        tomlStringValueNode.addDiagnostics(diagnosticForString);
    }

    private List<Diagnostic> getDiagnosticForString(TomlStringValueNode tomlStringValueNode) {
        if (this.schema.type() != Type.STRING) {
            if (!tomlStringValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlStringValueNode.location(), "TVE0002", "error" +
                        ".invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(schema, Type.STRING, key));
                return Collections.singletonList(diagnostic);
            }
            return Collections.emptyList();
        }
        StringSchema stringSchema = (StringSchema) this.schema;
        return stringSchema.validate(tomlStringValueNode, key);
    }

    @Override
    public void visit(TomlDoubleValueNodeNode tomlDoubleValueNodeNode) {
        if (schema.type() != Type.NUMBER) {
            if (!tomlDoubleValueNodeNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlDoubleValueNodeNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(schema, Type.NUMBER, key));
                tomlDoubleValueNodeNode.addDiagnostic(diagnostic);
            }
            return;
        }
        tomlDoubleValueNodeNode.addDiagnostics(schema.validate(tomlDoubleValueNodeNode, key));
    }

    @Override
    public void visit(TomlLongValueNode tomlLongValueNode) {
        if (schema.type() != Type.INTEGER) {
            if (!tomlLongValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlLongValueNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(schema, Type.INTEGER, key));
                tomlLongValueNode.addDiagnostic(diagnostic);
            }
            return;
        }
        tomlLongValueNode.addDiagnostics(schema.validate(tomlLongValueNode, key));
    }

    @Override
    public void visit(TomlArrayValueNode tomlArrayValueNode) {
        if (schema.type() != Type.ARRAY) {
            if (!tomlArrayValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic =
                        getTomlDiagnostic(tomlArrayValueNode.location(), "TVE0002", "error.invalid.type",
                                DiagnosticSeverity.ERROR, getTypeErrorMessage(schema, Type.ARRAY, key));
                tomlArrayValueNode.addDiagnostic(diagnostic);
            }
            return;
        }
        ArraySchema arraySchema = (ArraySchema) schema;
        tomlArrayValueNode.addDiagnostics(arraySchema.validate(tomlArrayValueNode, key));

        AbstractSchema items = arraySchema.items();
        for (TomlValueNode valueNode : tomlArrayValueNode.elements()) {
            visitNode(valueNode, items);
        }
    }

    @Override
    public void visit(TomlBooleanValueNode tomlBooleanValueNode) {
        if (schema.type() != Type.BOOLEAN) {
            if (!tomlBooleanValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlBooleanValueNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(schema, Type.BOOLEAN, key));
                tomlBooleanValueNode.addDiagnostic(diagnostic);
            }
            return;
        }
        BooleanSchema schema = (BooleanSchema) this.schema;
        tomlBooleanValueNode.addDiagnostics(schema.validate(tomlBooleanValueNode, key));
    }

    @Override
    public void visit(TomlInlineTableValueNode tomlInlineTableValueNode) {
        if (schema.type() != Type.OBJECT) {
            if (!tomlInlineTableValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(tomlInlineTableValueNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(schema, Type.OBJECT, key));
                tomlInlineTableValueNode.addDiagnostic(diagnostic);
            }
            return;
        }
        Schema schema = (Schema) this.schema;
        tomlInlineTableValueNode.addDiagnostics(schema.validate(tomlInlineTableValueNode, this.key));
        Map<String, AbstractSchema> properties = schema.properties();
        for (TopLevelNode topLevelNode : tomlInlineTableValueNode.elements()) {
            String key = topLevelNode.key().name();
            AbstractSchema abstractSchema = properties.get(key);
            if (abstractSchema != null) {
                visitNode(topLevelNode, abstractSchema, key);
            }
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
}
