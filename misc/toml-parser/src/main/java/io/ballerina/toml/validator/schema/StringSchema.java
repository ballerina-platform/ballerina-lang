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
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.ballerina.toml.validator.ValidationUtil.getTomlDiagnostic;
import static io.ballerina.toml.validator.ValidationUtil.getTypeErrorMessage;

/**
 * Represents String schema in JSON schema.
 *
 * @since 2.0.0
 */
public class StringSchema extends PrimitiveValueSchema<String> {
    private final String pattern;
    private final Integer minLength;
    private final Integer maxLength;

    public StringSchema(Type type, Map<String, String> message, String pattern, String defaultValue, Integer minLength
            , Integer maxLength) {
        super(type, message, defaultValue);
        this.pattern = pattern;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public Optional<String> pattern() {
        return Optional.ofNullable(pattern);
    }

    public Optional<Integer> minLength() {
        return Optional.ofNullable(minLength);
    }

    public Optional<Integer> maxLength() {
        return Optional.ofNullable(maxLength);
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T extends TomlNode> List<Diagnostic> validate(T givenValueNode, String key) {
        if (givenValueNode.kind() != TomlType.STRING) {
            if (!givenValueNode.isMissingNode()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(givenValueNode.location(), "TVE0002",
                        "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(this, givenValueNode.kind(),
                                key));
                return Collections.singletonList(diagnostic);
            }
            return Collections.emptyList();
        }
        List<Diagnostic> diagnostics = new ArrayList<>();
        TomlStringValueNode stringValueNode = (TomlStringValueNode) givenValueNode;
        if (this.pattern().isPresent()) {
            String pattern = this.pattern().get();
            if (!Pattern.compile(pattern).matcher(stringValueNode.getValue()).matches()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(givenValueNode.location(), "TVE0003",
                        "error.regex.mismatch", DiagnosticSeverity.ERROR, getPatternErrorMessage(pattern, key));
                diagnostics.add(diagnostic);
            }
        }

        if (this.maxLength().isPresent()) {
            int maxLength = this.maxLength().get();
            String value = stringValueNode.getValue();
            if (value.length() > maxLength) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(stringValueNode.location(), "TVE0007",
                        "error.maxlen.exceeded", DiagnosticSeverity.ERROR, getMaxLengthErrorMessage(maxLength, key));
                diagnostics.add(diagnostic);
            }
        }
        if (this.minLength().isPresent()) {
            int minLength = this.minLength().get();
            String value = stringValueNode.getValue();
            if (value.length() < minLength) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(stringValueNode.location(), "TVE0008",
                        "error.minlen.deceed", DiagnosticSeverity.ERROR, getMinLengthErrorMessage(minLength, key));
                diagnostics.add(diagnostic);
            }
        }
        return diagnostics;
    }

    private String getPatternErrorMessage(String pattern, String key) {
        Map<String, String> message = this.message();
        String typeCustomMessage = message.get(SchemaDeserializer.PATTERN);
        if (typeCustomMessage == null) {
            return String.format("value for key '%s' expected to match the regex: %s", key, pattern);
        }
        return typeCustomMessage;
    }

    private String getMaxLengthErrorMessage(int maxLength, String key) {
        Map<String, String> message = this.message();
        String typeCustomMessage = message.get(SchemaDeserializer.MAX_LENGTH);
        if (typeCustomMessage == null) {
            return String.format("length of the value for key '%s' is greater than defined max length %s", key,
                    maxLength);
        }
        return typeCustomMessage;
    }

    private String getMinLengthErrorMessage(int maxLength, String key) {
        Map<String, String> message = this.message();
        String typeCustomMessage = message.get(SchemaDeserializer.MIN_LENGTH);
        if (typeCustomMessage == null) {
            return String.format("length of the value for key '%s' is lower than defined min length %s", key,
                    maxLength);
        }
        return typeCustomMessage;
    }
}
