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

import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static io.ballerina.toml.validator.ValidationUtil.getTomlDiagnostic;

/**
 * Represents Composition schema in JSON schema.
 *
 * @since 2.0.0
 */
public class CompositionSchema extends AbstractSchema {
    List<AbstractSchema> schemas;

    public CompositionSchema(Type type, List<AbstractSchema> schemas) {
        super(type, new LinkedHashMap<>());
        this.schemas = schemas;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    public List<AbstractSchema> schemas() {
        return schemas;
    }

    @Override
    public <T extends TomlNode> List<Diagnostic> validate(T givenValueNode, String key) {
        if (this.type() == Type.ALL_OF) {
            List<Diagnostic> diagnostics = new ArrayList<>();
            for (AbstractSchema schema : this.schemas()) {
                List<Diagnostic> validate = schema.validate(givenValueNode, key);
                diagnostics.addAll(validate);
            }
            return diagnostics;
        }
        if (this.type() == Type.ANY_OF) {
            List<Diagnostic> diagnostics = new ArrayList<>();
            for (AbstractSchema schema : this.schemas()) {
                List<Diagnostic> validate = schema.validate(givenValueNode, key);
                if (validate.isEmpty()) {
                    return Collections.emptyList();
                }
                diagnostics.addAll(validate);
            }
            TomlDiagnostic diagnostic = getTomlDiagnostic(givenValueNode.location(), "TVE0007", 
                    "error" +
                    ".no.fields.matched.in.anyOf", DiagnosticSeverity.ERROR, "no fields matched in anyOf schema");
            diagnostics.add(diagnostic);
            return diagnostics;
        }

        if (this.type() == Type.ONE_OF) {
            List<Diagnostic> diagnostics = new ArrayList<>();
            TomlDiagnostic diagnostic = getTomlDiagnostic(givenValueNode.location(), "TVE0008", 
                    "error" +
                    ".must.match.exactly.one.schema", DiagnosticSeverity.ERROR, "must match exactly one schema in " +
                    "oneOf");
            diagnostics.add(diagnostic);
            int matchedCount = 0;
            for (AbstractSchema schema : this.schemas()) {
                List<Diagnostic> diagnosticForString = schema.validate(givenValueNode, key);
                if (diagnosticForString.isEmpty()) {
                    matchedCount++;
                }
                diagnostics.addAll(diagnosticForString);
            }
            if (matchedCount != 1) {
                return diagnostics;
            }
            return Collections.emptyList();
        }

        if (this.type() == Type.NOT) {
            AbstractSchema schema = this.schemas().get(0);
            List<Diagnostic> diagnosticForString = schema.validate(givenValueNode, key);
            if (diagnosticForString.isEmpty()) {
                TomlDiagnostic diagnostic = getTomlDiagnostic(givenValueNode.location(), 
                        "TVE0009", "error" +
                        ".schema.rule.must.not.valid", DiagnosticSeverity.ERROR, "schema rules must `NOT` be valid");
                return Collections.singletonList(diagnostic);
            }
           return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
