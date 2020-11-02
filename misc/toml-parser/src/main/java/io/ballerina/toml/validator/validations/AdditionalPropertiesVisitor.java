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

import io.ballerina.toml.semantic.ast.TomlNodeVisitor;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.validator.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Map;
import java.util.Set;

/**
 * Visitor to validate additional properties field in json schema.
 *
 * @since 2.0.0
 */
public class AdditionalPropertiesVisitor implements TomlNodeVisitor {
    private final Schema schema;
    private final Set<Diagnostic> diagnostics;

    public AdditionalPropertiesVisitor(Set<Diagnostic> diagnostics, Schema schema) {
        this.diagnostics = diagnostics;
        this.schema = schema;
    }

    @Override
    public void visit(TomlTableNode tomlTableNode) {
        if (!schema.isAdditionalProperties()) {
            Map<String, Schema> properties = schema.getProperties();
            Map<String, TopLevelNode> children = tomlTableNode.children();
            Set<Map.Entry<String, TopLevelNode>> entries = children.entrySet();
            for (Map.Entry<String, TopLevelNode> subEntry : entries) {
                if (!properties.containsKey(subEntry.getKey())) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo("TVE0001", "warn.unexpected.property",
                            DiagnosticSeverity.WARNING);
                    TomlDiagnostic diagnostic = new TomlDiagnostic(subEntry.getValue().location(), diagnosticInfo,
                            "Unexpected Property \"" + subEntry.getKey() + "\"");
                    this.diagnostics.add(diagnostic);
                }
            }
        }
    }
}
