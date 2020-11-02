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

package io.ballerina.toml.validator.validations;

import io.ballerina.toml.semantic.ast.TomlBooleanValueNode;
import io.ballerina.toml.semantic.ast.TomlDoubleValueNodeNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlNodeVisitor;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.validator.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Visitor to validate type related validations in json schema.
 *
 * @since 2.0.0
 */
public class TypeCheckerVisitor implements TomlNodeVisitor {

    private final String key;
    private final Schema schema;
    private final Set<Diagnostic> diagnostics;

    public TypeCheckerVisitor(Set<Diagnostic> diagnostics, Schema schema, String key) {
        this.diagnostics = diagnostics;
        this.schema = schema;
        this.key = key;
    }

    @Override
    public void visit(TomlTableNode tomlTableNode) {
        if (!schema.getType().equals("object")) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlTableNode.location(), "TVE0002", "error.invalid.type",
                    DiagnosticSeverity.ERROR, String.format("Key \"%s\" expects %s . Found object", this.key,
                            schema.getType()));
            diagnostics.add(diagnostic);
        }
    }

    @Override
    public void visit(TomlTableArrayNode tomlTableArrayNode) {
        if (!schema.getType().equals("array")) {
            TomlDiagnostic diagnostic =
                    getTomlDiagnostic(tomlTableArrayNode.location(), "TVE0002", "error.invalid.type",
                            DiagnosticSeverity.ERROR, String.format("Key \"%s\" expects %s . Found array", this.key,
                                    schema.getType()));
            diagnostics.add(diagnostic);
        }
    }

    @Override
    public void visit(TomlKeyValueNode keyValue) {
        TomlValueNode value = keyValue.value();
        value.accept(this);
    }

    @Override
    public void visit(TomlValueNode tomlValue) {
        tomlValue.accept(this);
    }

    @Override
    public void visit(TomlStringValueNode tomlStringValueNode) {
        if (!schema.getType().equals("string")) {
            TomlDiagnostic diagnostic =
                    getTomlDiagnostic(tomlStringValueNode.location(), "TVE0002", "error.invalid.type",
                            DiagnosticSeverity.ERROR,
                            String.format("Key \"%s\" expects %s . Found string", this.key, schema.getType()));
            diagnostics.add(diagnostic);
        } else {
            if (schema.getPattern() != null) {
                if (!Pattern.compile(schema.getPattern()).matcher(tomlStringValueNode.getValue()).matches()) {
                    TomlDiagnostic diagnostic = getTomlDiagnostic(tomlStringValueNode.location(), "TVE0003",
                            "error.regex.mismatch", DiagnosticSeverity.ERROR,
                            String.format("Key \"%s\" value does not match the Regex provided in Schema %s", this.key,
                                    schema.getPattern()));
                    diagnostics.add(diagnostic);
                }
            }
        }
    }

    @Override
    public void visit(TomlDoubleValueNodeNode tomlDoubleValueNodeNode) {
        if (!schema.getType().equals("number")) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlDoubleValueNodeNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR,
                    String.format("Key \"%s\" expects %s . Found number", this.key, schema.getType()));
            diagnostics.add(diagnostic);
        } else {
            validateMinMaxValues(tomlDoubleValueNodeNode.getValue(), tomlDoubleValueNodeNode.location());
        }
    }

    @Override
    public void visit(TomlLongValueNode tomlLongValueNode) {
        if (!schema.getType().equals("integer")) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlLongValueNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR,
                    String.format("Key \"%s\" expects %s . Found integer", this.key, schema.getType()));
            diagnostics.add(diagnostic);
        } else {
            validateMinMaxValues(Double.valueOf(tomlLongValueNode.getValue()), tomlLongValueNode.location());
        }
    }

    private void validateMinMaxValues(Double value, TomlNodeLocation location) {
        if (schema.getMaximum() != null) {
            if (value >= schema.getMaximum()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(location, "TVE0005", "error" +
                        ".maximum.value.exceed", DiagnosticSeverity.ERROR,
                        String.format("Key \"%s\" value can't be higher than %d", this.key, schema.getMaximum()));
                diagnostics.add(diagnostic);
            }
        }
        if (schema.getMinimum() != null) {
            if (value <= schema.getMinimum()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(location, "TVE0004",
                        "error.minimum.value.deceed", DiagnosticSeverity.ERROR,
                        String.format("Key \"%s\" value can't be lower than %d", this.key, schema.getMinimum()));
                diagnostics.add(diagnostic);
            }
        }
    }

    @Override
    public void visit(TomlBooleanValueNode tomlBooleanValueNode) {
        if (!schema.getType().equals("boolean")) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlBooleanValueNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR,
                    String.format("Key \"%s\" expects %s . Found boolean", this.key, schema.getType()));
            diagnostics.add(diagnostic);
        }
    }

    private TomlDiagnostic getTomlDiagnostic(TomlNodeLocation location, String code, String template,
                                             DiagnosticSeverity severity, String message) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, template, severity);
        return new TomlDiagnostic(location, diagnosticInfo, message);
    }
}
