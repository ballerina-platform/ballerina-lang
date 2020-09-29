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
package io.ballerina.toml.ast;

import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a TOML Node AST.
 */
public abstract class TomlNode implements Node {

    SyntaxKind kind;
    public TomlNodeLocation location; //The position of this node in the source file.
    private Set<Whitespace> ws;
    private List<TomlDiagnostic> diagnostics;

    public TomlNode(SyntaxKind kind) {
        this.diagnostics = new ArrayList<>();
        this.kind = kind;
    }

    public TomlNodeLocation getPosition() {
        return location;
    }

    public abstract void accept(TomlNodeVisitor visitor);

    @Override
    public Set<Whitespace> getWS() {
        return ws;
    }

    @Override
    public void addWS(Set<Whitespace> whitespaces) {
        if (this.ws == null) {
            this.ws = whitespaces;
        } else if (whitespaces != null) {
            this.ws.addAll(whitespaces);
        }
    }

    public List<TomlDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<TomlDiagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public void addDiagnostic(TomlDiagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    @Override
    public SyntaxKind getKind() {
        return kind;
    }
}
