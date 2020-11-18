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
import io.ballerina.toml.validator.NumericSchema;
import io.ballerina.toml.validator.Schema;
import io.ballerina.toml.validator.StringSchema;
import io.ballerina.toml.validator.TypeEnum;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Visitor to validate type related validations in json schema.
 *
 * @since 2.0.0
 */
public class TypeCheckerVisitor implements TomlNodeVisitor {

    private final String key;
    private final Schema schema;

    public TypeCheckerVisitor(Schema schema, String key) {
        this.schema = schema;
        this.key = key;
    }

    @Override
    public void visit(TomlTableNode tomlTableNode) {
        if (schema.getType() != TypeEnum.OBJECT) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlTableNode.location(), "TVE0002", "error.invalid.type",
                    DiagnosticSeverity.ERROR, String.format("Key \"%s\" expects %s . Found object", this.key,
                            schema.getType()));
            tomlTableNode.addDiagnostic(diagnostic);
        }
    }

    @Override
    public void visit(TomlTableArrayNode tomlTableArrayNode) {
        if (schema.getType() != TypeEnum.ARRAY) {
            TomlDiagnostic diagnostic =
                    getTomlDiagnostic(tomlTableArrayNode.location(), "TVE0002", "error.invalid.type",
                            DiagnosticSeverity.ERROR, String.format("Key \"%s\" expects %s . Found array", this.key,
                                    schema.getType()));
            tomlTableArrayNode.addDiagnostic(diagnostic);
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
        if (schema.getType() != TypeEnum.STRING) {
            TomlDiagnostic diagnostic =
                    getTomlDiagnostic(tomlStringValueNode.location(), "TVE0002", "error.invalid.type",
                            DiagnosticSeverity.ERROR,
                            String.format("Key \"%s\" expects %s . Found string", this.key, schema.getType()));
            tomlStringValueNode.addDiagnostic(diagnostic);
        } else {
            StringSchema stringSchema = (StringSchema) this.schema;
            if (stringSchema.getPattern().isPresent()) {
                String pattern = stringSchema.getPattern().get();
                if (!Pattern.compile(pattern).matcher(tomlStringValueNode.getValue()).matches()) {
                    TomlDiagnostic diagnostic = getTomlDiagnostic(tomlStringValueNode.location(), "TVE0003",
                            "error.regex.mismatch", DiagnosticSeverity.ERROR,
                            String.format("Key \"%s\" value does not match the Regex provided in Schema %s", this.key,
                                    pattern));
                    tomlStringValueNode.addDiagnostic(diagnostic);
                }
            }
        }
    }

    @Override
    public void visit(TomlDoubleValueNodeNode tomlDoubleValueNodeNode) {
        if (schema.getType() != TypeEnum.NUMBER) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlDoubleValueNodeNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR,
                    String.format("Key \"%s\" expects %s . Found number", this.key, schema.getType()));
            tomlDoubleValueNodeNode.addDiagnostic(diagnostic);
        } else {
            List<Diagnostic> diagnostics =
                    validateMinMaxValues((NumericSchema) schema, tomlDoubleValueNodeNode.getValue(),
                            tomlDoubleValueNodeNode.location());
            for (Diagnostic diagnostic : diagnostics) {
                tomlDoubleValueNodeNode.addDiagnostic(diagnostic);
            }
        }
    }

    @Override
    public void visit(TomlLongValueNode tomlLongValueNode) {
        if (schema.getType() != TypeEnum.INTEGER) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlLongValueNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR,
                    String.format("Key \"%s\" expects %s . Found integer", this.key, schema.getType()));
            tomlLongValueNode.addDiagnostic(diagnostic);
        } else {
            List<Diagnostic> diagnostics =
                    validateMinMaxValues((NumericSchema) schema, Double.valueOf(tomlLongValueNode.getValue()),
                            tomlLongValueNode.location());
            for (Diagnostic diagnostic : diagnostics) {
                tomlLongValueNode.addDiagnostic(diagnostic);
            }
        }
    }

    private List<Diagnostic> validateMinMaxValues(NumericSchema numericSchema, Double value,
                                                  TomlNodeLocation location) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (numericSchema.getMaximum().isPresent()) {
            Double max = numericSchema.getMaximum().get();
            if (value >= max) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(location, "TVE0005", "error" +
                                ".maximum.value.exceed", DiagnosticSeverity.ERROR,
                        String.format("Key \"%s\" value can't be higher than %f", this.key,
                                max));
                diagnostics.add(diagnostic);
            }
        }
        if (numericSchema.getMinimum().isPresent()) {
            Double min = numericSchema.getMinimum().get();
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
        if (schema.getType() != TypeEnum.BOOLEAN) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(tomlBooleanValueNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR,
                    String.format("Key \"%s\" expects %s . Found boolean", this.key, schema.getType()));
            tomlBooleanValueNode.addDiagnostic(diagnostic);
        }
    }

    private TomlDiagnostic getTomlDiagnostic(TomlNodeLocation location, String code, String template,
                                             DiagnosticSeverity severity, String message) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, template, severity);
        return new TomlDiagnostic(location, diagnosticInfo, message);
    }
}
