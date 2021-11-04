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

import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlDoubleValueNodeNode;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.toml.validator.ValidationUtil.getTomlDiagnostic;
import static io.ballerina.toml.validator.ValidationUtil.getTypeErrorMessage;

/**
 * Represents numeric schema in JSON schema.
 *
 * @since 2.0.0
 */
public class NumericSchema extends PrimitiveValueSchema<Double> {

    private final Double minimum;
    private final Double maximum;

    public NumericSchema(Type type, Map<String, String> message, Double minimum, Double maximum, Double defaultValue,
                         CompositionSchema compositionSchemas, String description) {
        super(type, message, defaultValue, compositionSchemas, description);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Optional<Double> minimum() {
        return Optional.ofNullable(minimum);
    }

    public Optional<Double> maximum() {
        return Optional.ofNullable(maximum);
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T extends TomlNode> List<Diagnostic> validate(T givenValueNode, String key) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        Double value;
        if (givenValueNode.kind() == TomlType.INTEGER) {
            TomlLongValueNode tomlLongValueNode = (TomlLongValueNode) givenValueNode;
            value = Double.valueOf(tomlLongValueNode.getValue());
        } else if (givenValueNode.kind() == TomlType.DOUBLE) {
            TomlDoubleValueNodeNode tomlDoubleValueNodeNode = (TomlDoubleValueNodeNode) givenValueNode;
            value = tomlDoubleValueNodeNode.getValue();
        } else {
            if (!givenValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(givenValueNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(this, givenValueNode.kind(),
                                key));
                diagnostics.add(diagnostic);
            }
            return diagnostics;
        }
        if (this.maximum().isPresent()) {
            Double max = this.maximum().get();
            if (value > max) {
                TomlDiagnostic diagnostic =
                        getTomlDiagnostic(givenValueNode.location(), "TVE0005", "error.maximum.value.exceed",
                                DiagnosticSeverity.ERROR, getMaxValueExceedErrorMessage(max, key));
                diagnostics.add(diagnostic);
            }
        }
        if (this.minimum().isPresent()) {
            Double min = this.minimum().get();
            if (value < min) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(givenValueNode.location(), "TVE0004",
                        "error.minimum.value.deceed", DiagnosticSeverity.ERROR, getMinValueDeceedErrorMessage(min,
                                key));
                diagnostics.add(diagnostic);
            }
        }

        diagnostics.addAll(super.validate(givenValueNode, key));
        return diagnostics;
    }

    private String getMaxValueExceedErrorMessage(Double max, String key) {
        Map<String, String> message = this.message();
        String maxCustomMessage = message.get(SchemaDeserializer.MAXIMUM);
        if (maxCustomMessage == null) {
            return String.format("value for key '%s' can't be higher than %f", key, max);
        }
        return maxCustomMessage;
    }

    private String getMinValueDeceedErrorMessage(Double min, String key) {
        Map<String, String> message = this.message();
        String minCustomMessage = message.get(SchemaDeserializer.MINIMUM);
        if (minCustomMessage == null) {
            return String.format("value for key '%s' can't be lower than %f", key, min);
        }
        return minCustomMessage;
    }
}
