/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.toml.semantic.ast;

import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a TOML Node AST.
 *
 * @since 2.0.0
 */
public abstract class TomlNode implements Node {

    private final TomlType kind;
    private final List<TomlDiagnostic> diagnostics;
    private final TomlNodeLocation location; //The position of this node in the source file.

    public TomlNode(TomlType kind, TomlNodeLocation location) {
        this.diagnostics = new ArrayList<>();
        this.kind = kind;
        this.location = location;
    }

    public abstract void accept(TomlNodeVisitor visitor);

    public List<TomlDiagnostic> diagnostics() {
        return diagnostics;
    }

    public void addDiagnostic(TomlDiagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public TomlNodeLocation location() {
        return location;
    }

    @Override
    public TomlType kind() {
        return kind;
    }
}
