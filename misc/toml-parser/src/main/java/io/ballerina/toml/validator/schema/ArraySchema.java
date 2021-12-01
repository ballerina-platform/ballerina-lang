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
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.ballerina.toml.validator.ValidationUtil.getTomlDiagnostic;
import static io.ballerina.toml.validator.ValidationUtil.getTypeErrorMessage;

/**
 * Represents array schema in JSON schema.
 *
 * @since 2.0.0
 */
public class ArraySchema extends AbstractSchema {

    private final AbstractSchema items;

    public ArraySchema(Type type, Map<String, String> message, AbstractSchema items, CompositionSchema comps,
                       String description) {
        super(type, message, comps, description);
        this.items = items;
    }

    public AbstractSchema items() {
        return items;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T extends TomlNode> List<Diagnostic> validate(T givenValueNode, String key) {
        if (givenValueNode.kind() == TomlType.ARRAY || givenValueNode.kind() == TomlType.TABLE_ARRAY) {
            return Collections.emptyList();
        }
        if (!givenValueNode.isMissingNode()) {
            TomlDiagnostic diagnostic = getTomlDiagnostic(givenValueNode.location(), "TVE0002",
                    "error.invalid.type", DiagnosticSeverity.ERROR, getTypeErrorMessage(this, givenValueNode.kind(),
                            key));
            return Collections.singletonList(diagnostic);
        }
        return super.validate(givenValueNode, key);
    }
}
